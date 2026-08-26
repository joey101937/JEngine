package Framework.Collision;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import java.util.List;

/**
 * Grants as much of a blocked movement as will fit, instead of none of it.
 *
 * {@link AxisCancelResolver} answers a blocked axis with zero, so an object stops as far short of
 * whatever blocked it as one tick of travel. At a few pixels per tick that reads as contact; at
 * twenty-six it reads as hovering. This resolver finds where the movement actually meets the
 * obstacle and stops exactly there.
 *
 * <h3>Touching is not overlapping</h3>
 * Edge contact reports a collision but does not block movement, so a body resting on a floor can
 * still walk along it. A blocker constrains an axis only while the two boxes share <i>space</i> on
 * the other axis, never merely an edge. Resting flush is a stable state that permits sliding.
 *
 * <h3>Exact, in doubles</h3>
 * Contact along an axis is the gap divided by the step, computed from the hitbox's floating-point
 * edges rather than {@link Hitbox#getBounds()}, whose integer truncation can place a resting body
 * a pixel inside the surface it landed on. There is no search and no tolerance, so the answer is
 * identical on every machine, which lockstep multiplayer requires. Rotated and circular hitboxes
 * have no closed form and fall back to fixed-step bisection, deterministic for the same reason
 * but resolving only to the pixel.
 *
 * <h3>Sliding</h3>
 * With sliding on, each axis is narrowed independently, so an object keeps the movement parallel
 * to a surface it is pressed against. With sliding off, one fraction covers the whole vector and
 * the object stops at the contact point without deflecting — what a projectile wants.
 *
 * @author Joseph
 */
public class ClampedResolver implements CollisionResolver {

    /** Bisection steps for hitboxes that are not axis-aligned boxes. */
    public static final int SEARCH_STEPS = 8;

    private final boolean sliding;

    public ClampedResolver(boolean sliding) {
        this.sliding = sliding;
    }

    @Override
    public boolean clampsPartialMovement() {
        return true;
    }

    @Override
    public CollisionResult resolve(GameObject2 self, DCoordinate proposedMovement,
                                   List<GameObject2> parts, List<GameObject2> candidates,
                                   ContactSink contacts) {
        double allowX = 1.0;
        double allowY = 1.0;

        for (GameObject2 part : parts) {
            if (!part.isSolid || part.getHitbox() == null) {
                continue;
            }
            Hitbox mine = part.getHitbox();

            for (GameObject2 other : candidates) {
                if (!self.canCollideWith(other)) {
                    continue;
                }
                Hitbox theirs = other.getHitbox();
                // Gated on the whole movement, as in AxisCancelResolver: something the x component
                // alone would hit, but the diagonal misses, is not in the way of where we are going.
                if (!mine.intersectsIfMoved(theirs, proposedMovement.x, proposedMovement.y)) {
                    continue;
                }
                contacts.contact(other);

                if (!part.preventOverlap || !other.preventOverlap) {
                    continue;   // a trigger volume: reported, never blocking
                }
                if (part.movesFreelyThrough(other)) {
                    continue;
                }
                if (!self.isBlockedBy(other, proposedMovement)) {
                    continue;   // a one-way blocker approached from its open side
                }

                if (isAxisAlignedBox(mine) && isAxisAlignedBox(theirs)) {
                    double[] a = edges(mine);
                    double[] b = edges(theirs);
                    double fx = axisFraction(a, b, proposedMovement.x, true);
                    double fy = axisFraction(a, b, proposedMovement.y, false);
                    if (sliding) {
                        allowX = Math.min(allowX, fx);
                        allowY = Math.min(allowY, fy);
                    } else {
                        double t = Math.min(fx, fy);
                        allowX = Math.min(allowX, t);
                        allowY = Math.min(allowY, t);
                    }
                } else {
                    double t = bisectClearFraction(mine, theirs, proposedMovement,
                            part, other, sliding);
                    allowX = Math.min(allowX, t);
                    allowY = Math.min(allowY, t);
                }
            }
        }

        return new CollisionResult(
                new DCoordinate(proposedMovement.x * allowX, proposedMovement.y * allowY),
                allowX < 1.0, allowY < 1.0, proposedMovement.x, proposedMovement.y);
    }

    /* ==================================================================
     *  axis-aligned boxes: closed form
     * ================================================================== */

    /** World edges of an unrotated box hitbox as {minX, minY, maxX, maxY}, without truncation. */
    private static double[] edges(Hitbox h) {
        DCoordinate c = h.getCenter();
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Coordinate v : h.vertices) {
            double x = c.x + v.x, y = c.y + v.y;
            minX = Math.min(minX, x);  maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);  maxY = Math.max(maxY, y);
        }
        return new double[]{minX, minY, maxX, maxY};
    }

    /**
     * Fraction of a single axis's movement that stays clear of the blocker.
     *
     * A blocker constrains an axis only while the boxes already share space on the <i>other</i>
     * axis — that is what distinguishes a floor underfoot (shares x, so it stops falling but not
     * walking) from a wall ahead (shares y, so it stops walking but not falling).
     *
     * @param horizontal true to evaluate x, false for y
     */
    private static double axisFraction(double[] a, double[] b, double delta, boolean horizontal) {
        if (delta == 0) {
            return 1.0;
        }
        double aMin = horizontal ? a[0] : a[1], aMax = horizontal ? a[2] : a[3];
        double bMin = horizontal ? b[0] : b[1], bMax = horizontal ? b[2] : b[3];
        double aPerpMin = horizontal ? a[1] : a[0], aPerpMax = horizontal ? a[3] : a[2];
        double bPerpMin = horizontal ? b[1] : b[0], bPerpMax = horizontal ? b[3] : b[2];

        // sharing an edge on the other axis is contact, not obstruction
        if (!(aPerpMin < bPerpMax && bPerpMin < aPerpMax)) {
            return 1.0;
        }
        if (delta > 0) {
            double gap = bMin - aMax;
            if (gap >= 0) return clamp01(gap / delta);
        } else {
            double gap = bMax - aMin;
            if (gap <= 0) return clamp01(gap / delta);
        }

        // Overlapping on this axis already - spawned inside it, pushed in, teleported in. The
        // object must be able to leave, so movement toward the nearer face is allowed in full,
        // but movement toward the far one is refused: letting it travel the long way out means
        // sinking through the whole body of whatever it is stuck in, which reads as falling
        // through the floor.
        double exitBackward = aMax - bMin;   // distance to clear it moving in -
        double exitForward = bMax - aMin;    // distance to clear it moving in +
        boolean towardNearerFace = delta > 0 ? exitForward <= exitBackward : exitBackward <= exitForward;
        return towardNearerFace ? 1.0 : 0.0;
    }

    private static double clamp01(double v) {
        if (Double.isNaN(v) || v < 0) return 0;
        return Math.min(1.0, v);
    }

    /* ==================================================================
     *  everything else: bisection against SAT
     * ================================================================== */

    /**
     * Rotated and circular hitboxes have no closed-form contact point, so the movement is halved
     * repeatedly against {@link Hitbox#intersectsIfMoved}. That test counts edge contact as an
     * intersection, so the unstick rule from {@link AxisCancelResolver} is still needed here:
     * without it an object already touching something would report blocked in every direction.
     */
    private double bisectClearFraction(Hitbox mine, Hitbox theirs, DCoordinate movement,
                                       GameObject2 part, GameObject2 other, boolean sliding) {
        if (mine.intersects(theirs)) {
            DCoordinate here = part.getLocation();
            DCoordinate there = here.copy().add(movement);
            DCoordinate otherCenter = other.getCenterForCollisionSliding();
            return there.distanceFrom(otherCenter) > here.distanceFrom(otherCenter) ? 1.0 : 0.0;
        }
        if (!blockedAt(mine, theirs, movement, 1.0)) {
            return 1.0;
        }
        double clear = 0.0, blocked = 1.0;
        for (int i = 0; i < SEARCH_STEPS; i++) {
            double mid = (clear + blocked) / 2.0;
            if (blockedAt(mine, theirs, movement, mid)) {
                blocked = mid;
            } else {
                clear = mid;
            }
        }
        return clear;
    }

    private boolean blockedAt(Hitbox mine, Hitbox theirs, DCoordinate movement, double fraction) {
        // Probes at full precision: most of the bisection's steps are fractions of a pixel apart.
        return mine.intersectsIfMoved(theirs, movement.x * fraction, movement.y * fraction);
    }

    private static boolean isAxisAlignedBox(Hitbox h) {
        return h.type == Hitbox.Type.box && h.vertices != null && h.getRotation() % 360 == 0;
    }
}

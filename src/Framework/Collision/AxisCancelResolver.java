package Framework.Collision;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import java.util.List;

/**
 * The engine's original resolution policy: a blocked axis yields <b>all</b> of its movement.
 *
 * Movement is granted per axis and all-or-nothing, so an object stops as far short of a blocker
 * as one tick of travel. That reads as contact at a few pixels per tick; a body falling at 26px
 * per tick comes to rest up to 26px above the floor. {@link ClampedResolver} stops at the contact
 * point instead. This is the engine default.
 *
 * <h3>Two rules that look redundant and are not</h3>
 * <ul>
 *   <li><b>The full-movement gate.</b> The per-axis tests only run for candidates that the
 *       <i>whole</i> proposed movement would hit. An object that would be hit by the x component
 *       alone, but not by the diagonal, is not a blocker — the diagonal is where the object is
 *       actually going.</li>
 *   <li><b>The unstick rule.</b> An object already overlapping something may move anyway when
 *       that increases the distance between them. Without it an overlapped object — from a spawn,
 *       a push, a teleport, a rotation — reports blocked in every direction and freezes.</li>
 * </ul>
 *
 * @author Joseph
 */
public class AxisCancelResolver implements CollisionResolver {

    /** Whether an unblocked axis survives when the other one is blocked. */
    private final boolean sliding;

    public AxisCancelResolver(boolean sliding) {
        this.sliding = sliding;
    }

    @Override
    public CollisionResult resolve(GameObject2 self, DCoordinate proposedMovement,
                                   List<GameObject2> parts, List<GameObject2> candidates,
                                   ContactSink contacts) {
        // Rounded away from zero, so the test is slightly conservative rather than optimistic.
        Coordinate rounded = new Coordinate(
                proposedMovement.x >= 0 ? (int) Math.ceil(proposedMovement.x) : (int) Math.floor(proposedMovement.x),
                proposedMovement.y >= 0 ? (int) Math.ceil(proposedMovement.y) : (int) Math.floor(proposedMovement.y));

        boolean xClear = true;
        boolean yClear = true;

        for (GameObject2 part : parts) {
            if (!part.isSolid || part.getHitbox() == null) {
                continue;
            }
            DCoordinate partLocation = part.getLocation();
            DCoordinate newLocation = partLocation.copy().add(proposedMovement);

            // Runs to completion even once both axes are blocked. The flags are only ever cleared,
            // so stopping early would not change the movement, but every remaining contact would
            // go unreported - and when two objects block each other, neither reaches the other,
            // so the pair is missed entirely.
            for (GameObject2 other : candidates) {
                if (!self.canCollideWith(other)) {
                    continue;
                }
                if (!part.getHitbox().intersectsIfMoved(other.getHitbox(), rounded)) {
                    continue;
                }
                contacts.contact(other);

                if (!part.preventOverlap || !other.preventOverlap) {
                    continue;   // a trigger volume: reported, never blocking
                }
                if (part.movesFreelyThrough(other)) {
                    continue;   // opted to plow through this particular blocker
                }
                boolean overlapping = part.getHitbox().intersects(other.getHitbox());
                if (!self.isBlockedBy(other, proposedMovement)) {
                    continue;   // a one-way blocker approached from its open side
                }
                if (overlapping) {
                    DCoordinate otherCenter = other.getCenterForCollisionSliding();
                    if (newLocation.distanceFrom(otherCenter) > partLocation.distanceFrom(otherCenter)) {
                        continue;   // already overlapping but moving away - let it out
                    }
                    xClear = false;
                    yClear = false;
                }
                if (sliding) {
                    if (part.getHitbox().intersectsIfMoved(other.getHitbox(), new Coordinate(rounded.x, 0))) {
                        xClear = false;
                    }
                    if (part.getHitbox().intersectsIfMoved(other.getHitbox(), new Coordinate(0, rounded.y))) {
                        yClear = false;
                    }
                } else {
                    xClear = false;
                    yClear = false;
                }
            }
        }

        return new CollisionResult(
                new DCoordinate(xClear ? proposedMovement.x : 0, yClear ? proposedMovement.y : 0),
                !xClear, !yClear, proposedMovement.x, proposedMovement.y);
    }
}

package Framework.Collision;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import java.util.List;

/**
 * The engine's original resolution policy: a blocked axis yields <b>all</b> of its movement.
 *
 * Movement is granted per axis and all-or-nothing, so an object stops as far short of a blocker
 * as one tick of travel — correct enough for top-down movement at a few pixels per tick, and the
 * reason a body falling at 26px per tick hovers up to 26px above the floor. {@link ClampedResolver}
 * is the answer to that; this class exists so the engine's behaviour is unchanged for anything
 * that has not opted into something else.
 *
 * <h3>Two details worth not "cleaning up"</h3>
 * <ul>
 *   <li><b>The full-movement gate.</b> The per-axis tests only run for candidates that the
 *       <i>whole</i> proposed movement would hit. An object that would be hit by the x component
 *       alone, but not by the diagonal, is correctly not a blocker — the diagonal is where the
 *       object is actually going.</li>
 *   <li><b>The unstick rule.</b> An object already overlapping something is allowed to move
 *       anyway when that increases the distance between them. Without it an overlapped object —
 *       from a spawn, a push, a teleport, a rotation — reports blocked everywhere and freezes.</li>
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

            // Runs to completion even once both axes are blocked: bailing early cannot change the
            // result, since these flags are only ever cleared, but it would skip reporting every
            // remaining contact - and when two objects block each other, neither would reach the
            // other and the pair would go unreported entirely.
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

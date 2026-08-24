package Framework.Collision;

import Framework.DCoordinate;

/**
 * What a {@link CollisionResolver} decided: how much of the movement was granted, and which
 * parts of it were cut short.
 *
 * Returning the blocked axes as well as the movement means callers do not have to infer contact
 * by comparing what they asked for against what they got — a comparison that gets the answer
 * wrong whenever the granted movement happens to equal zero for an unrelated reason. Standing on
 * the ground, running into a wall and hitting a ceiling are all read off this.
 *
 * Immutable; a new one is produced per resolution.
 *
 * @author Joseph
 */
public final class CollisionResult {

    /** Movement actually granted. */
    public final DCoordinate movement;
    /** Whether the x component was cut short by something. */
    public final boolean blockedX;
    /** Whether the y component was cut short by something. */
    public final boolean blockedY;
    /** The movement that was asked for, so the direction of a block can be recovered. */
    public final double attemptedX, attemptedY;

    public CollisionResult(DCoordinate movement, boolean blockedX, boolean blockedY,
                           double attemptedX, double attemptedY) {
        this.movement = movement;
        this.blockedX = blockedX;
        this.blockedY = blockedY;
        this.attemptedX = attemptedX;
        this.attemptedY = attemptedY;
    }

    /** Nothing was in the way. */
    public static CollisionResult clear(DCoordinate movement) {
        return new CollisionResult(movement, false, false, movement.x, movement.y);
    }

    /** Blocked while heading down — for a body under gravity, this is standing on something. */
    public boolean blockedDown() { return blockedY && attemptedY > 0; }

    /** Blocked while heading up — a ceiling. */
    public boolean blockedUp() { return blockedY && attemptedY < 0; }

    /** Blocked while heading right. */
    public boolean blockedRight() { return blockedX && attemptedX > 0; }

    /** Blocked while heading left. */
    public boolean blockedLeft() { return blockedX && attemptedX < 0; }

    /** Blocked on either axis. */
    public boolean blocked() { return blockedX || blockedY; }

    /**
     * Unit vector pointing out of the surface that stopped this movement, or (0,0) if nothing
     * did. Exact for axis-aligned resolution; a resolver that works with real surface angles
     * would report the true normal here instead.
     */
    public DCoordinate contactNormal() {
        double nx = blockedX ? -Math.signum(attemptedX) : 0;
        double ny = blockedY ? -Math.signum(attemptedY) : 0;
        return new DCoordinate(nx, ny);
    }
}

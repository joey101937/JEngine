package Framework.Collision;

/**
 * The stock collision resolvers, as shared immutable instances.
 *
 * A resolver holds no per-object state, so one instance serves every object that wants that
 * policy — assign a reference in a constructor rather than allocating per object. Configuration
 * (such as sliding) is constructor state, which is why these are a handful of named constants
 * instead of a single stateless singleton.
 *
 * @author Joseph
 */
public final class Resolvers {

    private Resolvers() {}

    /**
     * The engine default: a blocked axis loses all of its movement, an unblocked one survives.
     * Everything behaves this way unless it says otherwise.
     */
    public static final CollisionResolver AXIS_CANCEL = new AxisCancelResolver(true);

    /**
     * As {@link #AXIS_CANCEL}, but any block stops movement on both axes — no sliding along a
     * surface. Suits something that should stop dead on contact rather than deflect.
     */
    public static final CollisionResolver AXIS_CANCEL_NO_SLIDE = new AxisCancelResolver(false);

    /**
     * Grants as much of a blocked movement as fits, so an object comes to rest against what
     * stopped it rather than up to a tick of travel short of it. The right default for anything
     * moving fast enough that a tick of travel is visible — a falling body, a fast projectile —
     * and an improvement anywhere else, since units rest against walls instead of near them.
     */
    public static final CollisionResolver CLAMPED = new ClampedResolver(true);

    /**
     * As {@link #CLAMPED}, but movement stops at the contact point without deflecting along the
     * surface. What something that should stop dead on contact wants.
     */
    public static final CollisionResolver CLAMPED_NO_SLIDE = new ClampedResolver(false);
}

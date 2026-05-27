package GameDemo.RTSDemo.FogOfWar;

/**
 * Extends {@link VisionProvider} with a forward-facing cone that grants bonus range.
 * Tiles inside the cone use {@code getVisionRange() * getDirectionalRangeMultiplier()}
 * as their effective range; tiles outside the cone use the base range unchanged.
 * <p>
 * Cone membership is tested with a dot-product check — no sqrt or acos per tile:
 * <pre>
 *   dot = facing · (tile - origin)
 *   inCone = dot &gt; 0  &amp;&amp;  dot² ≥ cos²(halfAngle) × distSq
 * </pre>
 * The bounding box used for tile iteration is expanded to the directional range
 * so no in-cone tiles are missed.
 * <p>
 * Implement this on any {@link VisionProvider} that should see farther in one direction
 * (e.g. a tank with forward optics, a watchtower with a directional floodlight).
 */
public interface DirectionalVisionProvider extends VisionProvider {

    /**
     * Half-width of the vision cone in degrees.
     * The full cone spans {@code 2 * getDirectionalVisionHalfAngle()} degrees
     * centered on {@link #getVisionFacingDegrees()}.
     * Example: return 15 for a 30° total cone.
     */
    double getDirectionalVisionHalfAngle();

    /**
     * Multiplier applied to {@link #getVisionRange()} for tiles inside the cone.
     * Values above 1.0 extend range forward; below 1.0 would restrict it.
     * Example: return 1.4 for a 40% range bonus.
     */
    double getDirectionalRangeMultiplier();

    /**
     * The center direction of the vision cone in game-rotation degrees.
     * Uses the game convention: 0 = north (up), clockwise positive.
     * Typically returns the unit's hull or turret rotation.
     */
    double getVisionFacingDegrees();
}

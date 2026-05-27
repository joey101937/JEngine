package GameDemo.RTSDemo.FogOfWar;

import Framework.Coordinate;
import java.io.Serializable;

/**
 * Implemented by any object that grants visibility to a team on the fog-of-war grid.
 * {@link FogOfWarGrid} collects all enabled VisionProviders each update and marks
 * tiles visible within each provider's range, subject to {@link SightBlocker} occlusion.
 * <p>
 * RTSUnit implements this by default. Non-unit objects (watchtowers, sensors, etc.)
 * can also implement it to grant vision without being a unit.
 *
 * @see DirectionalVisionProvider for an extended version with a forward-facing range bonus
 */
public interface VisionProvider extends Serializable {

    /**
     * Returns true if this provider should currently contribute vision.
     * Dead/rubble units should return false so they stop revealing tiles.
     */
    boolean isVisionEnabled();

    /**
     * The team index whose fog grid this provider reveals (0-based).
     * Must be in range [0, {@link FogOfWarGrid#MAX_TEAMS}).
     */
    int getVisionTeam();

    /**
     * Radius in world pixels within which this provider reveals tiles.
     * Tiles whose centers fall outside this radius remain hidden.
     */
    int getVisionRange();

    /**
     * World position used as the origin for all LOS ray tests.
     * Typically the unit's current location.
     */
    Coordinate getVisionLocation();

    /**
     * When true, LOS rays from this provider pass through {@link SightBlocker}s
     * as if they were not there. Intended for air units that see over terrain.
     * Delegates to {@link SightBlockerImmune} if the provider also implements it.
     */
    default boolean ignoresSightBlockers() {
        return false;
    }
}

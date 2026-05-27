package GameDemo.RTSDemo.FogOfWar;

import java.awt.Rectangle;
import java.io.Serializable;

/**
 * Implemented by world objects that block line of sight between units and fog tiles.
 * During each fog update, rays cast from vision providers are tested against all
 * SightBlockers; any tile whose center is occluded by a blocker remains hidden.
 * <p>
 * If any tile within a blocker's own bounds becomes visible, all of the blocker's
 * tiles are revealed (so buildings show as a whole once spotted, not tile by tile).
 */
public interface SightBlocker extends Serializable {

    /**
     * Returns the axis-aligned bounding box of this blocker in world coordinates.
     * Used for both the ray-AABB intersection test and the blocker reveal pass.
     */
    Rectangle getBlockerBounds();

    /**
     * When false, this object is ignored by all LOS ray tests.
     * Useful for temporarily disabling blocking (e.g. a gate that opens).
     */
    default boolean isSightBlockingEnabled() { return true; }
}

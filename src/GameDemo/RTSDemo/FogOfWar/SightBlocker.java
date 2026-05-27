package GameDemo.RTSDemo.FogOfWar;

import java.awt.Rectangle;
import java.io.Serializable;

public interface SightBlocker extends Serializable {
    Rectangle getBlockerBounds();

    /** When false this object does not block line of sight. */
    default boolean isSightBlockingEnabled() { return true; }
}

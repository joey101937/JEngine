package GameDemo.RTSDemo.FogOfWar;

import Framework.Coordinate;
import java.io.Serializable;

public interface VisionProvider extends Serializable {
    boolean isVisionEnabled();
    int getVisionTeam();
    int getVisionRange();
    Coordinate getVisionLocation();

    default boolean ignoresSightBlockers() {
        return false;
    }
}

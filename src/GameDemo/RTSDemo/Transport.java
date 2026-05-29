package GameDemo.RTSDemo;

import java.util.List;

public interface Transport {
    boolean canLoad(RTSUnit unit);
    void load(RTSUnit unit);
    void unloadAll();
    List<RTSUnit> getLoadedUnits();
    int getMaxCapacity();
}

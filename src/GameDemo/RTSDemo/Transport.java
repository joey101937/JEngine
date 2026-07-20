package GameDemo.RTSDemo;

import Framework.GameObject2;
import java.util.Collection;
import java.util.List;

public interface Transport {
    boolean canLoad(RTSUnit unit);
    void load(RTSUnit unit);
    void unloadAll();
    List<RTSUnit> getLoadedUnits();
    int getMaxCapacity();

    /**
     * Passengers live inside the transport rather than in the handler, so the
     * deserialization pass over the handler's objects never reaches them.
     * Transports call this from onPostDeserialization for every collection of
     * units they hold so passengers have their graphics back when they disembark.
     */
    default void restorePassengers(Collection<RTSUnit> passengers) {
        for (RTSUnit passenger : passengers) {
            if (passenger == null) continue;
            passenger.onPostDeserialization();
            for (GameObject2 sub : passenger.getAllSubObjects()) {
                sub.onPostDeserialization();
            }
        }
    }
}

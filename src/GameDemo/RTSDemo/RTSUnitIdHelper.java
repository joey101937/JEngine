
package GameDemo.RTSDemo;

import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class RTSUnitIdHelper {
    private static final HashMap<String, Integer> idLogMap = new HashMap<>();

    public static synchronized String generateId(RTSUnit unit){
        String idLabel = unit.getName() + "_T" + unit.team;
        int curNumber = idLogMap.getOrDefault(idLabel, 1);
        idLogMap.put(idLabel, curNumber+1);
        return idLabel + "_"+ curNumber;
    }

    /**
     * Resets the ID counter map.
     * Used for determinism tests to ensure each test run starts with clean state.
     */
    public static synchronized void reset() {
        idLogMap.clear();
    }
}

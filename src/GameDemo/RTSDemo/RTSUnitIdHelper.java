
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
}

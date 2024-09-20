package GameDemo.RTSDemo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class ControlGroupHelper {

    public static final HashMap<Integer, ArrayList<RTSUnit>> groups = new HashMap<>();

    static {
        groups.put(1, new ArrayList<>());
        groups.put(2, new ArrayList<>());
        groups.put(3, new ArrayList<>());
        groups.put(4, new ArrayList<>());
        groups.put(5, new ArrayList<>());
        groups.put(6, new ArrayList<>());
        groups.put(7, new ArrayList<>());
        groups.put(8, new ArrayList<>());
        groups.put(9, new ArrayList<>());
        groups.put(0, new ArrayList<>());
    }

    public static void selectGroup(Integer groupNumber) {
        System.out.println("selecting");
        if(groupNumber < 0 || groupNumber > 9) return;
        ArrayList<RTSUnit> toSelect = groups.get(groupNumber);
        if(toSelect.isEmpty()) return;
        for(RTSUnit unit : SelectionBoxEffect.selectedUnits) {
            unit.setSelected(false);
        }
        SelectionBoxEffect.selectedUnits.clear();
        for (RTSUnit unit : toSelect) {
            unit.setSelected(true);
        }
        SelectionBoxEffect.selectedUnits.addAll(toSelect);
    }

    public static void addToGroup(Integer groupNumber, Collection<RTSUnit> units) {
        System.out.println("adding");
        if(groupNumber < 0 || groupNumber > 9) return;
        groups.get(groupNumber).addAll(units);
    }
    
    public static void clearGroup(Integer groupNumber) {
        System.out.println("clearing");
        if(groupNumber < 0 || groupNumber > 9) return;
        groups.get(groupNumber).clear();
    }
}

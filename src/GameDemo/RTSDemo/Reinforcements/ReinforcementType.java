package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public abstract class ReinforcementType {

    public String name;
    public ArrayList<String> infoLines = new ArrayList<String>();
    public HashMap<Class, Integer> contents = new HashMap<>();
    public abstract void onTrigger(Coordinate targetLocation, int team);
    
    public static ReinforcementType mediumTanks = new ReinforcementTypeMediumTanks();
    public static ReinforcementType lightTanks; // todo
    public static ReinforcementType infantry; // todo
    public static ReinforcementType hellicopters; // todo
    public static ReinforcementType transport; // todo
    public static ReinforcementType antiAir; // todo


}

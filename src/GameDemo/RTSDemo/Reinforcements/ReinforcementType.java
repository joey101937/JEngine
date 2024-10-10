package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public abstract class ReinforcementType {

    public String name;
    public ArrayList<String> infoLines = new ArrayList<>();
    public HashMap<Class, Integer> contents = new HashMap<>();
    public BufferedImage icon;
    public BufferedImage hoverIcon;
    public abstract void onTrigger(Coordinate targetLocation, int team);
    
    public static ReinforcementType mediumTanks = new ReinforcementTypeMediumTanks();
    public static ReinforcementType lightTanks = new ReinforcementTypeLightTanks();
    public static ReinforcementType infantry; // todo
    public static ReinforcementType hellicopters = new ReinforcementTypeHellicopters();
    public static ReinforcementType transport; // todo
    public static ReinforcementType antiAir; // todo


}

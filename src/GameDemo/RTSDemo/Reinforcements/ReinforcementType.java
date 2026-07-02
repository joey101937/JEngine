package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public abstract class ReinforcementType implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public ArrayList<String> infoLines = new ArrayList<>();
    public HashMap<Class, Integer> contents = new HashMap<>();
    public transient BufferedImage icon;
    public transient BufferedImage hoverIcon;
    /**
     * Spawns this reinforcement's units for the given team near the target.
     * @param commandGroup shared group id so both simulations group the spawned
     *                     units identically (generated once by the caller)
     */
    public abstract void onTrigger(Coordinate targetLocation, int team, String commandGroup);

    /**
     * Restore transient fields after deserialization.
     * Override in subclasses to restore icons.
     */
    protected abstract void restoreTransientFields();
    
    public static ReinforcementType mediumTanks = new ReinforcementTypeMediumTanks();
    public static ReinforcementType lightTanks = new ReinforcementTypeLightTanks();
    public static ReinforcementType infantry = new ReinforcementTypeInfantry(); // todo
    public static ReinforcementType hellicopters = new ReinforcementTypeHellicopters();
    public static ReinforcementType transport; // todo
    public static ReinforcementType antiAir; // todo


}

package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class PrioritizeVehiclesButton extends CommandButton {

    public PrioritizeVehiclesButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.prioritizeVehiclesButton;
        this.hoveredImage = iconImage;

        this.name = "Passive: Prioritize Vehicles";
        this.isPassive = true;
        tooltipLines.add("Picks vehicles and aircraft over infantry when several enemies");
        tooltipLines.add("are in range and no target has been assigned manually. Among");
        tooltipLines.add("vehicles, the nearest one is still chosen first.");
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.prioritizeVehiclesButton;
        this.hoveredImage = iconImage;
    }
}

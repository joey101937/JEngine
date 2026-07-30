package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class PrioritizeInfantryButton extends CommandButton {

    public PrioritizeInfantryButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.prioritizeInfantryButton;
        this.hoveredImage = iconImage;

        this.name = "Passive: Prioritize Infantry";
        this.isPassive = true;
        tooltipLines.add("Picks infantry over vehicles when several enemies are in range");
        tooltipLines.add("and no target has been assigned manually. Among infantry, the");
        tooltipLines.add("nearest one is still chosen first.");
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.prioritizeInfantryButton;
        this.hoveredImage = iconImage;
    }
}

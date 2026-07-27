package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class ReinforcedHullButton extends CommandButton {

    public ReinforcedHullButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.reinforcedHullButton;
        this.hoveredImage = iconImage;

        this.name = "Passive: Reinforced Hull";
        this.isPassive = true;
        tooltipLines.add("The hardened hull blunts every hit this unit takes");
        tooltipLines.add("Subtracts 1 damage from all sources");
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.reinforcedHullButton;
        this.hoveredImage = iconImage;
    }

}

package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class LayMineButton extends CommandButton {

    public LayMineButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.landmineSelectionImage;
        this.name = "Deploy Landmine";
        this.numUsesRemaining = 1;
        this.isPassive = false;
        this.tooltipLines.add("Creates a friendly landmine at current location.");
        this.tooltipLines.add("Mine deals 30 damage to enemy unit and immobilizes it for 10s.");
        this.tooltipLines.add("Mine is invisible to enemy unless nearby two or more enemy infantry.");

        this.onTrigger = c -> {
            numUsesRemaining--;
            o.triggerAbility(0, o.getPixelLocation());
        };

    }

}

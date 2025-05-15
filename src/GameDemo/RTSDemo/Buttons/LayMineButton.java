package GameDemo.RTSDemo.Buttons;

import Framework.Coordinate;
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
        this.iconImage = RTSAssetManager.layMineButton;
        this.name = "Deploy Landmine";
        this.numUsesRemaining = 3;
        this.isPassive = false;
        this.tooltipLines.add("Creates a friendly landmine at current location.");
        this.tooltipLines.add("Mine deals 30 damage to enemy unit and immobilizes it for 10s.");
        this.tooltipLines.add("Mine is invisible to enemy unless nearby two or more enemy infantry.");
        this.cooldownSeconds = 1.5;

        this.onTrigger = c -> {
            if(isOnCooldown()) return;
            if(numUsesRemaining>0) {
                Coordinate offset = new Coordinate(0, o.getHeight()/2);
                offset.adjustForRotation(o.getRotation());
                o.triggerAbility(0, o.getPixelLocation().add(offset));
                numUsesRemaining--;
                if(numUsesRemaining == 0) isDisabled = true;
                tickLastUsed = tickNumber;
            }                
        };

    }

}

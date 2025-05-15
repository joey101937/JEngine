package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class FrontalArmorButton extends CommandButton{
     

    public FrontalArmorButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.frontalArmorButton;
        this.hoveredImage = iconImage;
        this.cooldownSeconds = 5;
        
        this.name = "Passive: Frontal Armor";
        this.isPassive = true;
        tooltipLines.add("Frontal armor decreases damage when hit from the front");
        tooltipLines.add("Subtracts 5 base damage from all sources in front of the tank");
    }


}

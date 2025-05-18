package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class InfantryButton extends CommandButton{
     

    public InfantryButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.infantryHelmetButton;
        this.hoveredImage = iconImage;
        
        this.name = "Passive: Infantry";
        this.isPassive = true;
        tooltipLines.add("This unit is infantry. It has a longer vision radius, can");
        tooltipLines.add("spot landmines when nearby other infantry, and can take cover ");
        tooltipLines.add("in some terrain features ");
    }


}

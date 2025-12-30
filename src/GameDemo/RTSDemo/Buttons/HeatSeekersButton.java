package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class HeatSeekersButton extends CommandButton{
     

    public HeatSeekersButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.heatSeekersButton;
        this.hoveredImage = iconImage;

        this.name = "Passive: Heat Seekers";
        this.isPassive = true;
        tooltipLines.add("This unit has increased range and projectiles fired");
        tooltipLines.add("will follow vehicular targets");
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.heatSeekersButton;
        this.hoveredImage = iconImage;
    }

}

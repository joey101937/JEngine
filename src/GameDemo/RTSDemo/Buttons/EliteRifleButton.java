package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.Rifleman;

/**
 *
 * @author guydu
 */
public class EliteRifleButton extends CommandButton {

    public EliteRifleButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.eliteRifleButton;
        this.hoveredImage = iconImage;

        this.name = "Passive: Elite Rifle";
        this.isPassive = true;
        tooltipLines.add("Reaches " + Rifleman.ELITE_RANGE_BONUS_VS_INFANTRY + " further against infantry targets.");
        tooltipLines.add("Each shot deals +" + Rifleman.ELITE_CLOSE_DAMAGE_BONUS + " damage against any target");
        tooltipLines.add("within " + Rifleman.ELITE_CLOSE_RANGE + " of the rifleman.");
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.eliteRifleButton;
        this.hoveredImage = iconImage;
    }
}

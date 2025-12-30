
package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class DigOutButton extends CommandButton {
    
    public DigOutButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.digOutButton;
        this.cooldownSeconds = 5;
        
        this.name = "Dig Out";
        this.isPassive = false;
        this.tooltipLines.add("Removes sandbags from tank, allowing it to move again.");
        this.tooltipLines.add("This action disables the tank for 5 seconds.");
        
        this.onTrigger = c -> {
            if(!isDisabled) {
                this.setDisabled(true);
                tickLastUsed = tickNumber;
                TankUnit host = (TankUnit) owner;
                host.startPickingUpSandbags();
            }
        };
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.digOutButton;
        this.onTrigger = c -> {
            if(!isDisabled) {
                this.setDisabled(true);
                tickLastUsed = tickNumber;
                TankUnit host = (TankUnit) owner;
                host.startPickingUpSandbags();
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        TankUnit tank = (TankUnit) owner;
        isDisabled = isOnCooldown() || !tank.sandbagActive;
    }

}

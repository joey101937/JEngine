package GameDemo.RTSDemo.Buttons;

import Framework.Main;
import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class DigInButton extends CommandButton {    
    
    public DigInButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.digInButton;
        this.hoveredImage = RTSAssetManager.digInButtonHovered;
        this.disabledImage = RTSAssetManager.digInButtonDisabled;
        this.cooldownSeconds = 5;
        
        this.name = "Dig In";
        this.isPassive = false;
        this.tooltipLines.add("Deploys sandbags around tank, disabling movement but");
        this.tooltipLines.add("increasing survivability. This action disables the tank for 5 seconds.");
        this.tooltipLines.add("Sandbags reduce damage from large hits by 75% for up to two uses.");
        this.tooltipLines.add("Uses will recharge over time");

        this.onTrigger = c -> {
            if(!isDisabled) {
                this.setDisabled(true);
                tickLastUsed = tickNumber;
                TankUnit host = (TankUnit) o;
                host.startDeployingSandbags();             
            }
        };
    }
    
    @Override
    public void tick() {
        super.tick();
        TankUnit tank = (TankUnit) owner;
        isDisabled = isOnCooldown() || tank.sandbagActive;
    }
    
}

package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class DigInButton extends CommandButton {
    // the sandbag zone check is a spatial query, so it is refreshed periodically rather than every tick
    private static final int ZONE_CHECK_INTERVAL = 5;
    private boolean cachedZoneClear = true;

    public DigInButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.digInButton;
        this.cooldownSeconds = 5;
        
        this.name = "Dig In";
        this.isPassive = false;
        this.tooltipLines.add("Deploys sandbags around tank, disabling movement but");
        this.tooltipLines.add("increasing survivability. This action disables the tank for 5 seconds.");
        this.tooltipLines.add("Sandbags reduce damage from large hits by 75% for up to two uses.");
        this.tooltipLines.add("Uses will recharge over time");

        this.onTrigger = c -> {
            if (!isDisabled) {
                this.setDisabled(true);
                tickLastUsed = tickNumber;
            }
        };
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.digInButton;
        this.onTrigger = c -> {
            if (!isDisabled) {
                this.setDisabled(true);
                tickLastUsed = tickNumber;
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        TankUnit tank = (TankUnit) owner;
        boolean unavailable = isOnCooldown() || tank.sandbagActive || tank.isImmobilized;
        // The zone answer only matters while the button is otherwise available, and it is
        // refreshed on an interval rather than every tick. The actual deploy re-checks the zone
        // itself, so a stale value here can only affect how the button looks, never the outcome.
        if (!unavailable && tickNumber % ZONE_CHECK_INTERVAL == 0) {
            cachedZoneClear = tank.isSandbagZoneClear();
        }
        isDisabled = unavailable || !cachedZoneClear;
    }

}

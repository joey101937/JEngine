package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;

public class LaunchMissileButton extends CommandButton {

    public LaunchMissileButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.heatSeekersButton; // placeholder until missile icon is added
        this.name = "Launch Missile";
        this.isPassive = false;
        this.cooldownSeconds = 10;
        this.requiresTarget = true;
        this.maxCastRange = 700;
        this.minCastRange = 300;
        this.tooltipLines.add("Launches a missile at the target location.");
        this.tooltipLines.add("Deals damage in the impact area. Range: 600.");

        this.onTrigger = c -> {};  // cooldown started by Apache when firing actually begins
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.heatSeekersButton;
        this.onTrigger = c -> {};  // cooldown started by Apache when firing actually begins
    }
}

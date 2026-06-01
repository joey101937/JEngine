package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;

public class LandButton extends CommandButton {

    public LandButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.landButton;
        this.name = "Land";
        this.isPassive = false;
        this.tooltipLines.add("Lands the helicopter on the ground (~2 seconds).");
        this.tooltipLines.add("While landed: cannot move, loses sight-blocker immunity,");
        this.tooltipLines.add("and becomes targetable by ground-based weapons.");
        this.tooltipLines.add("Only available over non-water terrain.");
        this.onTrigger = c -> {};
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.landButton;
        this.onTrigger = c -> {};
    }

    @Override
    public void tick() {
        super.tick();
        TransportHelicopter heli = (TransportHelicopter) owner;
        isDisabled = heli.isLanded || heli.isLanding || heli.isTakingOff
                || heli.isRubble || !heli.isOnGroundTerrain() || !heli.isLandingZoneClear();
    }
}

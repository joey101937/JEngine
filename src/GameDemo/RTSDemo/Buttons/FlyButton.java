package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;

public class FlyButton extends CommandButton {

    public FlyButton(RTSUnit o) {
        super(o);
        this.iconImage = RTSAssetManager.flyButton;
        this.name = "Take Off";
        this.isPassive = false;
        this.tooltipLines.add("Takes off from the ground (~2 seconds).");
        this.tooltipLines.add("Restores flight, movement, and sight-blocker immunity.");
        this.onTrigger = c -> {};
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.flyButton;
        this.onTrigger = c -> {};
    }

    @Override
    public void tick() {
        super.tick();
        TransportHelicopter heli = (TransportHelicopter) owner;
        isDisabled = !heli.isLanded || heli.isLanding || heli.isTakingOff || heli.isRubble;
    }
}

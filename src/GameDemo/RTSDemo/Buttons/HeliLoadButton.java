package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Transport;
import GameDemo.RTSDemo.Units.TransportHelicopter;

public class HeliLoadButton extends LoadUnitButton {

    public HeliLoadButton(RTSUnit owner) {
        super(owner);
        this.tooltipLines.clear();
        this.tooltipLines.add("Order a friendly unit to board this helicopter.");
        this.tooltipLines.add("Helicopter must be landed. Accepts infantry and vehicles.");
    }

    @Override
    public void tick() {
        super.tick(); // handles capacity disable
        TransportHelicopter heli = (TransportHelicopter) owner;
        if (!heli.isLanded || heli.isRubble || heli.isUnloading()) {
            isDisabled = true;
        }
    }
}

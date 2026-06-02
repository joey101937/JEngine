package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;

public class HeliUnloadButton extends UnloadAllButton {

    public HeliUnloadButton(RTSUnit owner) {
        super(owner);
        this.tooltipLines.clear();
        this.tooltipLines.add("Unloads all transported units around the helicopter.");
        this.tooltipLines.add("Helicopter must be landed.");
    }

    @Override
    public void tick() {
        super.tick(); // handles empty-cargo disable
        TransportHelicopter heli = (TransportHelicopter) owner;
        if (!heli.isLanded || heli.isRubble || heli.isUnloading()) {
            isDisabled = true;
        }
    }
}

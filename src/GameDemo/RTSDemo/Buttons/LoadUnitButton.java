package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Transport;

public class LoadUnitButton extends CommandButton {

    public LoadUnitButton(RTSUnit owner) {
        super(owner);
        this.iconImage = RTSAssetManager.loadButton;
        this.name = "Load Unit";
        this.isPassive = false;
        this.requiresUnitTarget = true;
        this.tooltipLines.add("Order a friendly infantry unit to board this transport.");
        this.tooltipLines.add("Right-clicking this transport with infantry selected also works.");
        this.onTrigger = c -> {
            tickLastUsed = tickNumber;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (owner instanceof Transport t) {
            int currentLoad = t.getLoadedUnits().stream().mapToInt(u -> u.cargoSize).sum();
            isDisabled = currentLoad >= t.getMaxCapacity();
        }
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.loadButton;
        this.onTrigger = c -> {
            tickLastUsed = tickNumber;
        };
    }
}

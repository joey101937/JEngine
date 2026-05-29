package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Transport;

public class UnloadAllButton extends CommandButton {

    public UnloadAllButton(RTSUnit owner) {
        super(owner);
        this.iconImage = RTSAssetManager.unloadButton;
        this.name = "Unload All";
        this.isPassive = false;
        this.requiresTarget = false;
        this.tooltipLines.add("Unloads all transported units around this vehicle.");
        this.onTrigger = c -> {
            tickLastUsed = tickNumber;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (owner instanceof Transport t) {
            isDisabled = t.getLoadedUnits().isEmpty();
        }
    }

    @Override
    public void restoreTransientFields() {
        this.iconImage = RTSAssetManager.unloadButton;
        this.onTrigger = c -> {
            tickLastUsed = tickNumber;
        };
    }
}

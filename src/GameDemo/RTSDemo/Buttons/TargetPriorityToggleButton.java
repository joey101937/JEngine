package GameDemo.RTSDemo.Buttons;

import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.TargetPriority;
import java.awt.image.BufferedImage;

/**
 * Cycles the owning unit between neutral targeting, favouring infantry, and favouring
 * vehicles. The icon and name track the mode currently selected so the panel shows at a
 * glance what the unit is set to.
 *
 * @author guydu
 */
public class TargetPriorityToggleButton extends CommandButton {

    // Mode last drawn for, so the icon and text are only rebuilt when it actually changes
    private transient TargetPriority displayedPriority;

    public TargetPriorityToggleButton(RTSUnit o) {
        super(o);
        this.isPassive = false;
        this.onTrigger = c -> owner.targetPriority = owner.targetPriority.next();
        applyPriorityToDisplay();
    }

    @Override
    public void restoreTransientFields() {
        this.onTrigger = c -> owner.targetPriority = owner.targetPriority.next();
        this.displayedPriority = null;
        applyPriorityToDisplay();
    }

    @Override
    public void tick() {
        super.tick();
        isDisabled = owner.isRubble;
        applyPriorityToDisplay();
    }

    /** Points the icon, name and tooltip at whichever mode the unit is currently in. */
    private void applyPriorityToDisplay() {
        TargetPriority current = owner.targetPriority;
        if (current == displayedPriority) {
            return;
        }
        displayedPriority = current;
        this.iconImage = getIconFor(current);
        this.hoveredImage = iconImage;
        this.name = "Target Priority: " + current.displayName;
        tooltipLines.clear();
        tooltipLines.add("Currently favouring: " + describe(current));
        tooltipLines.add("Cycles between Neutral, Infantry and Vehicles.");
        tooltipLines.add("Applies when several enemies are in range and no target");
        tooltipLines.add("has been assigned manually; the nearest of the favoured");
        tooltipLines.add("kind is chosen first.");
    }

    private static BufferedImage getIconFor(TargetPriority priority) {
        return switch (priority) {
            case INFANTRY -> RTSAssetManager.prioritizeInfantryButton;
            case VEHICLES -> RTSAssetManager.prioritizeVehiclesButton;
            default -> RTSAssetManager.targetPriorityToggleButton;
        };
    }

    private static String describe(TargetPriority priority) {
        return switch (priority) {
            case INFANTRY -> "infantry";
            case VEHICLES -> "vehicles and aircraft";
            default -> "nothing in particular - shoots the nearest enemy";
        };
    }
}

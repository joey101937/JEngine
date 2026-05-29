package GameDemo.RTSDemo.Commands;

import Framework.Coordinate;
import Framework.Window;
import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSUnit;

public class TriggerAbilityCommand implements Command {
    private static final long serialVersionUID = 1L;

    private static final int NO_TARGET = Integer.MIN_VALUE;

    private final long executeTick;
    private final String subjectId;
    private final int abilityIndex;
    private final int targetX;
    private final int targetY;
    private final String targetUnitId; // non-null when ability targets a specific unit
    private boolean hasResolved = false;

    public TriggerAbilityCommand(long executeTick, String subjectId, int abilityIndex, Coordinate target, String targetUnitId) {
        this.executeTick = executeTick;
        this.subjectId = subjectId;
        this.abilityIndex = abilityIndex;
        this.targetUnitId = targetUnitId;
        if (target != null) {
            this.targetX = target.x;
            this.targetY = target.y;
        } else {
            this.targetX = NO_TARGET;
            this.targetY = NO_TARGET;
        }
    }

    @Override
    public long getExecuteTick() {
        return executeTick;
    }

    @Override
    public String getSubjectId() {
        return subjectId;
    }

    @Override
    public boolean execute() {
        RTSUnit subject = (RTSUnit) Window.currentGame.getObjectById(subjectId);
        if (subject == null) {
            System.out.println("ERROR: TriggerAbilityCommand - unit not found: " + subjectId);
            return true;
        }
        if (abilityIndex < 0 || abilityIndex >= subject.getButtons().size()) {
            System.out.println("ERROR: TriggerAbilityCommand - button " + abilityIndex + " not found on: " + subjectId);
            return true;
        }
        Coordinate target = (targetX == NO_TARGET) ? null : new Coordinate(targetX, targetY);
        CommandButton button = subject.getButtons().get(abilityIndex);
        if (button.onTrigger != null) {
            button.onTrigger.accept(target);
        }
        subject.triggerAbility(abilityIndex, target, targetUnitId);
        return true;
    }

    @Override
    public String toMpString() {
        if (targetUnitId != null) {
            return "ta:" + subjectId + "," + abilityIndex + ",unit:" + targetUnitId + "," + executeTick;
        }
        if (targetX == NO_TARGET) {
            return "ta:" + subjectId + "," + abilityIndex + ",null," + executeTick;
        }
        return "ta:" + subjectId + "," + abilityIndex + "," + targetX + "," + targetY + "," + executeTick;
    }

    @Override
    public boolean hasResolved() {
        return hasResolved;
    }

    @Override
    public void setHasResolved(boolean b) {
        hasResolved = b;
    }

    public static TriggerAbilityCommand generateFromMpString(String s) {
        String body = s.substring(3); // strip "ta:"
        String[] parts = body.split(",");
        String unitId = parts[0];
        int abilityIndex = Integer.parseInt(parts[1]);
        if (parts[2].startsWith("unit:")) {
            String targetUnitId = parts[2].substring(5);
            long executeTick = Long.parseLong(parts[3]);
            return new TriggerAbilityCommand(executeTick, unitId, abilityIndex, (Coordinate) null, targetUnitId);
        } else if ("null".equals(parts[2])) {
            long executeTick = Long.parseLong(parts[3]);
            return new TriggerAbilityCommand(executeTick, unitId, abilityIndex, null, null);
        } else {
            int tX = Integer.parseInt(parts[2]);
            int tY = Integer.parseInt(parts[3]);
            long executeTick = Long.parseLong(parts[4]);
            return new TriggerAbilityCommand(executeTick, unitId, abilityIndex, new Coordinate(tX, tY), null);
        }
    }

    @Override
    public String toString() {
        return toMpString();
    }
}

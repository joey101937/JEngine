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
    private boolean hasResolved = false;

    public TriggerAbilityCommand(long executeTick, String subjectId, int abilityIndex, Coordinate target) {
        this.executeTick = executeTick;
        this.subjectId = subjectId;
        this.abilityIndex = abilityIndex;
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
        subject.triggerAbility(abilityIndex, target);
        return true;
    }

    @Override
    public String toMpString() {
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
        if ("null".equals(parts[2])) {
            long executeTick = Long.parseLong(parts[3]);
            return new TriggerAbilityCommand(executeTick, unitId, abilityIndex, null);
        } else {
            int tX = Integer.parseInt(parts[2]);
            int tY = Integer.parseInt(parts[3]);
            long executeTick = Long.parseLong(parts[4]);
            return new TriggerAbilityCommand(executeTick, unitId, abilityIndex, new Coordinate(tX, tY));
        }
    }

    @Override
    public String toString() {
        return toMpString();
    }
}

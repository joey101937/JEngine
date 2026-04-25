package GameDemo.RTSDemo.Commands;

import Framework.Window;
import GameDemo.RTSDemo.RTSUnit;

public class SetPreferredTargetCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final long executeTick;
    private final String subjectID;
    private final String targetID;
    private boolean hasResolved = false;

    public SetPreferredTargetCommand(long executeTick, String subjectID, String targetID) {
        this.executeTick = executeTick;
        this.subjectID = subjectID;
        this.targetID = targetID;
    }

    @Override
    public long getExecuteTick() { return executeTick; }

    @Override
    public String getSubjectId() { return subjectID; }

    @Override
    public boolean execute() {
        RTSUnit subject = (RTSUnit) Window.currentGame.getObjectById(subjectID);
        if (subject == null) {
            return true;
        }
        subject.setPreferredTarget(targetID);
        return true;
    }

    @Override
    public boolean hasResolved() { return hasResolved; }

    @Override
    public void setHasResolved(boolean b) { hasResolved = b; }

    @Override
    public String toMpString() {
        return "pt:" + subjectID + "," + targetID + "," + executeTick;
    }

    public static SetPreferredTargetCommand generateFromMpString(String s) {
        var parts = s.substring(3).split(",");
        return new SetPreferredTargetCommand(Long.parseLong(parts[2]), parts[0], parts[1]);
    }

    @Override
    public String toString() {
        return toMpString();
    }
}

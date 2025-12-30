package GameDemo.RTSDemo.Commands;

import Framework.Window;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class StopCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final long executeTick;
    private String subjectID;
    private boolean hasResolved = false;

    public StopCommand (long t, String subjectID) {
        this.executeTick = t;
        this.subjectID = subjectID;
    }
    
    @Override
    public long getExecuteTick() {
        return executeTick;
    }

    @Override
    public String getSubjectId() {
        return this.subjectID;
    }

    @Override
    public boolean execute() {
        RTSUnit subject = (RTSUnit) Window.currentGame.getObjectById(subjectID);
        if (subject == null) {
            System.out.println("ERROR: unable to execute stop command - unit not found: " + subjectID);
            return true; // Mark as resolved so we don't keep trying
        }
        subject.setDesiredLocation(subject.getPixelLocation());
        subject.setCommandGroup("0");
        return true;
    }

    @Override
    public String toMpString() {
        return "s:" + subjectID + "," + executeTick;
    }

    @Override
    public boolean hasResolved() {
      return hasResolved;
    }

    @Override
    public void setHasResolved(boolean b) {
        hasResolved = b;
    }
    
    public static StopCommand generateFromMpString(String s) {
        var components = s.substring(2).split(",");
        String unitId = components[0];
        long executeTick = Long.parseLong(components[1]);
        return new StopCommand(executeTick, unitId);
    }
}

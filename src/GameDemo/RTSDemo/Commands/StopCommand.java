package GameDemo.RTSDemo.Commands;

import Framework.Coordinate;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class StopCommand implements Command {
    private final long executeTick;
    private final RTSUnit subject;
    private boolean hasResolved = false;
    
    public StopCommand (long t, RTSUnit subject) {
        this.executeTick = t;
        this.subject = subject;
    }
    
    @Override
    public long getExecuteTick() {
        return executeTick;
    }

    @Override
    public String getSubjectId() {
        return this.subject.ID;
    }

    @Override
    public boolean execute() {
        subject.setDesiredLocation(subject.getPixelLocation());
        subject.setCommandGroup("0");
        return true;
    }

    @Override
    public String toMpString() {
        return "s:" + subject.ID + "," + executeTick;
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
        long executeTick = Long.parseLong(components[1]); // this is when the input was actually done
        RTSUnit subject = (RTSUnit) Window.currentGame.getObjectById(unitId);
        if(subject == null) {
            System.out.println("ERROR: unable to find unit by id " + unitId);
        }
        return new StopCommand(executeTick, subject);
    }
}

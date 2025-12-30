package GameDemo.RTSDemo.Commands;

import Framework.Coordinate;
import Framework.Window;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class MoveCommand implements Command{
    private static final long serialVersionUID = 1L;

    private long executeTick;
    private String subjectID;
    private Coordinate target;
    private String commandGroup;
    private boolean hasResolved = false;

    public MoveCommand(long executeTick, String subjectID, Coordinate target, String commandGroup) {
        this.executeTick = executeTick;
        this.subjectID = subjectID;
        this.target = target;
        this.commandGroup = commandGroup;
        System.out.println("move command created " + executeTick);
    }
    
    @Override
    public long getExecuteTick() {
        return this.executeTick;
    }

    @Override
    public String getSubjectId() {
        return this.subjectID;
    }

    @Override
    public boolean execute() {
        RTSUnit subject = (RTSUnit) Window.currentGame.getObjectById(subjectID);
        if (subject == null) {
            System.out.println("ERROR: unable to execute move command - unit not found: " + subjectID);
            return true; // Mark as resolved so we don't keep trying
        }
        subject.setDesiredLocation(target);
        subject.setCommandGroup(commandGroup);
        return true;
    }

    @Override
    public boolean hasResolved() {
        return hasResolved;
    }

    @Override
    public void setHasResolved (boolean b) {
        hasResolved = b;
    }

    @Override
    public String toMpString() {
        return "m:" + subjectID + "," + target.x + ',' + target.y + "," + executeTick + ","+commandGroup;
    }
    
    public static MoveCommand generateFromMpString(String s) {
        var components = s.substring(2).split(",");
        String unitId = components[0];
        int x = Integer.parseInt(components[1]);
        int y = Integer.parseInt(components[2]);
        long executeTick = Long.parseLong(components[3]);
        String comGroup = components[4];
        return new MoveCommand(executeTick, unitId, new Coordinate(x, y), comGroup);
    }
    
}

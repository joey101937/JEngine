package GameDemo.RTSDemo.Commands;

import Framework.Coordinate;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class MoveCommand implements Command{
    private static final long serialVersionUID = 1L;

    private long executeTick;
    private transient RTSUnit subject;
    private String subjectID; // For serialization
    private Coordinate target;
    private String commandGroup;
    private boolean hasResolved = false;

    public MoveCommand(long executeTick, RTSUnit subject, Coordinate target, String commandGroup) {
        this.executeTick = executeTick;
        this.subject = subject;
        this.subjectID = subject != null ? subject.ID : null;
        this.target = target;
        this.commandGroup = commandGroup;
        System.out.println("move command created " + executeTick);
    }

    /**
     * Restore subject reference after deserialization
     */
    public void resolveSubject(Framework.Game game) {
        if (subject == null && subjectID != null) {
            subject = (RTSUnit) game.getObjectById(subjectID);
        }
    }
    
    @Override
    public long getExecuteTick() {
        return this.executeTick;
    }

    @Override
    public String getSubjectId() {
        return this.subject.ID;
    }

    @Override
    public boolean execute() {
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
        return "m:" + subject.ID + "," + target.x + ',' + target.y + "," + executeTick + ","+commandGroup;
    }
    
    public static MoveCommand generateFromMpString(String s) {
        var components = s.substring(2).split(",");
        String unitId = components[0];
        int x = Integer.parseInt(components[1]);
        int y = Integer.parseInt(components[2]);
        long executeTick = Long.parseLong(components[3]); // this is when the input was actually done
        String comGroup = components[4];
        RTSUnit subject = (RTSUnit) Window.currentGame.getObjectById(unitId);
        if(subject == null) {
            System.out.println("ERROR: unable to find unit by id " + unitId);
        }
        return new MoveCommand(executeTick, subject, new Coordinate(x,y), comGroup);
    }
    
}

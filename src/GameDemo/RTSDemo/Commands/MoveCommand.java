package GameDemo.RTSDemo.Commands;

import Framework.Coordinate;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class MoveCommand implements Command{
    private long executeTick;
    private RTSUnit subject;
    private Coordinate target;
    private String commandGroup;
    private boolean hasResolved = false;
    
    public MoveCommand(long executeTick, RTSUnit subject, Coordinate target, String commandGroup) {
        this.executeTick = executeTick;
        this.subject = subject;
        this.target = target;
        this.commandGroup = commandGroup;
        System.out.println("move command created" + executeTick);
    }
    
    @Override
    public long getExecuteTick() {
        return this.executeTick;
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
        RTSUnit subject = (RTSUnit) RTSGame.game.getObjectById(unitId);
        if(subject == null) {
            System.out.println("ERROR: unable to find unit by id " + unitId);
        }
        return new MoveCommand(executeTick, subject, new Coordinate(x,y), comGroup);
    }
    
}

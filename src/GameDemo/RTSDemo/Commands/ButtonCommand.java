package GameDemo.RTSDemo.Commands;

import Framework.Window;
import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class ButtonCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final long executeTick;
    private String subjectID;
    private int buttonIndex;
    private boolean hasResolved = false;

    public ButtonCommand (long t, String subjectID, int buttonIndex) {
        this.executeTick = t;
        this.buttonIndex = buttonIndex;
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
            System.out.println("ERROR: unable to execute button command - unit not found: " + subjectID);
            return true; // Mark as resolved so we don't keep trying
        }
        CommandButton button = subject.getButtons().get(buttonIndex);
        if(button == null) {
            System.out.println("ERROR: unable to execute button command - button " + buttonIndex + " not found on unit: " + subjectID);
            return true; // Mark as resolved so we don't keep trying
        }
        button.onTrigger.accept(null);
        return true;
    }

    @Override
    public String toMpString() {
        return "b:" + subjectID  + "," + buttonIndex + "," + executeTick;
    }

    @Override
    public boolean hasResolved() {
      return hasResolved;
    }

    @Override
    public void setHasResolved(boolean b) {
        hasResolved = b;
    }
    
    public static ButtonCommand generateFromMpString(String s) {
        var components = s.substring(2).split(",");
        String unitId = components[0];
        int buttonIndex = Integer.parseInt(components[1]);
        long executeTick = Long.parseLong(components[2]);
        return new ButtonCommand(executeTick, unitId, buttonIndex);
    }
    
    @Override
    public String toString() {
        return this.toMpString();
    }
}

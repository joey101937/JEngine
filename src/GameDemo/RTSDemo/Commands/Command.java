package GameDemo.RTSDemo.Commands;

/**
 *
 * @author guydu
 */
public interface Command {
    /**
     * @return what tick to execute the command on
     */
    public long getExecuteTick();
    /**
     * actually runs the command
     * @return true = success, false = failed
     */
    public boolean execute(); 
    /**
     * @return stringified form of this command for multiplayer transmission purposes
     */
    public String toMpString();
    
    /**
     * if the command has resolved
     * @return yes/no
     */
    public boolean hasResolved();
    
    /**
     * sets if the command has resolved 
     * @param b input
     */
    public void setHasResolved(boolean b);
}

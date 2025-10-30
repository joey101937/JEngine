package GameDemo.RTSDemo.Commands;

/**
 *
 * @author guydu
 */
public interface Command extends Comparable<Command> {
    /**
     * @return what tick to execute the command on
     */
    public long getExecuteTick();

    /**
     * @return the ID of the subject of this command
     */
    public String getSubjectId();

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

    /**
     * Default comparison: first by executeTick, then by subject ID
     */
    @Override
    default int compareTo(Command other) {
        int tickComparison = Long.compare(this.getExecuteTick(), other.getExecuteTick());
        if (tickComparison != 0) {
            return tickComparison;
        }
        return this.getSubjectId().compareTo(other.getSubjectId());
    }
}

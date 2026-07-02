package GameDemo.RTSDemo.Commands;

import Framework.Coordinate;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Reinforcements.ReinforcementHandler;
import GameDemo.RTSDemo.Reinforcements.ReinforcementType;

/**
 * Spawns a batch of reinforcements deterministically on every simulation.
 *
 * A player calling reinforcements is a purely local UI action, but the units
 * it spawns must appear identically on both machines. Routing the spawn through
 * a command means {@link #execute()} runs on the same tick on both simulations,
 * so the reinforcement units receive matching IDs and command groups.
 *
 * @author guydu
 */
public class CallReinforcementCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final long executeTick;
    private final int team;
    private final int reinforcementIndex;
    private final int targetX;
    private final int targetY;
    // Generated once by the caller and shipped over the wire so both machines
    // group the spawned units identically (avoids per-machine Math.random drift).
    private final String commandGroup;
    private boolean hasResolved = false;

    public CallReinforcementCommand(long executeTick, int team, int reinforcementIndex, Coordinate target, String commandGroup) {
        this.executeTick = executeTick;
        this.team = team;
        this.reinforcementIndex = reinforcementIndex;
        this.targetX = target.x;
        this.targetY = target.y;
        this.commandGroup = commandGroup;
    }

    @Override
    public long getExecuteTick() {
        return executeTick;
    }

    @Override
    public String getSubjectId() {
        // No single unit subject; a stable per-team id keeps command ordering deterministic.
        return "reinforcement_T" + team;
    }

    @Override
    public boolean execute() {
        ReinforcementHandler handler = RTSGame.reinforcementHandler;
        if (handler == null) {
            System.out.println("ERROR: CallReinforcementCommand - no reinforcement handler present");
            return true;
        }
        if (reinforcementIndex < 0 || reinforcementIndex >= handler.reinforcementTypes.size()) {
            System.out.println("ERROR: CallReinforcementCommand - bad reinforcement index: " + reinforcementIndex);
            return true;
        }
        ReinforcementType type = handler.reinforcementTypes.get(reinforcementIndex);
        type.onTrigger(new Coordinate(targetX, targetY), team, commandGroup);
        return true;
    }

    @Override
    public String toMpString() {
        return "rf:" + team + "," + reinforcementIndex + "," + targetX + "," + targetY + "," + executeTick + "," + commandGroup;
    }

    @Override
    public boolean hasResolved() {
        return hasResolved;
    }

    @Override
    public void setHasResolved(boolean b) {
        hasResolved = b;
    }

    public static CallReinforcementCommand generateFromMpString(String s) {
        String[] parts = s.substring(3).split(","); // strip "rf:"
        int team = Integer.parseInt(parts[0]);
        int reinforcementIndex = Integer.parseInt(parts[1]);
        int targetX = Integer.parseInt(parts[2]);
        int targetY = Integer.parseInt(parts[3]);
        long executeTick = Long.parseLong(parts[4]);
        String commandGroup = parts[5];
        return new CallReinforcementCommand(executeTick, team, reinforcementIndex, new Coordinate(targetX, targetY), commandGroup);
    }

    @Override
    public String toString() {
        return toMpString();
    }
}

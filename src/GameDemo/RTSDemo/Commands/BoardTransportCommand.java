package GameDemo.RTSDemo.Commands;

import Framework.Window;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Transport;

public class BoardTransportCommand implements Command {
    private static final long serialVersionUID = 1L;

    private final long executeTick;
    private final String unitId;
    private final String transportId;
    private boolean hasResolved = false;

    public BoardTransportCommand(long executeTick, String unitId, String transportId) {
        this.executeTick = executeTick;
        this.unitId = unitId;
        this.transportId = transportId;
    }

    @Override
    public long getExecuteTick() { return executeTick; }

    @Override
    public String getSubjectId() { return unitId; }

    @Override
    public boolean execute() {
        RTSUnit unit = (RTSUnit) Window.currentGame.getObjectById(unitId);
        RTSUnit transport = (RTSUnit) Window.currentGame.getObjectById(transportId);
        if (unit == null || unit.isRubble || !unit.isAlive()) return true;
        if (transport == null || transport.isRubble || !transport.isAlive()) return true;
        if (!(transport instanceof Transport t) || !t.canLoad(unit)) return true;
        unit.setBoardingTransportId(transportId);
        unit.clearPreferredTarget();
        return true;
    }

    @Override
    public boolean hasResolved() { return hasResolved; }

    @Override
    public void setHasResolved(boolean b) { hasResolved = b; }

    @Override
    public String toMpString() {
        return "bt:" + unitId + "," + transportId + "," + executeTick;
    }

    public static BoardTransportCommand generateFromMpString(String s) {
        String[] parts = s.substring(3).split(",");
        return new BoardTransportCommand(Long.parseLong(parts[2]), parts[0], parts[1]);
    }

    @Override
    public String toString() { return toMpString(); }
}

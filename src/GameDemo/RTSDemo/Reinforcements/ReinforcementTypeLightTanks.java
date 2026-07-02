package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.LightTank;

public class ReinforcementTypeLightTanks extends ReinforcementType {
    private static final long serialVersionUID = 1L;

    public ReinforcementTypeLightTanks() {
        this.name = "Light Tanks";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Maneuverable tanks");
        infoLines.add("Can lay landmines. Medium-Fast Speed.");
        contents.put(LightTank.class, 5);
    }

    @Override
    protected void restoreTransientFields() {
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
    }

    @Override
    public void onTrigger(Coordinate targetLocation, int team, String commandGroup) {
        ReinforcementPoint rp = ReinforcementPoint.getClosest(targetLocation, team);
        SpawnLocation spawn = rp.getSpawnLocation();
        Coordinate base = spawn.topLeft;
        int initialOffset = -360;
        for (int i = 0; i < 5; i++) {
            Coordinate spawnOffset = new Coordinate(initialOffset + (i * 120), 50);
            spawnOffset.adjustForRotation(spawn.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            LightTank tank = new LightTank(spawnLocation.x, spawnLocation.y, team);
            tank.setLocation(ReinforcementHandler.getClosestOpenLocation(spawnLocation, tank).toDCoordinate());
            tank.setRotation(spawn.rotation);
            tank.setCommandGroup(commandGroup);
            tank.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(tank);
        }
    }
}

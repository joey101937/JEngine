package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.TankUnit;


public class ReinforcementTypeMediumTanks extends ReinforcementType{
    private static final long serialVersionUID = 1L;

    public ReinforcementTypeMediumTanks(){
        this.name = "Medium Tanks";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Strong open-field combatants");
        infoLines.add("Can dig-in for extra defense. Medium Speed.");
        contents.put(TankUnit.class, 4);
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
        int initialOffset = -325;
        for (int i = 0; i < 4; i++) {
            Coordinate spawnOffset = new Coordinate(initialOffset + (i * 130), 50);
            spawnOffset.adjustForRotation(spawn.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            TankUnit tank = new TankUnit(spawnLocation.x, spawnLocation.y, team);
            tank.setLocation(ReinforcementHandler.getClosestOpenLocation(spawnLocation, tank).toDCoordinate());
            tank.setRotation(spawn.rotation);
            tank.setCommandGroup(commandGroup);
            tank.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(tank);
        }
    }
    
}

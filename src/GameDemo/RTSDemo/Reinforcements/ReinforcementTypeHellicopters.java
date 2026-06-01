package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSInput;
import GameDemo.RTSDemo.Units.Hellicopter;


public class ReinforcementTypeHellicopters extends ReinforcementType{
    private static final long serialVersionUID = 1L;

    public ReinforcementTypeHellicopters(){
        this.name = "Attack Hellicopters";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Air Strike");
        infoLines.add("Fast attack fliers. Can only be attacked by units with anti-air capabilities");
        contents.put(Hellicopter.class, 2);
    }

    @Override
    protected void restoreTransientFields() {
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
    }

    @Override
    public void onTrigger(Coordinate targetLocation, int team) {
        ReinforcementPoint rp = ReinforcementPoint.getClosest(targetLocation, team);
        SpawnLocation spawn = rp.getSpawnLocation();
        Coordinate base = spawn.topLeft;
        int initialOffset = -240;
        String commandGroup = RTSInput.generateRandomCommandGroup();
        for (int i = 0; i < 2; i++) {
            Coordinate spawnOffset = new Coordinate(initialOffset + (i * 200), 50);
            spawnOffset.adjustForRotation(spawn.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            Hellicopter heli = new Hellicopter(spawnLocation.x, spawnLocation.y, team);
            heli.setLocation(ReinforcementHandler.getClosestOpenLocation(spawnLocation, heli).toDCoordinate());
            heli.setRotation(spawn.rotation);
            heli.setCommandGroup(commandGroup);
            heli.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(heli);
        }
    }
    
}

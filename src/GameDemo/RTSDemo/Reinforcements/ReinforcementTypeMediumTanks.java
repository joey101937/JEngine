package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSInput;
import GameDemo.RTSDemo.Units.TankUnit;


public class ReinforcementTypeMediumTanks extends ReinforcementType{
    private static final long serialVersionUID = 1L;

    public ReinforcementTypeMediumTanks(){
        this.name = "Medium Tanks";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Strong open-field combatants");
        infoLines.add("Can dig-in for extra defense. Medium Speed.");
        contents.put(TankUnit.class, 5);
    }

    @Override
    protected void restoreTransientFields() {
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
    }

    @Override
    public void onTrigger(Coordinate targetLocation, int team) {
        KeyBuilding kb = KeyBuilding.getClosest(targetLocation, team);
        Coordinate base = kb.spawnLocation.topLeft;
        int initialOffset = -325;
        String commandGroup = RTSInput.generateRandomCommandGroup();
        for(int i = 0; i < 5; i ++) {
            Coordinate spawnOffset = new Coordinate(initialOffset + (i * 130), 50);
            spawnOffset.adjustForRotation(kb.spawnLocation.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            TankUnit tank = new TankUnit(spawnLocation.x, spawnLocation.y, team);
            tank.setLocation(ReinforcementHandler.getClosestOpenLocation(spawnLocation, tank).toDCoordinate());
            tank.setRotation(kb.spawnLocation.rotation);
            tank.setCommandGroup(commandGroup);
            tank.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(tank);
            
        }
    }
    
}

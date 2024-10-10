package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.TankUnit;


public class ReinforcementTypeMediumTanks extends ReinforcementType{
    
    public ReinforcementTypeMediumTanks(){
        this.name = "Medium tanks";
        infoLines.add("General Purpose Armor");
        infoLines.add("Can dig-in for extra defense. Medium Speed.");
        contents.put(TankUnit.class, 5);
    }

    @Override
    public void onTrigger(Coordinate targetLocation, int team) {
        KeyBuilding kb = KeyBuilding.getClosest(targetLocation, team);
        Coordinate base = kb.spawnLocation.topLeft;
        for(int i = 0; i < 5; i ++) {
            Coordinate spawnOffset = new Coordinate(i * 110, 50);
            spawnOffset.adjustForRotation(kb.spawnLocation.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            TankUnit tank = new TankUnit(spawnLocation.x, spawnLocation.y, team);
            tank.location = ReinforcementHandler.getClosestOpenLocation(spawnLocation, tank).toDCoordinate();
            tank.setRotation(kb.spawnLocation.rotation);
            tank.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(tank);
            
        }
    }
    
}

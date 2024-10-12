package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.Hellicopter;


public class ReinforcementTypeHellicopters extends ReinforcementType{
    
    public ReinforcementTypeHellicopters(){
        this.name = "Attack Hellicopters";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Air Strike");
        infoLines.add("Fast attack fliers. Can only be attacked by units with anti-air capabilities");
        contents.put(Hellicopter.class, 5);
    }

    @Override
    public void onTrigger(Coordinate targetLocation, int team) {
        KeyBuilding kb = KeyBuilding.getClosest(targetLocation, team);
        Coordinate base = kb.spawnLocation.topLeft;
        int initialOffset = -240;
        for(int i = 0; i < 3; i ++) {
            Coordinate spawnOffset = new Coordinate(initialOffset + (i * 160), 50);
            spawnOffset.adjustForRotation(kb.spawnLocation.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            Hellicopter heli = new Hellicopter(spawnLocation.x, spawnLocation.y, team);
            heli.location = ReinforcementHandler.getClosestOpenLocation(spawnLocation, heli).toDCoordinate();
            heli.setRotation(kb.spawnLocation.rotation);
            heli.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(heli);
            
        }
    }
    
}

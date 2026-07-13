package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TankUnit;
import java.util.ArrayList;


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
        ArrayList<RTSUnit> tanks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tanks.add(new TankUnit(spawn.topLeft.x, spawn.topLeft.y, team));
        }
        placeInRow(tanks, spawn, targetLocation, commandGroup, 40);
    }
    
}

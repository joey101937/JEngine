package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.Hellicopter;
import java.util.ArrayList;


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
    public void onTrigger(Coordinate targetLocation, int team, String commandGroup) {
        ReinforcementPoint rp = ReinforcementPoint.getClosest(targetLocation, team);
        SpawnLocation spawn = rp.getSpawnLocation();
        ArrayList<RTSUnit> helis = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            helis.add(new Hellicopter(spawn.topLeft.x, spawn.topLeft.y, team));
        }
        placeInRow(helis, spawn, targetLocation, commandGroup, 60);
    }
    
}

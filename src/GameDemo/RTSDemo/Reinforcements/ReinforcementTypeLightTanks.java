package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.LightTank;
import java.util.ArrayList;

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
        ArrayList<RTSUnit> tanks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tanks.add(new LightTank(spawn.topLeft.x, spawn.topLeft.y, team));
        }
        placeInRow(tanks, spawn, targetLocation, commandGroup, 35);
    }
}

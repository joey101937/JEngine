package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.SpawnLocation;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Rifleman;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class ReinforcementTypeInfantry extends ReinforcementType {
    private static final long serialVersionUID = 1L;

    public ReinforcementTypeInfantry() {
        this.name = "Infantry";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Core boots on the ground");
        contents.put(Rifleman.class, 16);
        contents.put(Bazookaman.class, 4);
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

        // Four squads of four riflemen + one bazookaman, added squad-by-squad.
        int squads = 4;
        ArrayList<RTSUnit> soldiers = new ArrayList<>();
        for (int s = 0; s < squads; s++) {
            for (int r = 0; r < 4; r++) {
                soldiers.add(new Rifleman(spawn.topLeft.x, spawn.topLeft.y, team));
            }
            soldiers.add(new Bazookaman(spawn.topLeft.x, spawn.topLeft.y, team));
        }

        // Each squad is a compact 2x2 of riflemen with the bazookaman centered
        // behind; the squads are strung out in a line across the spawn front.
        int foot = footprintOf(soldiers);
        int intra = foot + 12;               // spacing between soldiers within a squad
        int squadPitch = intra + foot + 70;  // spacing between adjacent squad centers
        int lineStart = -(squads * squadPitch) / 2 + squadPitch / 2;

        // Squad-local offsets (squad centered on its own x=0), in the same order the
        // soldiers were added above: four riflemen then the bazookaman.
        Coordinate[] squadLocal = {
            new Coordinate(-intra / 2, 0),
            new Coordinate(intra / 2, 0),
            new Coordinate(-intra / 2, intra),
            new Coordinate(intra / 2, intra),
            new Coordinate(0, 2 * intra),
        };

        ArrayList<Coordinate> offsets = new ArrayList<>();
        for (int s = 0; s < squads; s++) {
            int squadCenterX = lineStart + s * squadPitch;
            for (Coordinate local : squadLocal) {
                offsets.add(new Coordinate(squadCenterX + local.x, LOCAL_FORWARD + local.y));
            }
        }

        placeAtOffsets(soldiers, offsets, spawn, targetLocation, commandGroup);
    }

}

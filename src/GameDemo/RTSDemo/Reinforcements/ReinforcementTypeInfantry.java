package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSInput;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Rifleman;
import java.util.HashMap;
import java.util.List;

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
    public void onTrigger(Coordinate targetLocation, int team) {
        KeyBuilding kb = KeyBuilding.getClosest(targetLocation, team);
        Coordinate base = kb.spawnLocation.topLeft;
        String commandGroup = RTSInput.generateRandomCommandGroup();
        int initialOffset = -360;
        for (int i = 0; i < 4; i++) {
            Coordinate spawnOffset = new Coordinate(initialOffset + (i * 150), 50);
            spawnOffset.adjustForRotation(kb.spawnLocation.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            int padding = 40;
            Rifleman r1 = new Rifleman(spawnLocation.x, spawnLocation.y, team);
            Rifleman r2 = new Rifleman(spawnLocation.x + padding, spawnLocation.y, team);
            Rifleman r3 = new Rifleman(spawnLocation.x, spawnLocation.y + padding, team);
            Rifleman r4 = new Rifleman(spawnLocation.x + padding, spawnLocation.y + padding, team);
            Bazookaman b = new Bazookaman(spawnLocation.x + padding*2, spawnLocation.y, team);

            List<RTSUnit> created = List.of(r1, r2, r3, r4, b);
            HashMap<RTSUnit, Coordinate> innerOffsets = new HashMap<>();
            innerOffsets.put(r1, new Coordinate(0,0));
            innerOffsets.put(r2, new Coordinate(padding,0));
            innerOffsets.put(r3, new Coordinate(0,padding));
            innerOffsets.put(r4, new Coordinate(padding,padding));
            innerOffsets.put(b, new Coordinate(padding*2,0));


            for (RTSUnit u : created) {
                u.setLocation(ReinforcementHandler.getClosestOpenLocation(u.getLocation().toCoordinate(), u).toDCoordinate());
                u.setRotation(kb.spawnLocation.rotation);
                u.setCommandGroup(commandGroup);
                u.setDesiredLocation(targetLocation.copy().add(spawnOffset).add(innerOffsets.get(u)));
                RTSGame.game.addObject(u);
            }

        }
    }

}

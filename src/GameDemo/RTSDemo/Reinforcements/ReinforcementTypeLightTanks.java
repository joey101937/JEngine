package GameDemo.RTSDemo.Reinforcements;

import Framework.Coordinate;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.LightTank;

public class ReinforcementTypeLightTanks extends ReinforcementType {

    public ReinforcementTypeLightTanks() {
        this.name = "Light Tanks";
        this.icon = RTSAssetManager.layMineButton;
        this.hoverIcon = RTSAssetManager.layMineButtonHover;
        infoLines.add("Maneuverable tanks");
        infoLines.add("Can lay landmines. Medium-Fast Speed.");
        contents.put(LightTank.class, 5);
    }

    @Override
    public void onTrigger(Coordinate targetLocation, int team) {
        KeyBuilding kb = KeyBuilding.getClosest(targetLocation, team);
        Coordinate base = kb.spawnLocation.topLeft;
        for (int i = 0; i < 6; i++) {
            Coordinate spawnOffset = new Coordinate(i * 110, 50);
            spawnOffset.adjustForRotation(kb.spawnLocation.rotation);
            Coordinate spawnLocation = base.copy().add(spawnOffset);
            LightTank tank = new LightTank(spawnLocation.x, spawnLocation.y, team);
            tank.location = ReinforcementHandler.getClosestOpenLocation(spawnLocation, tank).toDCoordinate();
            tank.setRotation(kb.spawnLocation.rotation);
            tank.setDesiredLocation(targetLocation.copy().add(spawnOffset));
            RTSGame.game.addObject(tank);

        }
    }
}

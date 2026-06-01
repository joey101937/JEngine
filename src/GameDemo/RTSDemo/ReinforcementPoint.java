package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Window;

public interface ReinforcementPoint {
    int getOwningTeam();
    double getCaptureRadius();
    SpawnLocation getSpawnLocation();
    
    default boolean isActive () { return true; };
    default boolean isCapturable() { return true; }

    static ReinforcementPoint getClosest(Coordinate target, int team) {
        ReinforcementPoint closest = null;
        double closestDist = Double.MAX_VALUE;
        for (GameObject2 go : Window.currentGame.getAllObjects()) {
            if (go instanceof ReinforcementPoint rp && rp.getOwningTeam() == team && rp.isActive()) {
                double dist = go.distanceFrom(target);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = rp;
                }
            }
        }
        return closest;
    }

    static boolean anyOwnedBy(int team) {
        for (GameObject2 go : Window.currentGame.getAllObjects()) {
            if (go instanceof ReinforcementPoint rp && rp.getOwningTeam() == team && rp.isActive()) {
                return true;
            }
        }
        return false;
    }
}

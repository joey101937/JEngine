package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import GameDemo.RTSDemo.RTSUnit;
import java.util.concurrent.ConcurrentHashMap;

public class UnitPositionCache {
    private static ConcurrentHashMap<Long, Coordinate> cachedPositions = new ConcurrentHashMap<>();
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100; // 100ms = 1/10th of a second

    public static void updateCache(Game game) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        cachedPositions.clear();
        for (GameObject2 go : game.getAllObjects()) {
            if (go instanceof RTSUnit unit) {
                cachedPositions.put(unit.ID, unit.getPixelLocation());
            }
        }
        lastUpdateTime = currentTime;
    }

    public static Coordinate getCachedPosition(RTSUnit unit) {
        return cachedPositions.getOrDefault(unit.ID, unit.getPixelLocation());
    }
}

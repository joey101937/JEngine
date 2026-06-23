package GameDemo.RTSDemo.SceneryObjects;

import Framework.Coordinate;
import Framework.CoreLoop.QuadTree;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.WeakHashMap;

public interface SceneryObject {

    int QUERY_RADIUS = 700;

    // Scenery objects are large and stationary. The engine's center-point quadtree
    // misses them when a unit's center is more than collisionCheckRadius away from
    // the scenery's center, even if the bodies overlap. This registry supplements
    // collision by spatially indexing scenery per-game and querying within QUERY_RADIUS.
    class Registry {
        static final WeakHashMap<Game, QuadTree> quadTrees = new WeakHashMap<>();
    }

    static void register(SceneryObject s, Game g) {
        if (!(s instanceof GameObject2 go)) return;
        int r = Main.collisionCheckRadius;
        if (r > 0 && Math.max(go.getWidth(), go.getHeight()) /2 <= r) return;
        Registry.quadTrees
            .computeIfAbsent(g, game -> new QuadTree(0, new Rectangle(-2000, -2000, 24000, 24000)))
            .insert(go);
    }

    static ArrayList<GameObject2> getAll(Game g, Coordinate center) {
        QuadTree qt = Registry.quadTrees.get(g);
        if (qt == null) return new ArrayList<>(0);
        return qt.retrieve(center, QUERY_RADIUS);
    }

    /**
     * Drops a retired game's scenery index. The stored QuadTree holds scenery whose
     * hostGame points back to the game key, so the WeakHashMap can never evict the
     * entry on its own - it must be removed explicitly when retiring the game.
     */
    static void clearForGame(Game g) {
        Registry.quadTrees.remove(g);
    }

    default int getPathingPadding() {
        return 50;
    }
}

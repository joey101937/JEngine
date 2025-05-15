package Framework.CoreLoop;

import Framework.GameObject2;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class Snapshot {
    public final ArrayList<GameObject2> gameObjects;
    public final QuadTree quadTree;
    public final HashMap<String, GameObject2> objectMap = new HashMap<>();
    public final long tickNumber;
    public int largestSideLength;
    
    public Snapshot(ArrayList<GameObject2> gos, QuadTree tree, long tick){
        gameObjects = gos;
        quadTree = tree;
        tickNumber = tick;
        for(GameObject2 go: gos) {
            objectMap.put(go.ID, go);
        }
    }
}

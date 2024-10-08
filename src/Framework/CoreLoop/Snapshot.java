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
    public final HashMap<String, GameObject2> objectMap = new HashMap<>();
    public final long tickNumber;
    
    public Snapshot(ArrayList<GameObject2> gos, long tick){
        gameObjects = gos;
        tickNumber = tick;
        for(GameObject2 go: gos) {
            objectMap.put(go.ID, go);
        }
    }
}

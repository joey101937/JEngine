package GameDemo.RTSDemo.SceneryObjects;

import Framework.Game;
import Framework.GameObject2;
import java.util.ArrayList;

public interface SceneryObject {

    class Registry {
        static final ArrayList<SceneryObject> all = new ArrayList<>();
    }

    static void register(SceneryObject s) {
        Registry.all.add(s);
    }

    static ArrayList<GameObject2> getAll(Game g) {
        var out = new ArrayList<GameObject2>();
        for (SceneryObject s : Registry.all) {
            if (s instanceof GameObject2 go && go.getHostGame() == g) {
                out.add(go);
            }
        }
        return out;
    }

    default int getPathingPadding() {
        return 50;
    }
}

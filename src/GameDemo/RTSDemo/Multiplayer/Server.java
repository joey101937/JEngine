package GameDemo.RTSDemo.Multiplayer;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import GameDemo.RTSDemo.MapEditor.MapData;
import GameDemo.RTSDemo.MapEditor.MapLoader;
import GameDemo.RTSDemo.MapEditor.MapSerializer;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import static GameDemo.RTSDemo.RTSGame.game;
import static GameDemo.RTSDemo.RTSGame.setupUI;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.TextChatEffect;
import GameDemo.RTSDemo.Units.Apache;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import GameDemo.RTSDemo.Units.Truck;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author guydu
 */
public class Server {

    /** Shared map both players load in multiplayer, relative to the TankGame asset folder. */
    public static final String MP_MAP = "DemoAssets/TankGame/customMaps/mpmap.json";

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        TextChatEffect.localChatAlias = "Server";
        MapData mapData = loadMpMapData();
        RTSGame.game = new Game(loadMapBackground(mapData));
        RTSGame.minimap = new Minimap(game, new Coordinate(0, 0));
        Game g = RTSGame.game;
        Window.currentGame = g;
        ExternalCommunicator.initialize(true);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        game.setLoadingScreenActive(true);
        Window.initialize(g);
        RTSGame.setup(g);
        MapLoader.loadIntoGame(mapData, g);
        positionCameraForLocalTeam(g);
        RTSGame.game = g;
        game.setOnGameStabilized(x3 -> {
            setupUI(game);
            game.setLoadingScreenActive(false);
            System.out.println("Loading screen off");
            ExternalCommunicator.setAndCommunicateMultiplayerReady();
        });
    }

    /** Reads the shared multiplayer map so both players start from identical world state. */
    public static MapData loadMpMapData() {
        try {
            return MapSerializer.load(new File(Main.getAssets() + MP_MAP));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load multiplayer map " + MP_MAP, e);
        }
    }

    /** Resolves the map's background image, falling back to the grass background if missing. */
    public static BufferedImage loadMapBackground(MapData mapData) {
        BufferedImage bg = MapLoader.loadBackground(mapData.background);
        return bg != null ? bg : RTSAssetManager.grassBG;
    }

    /** Places the camera over the local player's side of the map. */
    public static void positionCameraForLocalTeam(Game g) {
        if (ExternalCommunicator.localTeam == 1) {
            g.getCamera().location.y = -g.getWorldHeight() - 2650;
        }
    }

    /**
     * Programmatic two-team unit layout retained for the determinism test harness,
     * whose recorded command replays are keyed to these exact unit IDs. Live
     * multiplayer instead loads the shared {@link #MP_MAP}.
     */
    public static void createStartingUnits(Game g) {
        int lineSize = 40;// 40; // lowering to 40 so that i can better debug
        int spacer = 160;
        for (int i = 0; i < lineSize; i++) {
            g.addObject(new Apache(200 + (i * spacer), 200, 0));
        }
        for (int i = 0; i < lineSize; i++) {
            if (i % 2 == 0) {
                g.addObject(new Bazookaman(200 + (i * spacer), 350, 0));
            } else {
                g.addObject(new Rifleman(200 + (i * spacer), 350, 0));
            }
        }
        for (int i = 0; i < lineSize; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 500, 0));
        }
        for (int i = 0; i < lineSize; i++) {
            g.addObject(new LightTank(200 + (i * spacer), 650, 0));
        }
        for (int i = 0; i < 4; i++) {
            g.addObject(new Truck(200 + (i * spacer), 800, 0));
        }

        for (GameObject2 go : g.getAllObjects()) {
            if (go instanceof RTSUnit unit) {
                if (unit.team == 0) {
                    unit.setRotation(180);
                }
            }
        }

        if (ExternalCommunicator.localTeam == 1) {
            g.getCamera().location.y = -g.getWorldHeight() - 2650;
        }

        // red side
        for (int i = 0; i < lineSize; i++) {
            if (i % 2 == 0) {
                g.addObject(new Rifleman(200 + (i * spacer), g.getWorldHeight() - 1650, 1));
            } else {
                g.addObject(new Bazookaman(200 + (i * spacer), g.getWorldHeight() - 1650, 1));
            }
        }
        for (int i = 0; i < lineSize; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), g.getWorldHeight() - 1500, 1));
        }
        for (int i = 0; i < lineSize; i++) {
            g.addObject(new LightTank(200 + (i * spacer), g.getWorldHeight() - 1350, 1));
        }
        for (int i = 0; i < lineSize; i++) {
            g.addObject(new Apache(200 + (i * spacer), g.getWorldHeight() - 1200, 1));
        }
        for (int i = 0; i < 4; i++) {
            g.addObject(new Truck(200 + (i * spacer), g.getWorldHeight() - 1050, 1));
        }
    }
}

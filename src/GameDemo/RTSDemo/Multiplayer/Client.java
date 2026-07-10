package GameDemo.RTSDemo.Multiplayer;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import static GameDemo.RTSDemo.Multiplayer.Server.loadMapBackground;
import static GameDemo.RTSDemo.Multiplayer.Server.loadMpMapData;
import static GameDemo.RTSDemo.Multiplayer.Server.positionCameraForLocalTeam;
import GameDemo.RTSDemo.MapEditor.MapData;
import GameDemo.RTSDemo.MapEditor.MapLoader;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import static GameDemo.RTSDemo.RTSGame.game;
import static GameDemo.RTSDemo.RTSGame.setupUI;
import GameDemo.RTSDemo.TextChatEffect;
import java.io.PrintStream;

/**
 *
 * @author guydu
 */
public class Client {

    public static PrintStream printStream;

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        TextChatEffect.localChatAlias = "Client";
        MapData mapData = loadMpMapData();
        GameDemo.RTSDemo.Replay.ReplayManager.setCurrentMap(mapData, "mpmap");
        RTSGame.game = new Game(loadMapBackground(mapData));
        RTSGame.minimap = new Minimap(game, new Coordinate(0, 0));
        Game g = RTSGame.game;
        Window.currentGame = g;
        ExternalCommunicator.initialize(false);
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

}

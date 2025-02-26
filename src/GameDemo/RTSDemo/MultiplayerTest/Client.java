package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import static GameDemo.RTSDemo.MultiplayerTest.Server.createStartingUnits;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import static GameDemo.RTSDemo.RTSGame.game;
import java.io.PrintStream;

/**
 *
 * @author guydu
 */
public class Client {

    public static PrintStream printStream;

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        RTSGame.game = new Game(RTSAssetManager.grassBGDark);
        RTSGame.minimap = new Minimap(game, new Coordinate(0, 0));
        Client c = new Client();
        Game g = RTSGame.game;
        Window.currentGame = g;
        ExternalCommunicator.initialize(false);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        createStartingUnits(g);
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }

}

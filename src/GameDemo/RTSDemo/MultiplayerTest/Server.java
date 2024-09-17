package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class Server{

    public static void main(String[] args) {
        Server server = new Server();
        Game g = new Game(SpriteManager.grassBG);
        System.out.println("adding");
        g.addObject(new Hellicopter(200, 200, 0));
        g.addObject(new Hellicopter(300, 200, 0));
        g.addObject(new Hellicopter(400, 200, 0));
        g.addObject(new Hellicopter(200, 1000, 1));
        g.addObject(new Hellicopter(300, 1000, 1));
        g.addObject(new Hellicopter(400, 1000, 1));
        ExternalCommunicator.initialize(true);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }
}

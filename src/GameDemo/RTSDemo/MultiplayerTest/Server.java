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
public class Server {

    public static void main(String[] args) {
        Game g = RTSGame.game;
        System.out.println("adding");
        int spacer = 160;
        for (int i = 0; i < 20; i++) {
            g.addObject(new Hellicopter(200 + (i * spacer), 200, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(350 + (i * spacer), 200, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new Hellicopter(200 + (i * spacer), 1000, 1));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 850, 1));
        }
        ExternalCommunicator.initialize(true);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }
}

package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Coordinate;
import Framework.Game;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class Server {

    public static void main(String[] args) {
        Game g = RTSGame.game;
        System.out.println("adding");
        createStartingUnits(g);
        ExternalCommunicator.initialize(true);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }

    public static void createStartingUnits(Game g) {
        int spacer = 160;
        for (int i = 0; i < 20; i++) {
            g.addObject(new Hellicopter(200 + (i * spacer), 200, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 350, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 500, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new Rifleman(200 + (i * spacer), 650, 0));
        }

        if(ExternalCommunicator.localTeam == 1) {
            g.getCamera().centerOn(new Coordinate(200, 2000));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new Hellicopter(200 + (i * spacer), 3000, 1));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 2850, 1));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 2700, 1));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new Rifleman(200 + (i * spacer), 2550, 1));
        }
    }
}

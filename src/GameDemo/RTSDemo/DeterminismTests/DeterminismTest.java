
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 * these determinism tests are one off battles intended to be run across multiple sessions so that the results can be compared
 * @author guydu
 */
public class DeterminismTest {

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        Game game = new Game(RTSAssetManager.grassBG);
        Main.tickThreadCount = 1;
        Main.setRandomSeed(10);
        Window.initialize(game);
        game.getCamera().camSpeed = 20;
        RTSGame.setup(game);
        game.setOnGameStabilized((x) -> {
            RTSGame.setupUI(game);
        });
        Main.splitBackgroundRender = true;

        int spacer = 160;

//        for (int i = 0; i < 20; i++) {
//            Hellicopter heli = new Hellicopter(100 + (i * spacer), 100, 0);
//            heli.setRotation(180);
//            game.addObject(heli);
//        }
        for (int i = 0; i < 20; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 750, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
       

       
        for (int i = 0; i < 20; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1400, 1));
        }
        
        Main.wait(1000);
        while(game.getGameTickNumber() < 10) {
            Main.wait(50);
        }
        for(GameObject2 go : game.getAllObjects()) {
            if(go instanceof RTSUnit unit) {
                unit.setDesiredLocation(new Coordinate(1000, 1000));
            }
        }
    }
}

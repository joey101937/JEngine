
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class DeterminismTest3 {
    public static void main(String[] args) {
        Game game = new Game(RTSAssetManager.grassBG);
        Main.setRandomSeed(10);
        Window.currentGame = game;
        
        int spacer = 160;

        for (int i = 0; i < 20; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 750, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
       
        for (int i = 0; i < 20; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1300, 1));
        }
        
        game.tick();
        for(GameObject2 go : game.getAllObjects()) {
            if(go instanceof RTSUnit unit) {
                unit.setDesiredLocation(new Coordinate(1000, 900));
            }
        }
        
        System.out.println("starting");

        boolean done = false;
        while(!done) {
            game.tick(); 
            boolean greenAlive = false;
            boolean redAlive = false;
            for(GameObject2 go : game.getAllObjects()) {
                if(go instanceof RTSUnit unit) {
                    if(unit.team == 0) greenAlive = true;
                    if(unit.team ==1 ) redAlive  = true;
                }
            }
            done = !greenAlive || !redAlive;
        }
        Window.initialize(game);
    }
}

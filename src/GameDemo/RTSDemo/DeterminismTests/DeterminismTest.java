
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSInput;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SelectionBoxEffect;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author guydu
 */
public class DeterminismTest {

    public static Game game = new Game(RTSAssetManager.grassBG);

    public static void main(String[] args) {
        Main.tickThreadCount = 1;
        Main.setRandomSeed(10);
        Window.initialize(game);
        game.setInputHandler(new RTSInput(null));
        game.getCamera().camSpeed = 20;
        game.addIndependentEffect(new SelectionBoxEffect());
//        Window.addUIElement(minimap);
//        Window.addUIElement(button);
//        minimap.setSimpleRenderHelper(new SimpleRenderHelperRTS());
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

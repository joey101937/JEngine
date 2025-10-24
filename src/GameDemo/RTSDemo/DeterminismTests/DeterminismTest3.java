package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.Hellicopter;
import java.util.List;

/**
 *
 * @author guydu
 */
public class DeterminismTest3 {
    
    private static String unitToString (RTSUnit unit) {
        return "" + unit.getLocation() + "" + unit.getRotation() + "" + unit.currentHealth;
    } 
    
    public static List<String> run (boolean show) {
        Game game = new Game(RTSAssetManager.grassBG);
        Main.setRandomSeed(10);
        Window.currentGame = game;
        RTSGame.setup(game);

        int spacer = 160;

        for (int i = 0; i < 20; i++) {
            RTSUnit unit = new Hellicopter(100 + (i * spacer), 750, 0);
            unit.setRotation(180);
            game.addObject(unit);
        }

        for (int i = 0; i < 20; i++) {
            game.addObject(new Hellicopter(100 + (i * spacer), 1300, 1));
        }

        game.tick();
        for (GameObject2 go : game.getAllObjects()) {
            if (go instanceof RTSUnit unit) {
                unit.setDesiredLocation(new Coordinate(1000, 900));
            }
        }

        System.out.println("starting");

        boolean done = false;
        while (!done) {
            game.tick();
            for (GameObject2 go : game.getAllObjects()) {
                if (go instanceof RTSUnit unit) {
                    unit.setDesiredLocation(new Coordinate(1000, 900));
                }
            }
            boolean greenAlive = false;
            boolean redAlive = false;
            for (GameObject2 go : game.getAllObjects()) {
                if (go instanceof RTSUnit unit) {
                    if (unit.team == 0) {
                        greenAlive = true;
                    }
                    if (unit.team == 1) {
                        redAlive = true;
                    }
                }
            }
            done = !greenAlive || !redAlive;
        }
        if(show) Window.initialize(game);
        return game.getAllObjects().stream().filter(x -> x instanceof RTSUnit).map(x -> unitToString((RTSUnit)x)).toList();
    }

    public static void main(String[] args) {
       RTSAssetManager.initialize();
       var res = run(true);
       System.out.println("res " + res);
    }
}

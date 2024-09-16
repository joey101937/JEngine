/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author Joseph
 */
public class RTSGame {

    public static Game game = new Game(SpriteManager.grassBG);
    public static Minimap minimap = new Minimap(game, new Coordinate(0, 0));
    public static MinimapButton button = new MinimapButton(game, new Coordinate(0, 0));

    public static void main(String[] args) {
        Main.tickType = Handler.TickType.modular;
        Main.tickThreadCount = 1;
        Window.initialize(game);
        game.setInputHandler(new RTSInput());
        game.getCamera().camSpeed = 20;
        game.addIndependentEffect(new SelectionBoxEffect());
        Window.addUIElement(minimap);
        Window.addUIElement(button);
        minimap.setSimpleRenderHelper(new SimpleRenderHelperRTS());
        minimap.setMinimapMouseListener(new MinimapListener(game, minimap));
        Main.splitBackgroundRender = true;
        minimap.setLocation(0, game.getWindowHeight() - minimap.getHeight() - 38);
        button.setLocation(0, game.getWindowHeight() - minimap.getHeight() - 38 - button.getHeight());
        Window.setFullscreen(true);
        Main.ignoreSubobjectCollision = true; // better performance
        
        int spacer = 160;

        for (int i = 0; i < 20; i++) {
            Hellicopter heli = new Hellicopter(100 + (i * spacer), 100, 0);
            heli.setRotation(180);
            game.addObject(heli);
        }
        for (int i = 0; i < 20; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 300, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < 20; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 450, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < 20; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 600, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < 20; i++) {
            Rifleman rifleman = new Rifleman(100 + (i * spacer), 750, 0);
            rifleman.setRotation(180);
            game.addObject(rifleman);
        }

        for (int i = 0; i < 20; i++) {
            game.addObject(new Rifleman(100 + (i * spacer), 1450, 1));
        }
        for (int i = 0; i < 20; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1550, 1));
        }
        for (int i = 0; i < 20; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1700, 1));
        }
        for (int i = 0; i < 20; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1850, 1));
        }
        for (int i = 0; i < 20; i++) {
            game.addObject(new Hellicopter(100 + (i * spacer), 2000, 1));
        }
    }
}

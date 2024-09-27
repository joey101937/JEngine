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
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author Joseph
 */
public class RTSGame {

    public static Game game = new Game(SpriteManager.grassBG);
    public static Minimap minimap = new Minimap(game, new Coordinate(0, 0));
    public static InfoPanel infoPanel;

    public static void setup(Game g) {
        // preload static asset
        var x = Bazookaman.baseSprite;
        var y = Rifleman.baseSprite;
        
        Main.tickType = Handler.TickType.modular;
        Main.tickThreadCount = 1;
        Main.ticksPerSecond = 60;
        g.setInputHandler(new RTSInput());
        g.getCamera().camSpeed = 20;
        g.addIndependentEffect(new SelectionBoxEffect());
        g.addIndependentEffect(new SelectionBoxEffectAir());
        Window.addUIElement(minimap);
        minimap.setSimpleRenderHelper(new SimpleRenderHelperRTS());
        minimap.setMinimapMouseListener(new MinimapListener(g, minimap));
        Main.splitBackgroundRender = true;
        minimap.setLocation(0, g.getWindowHeight() - minimap.getHeight());
        // minimap.setLocation(g.getWindowWidth() - minimap.getWidth(), 0);
        // infoPanel = new InfoPanel(g, g.getWindowWidth() - minimap.getWidth(), minimap.getHeight(), minimap.getWidth());
        infoPanel = new InfoPanel(g, minimap.getWidth(), g.getWindowHeight() -200, 700);
        Window.addUIElement(infoPanel);
        Main.ignoreSubobjectCollision = true; // better performance
        Main.ignoreCollisionsForStillObjects = true; // better performance
    }

    public static void main(String[] args) {
        Window.initializeFullScreen(game);
        setup(game);

        int spacer = 160;
        
        
//        game.addObject(new LightTank(500, 500, 0));
//        game.addObject(new LightTank(1050, 500, 1));
//        
//        game.addObject(new TankUnit(500, 660, 0));
//        game.addObject(new LightTank(1050, 660, 1));
        for (int i = 0; i < 40; i++) {
            Hellicopter heli = new Hellicopter(100 + (i * spacer), 100, 0);
            heli.setRotation(180);
            game.addObject(heli);
        }
        for (int i = 0; i < 40; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 300, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < 40; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 450, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < 40; i++) {
            LightTank tank = new LightTank(100 + (i * spacer), 600, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < 40; i++) {
            RTSUnit infantryUnit = i % 2 == 0 ? new Bazookaman(100 + (i * spacer), 750, 0) : new Rifleman(100 + (i * spacer), 750, 0);
            infantryUnit.setRotation(180);
            game.addObject(infantryUnit);
        }

        for (int i = 0; i < 40; i++) {
            if(i % 2 == 0 ){ 
                game.addObject(new Bazookaman(100 + (i * spacer), 1450, 1));
            } else {
                game.addObject(new Rifleman(100 + (i * spacer), 1450, 1));
            }
        }
        for (int i = 0; i < 40; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1550, 1));
        }
        for (int i = 0; i < 40; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1700, 1));
        }
        for (int i = 0; i < 40; i++) {
            game.addObject(new LightTank(100 + (i * spacer), 1850, 1));
        }
        for (int i = 0; i < 40; i++) {
            game.addObject(new Hellicopter(100 + (i * spacer), 2000, 1));
        }


    }
}

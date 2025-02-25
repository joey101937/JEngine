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
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import GameDemo.RTSDemo.Pathfinding.NavigationManager;
import GameDemo.RTSDemo.Reinforcements.ReinforcementHandler;
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

    public static Game game = null;
    public static Minimap minimap = null;
    public static InfoPanelEffect infoPanelEffect;
    public static ReinforcementHandler reinforcementHandler;
    public static NavigationManager tileManager;

    public static void setup(Game g) {
        Main.ignoreSubobjectCollision = true; // better performance
        Main.ignoreCollisionsForStillObjects = true; // better performance
        Main.tickType = Handler.TickType.modular;
        Main.tickThreadCount = 1;
        Main.ticksPerSecond = 90;
        g.getCamera().camSpeed = 20;
        g.addIndependentEffect(new SelectionBoxEffect());
        g.addIndependentEffect(new SelectionBoxEffectAir());
        g.addIndependentEffect(new StatusIconHelper());
        minimap.setSimpleRenderHelper(new SimpleRenderHelperRTS());
        minimap.setMinimapMouseListener(new MinimapListener(g, minimap));
        Main.splitBackgroundRender = true;
        minimap.setLocation(0, g.getWindowHeight() - minimap.getHeight());
        infoPanelEffect = new InfoPanelEffect(g, minimap.getWidth(), g.getWindowHeight() - 200, 700, 200);
        g.setInputHandler(new RTSInput(infoPanelEffect));
        Window.addUIElement(minimap);
        g.addIndependentEffect(infoPanelEffect);
        g.addIndependentEffect(new TooltipHelper());
        g.addIndependentEffect(new KeyBuildingRingEffect());
        g.addIndependentEffect(new FogOfWarEffect());
        reinforcementHandler = new ReinforcementHandler(new Coordinate(0, game.getWindowHeight() - minimap.getHeight() - 30), 10);
        g.addIndependentEffect(reinforcementHandler);
        tileManager = new NavigationManager(g);
        g.addIndependentEffect(tileManager);
    }

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        game = new Game(RTSAssetManager.grassBGDark);
        minimap = new Minimap(game, new Coordinate(0, 0));
        
        Window.initializeFullScreen(game);
        setup(game);

        int spacer = 160;
        int lineLength = 40;
        
        
        game.addObject(new KeyBuilding(10000, 400, 0, 0, 400, 180));
        game.addObject(new KeyBuilding(3000, 3000, 1));
        
        // greens 
        for (int i = 0; i < lineLength; i++) {
            Hellicopter heli = new Hellicopter(100 + (i * spacer), 100, 0);
            heli.setRotation(180);
            game.addObject(heli);
        }
        for (int i = 0; i < lineLength; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 300, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < lineLength; i++) {
            TankUnit tank = new TankUnit(100 + (i * spacer), 450, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < lineLength; i++) {
            LightTank tank = new LightTank(100 + (i * spacer), 600, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        for (int i = 0; i < lineLength; i++) {
            RTSUnit infantryUnit = i % 2 == 0 ? new Bazookaman(100 + (i * spacer), 750, 0) : new Rifleman(100 + (i * spacer), 750, 0);
            infantryUnit.setRotation(180);
            game.addObject(infantryUnit);
        }
        
        // reds

        for (int i = 0; i < lineLength; i++) {
            if(i % 2 == 0 ){ 
                game.addObject(new Bazookaman(100 + (i * spacer), 1750, 1));
            } else {
                game.addObject(new Rifleman(100 + (i * spacer), 1750, 1));
            }
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 1850, 1));
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new TankUnit(100 + (i * spacer), 2000, 1));
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new LightTank(100 + (i * spacer), 2150, 1));
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new Hellicopter(100 + (i * spacer), 2300, 1));
        } 
    }
}

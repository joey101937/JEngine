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
import Framework.PathingLayer;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import GameDemo.RTSDemo.Pathfinding.NavigationManager;
import GameDemo.RTSDemo.Pathfinding.TerrainTileMap;
import GameDemo.RTSDemo.Reinforcements.ReinforcementHandler;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import java.awt.Color;

/**
 *
 * @author Joseph
 */
public class RTSGame {

    public static Game game = null;
    public static Minimap minimap = null;
    public static InfoPanelEffect infoPanelEffect;
    public static ReinforcementHandler reinforcementHandler;
    public static NavigationManager navigationManager;

    public static void setup(Game g) {
        PathingLayer pathing = new PathingLayer(RTSAssetManager.rtsPathing);
        pathing.assignColor(new Color(0,255,255), TerrainTileMap.paddingType);
        g.setPathingLayer(pathing);
        Main.ignoreSubobjectCollision = false; // better performance
        Main.ignoreCollisionsForStillObjects = true; // better performance
        Main.ignoreCollisionsOnRotation = true;
        Main.collisionCheckRadius = 100;
        Main.onScreenPadding = 400;
        Main.tickType = Handler.TickType.modular;
        Main.tickThreadCount = 1;
        Main.ticksPerSecond = 90;
        Main.enableLerping = true;
        g.getCamera().camSpeed = 20;
        g.addIndependentEffect(new SelectionBoxEffect());
        g.addIndependentEffect(new SelectionBoxEffectAir());
        g.addIndependentEffect(new StatusIconHelper());
        Main.splitBackgroundRender = true;
        g.addIndependentEffect(new KeyBuildingRingEffect());
        g.addIndependentEffect(new FogOfWarEffect());
        navigationManager = new NavigationManager(g);
        g.addIndependentEffect(navigationManager);
        g.addIndependentEffect(RTSSoundManager.get());
        TerrainTileMap.loadAll();
    }

    public static void setupUI(Game g) {
        minimap = new Minimap(game, new Coordinate(0, 0));
        minimap.setSimpleRenderHelper(new MinimapRenderHelperRTS());
        minimap.setMinimapMouseListener(new MinimapListener(g, minimap));
        minimap.setLocation(0, g.getWindowHeight() - minimap.getHeight());
        Window.addUIElement(minimap);

        infoPanelEffect = new InfoPanelEffect(g, minimap.getWidth(), g.getWindowHeight() - 200, 700, 200);
        g.setInputHandler(new RTSInput(infoPanelEffect));
        g.addIndependentEffect(infoPanelEffect);

        g.addIndependentEffect(new TooltipHelper());

        reinforcementHandler = new ReinforcementHandler(new Coordinate(0, game.getWindowHeight() - minimap.getHeight() - 30), 10);
        g.addIndependentEffect(reinforcementHandler);
    }

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        game = new Game(RTSAssetManager.grassBGDark);
        game.setLoadScreenRender(g -> {
            g.setColor(new Color(100, 100, 100));
            Coordinate camLocation = game.getCamera().getWorldLocation().toCoordinate();
            Coordinate camCenter = game.getCamera().getCenterPoint();
            var fov = game.getCamera().getFieldOfView();
            g.fillRect(camLocation.x, camLocation.y, fov.width, fov.height);
            Coordinate imageRenderLocation = new Coordinate(
                    camCenter.x - RTSAssetManager.JEngineIconLoading.getWidth() / 2,
                    camCenter.y - RTSAssetManager.JEngineIconLoading.getHeight() / 2
            );
            g.drawImage(RTSAssetManager.JEngineIconLoading, imageRenderLocation.x, imageRenderLocation.y, null);
        });
        game.setLoadingScreenActive(true);

        Window.initializeFullScreen(game);

        setup(game);

        int spacer = 160;
        int lineLength = 40; // units = this x 10

        game.addObject(new KeyBuilding(10000, 400, 0, 0, 400, 180));
        game.addObject(new KeyBuilding(3000, 3000, 1));
//          
//        ArrayList<RTSUnit> hellis = new ArrayList<>();
//        
//         for (int i = 0; i < 4; i++) {
//            Hellicopter heli = new Hellicopter(100 + (i * spacer), 100, 0);
//            heli.setRotation(180);
//            game.addObject(heli);
//            hellis.add(heli);
//        }
//         String commandgroup = RTSInput.generateRandomCommandGroup();
//         for(RTSUnit u : hellis) {
//             game.addTickDelayedEffect(10, c -> {
//                u.commandGroup = commandgroup;
//                u.setDesiredLocation(new Coordinate(500,500));
//             });
//         }

//        RTSUnit infantryUnit = new Bazookaman(500, 750, 0);
//        infantryUnit.setRotation(180);
//        game.addObject(infantryUnit);
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

//        // reds
        for (int i = 0; i < lineLength; i++) {
            if (i % 2 == 0) {
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
        game.setOnGameStabilized(x3 -> {
            setupUI(game);
            game.setLoadingScreenActive(false);
            System.out.println("Loading screen off");
        });
    }
}

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
import GameDemo.RTSDemo.Commands.CommandHandler;
import GameDemo.RTSDemo.FogOfWar.FogOfWarEffect;
import GameDemo.RTSDemo.FogOfWar.FogOfWarGrid;
import GameDemo.RTSDemo.Pathfinding.NavigationManager;
import GameDemo.RTSDemo.Pathfinding.TerrainTileMap;
import GameDemo.RTSDemo.Reinforcements.ReinforcementHandler;
import GameDemo.RTSDemo.SceneryObjects.BuildingGreen1;
import GameDemo.RTSDemo.SceneryObjects.GreenShippingContainer;
import GameDemo.RTSDemo.SceneryObjects.Hangar;
import GameDemo.RTSDemo.SceneryObjects.OrangeWoodHouse;
import GameDemo.RTSDemo.SceneryObjects.PropaneTank;
import GameDemo.RTSDemo.Units.Apache;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;
import GameDemo.RTSDemo.Units.Truck;
import java.awt.Color;
import java.util.Set;

/**
 *
 * @author Joseph
 */
public class RTSGame {

    public static final Set<Integer> activeTeams = Set.of(-1, 0, 1, 2, 3, 4);

    public static Game game = null;
    public static Minimap minimap = null;
    public static InfoPanelEffect infoPanelEffect;
    public static ReinforcementHandler reinforcementHandler;
    public static NavigationManager navigationManager;
    public static CommandHandler commandHandler;
    public static TextChatEffect textChatEffect;
    public static GameMenuEffect gameMenuEffect;
    public static int desiredTPS = 90;
    public static FogOfWarGrid fogOfWarGrid;

    public static void setup(Game g) {
        PathingLayer pathing = new PathingLayer(RTSAssetManager.rtsPathing, "DemoAssets/TankGame/terrainPlaygroundPathing.png");
        pathing.assignColor(new Color(0,255,255), TerrainTileMap.paddingType);
        g.setPathingLayer(pathing);
        Main.ignoreSubobjectCollision = false; // better performance
        Main.ignoreCollisionsForStillObjects = true; // better performance
        Main.ignoreCollisionsOnRotation = true;
        Main.collisionCheckRadius = 100;
        Main.onScreenPadding = 400;
        Main.tickType = Handler.TickType.modular;
        Main.tickThreadCount = 1;
        Main.ticksPerSecond = desiredTPS;
        Main.enableLerping = true;
        g.getCamera().camSpeed = tickAdjust(20);
        g.addIndependentEffect(new SelectionBoxEffect(g));
        g.addIndependentEffect(new SelectionBoxEffectAir(g));
        g.addIndependentEffect(new StatusIconHelper(g));
        Main.splitBackgroundRender = true;
        g.addIndependentEffect(new KeyBuildingRingEffect(g));
        fogOfWarGrid = new FogOfWarGrid(g.getWorldWidth(), g.getWorldHeight());
        g.addIndependentEffect(new FogOfWarEffect(g));
        navigationManager = new NavigationManager(g);
        g.addIndependentEffect(navigationManager);
        commandHandler = new CommandHandler(g);
        g.addIndependentEffect(commandHandler);
        g.addIndependentEffect(RTSSoundManager.get());
        TerrainTileMap.loadAll();
    }

    public static void setupUI(Game g) {
        minimap = new Minimap(g, new Coordinate(0, 0));
        minimap.setSimpleRenderHelper(new MinimapRenderHelperRTS());
        minimap.setMinimapMouseListener(new MinimapListener(g, minimap));
        minimap.setLocation(0, g.getWindowHeight() - minimap.getHeight());
        Window.addUIElement(minimap);

        infoPanelEffect = new InfoPanelEffect(g, minimap.getWidth(), g.getWindowHeight() - 200, 700, 200);
        g.setInputHandler(new RTSInput(infoPanelEffect));
        g.addIndependentEffect(infoPanelEffect);
        g.addIndependentEffect(new TargetingModeManager());

        reinforcementHandler = new ReinforcementHandler(new Coordinate(0, g.getWindowHeight() - minimap.getHeight() - 30), 10);
        g.addIndependentEffect(reinforcementHandler);

        textChatEffect = new TextChatEffect(g);
        g.addIndependentEffect(textChatEffect);

        gameMenuEffect = new GameMenuEffect(g);
        g.addIndependentEffect(gameMenuEffect);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1");
        RTSAssetManager.initialize();
        game = new Game(RTSAssetManager.grassBG);
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

        game.addObject(new KeyBuilding(10000, 400, 0, 0, 400, 180));
        game.addObject(new KeyBuilding(3000, 2500, 1));
        
        game.addObject(new Hangar(10000, 2000, 100));
        game.addObject(new BuildingGreen1(10700, 2000, 0));
        
        game.addObject(new OrangeWoodHouse(10000, 2700, 100));
        game.addObject(new PropaneTank(10700, 2700, 0));

        
        spawnLines();
        spawnTeamShowcase();
//        spawnTestHeli();
       
        game.setOnGameStabilized(x3 -> {
            setupUI(game);
            game.setLoadingScreenActive(false);
            System.out.println("Loading screen off");
        });
    }
    
    private static void spawnTeamShowcase() {
        // team 2 (yellow)
        game.addObject(new TankUnit(500, 3500, 2));
        game.addObject(new LightTank(700, 3500, 2));
        game.addObject(new Truck(900, 3500, 2));
        game.addObject(new TransportHelicopter(1100, 3500, 2));
        game.addObject(new Hellicopter(1300, 3500, 2));
        game.addObject(new Apache(1500, 3500, 2));
        game.addObject(new Landmine(1700, 3500, 2));
        game.addObject(new Rifleman(1900, 3500, 2));
        game.addObject(new Bazookaman(2100, 3500, 2));
        // team 3 (tan)
        game.addObject(new TankUnit(500, 4200, 3));
        game.addObject(new LightTank(700, 4200, 3));
        game.addObject(new Truck(900, 4200, 3));
        game.addObject(new TransportHelicopter(1100, 4200, 3));
        game.addObject(new Hellicopter(1300, 4200, 3));
        game.addObject(new Apache(1500, 4200, 3));
        game.addObject(new Landmine(1700, 4200, 3));
        game.addObject(new Rifleman(1900, 4200, 3));
        game.addObject(new Bazookaman(2100, 4200, 3));
        // team 4 (arctic)
        game.addObject(new TankUnit(500, 4900, 4));
        game.addObject(new LightTank(700, 4900, 4));
        game.addObject(new Truck(900, 4900, 4));
        game.addObject(new TransportHelicopter(1100, 4900, 4));
        game.addObject(new Hellicopter(1300, 4900, 4));
        game.addObject(new Apache(1500, 4900, 4));
        game.addObject(new Landmine(1700, 4900, 4));
        game.addObject(new Rifleman(1900, 4900, 4));
        game.addObject(new Bazookaman(2100, 4900, 4));
        // team -1 (silver)
        game.addObject(new TankUnit(500, 5600, -1));
        game.addObject(new LightTank(700, 5600, -1));
        game.addObject(new Truck(900, 5600, -1));
        game.addObject(new TransportHelicopter(1100, 5600, -1));
        game.addObject(new Hellicopter(1300, 5600, -1));
        game.addObject(new Apache(1500, 5600, -1));
        game.addObject(new Landmine(1700, 5600, -1));
        game.addObject(new Rifleman(1900, 5600, -1));
        game.addObject(new Bazookaman(2100, 5600, -1));
    }

    private static void spawnTestHeli() {
        int spacer = 160;
        int lineLength = 6; // units = this x 10

         for (int i = 0; i < lineLength; i++) {
            Apache heli = new Apache(100 + (i * spacer), 100, 0);
            heli.setRotation(180);
            game.addObject(heli);
        }
         
                 game.addTickDelayedEffect(2000, (g) -> {
            game.getAllObjects().stream().forEach(x -> {
                if(x instanceof RTSUnit unit) {
                    unit.setDesiredLocation(new Coordinate(600,700));
                    unit.setCommandGroup("test");
                }
            });
        });
        
        game.addTickDelayedEffect(4000, (g) -> {
            game.getAllObjects().stream().forEach(x -> {
                if(x instanceof RTSUnit unit) {
                    unit.setDesiredLocation(unit.getPixelLocation());
                }
            });
        });
    }
    
    private static void spawnLines() {
        int spacer = 160;
        int lineLength = 35;
        
         for (int i = 0; i < lineLength; i++) {
            Apache heli = new Apache(100 + (i * spacer), 100, 0);
            heli.setRotation(180);
            game.addObject(heli);
        }
        for (int i = 0; i < lineLength; i++) {
            TransportHelicopter transport = new TransportHelicopter(100 + (i * spacer), 200, 0);
            transport.setRotation(180);
            game.addObject(transport);
        }
        for (int i = 0; i < lineLength; i++) {
            Truck truck = new Truck(100 + (i * spacer), 300, 0);
            truck.setRotation(180);
            game.addObject(truck);
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
            game.addObject(new Truck(100 + (i * spacer), 2000, 1));
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new LightTank(100 + (i * spacer), 2150, 1));
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new Apache(100 + (i * spacer), 2300, 1));
        }
        for (int i = 0; i < lineLength; i++) {
            game.addObject(new TransportHelicopter(100 + (i * spacer), 2450, 1));
        }
    }
    
    
    /**
     * accepts a value and scales it based on tick rate relative to the baseTick
     * for example, if the game is running at 30tps and baseTick = 90, then the value will be scaled 3x
     * @param value value
     * @return scaled value
     */
    public static int tickAdjust(int value) {
        int baseTick = 90;
        
        double ratio = baseTick/RTSGame.desiredTPS;
        
        return (int)(value * ratio);
    }
    
    public static double tickAdjust(double value) {
        int baseTick = 90;
        
        double ratio = baseTick/desiredTPS;
        
        return (value * ratio);
    }
}

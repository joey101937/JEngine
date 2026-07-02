package GameDemo.RTSDemo.MapEditor;

import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import GameDemo.RTSDemo.SceneryObjects.BuildingGreen1;
import GameDemo.RTSDemo.SceneryObjects.GreenShippingContainer;
import GameDemo.RTSDemo.SceneryObjects.Hangar;
import GameDemo.RTSDemo.SceneryObjects.MetalShack;
import GameDemo.RTSDemo.SceneryObjects.OrangeWoodHouse;
import GameDemo.RTSDemo.SceneryObjects.PropaneTank;
import GameDemo.RTSDemo.SceneryObjects.Barrel1;
import GameDemo.RTSDemo.SceneryObjects.Barrel2;
import GameDemo.RTSDemo.SceneryObjects.BarbedWire;
import GameDemo.RTSDemo.SceneryObjects.StoneWall1;
import GameDemo.RTSDemo.SceneryObjects.BarbedWireLong;
import GameDemo.RTSDemo.SceneryObjects.Bush1;
import GameDemo.RTSDemo.SceneryObjects.Log1;
import GameDemo.RTSDemo.SceneryObjects.Log2;
import GameDemo.RTSDemo.SceneryObjects.Stump1;
import GameDemo.RTSDemo.SceneryObjects.Stump2;
import GameDemo.RTSDemo.SceneryObjects.Stump3;
import GameDemo.RTSDemo.SceneryObjects.Rock1;
import GameDemo.RTSDemo.SceneryObjects.Rock2;
import GameDemo.RTSDemo.SceneryObjects.Bush2;
import GameDemo.RTSDemo.SceneryObjects.Bush3;
import GameDemo.RTSDemo.SceneryObjects.Tree1;
import GameDemo.RTSDemo.SceneryObjects.Tree2;
import GameDemo.RTSDemo.SceneryObjects.Tree3;
import GameDemo.RTSDemo.Units.Apache;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;
import GameDemo.RTSDemo.Units.Truck;

public class MapLoader {

    /** Caches loaded background images by filename so each large terrain image is only read from disk once. */
    private static final Map<String, BufferedImage> backgroundCache = new HashMap<>();

    /**
     * Loads a TankGame background image by filename, returning a cached instance when available.
     * The shared preloaded terrain image ({@link RTSAssetManager#grassBG}) is reused rather than
     * loading a duplicate ~330MB copy. Returns {@code null} if {@code filename} is null.
     */
    public static BufferedImage loadBackground(String filename) {
        if (filename == null) return null;
        if (filename.equals(RTSAssetManager.grassBGFile) && RTSAssetManager.grassBG != null) {
            backgroundCache.putIfAbsent(filename, RTSAssetManager.grassBG);
        }
        return backgroundCache.computeIfAbsent(filename, f -> Graphic.load("DemoAssets/TankGame/" + f));
    }

    /** Instantiates all objects from {@code data} and adds them to {@code game}. */
    public static void loadIntoGame(MapData data, Game game) {
        for (PlacedObject p : data.objects) {
            GameObject2 obj = createObject(p);
            if (obj == null) continue;
            obj.setLocation(p.x, p.y);
            obj.setRotation(p.rotation);
            if (p.zLayer != Integer.MIN_VALUE) obj.setZLayer(p.zLayer);
            game.addObject(obj);
        }
    }

    private static GameObject2 createObject(PlacedObject p) {
        switch (p.type) {
            case "TankUnit": {
                TankUnit u = new TankUnit(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "LightTank": {
                LightTank u = new LightTank(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "Truck": {
                Truck u = new Truck(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "Hellicopter": {
                Hellicopter u = new Hellicopter(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "Apache": {
                Apache u = new Apache(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "TransportHelicopter": {
                TransportHelicopter u = new TransportHelicopter(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "Rifleman": {
                Rifleman u = new Rifleman(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "Bazookaman": {
                Bazookaman u = new Bazookaman(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "Landmine": {
                Landmine u = new Landmine(p.x, p.y, p.team);
                applyHp(u, p.hpPercent);
                return u;
            }
            case "KeyBuilding":           return new KeyBuilding(p.x, p.y, p.team, p.spawnOffsetX, p.spawnOffsetY, p.spawnRotation);
            case "Hangar":                return new Hangar(p.x, p.y);
            case "BuildingGreen1":        return new BuildingGreen1(p.x, p.y);
            case "OrangeWoodHouse":       return new OrangeWoodHouse(p.x, p.y);
            case "PropaneTank":           return new PropaneTank(p.x, p.y);
            case "GreenShippingContainer":return new GreenShippingContainer(p.x, p.y);
            case "MetalShack":            return new MetalShack(p.x, p.y);
            case "StoneWall1":            return new StoneWall1(p.x, p.y);
            case "Barrel1":               return new Barrel1(p.x, p.y);
            case "Barrel2":               return new Barrel2(p.x, p.y);
            case "Rock1":                 return new Rock1(p.x, p.y);
            case "Rock2":                 return new Rock2(p.x, p.y);
            case "Log1":                  return new Log1(p.x, p.y);
            case "Log2":                  return new Log2(p.x, p.y);
            case "BarbedWire":            return new BarbedWire(p.x, p.y);
            case "BarbedWireLong":        return new BarbedWireLong(p.x, p.y);
            case "Stump1":                return new Stump1(p.x, p.y);
            case "Stump2":                return new Stump2(p.x, p.y);
            case "Stump3":                return new Stump3(p.x, p.y);
            case "Bush1":                 return new Bush1(p.x, p.y);
            case "Bush2":                 return new Bush2(p.x, p.y);
            case "Bush3":                 return new Bush3(p.x, p.y);
            case "Tree1":                 return new Tree1(p.x, p.y);
            case "Tree2":                 return new Tree2(p.x, p.y);
            case "Tree3":                 return new Tree3(p.x, p.y);
            default:
                System.err.println("MapLoader: unknown type '" + p.type + "', skipping");
                return null;
        }
    }

    private static void applyHp(RTSUnit unit, int hpPercent) {
        unit.currentHealth = Math.max(1, (int)(unit.maxHealth * hpPercent / 100.0));
    }
}

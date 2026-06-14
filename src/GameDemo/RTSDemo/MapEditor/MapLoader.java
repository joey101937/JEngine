package GameDemo.RTSDemo.MapEditor;

import Framework.Game;
import Framework.GameObject2;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SceneryObjects.BuildingGreen1;
import GameDemo.RTSDemo.SceneryObjects.GreenShippingContainer;
import GameDemo.RTSDemo.SceneryObjects.Hangar;
import GameDemo.RTSDemo.SceneryObjects.MetalShack;
import GameDemo.RTSDemo.SceneryObjects.OrangeWoodHouse;
import GameDemo.RTSDemo.SceneryObjects.PropaneTank;
import GameDemo.RTSDemo.SceneryObjects.Bush1;
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

    /** Instantiates all objects from {@code data} and adds them to {@code game}. */
    public static void loadIntoGame(MapData data, Game game) {
        for (PlacedObject p : data.objects) {
            GameObject2 obj = createObject(p);
            if (obj == null) continue;
            obj.setLocation(p.x, p.y);
            obj.setRotation(p.rotation);
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
            case "KeyBuilding":           return new KeyBuilding(p.x, p.y, p.team);
            case "Hangar":                return new Hangar(p.x, p.y);
            case "BuildingGreen1":        return new BuildingGreen1(p.x, p.y);
            case "OrangeWoodHouse":       return new OrangeWoodHouse(p.x, p.y);
            case "PropaneTank":           return new PropaneTank(p.x, p.y);
            case "GreenShippingContainer":return new GreenShippingContainer(p.x, p.y);
            case "MetalShack":            return new MetalShack(p.x, p.y);
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

package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.Main;
import Framework.PathingLayer;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author guydu
 */
public class TerrainTileMap implements Serializable {

    public static TerrainTileMap map1TerrainMapNormal, map1TerrainMapFine, map1TerrainMapLarge, map1TerrainMapGiantTerrain = null;

    public static PathingLayer.Type paddingType = new PathingLayer.Type("padding", new Color(0, 255, 255));

    public HashMap<Coordinate, Boolean> blockedMap = new HashMap<>();
    public int tileSize;

    public static synchronized TerrainTileMap getCurrentTerrainTileMapForSize(int size) {
        if (size == Tile.tileSizeNormal) {
            return getCurrentTerrainTileMapNormal();
        }
        if (size == Tile.tileSizeFine) {
            return getCurrentTerrainTileMapFine();
        }
        if (size == Tile.tileSizeLarge) {
            return getCurrentTerrainTileMapLarge();
        }
        if (size == Tile.tileSizeGiantTerrain) {
            return getCurrentTerrainTileMapGiantTerrain();
        }

        return null;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapNormal() {
        if (map1TerrainMapNormal == null) {
            map1TerrainMapNormal = loadFromFile(Main.assets + "terrain_Normal");
        }

        return map1TerrainMapNormal;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapFine() {
        if (map1TerrainMapFine == null) {
            map1TerrainMapFine = loadFromFile(Main.assets + "terrain_Fine");
        }

        return map1TerrainMapFine;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapLarge() {
        if (map1TerrainMapLarge == null) {
            map1TerrainMapLarge = loadFromFile(Main.assets + "terrain_Large");
        }

        return map1TerrainMapLarge;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapGiantTerrain() {
        if (map1TerrainMapGiantTerrain == null) {
            map1TerrainMapGiantTerrain = loadFromFile(Main.assets + "terrain_GiantTerrain");
        }

        return map1TerrainMapGiantTerrain;
    }

    public TerrainTileMap(int tileSize) {
        this.tileSize = tileSize;
    }

    public boolean isTileBlocked(Tile t) {
        return blockedMap.getOrDefault(t.getGridLocation(), Boolean.FALSE);
    }

    public int getNumBlockedTiles() {
        int numBlocked = 0;
        for (Boolean b : blockedMap.values()) {
            numBlocked++;
        }
        return numBlocked;
    }

    private static boolean pathingClearForTileAtPixel(int inputX, int inputY, PathingLayer pl, int tileSize) {
        int padding = 0;
        int pixelX = inputX - padding;
        int pixelY = inputY - padding;
        for (int x = 0; x < tileSize + padding; x++) {
            for (int y = 0; y < tileSize + padding; y++) {
                if (pl.getTypeAt(x + pixelX, y + pixelY) == PathingLayer.Type.water
                        || pl.getTypeAt(x + pixelX, y + pixelY) == PathingLayer.Type.impass
                        || pl.getTypeAt(x + pixelX, y + pixelY) == paddingType) {
                    return false;
                }
            }
        }
        return true;
    }

    public static TerrainTileMap generate(PathingLayer pathingLayer, int tileSize) {
        System.out.println("generating");
        TerrainTileMap out = new TerrainTileMap(tileSize);
        for (int x = 0; x < pathingLayer.getSource().getWidth(); x += tileSize) {
            for (int y = 0; y < pathingLayer.getSource().getHeight(); y += tileSize) {
                out.blockedMap.put(new Coordinate(x / tileSize, y / tileSize), !pathingClearForTileAtPixel(x, y, pathingLayer, tileSize));
            }
        }
        int numBlocked = 0;
        for (Boolean b : out.blockedMap.values()) {
            numBlocked++;
        }
        System.out.println("generation " + tileSize + " done (" + numBlocked + ")");
        return out;
    }

    public static TerrainTileMap loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (TerrainTileMap) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveToFile(String filename) {
        System.out.println("saving");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        generateAll();
        System.out.println("All Complete");
        System.exit(0);
    }

    public static void loadAll() {
        CompletableFuture<TerrainTileMap> normalFuture = CompletableFuture.supplyAsync(() -> getCurrentTerrainTileMapNormal());
        CompletableFuture<TerrainTileMap> fineFuture = CompletableFuture.supplyAsync(() -> getCurrentTerrainTileMapFine());
        CompletableFuture<TerrainTileMap> largeFuture = CompletableFuture.supplyAsync(() -> getCurrentTerrainTileMapLarge());
        CompletableFuture<TerrainTileMap> giantTerrainFuture = CompletableFuture.supplyAsync(() -> getCurrentTerrainTileMapGiantTerrain());

        int numBlocked = 0;
        for (Boolean b : getCurrentTerrainTileMapGiantTerrain().blockedMap.values()) {
            numBlocked++;
        }
        System.out.println("gtt has " + " (" + numBlocked + ")");

        CompletableFuture.allOf(normalFuture, fineFuture, largeFuture, giantTerrainFuture).join();
        System.out.println("terrain map loaded");
    }

    public static void generateAll() {
        PathingLayer pl = new PathingLayer(RTSAssetManager.rtsPathing, "DemoAssets/TankGame/terrainPlaygroundPathing.png");
        pl.generateMap();

        CompletableFuture<Void> normalFuture = CompletableFuture.runAsync(() -> {
            TerrainTileMap normal = TerrainTileMap.generate(pl, Tile.tileSizeNormal);
            normal.saveToFile(Main.assets + "terrain_Normal");
        });

        CompletableFuture<Void> fineFuture = CompletableFuture.runAsync(() -> {
            TerrainTileMap fine = TerrainTileMap.generate(pl, Tile.tileSizeFine);
            fine.saveToFile(Main.assets + "terrain_Fine");
        });

        CompletableFuture<Void> largeFuture = CompletableFuture.runAsync(() -> {
            TerrainTileMap large = TerrainTileMap.generate(pl, Tile.tileSizeLarge);
            large.saveToFile(Main.assets + "terrain_Large");
        });

        CompletableFuture<Void> giantTerrainFuture = CompletableFuture.runAsync(() -> {
            TerrainTileMap giantTerrain = TerrainTileMap.generate(pl, Tile.tileSizeGiantTerrain);
            giantTerrain.saveToFile(Main.assets + "terrain_GiantTerrain");
        });

        CompletableFuture.allOf(normalFuture, fineFuture, largeFuture, giantTerrainFuture).join();
    }
}

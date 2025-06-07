package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.Main;
import Framework.PathingLayer;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class TerrainTileMap implements Serializable {
    
    public static TerrainTileMap map1TerrainMapNormal, map1TerrainMapFine, map1TerrainMapLarge = null;

    public static PathingLayer.Type paddingType = new PathingLayer.Type("noPath", new Color(0, 255, 255));

    public HashMap<Coordinate, Boolean> blockedMap = new HashMap<>();
    public int tileSize;
    
    public static synchronized TerrainTileMap getCurrentTerrainTileMapForSize(int size) {
        if(size == Tile.tileSizeNormal) return getCurrentTerrainTileMapNormal();
        if(size == Tile.tileSizeFine) return getCurrentTerrainTileMapFine();
        if(size == Tile.tileSizeLarge) return getCurrentTerrainTileMapLarge();
        return null;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapNormal() {
        if (map1TerrainMapNormal == null) {
            map1TerrainMapNormal = loadFromFile(Main.assets + "terrain_Normal"); // generate(Window.currentGame.getPathingLayer(), Tile.tileSizeNormal);
        }

        return map1TerrainMapNormal;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapFine() {
        if (map1TerrainMapFine == null) {
            map1TerrainMapFine = loadFromFile(Main.assets + "terrain_Fine"); //generate(Window.currentGame.getPathingLayer(), Tile.tileSizeFine);
        }

        return map1TerrainMapFine;
    }

    public static synchronized TerrainTileMap getCurrentTerrainTileMapLarge() {
        if (map1TerrainMapLarge == null) {
            map1TerrainMapLarge = map1TerrainMapFine = loadFromFile(Main.assets + "terrain_Large"); //generate(Window.currentGame.getPathingLayer(), Tile.tileSizeLarge);
        }

        return map1TerrainMapLarge;
    }

    public TerrainTileMap(int tileSize) {
        this.tileSize = tileSize;
    }

    public boolean isTileBlocked(Tile t) {
        return blockedMap.getOrDefault(t.getGridLocation(), Boolean.FALSE);
    }

    private static boolean pathingClearForTileAtPixel(int inputX, int inputY, PathingLayer pl, int tileSize) {
        int padding = 0;
        int pixelX = inputX - padding;
        int pixelY = inputY - padding;
        boolean clear = false;
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
        System.out.println("generation done");
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
        PathingLayer pl = new PathingLayer(RTSAssetManager.rtsPathing);
        pl.generateMap();
        TerrainTileMap out = TerrainTileMap.generate(pl, Tile.tileSizeFine);
        out.saveToFile(Main.assets + "terrain_Fine");
    }
}

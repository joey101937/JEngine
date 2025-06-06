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
    public static TerrainTileMap map1TerrainMap = null;
    
    public static PathingLayer.Type paddingType = new PathingLayer.Type("noPath", new Color(0,255,255));
    
    public HashMap<Coordinate, Boolean> blockedMap = new HashMap<>();
    
    public static synchronized TerrainTileMap getCurrentTerrainTileMap() {
        if(map1TerrainMap == null) {
            map1TerrainMap = generate(Window.currentGame.getPathingLayer());
        }
        
        return map1TerrainMap;
    };
    
    
    public TerrainTileMap() {
        
    }
    
    public boolean isTileBlocked (Tile t) {
        return blockedMap.getOrDefault(t.getGridLocation(), Boolean.FALSE);
    }
    
    private static boolean pathingClearForTileAtPixel(int inputX, int inputY, PathingLayer pl) {
        int padding = 0;
        int pixelX = inputX - padding;
        int pixelY = inputY - padding;
        boolean clear = false;
        for(int x = 0; x < Tile.tileSize + padding; x++) {
            for(int y = 0; y < Tile.tileSize + padding; y++) {
                if(pl.getTypeAt(x + pixelX, y + pixelY) == PathingLayer.Type.water
                        || pl.getTypeAt(x + pixelX, y + pixelY) == PathingLayer.Type.impass
                        || pl.getTypeAt(x + pixelX, y + pixelY) == paddingType) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static TerrainTileMap generate(PathingLayer pathingLayer) {
        System.out.println("generating");
        TerrainTileMap out = new TerrainTileMap();
        for(int x = 0; x < pathingLayer.getSource().getWidth(); x+=Tile.tileSize) {
            for(int y = 0; y < pathingLayer.getSource().getHeight(); y+=Tile.tileSize) {
                out.blockedMap.put(new Coordinate(x/Tile.tileSize, y/Tile.tileSize), !pathingClearForTileAtPixel(x,y, pathingLayer));
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
        TerrainTileMap out = TerrainTileMap.generate(pl);
        out.saveToFile(Main.assets + "terrainTest.terrainTileMap");
    }
}

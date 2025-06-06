package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.PathingLayer;
import GameDemo.RTSDemo.RTSAssetManager;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class TerrainTileMap implements Serializable {
    public static TerrainTileMap map1TerrainMap = new TerrainTileMap();
    
    public HashMap<Coordinate, Boolean> blockedMap = new HashMap<>();
    
    public static TerrainTileMap getCurrentTerrainTileMap() {
        return map1TerrainMap;
    };
    
    
    public TerrainTileMap() {
        
    }
    
    public boolean isTileBlocked (Tile t) {
        return blockedMap.getOrDefault(t.getGridLocation(), Boolean.FALSE);
    }
    
    private static boolean pathingClearInRange(int pixelX, int pixelY, PathingLayer pl) {
        boolean clear = false;
        for(int x = 0; x < Tile.tileSize; x++) {
            for(int y = 0; y < Tile.tileSize; y++) {
                if(pl.getTypeAt(x + pixelX, y + pixelY) == PathingLayer.Type.water
                        || pl.getTypeAt(x + pixelX, y + pixelY) == PathingLayer.Type.impass) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static TerrainTileMap generate(PathingLayer pathingLayer) {
        TerrainTileMap out = new TerrainTileMap();
        for(int x = 0; x < pathingLayer.getSource().getWidth(); x+=Tile.tileSize) {
            for(int y = 0; y < pathingLayer.getSource().getHeight(); y+=Tile.tileSize) {
                out.blockedMap.put(new Coordinate(x/Tile.tileSize, y/Tile.tileSize), pathingClearInRange(x,y, pathingLayer));
            }
        }
        
        return out;
    }
    
    public static TerrainTileMap loadFromFile (String filename) {
        // todo
        return null;
    }
    
    public void saveToFile (String filename) {
        // todo
    }
    
    public static void main(String[] args) {
        RTSAssetManager.initialize();
        PathingLayer pl = new PathingLayer(RTSAssetManager.rtsPathing);
        pl.generateMap();
        TerrainTileMap out = TerrainTileMap.generate(pl);
    }
}

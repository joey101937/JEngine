package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import java.io.Serializable;

public class Tile implements Serializable{
    public static int tileSizeFine = 12;
    public static int tileSizeNormal = 26;
    public static int tileSizeLarge = 50;
    public static int tileSizeGiantTerrain = 250;
    
    /**
     * grid coords
     */
    public int x,y;
    public TileMap tileMap;
    public int tileSize;
    
    public boolean isBlocked(String pathingSignature) {
        OccupationMap om = tileMap.occupationMaps.get(pathingSignature);
        if (om == null) {
            return false;
        }
        return om.isTileBlocked(this);
    }
    
    public Tile(TileMap tileMap, int x, int y, int size) {
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;
        this.tileSize = size;
    }
    
    public Coordinate getCenterPoint () {
        return new Coordinate(x*tileSize + tileSize/2, y*tileSize + tileSize/2);
    }
    
    public Coordinate getGridLocation () {
        return new Coordinate(x,y);
    }
    
    @Override
    public String toString() {
        return getGridLocation().toString();
    }
}

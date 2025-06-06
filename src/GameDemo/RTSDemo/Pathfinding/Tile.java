package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import java.io.Serializable;

public class Tile implements Serializable{
    public static int tileSize = 36;
    
    /**
     * grid coords
     */
    public int x,y;
    public TileMap tileMap;
    
    public boolean isBlocked(String pathingSignature) {
        return tileMap.occupationMaps.get(pathingSignature).isTileBlocked(this);
    }
    
    public Tile(TileMap tileMap, int x, int y) {
        this.tileMap = tileMap;
        this.x = x;
        this.y = y;
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

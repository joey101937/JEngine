package GameDemo.RTSDemo.Pathfinding;

import java.io.Serializable;

public class Tile implements Serializable{
    public static int tileSize = 20;
    
    public int x,y;
    public TileMap tileMap;
    
    public boolean isBlocked() {
        return tileMap.occupiedMap.get(this);
    }
    
    public Tile(TileMap tileMap, int x, int y) {
        this.tileMap = tileMap;
    }
}

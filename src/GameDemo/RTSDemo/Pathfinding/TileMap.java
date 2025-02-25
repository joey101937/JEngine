package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import GameDemo.RTSDemo.RTSUnit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author guydu
 */
public class TileMap implements Serializable{
    public static ExecutorService occupationService = Executors.newFixedThreadPool(12);
    public Tile[][] tileGrid;
    public int worldWidth, worldHeight;
    public HashMap<Tile, Boolean> occupiedMap = new HashMap<>();
    
    public void updateOccupationMap(Game game) {
        occupiedMap.clear();
        Collection<Future<?>> occupationTasks = new LinkedList<>();
        
        for(GameObject2 go : game.getAllObjects()){
            if(go instanceof RTSUnit unit) {
                occupationTasks.add(occupationService.submit(() -> {
                    for(Coordinate coord : getTilesNearPoint(unit.getPixelLocation(), unit.getWidth()/2)) {
                        occupiedMap.put(tileGrid[coord.x][coord.y], true);
                    }
                    return true;
                }));
                
            }
        }
        
        Handler.waitForAllJobs(occupationTasks);
    }
    
    
     public TileMap(int worldWidth, int worldHeight) {
        tileGrid = new Tile[worldWidth/Tile.tileSize][worldHeight/Tile.tileSize];
        for(int x = 0; x < tileGrid.length; x++) {
            for(int y = 0; y <tileGrid[0].length; y++) {
                tileGrid[x][y] = new Tile(this, x, y);
            }
        }
    }
     
     
     public Tile getTileAtLocation(Coordinate location) {
         int tileX = location.x / Tile.tileSize;
         int tileY = location.y / Tile.tileSize;
         
         // Check if the calculated tile coordinates are within the grid bounds
         if (tileX >= 0 && tileX < tileGrid.length && tileY >= 0 && tileY < tileGrid[0].length) {
             return tileGrid[tileX][tileY];
         } else {
             // Return null or throw an exception if the location is out of bounds
             return null;
         }
     }
     
    
    public List<Coordinate> getTilesNearPoint(Coordinate point, int radius) {
        List<Coordinate> affectedTiles = new ArrayList<>();
        
        // Calculate the bounding box of the circle
        int minTileX = (int) Math.floor((point.x - radius) / Tile.tileSize);
        int maxTileX = (int) Math.ceil((point.x + radius) / Tile.tileSize);
        int minTileY = (int) Math.floor((point.y - radius) / Tile.tileSize);
        int maxTileY = (int) Math.ceil((point.y + radius) / Tile.tileSize);
        
        // Check each tile within the bounding box
        for (int tileY = minTileY; tileY < maxTileY; tileY++) {
            for (int tileX = minTileX; tileX < maxTileX; tileX++) {
                // Calculate the closest point on the tile to the circle's center
                double closestX = Math.max(tileX * Tile.tileSize, Math.min(point.x, (tileX + 1) * Tile.tileSize));
                double closestY = Math.max(tileY * Tile.tileSize, Math.min(point.y, (tileY + 1) * Tile.tileSize));
                
                // Calculate the distance between the closest point and the circle's center
                double distance = Math.sqrt(Math.pow(closestX - point.x, 2) + Math.pow(closestY - point.y, 2));
                
                // If the distance is less than or equal to the radius, the tile is affected
                if (distance <= radius) {
                    affectedTiles.add(new Coordinate(tileX, tileY));
                }
            }
        }
        
        return affectedTiles;
    }
}

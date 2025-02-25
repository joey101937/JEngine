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
    public static ExecutorService occupationService = Executors.newFixedThreadPool(200);
    public Tile[][] tileGrid;
    public int worldWidth, worldHeight;
    public HashMap<Tile, Boolean> occupiedMap = new HashMap<>();
    
    public void updateOccupationMap(Game game) {
        occupiedMap.clear();
        Collection<Future<?>> occupationTasks = new LinkedList<>();
        
        for(GameObject2 go : game.getAllObjects()){
            if(go instanceof RTSUnit unit && !unit.hasNonzeroVelocity()) {
                occupationTasks.add(occupationService.submit(() -> {
                    for(Coordinate coord : getTilesNearPoint(unit.getPixelLocation(), unit.getWidth() + 0)) {
                        try {
                            occupiedMap.put(tileGrid[coord.x][coord.y], true);
                        } catch (IndexOutOfBoundsException ib) {
                            // ignore ib
                        }
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
    
    
    
    public ArrayList<Tile> getNeighbors(Coordinate sourceTilePos) {
       ArrayList<Tile> out = new ArrayList<>();
       try {
           out.add(tileGrid[sourceTilePos.x+1][sourceTilePos.y]);
       } catch (Exception e) {
           // doesnt exist
       }
       try {
           out.add(tileGrid[sourceTilePos.x-1][sourceTilePos.y]);
       } catch (Exception e) {
           // doesnt exist
       }
       try {
           out.add(tileGrid[sourceTilePos.x][sourceTilePos.y + 1]);
       } catch (Exception e) {
           // doesnt exist
       }
       try {
           out.add(tileGrid[sourceTilePos.x][sourceTilePos.y - 1]);
       } catch (Exception e) {
           // doesnt exist
       }
       return out;
    };
    
    
    /**
     * gets the tile closest open to the target pixel. If there are multiple of the same distance, use the one that is closest to the tiebreaker pixel location
     * @param targetPixel point we want to get closest to in pixels
     * @param tiebreakerPixel point to use as tiebreaker. CAN BE NULL. if this is null, pick one at random
     * @return tile that meets criteria
     */
    public Tile getClosestOpenTile(Coordinate targetPixel, Coordinate tiebreakerPixel) {
        Tile closestTile = null;
        double closestDistance = Double.MAX_VALUE;
        List<Tile> tiesToBreak = new ArrayList<>();

        for (int x = 0; x < tileGrid.length; x++) {
            for (int y = 0; y < tileGrid[0].length; y++) {
                Tile currentTile = tileGrid[x][y];
                if (!occupiedMap.getOrDefault(currentTile, false)) {
                    Coordinate tileCenter = new Coordinate(
                        (x * Tile.tileSize) + (Tile.tileSize / 2),
                        (y * Tile.tileSize) + (Tile.tileSize / 2)
                    );
                    double distance = tileCenter.distanceFrom(targetPixel);

                    if (distance < closestDistance) {
                        closestTile = currentTile;
                        closestDistance = distance;
                        tiesToBreak.clear();
                        tiesToBreak.add(currentTile);
                    } else if (distance == closestDistance) {
                        tiesToBreak.add(currentTile);
                    }
                }
            }
        }

        if (tiesToBreak.size() > 1 && tiebreakerPixel != null) {
            return tiesToBreak.stream()
                .min((t1, t2) -> {
                    Coordinate c1 = new Coordinate(
                        (t1.x * Tile.tileSize) + (Tile.tileSize / 2),
                        (t1.y * Tile.tileSize) + (Tile.tileSize / 2)
                    );
                    Coordinate c2 = new Coordinate(
                        (t2.x * Tile.tileSize) + (Tile.tileSize / 2),
                        (t2.y * Tile.tileSize) + (Tile.tileSize / 2)
                    );
                    return Double.compare(c1.distanceFrom(tiebreakerPixel), c2.distanceFrom(tiebreakerPixel));
                })
                .orElse(closestTile);
        } else {
            return closestTile;
        }
    }
}

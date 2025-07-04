package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author guydu
 */
public class TileMap implements Serializable{
    public Tile[][] tileGrid;
    public int worldWidth, worldHeight;
    public HashMap<String, OccupationMap> occupationMaps = new HashMap<>();
    public int tileSize;
    
    
    public OccupationMap generateOccupationMapFromSignature(String signature) {
        String[] parts = signature.split(",");
        if (parts.length != 5) {
            System.out.println("signature: " + signature);
            throw new IllegalArgumentException("Invalid pathing signature format");
        }
        int padding = Integer.parseInt(parts[0]);
        int team = Integer.parseInt(parts[1]);
        int plane = Integer.parseInt(parts[2]);
        String commandGroup = parts[3];
        int tileSize = Integer.parseInt(parts[4]);
        return new OccupationMap(padding, commandGroup, team, plane, this, this.tileSize);
    }
    
     public TileMap(int worldWidth, int worldHeight, int tileSize) {
        tileGrid = new Tile[worldWidth/tileSize][worldHeight/tileSize];
        for(int x = 0; x < tileGrid.length; x++) {
            for(int y = 0; y <tileGrid[0].length; y++) {
                tileGrid[x][y] = new Tile(this, x, y, tileSize);
            }
        }
        this.tileSize = tileSize;
    }
     
     
     public Tile getTileAtLocation(Coordinate location) {
         int tileX = location.x / tileSize;
         int tileY = location.y / tileSize;
         
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
        int minTileX = (int) Math.floor((point.x - radius) / tileSize);
        int maxTileX = (int) Math.ceil((point.x + radius) / tileSize);
        int minTileY = (int) Math.floor((point.y - radius) / tileSize);
        int maxTileY = (int) Math.ceil((point.y + radius) / tileSize);
        
        // Check each tile within the bounding box
        for (int tileY = minTileY; tileY < maxTileY; tileY++) {
            for (int tileX = minTileX; tileX < maxTileX; tileX++) {
                // Calculate the closest point on the tile to the circle's center
                double closestX = Math.max(tileX * tileSize, Math.min(point.x, (tileX + 1) * tileSize));
                double closestY = Math.max(tileY * tileSize, Math.min(point.y, (tileY + 1) * tileSize));
                
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
    
    /**
     * Returns a list of all tiles in the tile map which intersect with the given line
     * Line coordinates are in pixels
     * @param start The starting point of the line
     * @param end The ending point of the line
     * @return ArrayList of Tiles that the line passes through
     */
    public ArrayList<Tile> getTilesIntersectingLine(Coordinate start, Coordinate end) {
        ArrayList<Tile> intersectingTiles = new ArrayList<>();
        
        int x0 = start.x / tileSize;
        int y0 = start.y / tileSize;
        int x1 = end.x / tileSize;
        int y1 = end.y / tileSize;
        
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        
        while (true) {
            if (x0 >= 0 && x0 < tileGrid.length && y0 >= 0 && y0 < tileGrid[0].length) {
                intersectingTiles.add(tileGrid[x0][y0]);
            }
            
            if (x0 == x1 && y0 == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
        
        return intersectingTiles;
    }
    
    public ArrayList<Tile> getTilesIntersectingRectangle(Rectangle rect){
        ArrayList<Tile> intersectingTiles = new ArrayList<>();
        
        // Convert pixel coordinates to tile coordinates
        int startTileX = Math.max(0, rect.x / tileSize);
        int startTileY = Math.max(0, rect.y / tileSize);
        int endTileX = Math.min(tileGrid.length - 1, (rect.x + rect.width) / tileSize);
        int endTileY = Math.min(tileGrid[0].length - 1, (rect.y + rect.height) / tileSize);
        
        // Add all tiles within the rectangle bounds
        for (int x = startTileX; x <= endTileX; x++) {
            for (int y = startTileY; y <= endTileY; y++) {
                intersectingTiles.add(tileGrid[x][y]);
            }
        }
        
        return intersectingTiles;
    }
    
    /**
     * Returns a list of all tiles in the tile map which intersect with the given line
     * Line coordinates are in pixels
     * @param start The starting point of the line
     * @param end The ending point of the line
     * @param radius how thick the line is
     * @return ArrayList of Tiles that the line passes through
     */
    public ArrayList<Tile> getTileIntersectingThickLine(Coordinate start, Coordinate end, int radius) {
        HashSet<Tile> resultTiles = new HashSet<>();  // Using HashSet to avoid duplicates
        
        // Get the basic line tiles first
        ArrayList<Tile> lineTiles = getTilesIntersectingLine(start, end);
        
        // For each tile in the line, get tiles within radius
        for (Tile tile : lineTiles) {
            Coordinate tileCenter = new Coordinate(
                tile.x * tileSize + tileSize / 2,
                tile.y * tileSize + tileSize / 2
            );
            
            List<Coordinate> nearbyTileCoords = getTilesNearPoint(tileCenter, radius);
            for (Coordinate coord : nearbyTileCoords) {
                if (coord.x >= 0 && coord.x < tileGrid.length && 
                    coord.y >= 0 && coord.y < tileGrid[0].length) {
                    resultTiles.add(tileGrid[coord.x][coord.y]);
                }
            }
        }
        
        return new ArrayList<>(resultTiles);
    }
    
    
    public ArrayList<Tile> getNeighbors(Coordinate sourceTilePos) {
        ArrayList<Tile> out = new ArrayList<>();
        // Check all 8 directions: cardinal and diagonal
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the center tile
                
                try {
                    out.add(tileGrid[sourceTilePos.x + dx][sourceTilePos.y + dy]);
                } catch (Exception e) {
                    // Position doesn't exist, skip it
                }
            }
        }
        return out;
    }
    
    
    /**
     * gets the tile closest open to the target pixel. If there are multiple of the same distance, use the one that is closest to the tiebreaker pixel location
     * @param targetPixel point we want to get closest to in pixels
     * @param tiebreakerPixel point to use as tiebreaker.
     * @return tile that meets criteria
     */
    public Tile getClosestOpenTile(Coordinate targetPixel, Coordinate tiebreakerPixel, String pathingSignature) {
        Tile closestTile = null;
        double closestDistance = Double.MAX_VALUE;
        double closestTiebreakerDistance = Double.MAX_VALUE;

        int centerX = targetPixel.x / tileSize;
        int centerY = targetPixel.y / tileSize;
        int maxRadius = 10; // 20 tile area 

        for (int radius = 0; radius < maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) == radius || Math.abs(dy) == radius) {
                        int x = centerX + dx;
                        int y = centerY + dy;

                        if (x >= 0 && x < tileGrid.length && y >= 0 && y < tileGrid[0].length) {
                            Tile currentTile = tileGrid[x][y];
                            if (!occupationMaps.get(pathingSignature).isTileBlocked(currentTile)) {
                                Coordinate tileCenter = new Coordinate(
                                    (x * tileSize) + (tileSize / 2),
                                    (y * tileSize) + (tileSize / 2)
                                );
                                double distance = tileCenter.distanceFrom(targetPixel);

                                if (distance < closestDistance) {
                                    closestTile = currentTile;
                                    closestDistance = distance;
                                    closestTiebreakerDistance = (tiebreakerPixel != null) ? tileCenter.distanceFrom(tiebreakerPixel) : 0;
                                } else if (distance == closestDistance && tiebreakerPixel != null) {
                                    double tiebreakerDistance = tileCenter.distanceFrom(tiebreakerPixel);
                                    if (tiebreakerDistance < closestTiebreakerDistance) {
                                        closestTile = currentTile;
                                        closestTiebreakerDistance = tiebreakerDistance;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (closestTile != null) {
                break;
            }
        }

        return closestTile;
    }
    
    
    public boolean allClear (List<Tile> input, String pathingSignature) {
        for(Tile t : input) {
            if(t.isBlocked(pathingSignature)) return false;
        }
        return true;
    }

    public void refreshOccupationmaps(Game game) {
        if(this.tileSize == Tile.tileSizeGiantTerrain) {
            refreshOccupationmapsGiantTerrain(game);
            return;
        }
        HashSet<String> pathingSignatures = new HashSet<>();
        for(GameObject2 go : game.getAllObjects()) {
            if(go instanceof RTSUnit unit) {
                if(unit.getNavTileSize() == tileSize) {
                    pathingSignatures.add(unit.getPathingSignature());
                }
            }
        }
        
        occupationMaps.clear();
        for(String s : pathingSignatures) {
            occupationMaps.put(s, generateOccupationMapFromSignature(s));
            occupationMaps.get(s).updateOccupationMap(game);
        }
    }
    
    private void refreshOccupationmapsGiantTerrain(Game game){
        HashSet<String> pathingSignatures = new HashSet<>();
        for(GameObject2 go : game.getAllObjects()) {
            if(go instanceof RTSUnit unit) {
                    pathingSignatures.add(unit.getPathingSignature());
            }
        }
        occupationMaps.clear();
        for(String s : pathingSignatures) {
            occupationMaps.put(s, generateOccupationMapFromSignature(s));
        }
    }
  
}

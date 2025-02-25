package GameDemo.TileMaker;

import Framework.Coordinate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This represents the state of the program. Can be saved to disk and reloaded.
 * @author guydu
 */
public class TileMap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public Tile[][] tileGrid;
    public String name = "Untitled";
    public String backgroundName = "DemoAssets/whitebg.png"; // "DemoAssets/TankGame/grassTerrain_mega3.png";
    
    private static final int TILE_SIZE = 20;

    /**
     * Get all tiles that would be covered in whole or in part by a circle
     * centered at the given point with the given radius.
     * @param centerX X-coordinate of the circle's center
     * @param centerY Y-coordinate of the circle's center
     * @param radius Radius of the circle
     * @return List of Coordinates representing the affected tiles
     */
    public List<Coordinate> getTilesNearPoint(double centerX, double centerY, double radius) {
        List<Coordinate> affectedTiles = new ArrayList<>();
        
        // Calculate the bounding box of the circle
        int minTileX = (int) Math.floor((centerX - radius) / TILE_SIZE);
        int maxTileX = (int) Math.ceil((centerX + radius) / TILE_SIZE);
        int minTileY = (int) Math.floor((centerY - radius) / TILE_SIZE);
        int maxTileY = (int) Math.ceil((centerY + radius) / TILE_SIZE);
        
        // Check each tile within the bounding box
        for (int tileY = minTileY; tileY < maxTileY; tileY++) {
            for (int tileX = minTileX; tileX < maxTileX; tileX++) {
                // Calculate the closest point on the tile to the circle's center
                double closestX = Math.max(tileX * TILE_SIZE, Math.min(centerX, (tileX + 1) * TILE_SIZE));
                double closestY = Math.max(tileY * TILE_SIZE, Math.min(centerY, (tileY + 1) * TILE_SIZE));
                
                // Calculate the distance between the closest point and the circle's center
                double distance = Math.sqrt(Math.pow(closestX - centerX, 2) + Math.pow(closestY - centerY, 2));
                
                // If the distance is less than or equal to the radius, the tile is affected
                if (distance <= radius) {
                    affectedTiles.add(new Coordinate(tileX, tileY));
                }
            }
        }
        
        return affectedTiles;
    }
}

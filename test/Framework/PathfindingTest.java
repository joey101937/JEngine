package Framework;

import GameDemo.RTSDemo.Pathfinding.*;
import GameDemo.RTSDemo.RTSUnit;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.List;

/**
 * Tests for NavigationManager pathfinding functionality
 * @author guydu
 */
public class PathfindingTest {
    
    private Game game;
    private NavigationManager navigationManager;
    private RTSUnit testUnit;
    
    @Before
    public void setUp() {
        game = new Game(1000, 1000);
        navigationManager = new NavigationManager(game);
        testUnit = new RTSUnit(100, 100, 1) {
            @Override
            public int getNavTileSize() {
                return Tile.tileSizeNormal;
            }
            
            @Override
            public String getPathingSignature() {
                return "16,1,0,testGroup," + Tile.tileSizeNormal;
            }
            
            @Override
            public int getWidth() {
                return 32;
            }
            
            @Override
            public int getHeight() {
                return 32;
            }
        };
        game.addObject(testUnit);
    }

    @Test
    public void testGetPathDeterministic() {
        // Test points
        Coordinate start = new Coordinate(100, 100);
        Coordinate end = new Coordinate(500, 500);
        
        // Get paths multiple times
        List<Coordinate> path1 = navigationManager.getPath(start, end, testUnit);
        List<Coordinate> path2 = navigationManager.getPath(start, end, testUnit);
        List<Coordinate> path3 = navigationManager.getPath(start, end, testUnit);
        
        // Assert paths are not null
        assertNotNull("Path should not be null", path1);
        assertNotNull("Path should not be null", path2);
        assertNotNull("Path should not be null", path3);
        
        // Assert paths have same length
        assertEquals("Paths should have same length", path1.size(), path2.size());
        assertEquals("Paths should have same length", path1.size(), path3.size());
        
        // Compare each coordinate in the paths
        for(int i = 0; i < path1.size(); i++) {
            assertEquals("X coordinate should match", path1.get(i).x, path2.get(i).x);
            assertEquals("Y coordinate should match", path1.get(i).y, path2.get(i).y);
            assertEquals("X coordinate should match", path1.get(i).x, path3.get(i).x);
            assertEquals("Y coordinate should match", path1.get(i).y, path3.get(i).y);
        }
    }
    
    @Test
    public void testGetPathWithObstacles() {
        // Add some terrain obstacles
        TerrainTileMap terrainMap = TerrainTileMap.getCurrentTerrainTileMapForSize(Tile.tileSizeNormal);
        terrainMap.blockedMap.put(new Coordinate(5, 5), true);
        terrainMap.blockedMap.put(new Coordinate(5, 6), true);
        terrainMap.blockedMap.put(new Coordinate(6, 5), true);
        
        // Test points around obstacle
        Coordinate start = new Coordinate(100, 100);
        Coordinate end = new Coordinate(300, 300);
        
        List<Coordinate> path = navigationManager.getPath(start, end, testUnit);
        
        assertNotNull("Path should not be null", path);
        assertFalse("Path should not be empty", path.isEmpty());
        
        // Verify path avoids obstacles
        for(Coordinate c : path) {
            Tile tile = navigationManager.getTileMapBySize(testUnit.getNavTileSize()).getTileAtLocation(c);
            assertFalse("Path should not contain blocked tiles", 
                terrainMap.isTileBlocked(tile));
        }
    }
    
    @Test
    public void testGetPathMaxDistance() {
        // Test points beyond max calculation distance
        Coordinate start = new Coordinate(100, 100);
        Coordinate end = new Coordinate(2000, 2000); // Beyond NavigationManager.maxCalculationDistance
        
        List<Coordinate> path = navigationManager.getPath(start, end, testUnit);
        
        assertNotNull("Path should not be null", path);
        assertFalse("Path should not be empty", path.isEmpty());
        
        // Verify the path length is reasonable
        double pathLength = 0;
        for(int i = 0; i < path.size() - 1; i++) {
            pathLength += path.get(i).distanceFrom(path.get(i + 1));
        }
        
        assertTrue("Path should not exceed max calculation distance", 
            pathLength <= NavigationManager.maxCalculationDistance * 1.5); // Allow some buffer
    }
}

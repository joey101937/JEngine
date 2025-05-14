package Framework;

import Framework.CoreLoop.QuadTree;
import Framework.UtilityObjects.BlockObject;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class QuadTreeTest {
    
    @Test
    public void testInsertAndRetrieveByArea() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Create test blocks at different locations
        BlockObject block1 = new BlockObject(new Coordinate(100, 100), 20, 20);
        BlockObject block2 = new BlockObject(new Coordinate(500, 500), 20, 20);
        BlockObject block3 = new BlockObject(new Coordinate(900, 900), 20, 20);
        
        block1.setColor(Color.RED);
        block2.setColor(Color.GREEN);
        block3.setColor(Color.BLUE);
        
        // Insert blocks into quad tree
        qt.insert(block1);
        qt.insert(block2);
        qt.insert(block3);
        
        // Test retrieving objects in top-left quadrant
        List<GameObject2> topLeft = qt.retrieve(new Rectangle(0, 0, 250, 250));
        assertEquals("Should find one object in top-left", 1, topLeft.size());
        assertTrue("Should contain block1", topLeft.contains(block1));
        
        // Test retrieving objects in center
        List<GameObject2> center = qt.retrieve(new Rectangle(400, 400, 200, 200));
        assertEquals("Should find one object in center", 1, center.size());
        assertTrue("Should contain block2", center.contains(block2));
        
        // Test retrieving objects in bottom-right
        List<GameObject2> bottomRight = qt.retrieve(new Rectangle(800, 800, 200, 200));
        assertEquals("Should find one object in bottom-right", 1, bottomRight.size());
        assertTrue("Should contain block3", bottomRight.contains(block3));
    }
    
    @Test
    public void testRetrieveByRadius() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Create a cluster of blocks
        BlockObject center = new BlockObject(new Coordinate(500, 500), 10, 10);
        BlockObject near1 = new BlockObject(new Coordinate(520, 520), 10, 10);
        BlockObject near2 = new BlockObject(new Coordinate(480, 480), 10, 10);
        BlockObject far = new BlockObject(new Coordinate(700, 700), 10, 10);
        
        qt.insert(center);
        qt.insert(near1);
        qt.insert(near2);
        qt.insert(far);
        
        // Test small radius (should only get center)
        List<GameObject2> smallRadius = qt.retrieve(new Coordinate(500, 500), 15);
        assertEquals("Small radius should find 1 object", 1, smallRadius.size());
        assertTrue("Should contain center block", smallRadius.contains(center));
        
        // Test medium radius (should get center and near blocks)
        List<GameObject2> mediumRadius = qt.retrieve(new Coordinate(500, 500), 50);
        assertEquals("Medium radius should find 3 objects", 3, mediumRadius.size());
        assertTrue("Should contain all near blocks", 
                mediumRadius.contains(center) && 
                mediumRadius.contains(near1) && 
                mediumRadius.contains(near2));
        
        // Test large radius (should get all blocks)
        List<GameObject2> largeRadius = qt.retrieve(new Coordinate(500, 500), 300);
        assertEquals("Large radius should find all objects", 4, largeRadius.size());
    }
    
    @Test
    public void testClear() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Add several blocks
        for(int i = 0; i < 20; i++) {
            qt.insert(new BlockObject(new Coordinate(i * 50, i * 50), 10, 10));
        }
        
        // Verify objects were added
        List<GameObject2> beforeClear = qt.retrieve(new Rectangle(0, 0, 1000, 1000));
        assertEquals("Should have 20 objects before clear", 20, beforeClear.size());
        
        // Clear the tree
        qt.clear();
        
        // Verify objects were removed
        List<GameObject2> afterClear = qt.retrieve(new Rectangle(0, 0, 1000, 1000));
        assertEquals("Should have 0 objects after clear", 0, afterClear.size());
    }
    
    @Test
    public void testMaxCapacityAndSplit() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Add more than MAX_OBJECTS blocks to same location to force split
        for(int i = 0; i < 15; i++) {
            BlockObject block = new BlockObject(new Coordinate(100, 100), 10, 10);
            qt.insert(block);
        }
        
        // Test that objects are still retrievable after split
        List<GameObject2> retrieved = qt.retrieve(new Rectangle(50, 50, 101, 101));
        assertEquals("Should find all 15 objects after split", 15, retrieved.size());
    }

    @Test
    public void testRemove() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Create and insert test objects
        BlockObject block1 = new BlockObject(new Coordinate(100, 100), 20, 20);
        BlockObject block2 = new BlockObject(new Coordinate(500, 500), 20, 20);
        BlockObject block3 = new BlockObject(new Coordinate(900, 900), 20, 20);
        
        qt.insert(block1);
        qt.insert(block2);
        qt.insert(block3);
        
        // Test removing an existing object
        assertTrue("Should successfully remove block2", qt.remove(block2));
        
        // Verify block2 was removed
        List<GameObject2> retrieved = qt.retrieve(new Rectangle(400, 400, 200, 200));
        assertEquals("Should find no objects after removal", 0, retrieved.size());
        
        // Test removing a non-existent object
        BlockObject nonExistent = new BlockObject(new Coordinate(300, 300), 20, 20);
        assertFalse("Should return false when removing non-existent object", qt.remove(nonExistent));
        
        // Verify other objects still exist
        List<GameObject2> allRemaining = qt.retrieve(new Rectangle(0, 0, 1000, 1000));
        assertEquals("Should still have 2 remaining objects", 2, allRemaining.size());
        assertTrue("Should still contain block1", allRemaining.contains(block1));
        assertTrue("Should still contain block3", allRemaining.contains(block3));
    }
    
    @Test
    public void testLargeScaleSpatialQueries() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 10000, 10000));
        BlockObject[] blocks = new BlockObject[400];
        
        // Create 400 blocks at random positions
        for(int i = 0; i < blocks.length; i++) {
            int x = (int)(Math.random() * 9900); // Leave room for block width
            int y = (int)(Math.random() * 9900);
            blocks[i] = new BlockObject(new Coordinate(x, y), 20, 20);
            blocks[i].setColor(new Color((int)(Math.random() * 0xFFFFFF))); // Random color
            qt.insert(blocks[i]);
        }
        
        // Test finding objects in different regions
        
        // Test center region
        List<GameObject2> centerGroup = qt.retrieve(new Coordinate(5000, 5000), 1000);
        System.out.println("Found " + centerGroup.size() + " objects in center region");
        assertTrue("Should find some objects in center", centerGroup.size() > 0);
        
        // Test corner regions
        List<GameObject2> topLeft = qt.retrieve(new Rectangle(0, 0, 2000, 2000));
        List<GameObject2> bottomRight = qt.retrieve(new Rectangle(8000, 8000, 2000, 2000));
        System.out.println("Found " + topLeft.size() + " objects in top-left corner");
        System.out.println("Found " + bottomRight.size() + " objects in bottom-right corner");
        
        // Test finding nearby objects for each block
        int totalGroups = 0;
        for(BlockObject block : blocks) {
            List<GameObject2> nearby = qt.retrieve(block.getPixelLocation(), 500);
            if(nearby.size() > 5) { // Found a group of at least 6 objects
                totalGroups++;
                System.out.println("Found group of " + nearby.size() + " objects at " + 
                                 block.getPixelLocation().x + "," + block.getPixelLocation().y);
            }
        }
        System.out.println("Found " + totalGroups + " groups with 6+ objects within 500 pixels");
        
        // Test overlapping queries
        List<GameObject2> area1 = qt.retrieve(new Rectangle(2000, 2000, 3000, 3000));
        List<GameObject2> area2 = qt.retrieve(new Rectangle(3000, 3000, 3000, 3000));
        List<GameObject2> combinedArea = qt.retrieve(new Rectangle(2000, 2000, 4000, 4000));
        
        System.out.println("Area1: " + area1.size() + " objects");
        System.out.println("Area2: " + area2.size() + " objects");
        System.out.println("Combined overlapping area: " + combinedArea.size() + " objects");
        
        assertTrue("Combined area should find more objects than individual areas",
                  combinedArea.size() > area1.size() && combinedArea.size() > area2.size());
    }
}

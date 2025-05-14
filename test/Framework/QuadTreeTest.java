package Framework;

import Framework.CoreLoop.QuadTree;
import Framework.UtilityObjects.BlockObject;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;
import static org.junit.Assert.assertEquals;
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
}

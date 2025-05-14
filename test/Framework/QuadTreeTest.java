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
        
        // Test circular pattern selections
        int[] testRadii = {250, 500, 1000, 2000};
        Coordinate center = new Coordinate(5000, 5000);
        
        // Test concentric circles from center
        for(int radius : testRadii) {
            List<GameObject2> objectsInCircle = qt.retrieve(center, radius);
            System.out.println("Found " + objectsInCircle.size() + " objects within " + radius + " radius of center");
            
            // Verify that all objects are actually within the radius
            for(GameObject2 obj : objectsInCircle) {
                double distance = center.distanceFrom(obj.getPixelLocation());
                assertTrue("Object should be within radius", distance <= radius);
            }
        }
        
        // Test overlapping circles
        int radius = 1000;
        Coordinate[] centers = {
            new Coordinate(4000, 4000),
            new Coordinate(4500, 4500),
            new Coordinate(5000, 5000)
        };
        
        List<GameObject2>[] circleResults = new List[centers.length];
        for(int i = 0; i < centers.length; i++) {
            circleResults[i] = qt.retrieve(centers[i], radius);
            System.out.println("Circle " + (i+1) + " contains " + circleResults[i].size() + " objects");
        }
        
        // Verify overlapping circles have some common objects
        boolean hasCommonObjects = false;
        for(GameObject2 obj : circleResults[0]) {
            if(circleResults[1].contains(obj) || circleResults[2].contains(obj)) {
                hasCommonObjects = true;
                break;
            }
        }
        assertTrue("Overlapping circles should have common objects", hasCommonObjects);
        
        // Test circular selection at corners
        Coordinate[] corners = {
            new Coordinate(0, 0),
            new Coordinate(10000, 0),
            new Coordinate(0, 10000),
            new Coordinate(10000, 10000)
        };
        
        for(int i = 0; i < corners.length; i++) {
            List<GameObject2> cornerObjects = qt.retrieve(corners[i], 1500);
            System.out.println("Corner " + (i+1) + " circle contains " + cornerObjects.size() + " objects");
            
            // Verify distances for corner objects
            for(GameObject2 obj : cornerObjects) {
                double distance = corners[i].distanceFrom(obj.getPixelLocation());
                assertTrue("Corner object should be within radius", distance <= 1500);
            }
        }
        
        // Test dense vs sparse areas
        int smallRadius = 300;
        int samplingPoints = 20;
        int maxObjects = 0;
        int minObjects = Integer.MAX_VALUE;
        
        for(int i = 0; i < samplingPoints; i++) {
            Coordinate randomPoint = new Coordinate(
                (int)(Math.random() * 9900),
                (int)(Math.random() * 9900)
            );
            List<GameObject2> localObjects = qt.retrieve(randomPoint, smallRadius);
            maxObjects = Math.max(maxObjects, localObjects.size());
            minObjects = Math.min(minObjects, localObjects.size());
        }
        
        System.out.println("Density analysis in " + smallRadius + "px radius circles:");
        System.out.println("Most dense area: " + maxObjects + " objects");
        System.out.println("Least dense area: " + minObjects + " objects");
        assertTrue("Should find varying densities", maxObjects > minObjects);
    }
    
    @Test
    public void testCopyFunction() {
        QuadTree original = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Create test blocks in different quadrants
        BlockObject block1 = new BlockObject(new Coordinate(100, 100), 20, 20);
        BlockObject block2 = new BlockObject(new Coordinate(900, 100), 20, 20);
        BlockObject block3 = new BlockObject(new Coordinate(100, 900), 20, 20);
        BlockObject block4 = new BlockObject(new Coordinate(900, 900), 20, 20);
        
        // Add enough blocks at each location to force splits
        for(int i = 0; i < 12; i++) {
            original.insert(new BlockObject(new Coordinate(100, 100), 20, 20));
            original.insert(new BlockObject(new Coordinate(900, 100), 20, 20));
            original.insert(new BlockObject(new Coordinate(100, 900), 20, 20));
            original.insert(new BlockObject(new Coordinate(900, 900), 20, 20));
        }
        
        // Create the copy
        QuadTree copy = original.copy();
        
        // Test that both trees return the same results for various queries
        
        // Test corner regions
        List<GameObject2> originalTopLeft = original.retrieve(new Rectangle(0, 0, 500, 500));
        List<GameObject2> copyTopLeft = copy.retrieve(new Rectangle(0, 0, 500, 500));
        assertEquals("Top-left quadrant should have same number of objects", 
                    originalTopLeft.size(), copyTopLeft.size());
        
        List<GameObject2> originalBottomRight = original.retrieve(new Rectangle(500, 500, 500, 500));
        List<GameObject2> copyBottomRight = copy.retrieve(new Rectangle(500, 500, 500, 500));
        assertEquals("Bottom-right quadrant should have same number of objects", 
                    originalBottomRight.size(), copyBottomRight.size());
        
        // Test radius searches
        List<GameObject2> originalRadius = original.retrieve(new Coordinate(100, 100), 50);
        List<GameObject2> copyRadius = copy.retrieve(new Coordinate(100, 100), 50);
        assertEquals("Radius search should return same number of objects", 
                    originalRadius.size(), copyRadius.size());
        
        // Test that modifications to copy don't affect original
        BlockObject newBlock = new BlockObject(new Coordinate(500, 500), 20, 20);
        copy.insert(newBlock);
        
        List<GameObject2> originalAll = original.retrieve(new Rectangle(0, 0, 1000, 1000));
        List<GameObject2> copyAll = copy.retrieve(new Rectangle(0, 0, 1000, 1000));
        assertEquals("Copy should have one more object after insertion", 
                    originalAll.size() + 1, copyAll.size());
        
        // Test that removing from copy doesn't affect original
        copy.remove(newBlock);
        copyAll = copy.retrieve(new Rectangle(0, 0, 1000, 1000));
        assertEquals("Trees should have same number of objects after removal", 
                    originalAll.size(), copyAll.size());
    }
    
    @Test
    public void testSize() {
        QuadTree qt = new QuadTree(0, new Rectangle(0, 0, 1000, 1000));
        
        // Test empty tree
        assertEquals("Empty tree should have size 0", 0, qt.size());
        
        // Add objects to root node
        BlockObject block1 = new BlockObject(new Coordinate(100, 100), 20, 20);
        BlockObject block2 = new BlockObject(new Coordinate(150, 150), 20, 20);
        qt.insert(block1);
        qt.insert(block2);
        assertEquals("Tree should have size 2", 2, qt.size());
        
        // Add enough objects at one point to force splits
        for(int i = 0; i < 12; i++) {
            qt.insert(new BlockObject(new Coordinate(900, 900), 20, 20));
        }
        
        // Total should be: 2 (from earlier) + 12 (new blocks) = 14
        assertEquals("Tree should have size 14 after splitting", 14, qt.size());
        
        // Remove an object and verify size decreases
        qt.remove(block1);
        assertEquals("Tree should have size 13 after removal", 13, qt.size());
        
        // Clear the tree
        qt.clear();
        assertEquals("Cleared tree should have size 0", 0, qt.size());
    }
}

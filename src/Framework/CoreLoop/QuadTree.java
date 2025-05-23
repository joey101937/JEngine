package Framework.CoreLoop;

import Framework.Coordinate;
import Framework.GameObject2;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuadTree implements Serializable{
    private static final int MAX_OBJECTS = 50;
    private static final int MAX_LEVELS = 6;
    
    private int level;
    private List<GameObject2> objects;
    public Rectangle bounds;
    public QuadTree[] nodes;

    public QuadTree(int level, Rectangle bounds) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.bounds = bounds;
        this.nodes = new QuadTree[4];
    }

    public void clear() {
        objects.clear();
        nodes = new QuadTree[4];
    }

    private void split() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int y = bounds.y;

        nodes[0] = new QuadTree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new QuadTree(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new QuadTree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new QuadTree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    private int getIndex(GameObject2 object) {
        Coordinate loc = object.getPixelLocation();
        int verticalMidpoint = bounds.x + (bounds.width / 2);
        int horizontalMidpoint = bounds.y + (bounds.height / 2);

        boolean topQuadrant = loc.y < horizontalMidpoint;
        boolean bottomQuadrant = loc.y >= horizontalMidpoint;
        
        if (loc.x < verticalMidpoint) {
            if (topQuadrant) return 1;
            if (bottomQuadrant) return 2;
        } else if (loc.x >= verticalMidpoint) {
            if (topQuadrant) return 0;
            if (bottomQuadrant) return 3;
        }
        
        return -1;
    }

    public void insert(GameObject2 object) {
        if (nodes[0] != null) {
            int index = getIndex(object);
            if (index != -1) {
                nodes[index].insert(object);
                return;
            }
        }

        objects.add(object);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    public ArrayList<GameObject2> retrieve(Rectangle area) {
        ArrayList<GameObject2> returnObjects = new ArrayList<>();
        if (nodes[0] != null) {
            // Check each node that intersects with the search area
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null && nodes[i].bounds.intersects(area)) {
                    returnObjects.addAll(nodes[i].retrieve(area));
                }
            }
        }

        // Add objects from current node that intersect with search area
        for (GameObject2 obj : objects) {
            Coordinate loc = obj.getPixelLocation();
            if (area.contains(loc.x, loc.y)) {
                returnObjects.add(obj);
            }
        }
        
        return returnObjects;
    }

    public ArrayList<GameObject2> retrieve(Coordinate point, int radius) {
        Rectangle area = new Rectangle(point.x - radius, point.y - radius, radius * 2, radius * 2);
        ArrayList<GameObject2> possibleObjects = retrieve(area);
        ArrayList<GameObject2> withinRadius = new ArrayList<>();
        
        for (GameObject2 obj : possibleObjects) {
            if (point.distanceFrom(obj.getPixelLocation()) <= radius) {
                withinRadius.add(obj);
            }
        }
        
        return withinRadius;
    }

    private int getIndex(Rectangle area) {
        int verticalMidpoint = bounds.x + (bounds.width / 2);
        int horizontalMidpoint = bounds.y + (bounds.height / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant = (area.y + area.height < horizontalMidpoint);
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (area.y > horizontalMidpoint);
        // Object can completely fit within the left quadrants
        boolean leftQuadrant = (area.x + area.width < verticalMidpoint);
        // Object can completely fit within the right quadrants
        boolean rightQuadrant = (area.x > verticalMidpoint);

        if (leftQuadrant) {
            if (topQuadrant) return 1;
            if (bottomQuadrant) return 2;
        }
        else if (rightQuadrant) {
            if (topQuadrant) return 0;
            if (bottomQuadrant) return 3;
        }

        return -1;
    }

    /**
     * Creates a deep copy of this QuadTree
     * @return A new QuadTree with copied contents
     */
    public QuadTree copy() {
        QuadTree copy = new QuadTree(this.level, new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
        copy.objects = new ArrayList<>(this.objects);
        
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                copy.nodes[i] = nodes[i].copy();
            }
        }
        
        return copy;
    }

    /**
     * Removes the specified object from the QuadTree
     * @param object The object to remove
     * @return true if the object was found and removed, false otherwise
     */
    public boolean remove(GameObject2 object) {  
        // First check if the object belongs in a subnode
        if (nodes[0] != null) {
            int index = getIndex(object);
            if (index != -1) {
                return nodes[index].remove(object);
            }
        }

        // If we reach here, check if the object is in the current node's list
        return objects.remove(object);
    }
    
    public int size() {
        int total = objects.size();
        
        // Add sizes from all child nodes
        for (QuadTree node : nodes) {
            if (node != null) {
                total += node.size();
            }
        }
        
        return total;
    }
}

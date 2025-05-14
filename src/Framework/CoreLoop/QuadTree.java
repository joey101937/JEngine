package Framework.CoreLoop;

import Framework.Coordinate;
import Framework.GameObject2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;
    
    private int level;
    private List<GameObject2> objects;
    private Rectangle bounds;
    private QuadTree[] nodes;

    public QuadTree(int level, Rectangle bounds) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.bounds = bounds;
        this.nodes = new QuadTree[4];
    }

    public void clear() {
        objects.clear();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
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

    public List<GameObject2> retrieve(Rectangle area) {
        List<GameObject2> returnObjects = new ArrayList<>();
        
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

    public List<GameObject2> retrieve(Coordinate point, int radius) {
        Rectangle area = new Rectangle(point.x - radius, point.y - radius, radius * 2, radius * 2);
        List<GameObject2> possibleObjects = retrieve(area);
        List<GameObject2> withinRadius = new ArrayList<>();
        
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
}

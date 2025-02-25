package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import Framework.Main;
import GameDemo.RTSDemo.ControlGroupHelper;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 * @author guydu
 */
public class NavigationManager extends IndependentEffect {

    public static int updateInterval = Main.ticksPerSecond / 10;

    public Game game;
    public TileMap tileMap;
    public LinkedList<Coordinate> path = new LinkedList<>();

    public NavigationManager(Game g) {
        game = g;
        tileMap = new TileMap(g.getWorldWidth(), g.getWorldHeight());
    }

    @Override
    public void render(Graphics2D g) {
         for(int x = 0; x < 100; x++) {
             for(int y = 0 ; y < 100; y++) {
                g.setColor(tileMap.tileGrid[x][y].isBlocked() ? Color.RED : Color.BLUE);
                g.fillRect(x*Tile.tileSize, y*Tile.tileSize, Tile.tileSize, Tile.tileSize);
             }
         }
         g.setColor(Color.YELLOW);
         if(path != null && !path.isEmpty()) {
             path.forEach(coord -> {
                 g.fillRect(coord.x-10, coord.y-10, 20, 20);
             });
         }
    }

    @Override
    public void tick() {
        if (game.getGameTickNumber() % updateInterval != 0) {
            return;
        }
        tileMap.updateOccupationMap(game);
        var groupOne = ControlGroupHelper.groups.getOrDefault(1, null);
        var groupTwo = ControlGroupHelper.groups.getOrDefault(2, null);
        if(groupOne != null && !groupOne.isEmpty() && groupTwo != null && !groupTwo.isEmpty()) {
            path = getPath(new Coordinate(40,40), new Coordinate(400, 60) );
        }
    }

    public LinkedList<Coordinate> getPath(Coordinate startCoord, Coordinate endCoord) {
        Tile start = tileMap.getTileAtLocation(startCoord);
        Tile originalStart = tileMap.getTileAtLocation(startCoord);
        Tile goal = tileMap.getTileAtLocation(endCoord);
        Tile originalGoal = tileMap.getTileAtLocation(endCoord);
        boolean adjustedGoal = false;
        boolean adjustedStart = false;
        
        
        if(goal.isBlocked()) {
            adjustedGoal = true;
            goal = tileMap.getClosestOpenTile(endCoord, startCoord);
        }
        
        if(start.isBlocked()) {
            adjustedStart = true;
            start = tileMap.getClosestOpenTile(startCoord, endCoord);
        }
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Tile, Node> allNodes = new HashMap<>();
        
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);
        
         while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.tile == goal) {
                var path = reconstructPath(current);
                path.push(endCoord);
                return path;
            }

            for (Tile neighbor : tileMap.getNeighbors(current.tile.getGridLocation())) {
                if (neighbor.isBlocked()) continue;

                double gScore = current.g + 1;
                Node neighborNode = allNodes.get(neighbor);

                if (neighborNode == null || gScore < neighborNode.g) {
                    neighborNode = new Node(neighbor, current, gScore, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }

        System.out.println("no path found");
        LinkedList<Coordinate> out = new LinkedList<>();
        out.add(endCoord);
        return out;
    }
    
    
    private LinkedList<Coordinate> reconstructPath(Node node) {
        LinkedList<Coordinate> path = new LinkedList<>();
        while (node != null) {
            path.add(node.tile.getCenterPoint());
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
    
    private double heuristic(Tile a, Tile b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static class Node {

        Tile tile;
        Node parent;
        double g, h, f;

        public Node(Tile tile, Node parent, double g, double h) {
            this.tile = tile;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
    
    @Override
    public int getZLayer(){
        return -90;
    }

}

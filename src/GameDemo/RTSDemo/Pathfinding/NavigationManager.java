package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author guydu
 */
public class NavigationManager extends IndependentEffect {

    public static int updateInterval = Main.ticksPerSecond / 10;
    public static ExecutorService unitPathingService = Executors.newFixedThreadPool(200);

    public Game game;
    public TileMap tileMap;
    public ArrayList<Coordinate> path = new ArrayList<>();

    public NavigationManager(Game g) {
        game = g;
        tileMap = new TileMap(g.getWorldWidth(), g.getWorldHeight());
    }

    @Override
    public void render(Graphics2D g) {
//         for(int x = 0; x < 100; x++) {
//             for(int y = 0 ; y < 100; y++) {
//                g.setColor(tileMap.tileGrid[x][y].isBlocked() ? Color.RED : Color.BLUE);
//                if(!tileMap.tileGrid[x][y].isBlocked()) continue;
//                g.fillRect(x*Tile.tileSize, y*Tile.tileSize, Tile.tileSize, Tile.tileSize);
//             }
//         }
//        g.setColor(Color.RED);
//        for(Tile tile : tileMap.occupiedMap.keySet()) {
//             g.fillRect(tile.x*Tile.tileSize, tile.y*Tile.tileSize, Tile.tileSize, Tile.tileSize);
//        }
//         g.setColor(Color.YELLOW);
//         if(path != null && !path.isEmpty()) {
//             path.forEach(coord -> {
//                 g.fillRect(coord.x-5, coord.y-5, 10, 10);
//             });
//         }
    }

    @Override
    public void tick() {
        if (game.getGameTickNumber() % updateInterval != 0) {
            return;
        }
        tileMap.updateOccupationMap(game);
        Collection<Future<?>> pathingTasks = new ArrayList<>();
        for(GameObject2 go : game.getAllObjects()) {
            if(go instanceof RTSUnit unit && !unit.isCloseEnoughToDesired()) {
                pathingTasks.add(unitPathingService.submit(() -> {
                    unit.updateWaypoints();
                    return true;
                }));
            }
        }
        Handler.waitForAllJobs(pathingTasks);
//        var groupOne = ControlGroupHelper.groups.getOrDefault(1, null);
//        var groupTwo = ControlGroupHelper.groups.getOrDefault(2, null);
//        if(groupOne != null && !groupOne.isEmpty() && groupTwo != null && !groupTwo.isEmpty()) {
//           // path = getPath(new Coordinate(40,40), new Coordinate(400, 60) );
//            System.out.println(groupOne.get(0).getWidth());
//           path = getPath(groupOne.get(0).getPixelLocation(), groupTwo.get(0).getPixelLocation() );
//           for(int i = path.size()-1; i > 0; i--) {
//               Coordinate cur = path.get(i);
//               if(cur.distanceFrom(groupTwo.get(0).getPixelLocation()) < groupTwo.get(0).getWidth()) {
//                   path.removeLast();
//               }
//               else break;
//           }
//           groupTwo.get(0).setDesiredLocation(path.getLast());
//        }
    }

    public List<Coordinate> getPath(Coordinate startCoord, Coordinate endCoord) {
        Tile start = tileMap.getTileAtLocation(startCoord);
        Tile goal = tileMap.getTileAtLocation(endCoord);
        
        if(startCoord.distanceFrom(endCoord) > 1000) {
            endCoord = Coordinate.nearestPointOnCircle(startCoord, endCoord, 1000);
             goal = tileMap.getTileAtLocation(endCoord);
        }
        
        if(goal == null) {
            System.out.println("null goal");
            goal = tileMap.getClosestOpenTile(endCoord, startCoord);
        }
        
        
        if(goal.isBlocked()) {
            goal = tileMap.getClosestOpenTile(endCoord, startCoord);
        }
        
        if(start.isBlocked()) {
            start = tileMap.getClosestOpenTile(startCoord, endCoord);
        }
        
        if(start == null || goal == null) {
            System.out.println("no path found (null start or end)");
            ArrayList<Coordinate> out = new ArrayList<>();
            out.add(endCoord);
            return out;
        }
        
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Tile, Node> allNodes = new HashMap<>();
        int numTraversed = 0;
        
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);
        
         while (!openSet.isEmpty() && numTraversed < 8000) {
            Node current = openSet.poll();
            numTraversed++;
            
            if (current.tile == goal) {
                var path = reconstructPath(current);
                path.add(endCoord);
                return smoothenPath(path);
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

        // System.out.println("no path found");
        ArrayList<Coordinate> out = new ArrayList<>();
        out.add(endCoord);
        return out;
    }
    
    
    private List<Coordinate> smoothenPath(ArrayList<Coordinate> path) {
        int farLimit = Math.min(30, path.size() - 1);
        Coordinate goalFar = path.get(farLimit);
        
        if(tileMap.noneBlocked(tileMap.getTilesIntersectingLine(path.get(0), goalFar))) {
            return path.subList(farLimit, path.size()-1);
        }
        
        return path;
    };
    
    private ArrayList<Coordinate> reconstructPath(Node node) {
        ArrayList<Coordinate> path = new ArrayList<>();
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

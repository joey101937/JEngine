package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SelectionBoxEffect;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public NavigationManager(Game g) {
        game = g;
        tileMap = new TileMap(g.getWorldWidth(), g.getWorldHeight());
    }

    @Override
    public void render(Graphics2D g) {

    }

    @Override
    public void tick() {
        if (game.getGameTickNumber() % updateInterval != 0) {
            return;
        }
        tileMap.refreshOccupationmaps(game);
        
        Collection<Future<?>> pathingTasks = new ArrayList<>();
        for (GameObject2 go : game.getAllObjects()) {
            if (go instanceof RTSUnit unit && !unit.isCloseEnoughToDesired()) {
                pathingTasks.add(unitPathingService.submit(() -> {
                    unit.updateWaypoints();
                    return true;
                }));
            }
        }
        Handler.waitForAllJobs(pathingTasks);
    }

    public List<Coordinate> getPath(Coordinate startCoord, Coordinate endCoord, TileMap tileMap, RTSUnit self) {
        Tile start = tileMap.getTileAtLocation(startCoord);
        Tile goal = tileMap.getTileAtLocation(endCoord);
        String pathingSignature = self.getPathingSignature();
        int maxCalculationDistance = 1600;

        if (startCoord.distanceFrom(endCoord) > maxCalculationDistance) {
            endCoord = Coordinate.nearestPointOnCircle(startCoord, endCoord, maxCalculationDistance);
            goal = tileMap.getTileAtLocation(endCoord);
        }

        if (goal == null) {
            goal = tileMap.getClosestOpenTile(endCoord, startCoord, pathingSignature);
            if(goal == null) {
                // no nearby open tiles, just return the end goal directly
                ArrayList<Coordinate> out = new ArrayList<>();
                out.add(endCoord);
                return out;
            }
        }
        
        if (start.isBlocked(pathingSignature)) {
            start = tileMap.getClosestOpenTile(startCoord, endCoord, pathingSignature);
        }

        if (goal.isBlocked(pathingSignature)) {
            goal = tileMap.getClosestOpenTile(endCoord, startCoord, pathingSignature);
            if(self != null && Coordinate.distanceBetween(self.getPixelLocation(), endCoord) <= (200 + self.getWidth()/2)) {
                ArrayList<Coordinate> out = new ArrayList<>();
                out.add(endCoord);
                return out;
            }
        }      

        if (start == null || goal == null) {
            if(self.isSelected()) System.out.println("no path found (null start or end)");
            ArrayList<Coordinate> out = new ArrayList<>();
            out.add(endCoord);
            return out;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> {
            // First compare by f value
            int fCompare = Double.compare(a.f, b.f);
            if (fCompare != 0) return fCompare;
            
            // If f values are equal, compare by h value
            int hCompare = Double.compare(a.h, b.h);
            if (hCompare != 0) return hCompare;
            
            // If h values are equal, compare by tile x coordinate
            int xCompare = Integer.compare(a.tile.x, b.tile.x);
            if (xCompare != 0) return xCompare;
            
            // Finally compare by tile y coordinate
            return Integer.compare(a.tile.y, b.tile.y);
        });
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
                return smoothenPath(path, tileMap, self);
            }

            for (Tile neighbor : tileMap.getNeighbors(current.tile.getGridLocation())) {
                if (neighbor.isBlocked(pathingSignature)) {
                    continue;
                }

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

    private List<Coordinate> smoothenPath(List<Coordinate> path, TileMap tileMap, RTSUnit self) {
        String pathingSignature = self.getPathingSignature();
        
        RTSUnit selectedUnit = SelectionBoxEffect.selectedUnits.size() > 0 ? (RTSUnit)SelectionBoxEffect.selectedUnits.toArray()[0] : null;
        
        if(RTSGame.navigationManager.tileMap.getTileAtLocation(self.getPixelLocation()).isBlocked(self.getPathingSignature())) {
            return path;
        }
        
        if(tileMap.getTileAtLocation(path.get(0)).isBlocked(pathingSignature)) return path;
        
        int spacing = (tileMap.occupationMaps.get(pathingSignature).getPadding() + Tile.tileSize)/2;
        Coordinate start = path.get(0);

        int farLimit = Math.min(60, path.size() - 1);
        Coordinate goalFar = path.get(farLimit);

        int medLimit = Math.min(30, path.size() - 1);
        Coordinate goalMed = path.get(medLimit);

        int nearLimit = Math.min(9, path.size() - 1);
        Coordinate goalNear = path.get(nearLimit);

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalFar, self.getWidth()), pathingSignature)) {
            if(self == selectedUnit) System.out.println("using farLimit shortcut");
            return path.subList(farLimit, path.size() - 1);
        }

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalMed, self.getWidth()), pathingSignature)) {
             if(self == selectedUnit) System.out.println("using medLimit shortcut");
            return path.subList(medLimit, path.size() - 1);
        }

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalNear, self.getWidth()), pathingSignature)) {
            if(self == selectedUnit) System.out.println("using nearlimit shortcut");
            return path.subList(nearLimit, path.size() - 1);
        }

        return path;
    }
    
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
    public int getZLayer() {
        return -90;
    }

}

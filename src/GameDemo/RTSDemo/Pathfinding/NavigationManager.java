package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SelectionBoxEffect;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    // private static final long UPDATE_INTERVAL_NANOS = 100_000_000; // 100ms in nanoseconds
    public static int updateInterval = Main.ticksPerSecond / 10;
    public static ExecutorService unitPathingService = Executors.newFixedThreadPool(200);

    public Game game;
    public TileMap tileMapNormal;
    public TileMap tileMapFine;
    public TileMap tileMapLarge;
    public TileMap tileMapGiantTerrain;

    public NavigationManager(Game g) {
        game = g;
        tileMapNormal = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeNormal);
        tileMapFine = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeFine);
        tileMapLarge = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeLarge);
        tileMapLarge = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeGiantTerrain);
    }

    @Override
    public void render(Graphics2D g) {
        HashSet<String> pathingSignatures = new HashSet<>();
        for(GameObject2 go : game.getAllObjects()) {
            if(go instanceof RTSUnit unit) {
                    pathingSignatures.add(unit.getPathingSignature());
            }
        }
        var camLoc = RTSGame.game.getCamera().getWorldRenderLocation().toCoordinate();
        g.drawString("ps: " + pathingSignatures.size(), camLoc.x + 10 , camLoc.y + 10);
        
        if(1==2) return;
        if(SelectionBoxEffect.selectedUnits.isEmpty()) return;
        RTSUnit unit = (RTSUnit)SelectionBoxEffect.selectedUnits.toArray()[0];
        TileMap tm = getTileMapBySize(unit.getNavTileSize());
        try {
        tm.getTilesNearPoint(Window.currentGame.getCameraCenterPosition(), 500).forEach(coord -> {
            Tile tile = tm.tileGrid[coord.x][coord.y];
            if(tile.isBlocked(unit.getPathingSignature())) g.setColor(Color.red);
            else g.setColor(Color.green);
            g.drawRect(tile.x * tm.tileSize, tile.y * tm.tileSize, tm.tileSize, tm.tileSize);
        });            
        } catch (Exception e) {
            
        }
    
    }

    @Override
    public void tick() {
        if (game.getGameTickNumber() % updateInterval != 0) {
            return;
        }
        
        Collection<Future<?>> refreshTasks = new ArrayList<>();
        refreshTasks.add(unitPathingService.submit(() -> {
            tileMapFine.refreshOccupationmaps(game);
        }));
        refreshTasks.add(unitPathingService.submit(() -> {
            tileMapLarge.refreshOccupationmaps(game);
        }));
         refreshTasks.add(unitPathingService.submit(() -> {
            tileMapNormal.refreshOccupationmaps(game);
        }));
         
         // note not adding giantTerrain map here
        
        Handler.waitForAllJobs(refreshTasks);
        
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
    
    public TileMap getTileMapBySize (int size) {
        if(size == tileMapFine.tileSize) return tileMapFine;
        if(size == tileMapNormal.tileSize) return tileMapNormal;
        if(size == tileMapLarge.tileSize) return tileMapLarge;
        // not using giantTerrain
        return null;
    }

    public List<Coordinate> getPath(Coordinate startCoord, Coordinate endCoord, RTSUnit self) {
        
        if(1==2) {
            ArrayList<Coordinate> out = new ArrayList<>();
            out.add(endCoord);
            return out;
        }
        
        try {
        TileMap tileMap = getTileMapBySize(self.getNavTileSize());
        Tile start = tileMap.getTileAtLocation(startCoord);
        Tile goal = tileMap.getTileAtLocation(endCoord);
        String pathingSignature = self.getPathingSignature();
        int maxCalculationDistance = 1400;
        int maxCalculationAmount = 8000;

        if (startCoord.distanceFrom(endCoord) > maxCalculationDistance) {
            // update this calculation. If the end destination is too far away, we should first calculate a path using the 
            // giantTerrain tile map. that is a tilemap with very large tiles for easy compute. we will then use the path direction
            // to decide which area we want to have our shorter goal. the idea is that we can use this standard a* algorithm to pathfind only nearby obsticles while
            // following the giantTerrain path at a zoomed out levl until we get close enough to the destination that we can a* pathfind directly to the destination.
            // the giant terrain map will only block tiles due to terrain. The tilemap in this function will account for both terrain and other units. 
            
            // what we are doing now is a very simple version of this system where we simply use a circle to calculate intermediate destinations for the a* algo.
            // this works good without terrain but what if we need to get around a large lake? we need the large tiles to give us a general direction of where to go
            // to get around the lake.
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
            if(self != null && Coordinate.distanceBetween(self.getPixelLocation(), endCoord) <= (200 + self.getWidth()/6)) {
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

        while (!openSet.isEmpty() && numTraversed < maxCalculationAmount) {
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
        } catch (Exception e){
            e.printStackTrace();
        }
        
        // failed
        ArrayList<Coordinate> out = new ArrayList<>();
        out.add(endCoord);
        return out;
    }

    private List<Coordinate> smoothenPath(List<Coordinate> path, TileMap tileMap, RTSUnit self) {
        String pathingSignature = self.getPathingSignature();
        
        if(tileMap.getTileAtLocation(self.getPixelLocation()).isBlocked(self.getPathingSignature())) {
            return path;
        }
        
        if(tileMap.getTileAtLocation(path.get(0)).isBlocked(pathingSignature)) return path;
        
        int spacing = (tileMap.occupationMaps.get(pathingSignature).getPadding() + tileMap.tileSize)/2;
        Coordinate start = path.get(0);

        int farLimit = Math.min(60, path.size() - 1);
        Coordinate goalFar = path.get(farLimit);

        int medLimit = Math.min(30, path.size() - 1);
        Coordinate goalMed = path.get(medLimit);

        int nearLimit = Math.min(9, path.size() - 1);
        Coordinate goalNear = path.get(nearLimit);

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalFar, (int)(self.getWidth()*.75)), pathingSignature)) {
            return path.subList(farLimit, path.size() - 1);
        }

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalMed, (int)(self.getWidth()*.75)), pathingSignature)) {
            return path.subList(medLimit, path.size() - 1);
        }

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalNear, (int)(self.getWidth()*.75)), pathingSignature)) {
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

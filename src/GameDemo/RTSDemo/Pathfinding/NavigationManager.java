package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Window;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSInput;
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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author guydu
 */
public class NavigationManager extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    public static boolean displayPathingDebugInfo = true;
    public static int updateInterval = RTSGame.desiredTPS / 10;
    public static transient ExecutorService unitPathingService = reapingPool(200);

    /**
     * Bounded pool that spins up to {@code max} threads under load but lets all of
     * them (core included) die after 30s idle, so pathfinding threads clean
     * themselves up once no game is actively pathing instead of lingering for the
     * life of the process. Recreated lazily by execute() when the next task lands.
     */
    private static ExecutorService reapingPool(int max) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(max, max, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }
    public static int maxCalculationDistance = 1400;
    public static String SEPERATOR_GROUP = "seperator";

    public transient Game game;
    public transient TileMap tileMapNormal;
    public transient TileMap tileMapFine;
    public transient TileMap tileMapLarge;
    public transient TileMap tileMapGiantTerrain;

    public NavigationManager(Game g) {
        game = g;
        tileMapNormal = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeNormal);
        tileMapFine = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeFine);
        tileMapLarge = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeLarge);
        tileMapGiantTerrain = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeGiantTerrain);
    }

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;

        // Recreate TileMaps (these are transient and not serialized)
        tileMapNormal = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeNormal);
        tileMapFine = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeFine);
        tileMapLarge = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeLarge);
        tileMapGiantTerrain = new TileMap(g.getWorldWidth(), g.getWorldHeight(), Tile.tileSizeGiantTerrain);

        if (unitPathingService == null) {
            unitPathingService = reapingPool(200);
        }

        // Update static reference so other code uses the new deserialized instance
        RTSGame.navigationManager = this;
    }

    /**
     * Updates the static reference in RTSGame to point to this instance.
     * Called after deserialization.
     */
    private void updateStaticReference() {
        try {
            Class<?> rtsGameClass = Class.forName("GameDemo.RTSDemo.RTSGame");
            java.lang.reflect.Field navigationManagerField = rtsGameClass.getDeclaredField("navigationManager");
            navigationManagerField.setAccessible(true);
            navigationManagerField.set(null, this);
        } catch (Exception e) {
            System.err.println("Could not update NavigationManager static reference: " + e.getMessage());
        }
    }

    public int getNavTileSize(RTSUnit unit) {
        if (unit.isInPathingRestrictedMode()) {
            return Tile.tileSizeLarge;
        }
        int distance = (int) unit.distanceFrom(unit.getDesiredLocation());
        // if (distance < 600) return Tile.tileSizeFine;
        if (distance < 900) return Tile.tileSizeNormal;
        return Tile.tileSizeLarge;
    }

    @Override
    public void render(Graphics2D g) {
        HashSet<String> pathingSignatures = new HashSet<>();
        for (GameObject2 go : game.getAllObjects()) {
            if (go instanceof RTSUnit unit) {
                pathingSignatures.add(unit.getPathingSignature());
            }
        }
        var camLoc = game.getCamera().getWorldRenderLocation().toCoordinate();
        g.drawString("ps: " + pathingSignatures.size(), camLoc.x + 10, camLoc.y + 10);
        g.drawString("FPS: " + game.getCurrentFPS(), camLoc.x + 10, camLoc.y + 20);
        g.drawString("TPS: " + game.getCurrentTPS(), camLoc.x + 10, camLoc.y + 30);
        if(ExternalCommunicator.isMultiplayer){
            g.drawString("MP: " + (ExternalCommunicator.isResyncing ? "Resyncing" : (int)ExternalCommunicator.tickTimingOffset + " | " + RTSInput.getInputDelay()), camLoc.x + 10, camLoc.y + 40);
            g.drawString("Ping: " + ExternalCommunicator.currentPingMs, camLoc.x + 10, camLoc.y + 50);

        }        

        if (!displayPathingDebugInfo) {
            return;
        }
        if (SelectionBoxEffect.selectedUnits.isEmpty()) {
            return;
        }
        // renders visible tile grid that selected unit is using in center of screen
        RTSUnit unit = (RTSUnit) SelectionBoxEffect.selectedUnits.toArray()[0];
        TileMap tm = getTileMapBySize(getNavTileSize(unit));
        try {
            tm.getTilesNearPoint(Window.currentGame.getCameraCenterPosition(), 500).forEach(coord -> {
                Tile tile = tm.tileGrid[coord.x][coord.y];
                if (tile.isBlocked(unit.getPathingSignature())) {
                    g.setColor(Color.red);
                } else {
                    g.setColor(Color.green);
                }
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
        refreshTasks.add(unitPathingService.submit(() -> tileMapFine.refreshOccupationmaps(game)));
        refreshTasks.add(unitPathingService.submit(() -> tileMapLarge.refreshOccupationmaps(game)));
        refreshTasks.add(unitPathingService.submit(() -> tileMapNormal.refreshOccupationmaps(game)));
        refreshTasks.add(unitPathingService.submit(() -> tileMapGiantTerrain.refreshOccupationmaps(game)));
        // note not adding giantTerrain map here because that map only handles terrain.
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

    public TileMap getTileMapBySize(int size) {
        if (size == tileMapFine.tileSize) {
            return tileMapFine;
        }
        if (size == tileMapNormal.tileSize) {
            return tileMapNormal;
        }
        if (size == tileMapLarge.tileSize) {
            return tileMapLarge;
        }
        if (size == tileMapGiantTerrain.tileSize) {
            return tileMapGiantTerrain;
        }
        return null;
    }

    public List<Coordinate> getPath(Coordinate startCoord, Coordinate endCoord, RTSUnit self) {
//            System.out.println("getting path for " + self.ID);
        try {
            int maxCalculationAmount = self.isInPathingRestrictedMode() ? 500 : 2000;

            TileMap tileMap = getTileMapBySize(getNavTileSize(self));
            if (tileMap != null && !tileMap.occupationMaps.containsKey(self.getPathingSignature())) {
                System.out.println("getPath: occupation map not yet built for signature " + self.getPathingSignature() + " (unit " + self.ID + ")");
            }
            Tile start = tileMap.getTileAtLocation(startCoord);
            Tile goal = tileMap.getTileAtLocation(endCoord);
            String pathingSignature = self.getPathingSignature();
            if (startCoord.distanceFrom(endCoord) > maxCalculationDistance) {
                // Use giant terrain map for long-distance pathfinding
                // Get high-level path using giant terrain tiles
                List<Coordinate> giantPath = getTerrainPath(startCoord, endCoord, self);

                if (giantPath != null && !giantPath.isEmpty()) {
                    endCoord = calcIntermediateGoal(startCoord, giantPath, pathingSignature);
                    goal = tileMap.getTileAtLocation(endCoord);
                } else {
                    // Fall back to circle method if terrain path fails
                    endCoord = Coordinate.nearestPointOnCircle(startCoord, endCoord, maxCalculationDistance);
                    goal = tileMap.getTileAtLocation(endCoord);
                }
            }

            if (goal == null) {
                goal = tileMap.getClosestOpenTile(endCoord, startCoord, pathingSignature);
                if (goal == null) {
                    // no nearby open tiles, just return the end goal directly
                    ArrayList<Coordinate> out = new ArrayList<>();
                    out.add(endCoord);
                    return out;
                }
            }

            if (start == null) {
                System.out.println("getPath: start tile is null for unit " + self.ID + " at " + startCoord);
                ArrayList<Coordinate> out = new ArrayList<>();
                out.add(endCoord);
                return out;
            }

            if (start.isBlocked(pathingSignature)) {
                start = tileMap.getClosestOpenTile(startCoord, endCoord, pathingSignature);
            }
 
            if (goal.isBlocked(pathingSignature)) {
                goal = tileMap.getClosestOpenTile(endCoord, startCoord, pathingSignature);
                if (self != null && Coordinate.distanceBetween(self.getPixelLocation(), endCoord) <= (self.getWidth())) {
                    ArrayList<Coordinate> out = new ArrayList<>();
                    out.add(endCoord);
                    return out;
                }
            }

            if (start == null || goal == null) {
                if (self.isSelected()) {
                    System.out.println("no path found (null start or end)");
                }
                ArrayList<Coordinate> out = new ArrayList<>();
                out.add(endCoord);
                return out;
            }

//            System.out.println(self.ID + "start: "+ start.getGridLocation() + " end: " + goal.getGridLocation());
            PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> {
                // First compare by f value
                int fCompare = Double.compare(a.f, b.f);
                if (fCompare != 0) {
                    return fCompare;
                }

                // If f values are equal, compare by h value
                int hCompare = Double.compare(a.h, b.h);
                if (hCompare != 0) {
                    return hCompare;
                }

                // If h values are equal, compare by tile x coordinate
                int xCompare = Integer.compare(a.tile.x, b.tile.x);
                if (xCompare != 0) {
                    return xCompare;
                }

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

                    // Calculate movement cost - √2 for diagonal, 1 for cardinal
                    double moveCost = (neighbor.x != current.tile.x && neighbor.y != current.tile.y) ? Math.sqrt(2) : 1;
                    double gScore = current.g + moveCost;
                    Node neighborNode = allNodes.get(neighbor);

                    if (neighborNode == null || gScore < neighborNode.g) {
                        neighborNode = new Node(neighbor, current, gScore, heuristic(neighbor, goal));
                        allNodes.put(neighbor, neighborNode);
                        openSet.add(neighborNode);
                    }
                }
            }

            if(self.isSelected()) System.out.println("numTraversed capped out "+ numTraversed);
            ArrayList<Coordinate> out = new ArrayList<>();
            if(Coordinate.distanceBetween(startCoord, self.getDesiredLocation()) < 110) {
                out.add(endCoord);
            } else {
                // budget exhausted — return a partial path to whichever explored tile got closest to the goal
                Node bestNode = null;
                for (Node n : allNodes.values()) {
                    if (bestNode == null || n.h < bestNode.h
                            || (n.h == bestNode.h && (n.tile.x < bestNode.tile.x
                                || (n.tile.x == bestNode.tile.x && n.tile.y < bestNode.tile.y)))) {
                        bestNode = n;
                    }
                }
                if (bestNode != null) {
                    var partial = reconstructPath(bestNode);
                    partial.add(endCoord);
                    return smoothenPath(partial, tileMap, self);
                }
            }
            return out;
        } catch (Exception e) {
            System.out.println("exception in nag manager getPath for " + self);
            e.printStackTrace();
        }

        // failed
        ArrayList<Coordinate> out = new ArrayList<>();
        out.add(endCoord);
        return out;
    }

    private List<Coordinate> smoothenPath(List<Coordinate> path, TileMap tileMap, RTSUnit self) {
        String pathingSignature = self.getPathingSignature();

        Tile selfTile = tileMap.getTileAtLocation(self.getPixelLocation());
        if (selfTile == null) {
            System.out.println("smoothenPath: self tile is null for unit " + self.ID + " at " + self.getPixelLocation());
            return path;
        }
        if (selfTile.isBlocked(self.getPathingSignature())) {
            return path;
        }

        Tile firstTile = tileMap.getTileAtLocation(path.get(0));
        if (firstTile == null) {
            System.out.println("smoothenPath: first waypoint tile is null for unit " + self.ID + " at " + path.get(0));
            return path;
        }
        if (firstTile.isBlocked(pathingSignature)) {
            return path;
        }

        var occupationMap = tileMap.occupationMaps.get(pathingSignature);
        if (occupationMap == null) {
            System.out.println("smoothenPath: no occupation map for signature " + pathingSignature + " (unit " + self.ID + ")");
            return path;
        }
        int spacing = (occupationMap.getPadding() + tileMap.tileSize) / 2;
        Coordinate start = path.get(0);

        int farLimit = Math.min(60, path.size() - 1);
        Coordinate goalFar = path.get(farLimit);

        int medLimit = Math.min(30, path.size() - 1);
        Coordinate goalMed = path.get(medLimit);

        int nearLimit = Math.min(9, path.size() - 1);
        Coordinate goalNear = path.get(nearLimit);

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalFar, (int) (self.getWidth())), pathingSignature)) {
//            if(self.isSelected()) System.out.println("using far limit");
            var smothened = new ArrayList<>(path.subList(farLimit, path.size() - 1));
            if(smothened.isEmpty()) smothened.add(path.getLast());
            return smothened;
        }

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalMed, (int) (self.getWidth())), pathingSignature)) {
//            if(self.isSelected()) System.out.println("using med limit");
            var smothened = new ArrayList<>(path.subList(medLimit, path.size() - 1));
            if(smothened.isEmpty()) smothened.add(path.getLast());
            return smothened;
        }

        if (tileMap.allClear(tileMap.getTileIntersectingThickLine(start, goalNear, (int) (self.getWidth())), pathingSignature)) {
//            if(self.isSelected()) System.out.println("using near limit");
            var smothened = new ArrayList<>(path.subList(nearLimit, path.size() - 1));
            if(smothened.isEmpty()) smothened.add(path.getLast());
            return smothened;
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

    private List<Coordinate> getTerrainPath(Coordinate startCoord, Coordinate endCoord, RTSUnit self) {
        int maxCalculationAmount = 500;
        Tile start = tileMapGiantTerrain.getTileAtLocation(startCoord);
        Tile goal = tileMapGiantTerrain.getTileAtLocation(endCoord);
        if (start == null || goal == null) {
            return null;
        }
        
        String pathingSignature = self.getPathingSignature();
        
         if (start.isBlocked(pathingSignature)) {
                start = tileMapGiantTerrain.getClosestOpenTile(startCoord, endCoord, pathingSignature);
            }

            if (goal.isBlocked(pathingSignature)) {
                goal = tileMapGiantTerrain.getClosestOpenTile(endCoord, startCoord, pathingSignature);
            }

        PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> {
            int fCompare = Double.compare(a.f, b.f);
            if (fCompare != 0) {
                return fCompare;
            }
            return Double.compare(a.h, b.h);
        });

        Map<Tile, Node> allNodes = new HashMap<>();
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int num = 0;
        
        while (!openSet.isEmpty() && num < maxCalculationAmount) {
            Node current = openSet.poll();
            num++;
            if (current.tile == goal) {
                return reconstructPath(current);
            }

            for (Tile neighbor : tileMapGiantTerrain.getNeighbors(current.tile.getGridLocation())) {
                if (TerrainTileMap.getCurrentTerrainTileMapForSize(tileMapGiantTerrain.tileSize).isTileBlocked(neighbor)) {
                    continue;
                }

                // Calculate movement cost - √2 for diagonal, 1 for cardinal
                double moveCost = (neighbor.x != current.tile.x && neighbor.y != current.tile.y) ? Math.sqrt(2) : 1;
                double gScore = current.g + moveCost;
                Node neighborNode = allNodes.get(neighbor);

                if (neighborNode == null || gScore < neighborNode.g) {
                    neighborNode = new Node(neighbor, current, gScore, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }

        return null;
    }

    private Coordinate calcIntermediateGoal(Coordinate start, List<Coordinate> waypoints, String signature) {
        for(Coordinate c : waypoints) {
            if(c.distanceFrom(start) > maxCalculationDistance && !tileMapGiantTerrain.getTileAtLocation(c).isBlocked(signature)) {
                return c;
            }
        }
        
        return waypoints.getLast();
    }
}

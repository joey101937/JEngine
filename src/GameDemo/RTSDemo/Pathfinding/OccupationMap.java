package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SceneryObjects.SceneryObject;
import GameDemo.RTSDemo.Units.Landmine;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author guydu
 */
public class OccupationMap implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public static transient ExecutorService occupationService = Executors.newFixedThreadPool(200);

    private int padding;
    private String commandGroup;
    private int team;
    private int plane;
    private TileMap tileMap;
    private int tileSize;

    public ConcurrentHashMap<Tile, Boolean> occupiedMap = new ConcurrentHashMap<>();
    
    public Coordinate getGridLocationOf(Coordinate input) {
        int adjustedX = (int)((input.x / tileSize) * tileSize) + tileSize;
        int adjustedY = (int)((input.y / tileSize) * tileSize) + tileSize;
        return new Coordinate(adjustedX, adjustedY);
    }
    
    public void updateOccupationMap(Game game) {
        // Reinitialize ExecutorService if needed (after deserialization)
        if (occupationService == null) {
            occupationService = Executors.newFixedThreadPool(200);
        }

        occupiedMap.clear();
        Collection<Future<?>> occupationTasks = new LinkedList<>();
        Tile[][] tileGrid = tileMap.tileGrid;
        for(GameObject2 go : game.getAllObjects()){
            if(go instanceof RTSUnit unit
                    && !(go instanceof Landmine)
                    && (!(unit.getCommandGroup().equals(commandGroup)
                    && !unit.getCommandGroup().equals(NavigationManager.SEPERATOR_GROUP)) || unit.isRubble)
                    && unit.isSolid
                    && unit.plane == plane
                    && (unit.team == team || unit.isRubble)) {
                occupationTasks.add(occupationService.submit(() -> {
                    boolean isForInfantry = padding == 16;
                    for(Coordinate coord : tileMap.getTilesNearPoint(getGridLocationOf(unit.getPixelLocation()), (int)(unit.getWidthForPathing() * (isForInfantry ? .7 : .7)) + tileSize + padding)) {
                        try {
                            occupiedMap.put(tileGrid[coord.x][coord.y], true);
                        } catch (IndexOutOfBoundsException ib) {
                            // ignore ib
                        } catch(ConcurrentModificationException cme) {
                            int tries = 0;
                            while (tries < 30 && occupiedMap.getOrDefault(tileGrid[coord.x][coord.y], false)) {
                                tries ++;
                                occupiedMap.put(tileGrid[coord.x][coord.y], true);
                            }
                            System.out.println("tries " + tries);
                        }
                    }
                    return true;
                }));
                
            }
            if(plane == 0 && go instanceof SceneryObject scenery && go.isSolid && go.getWidth() > 0) {
                int objectPadding = padding + scenery.getPathingPadding();
                float cx = go.getPixelLocation().x;
                float cy = go.getPixelLocation().y;
                float hw = go.getWidth()  / 2f + objectPadding;
                float hh = go.getHeight() / 2f + objectPadding;
                double rot = Math.toRadians(go.getRotation());
                float cosA = (float) Math.abs(Math.cos(rot));
                float sinA = (float) Math.abs(Math.sin(rot));
                // AABB of the rotated padded rect — used to pull candidate tiles
                int aabbHW = (int)(hw * cosA + hh * sinA) + 1;
                int aabbHH = (int)(hw * sinA + hh * cosA) + 1;
                Rectangle aabb = new Rectangle((int)(cx - aabbHW), (int)(cy - aabbHH), aabbHW * 2, aabbHH * 2);
                float cosR = (float) Math.cos(rot);
                float sinR = (float) Math.sin(rot);
                for (Tile tile : tileMap.getTilesIntersectingRectangle(aabb)) {
                    if (tileIntersectsOBB(tile, cx, cy, hw, hh, cosR, sinR, cosA, sinA)) {
                        occupiedMap.put(tile, true);
                    }
                }
            }
        }
        
        Handler.waitForAllJobs(occupationTasks);
    }
    
    
    public OccupationMap(int padding, String commandGroup, int team, int plane, TileMap map, int tileSize) {
        this.padding = padding;
        this.commandGroup = commandGroup;
        this.tileMap = map;
        this.team = team;
        this.plane = plane;
        this.tileSize = tileSize;
    }
    
    public Boolean isTileBlocked(Tile t) {
        return occupiedMap.getOrDefault(t, Boolean.FALSE) || (this.plane < 2 && TerrainTileMap.getCurrentTerrainTileMapForSize(tileSize).isTileBlocked(t));
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public String getCommandGroup() {
        return commandGroup;
    }

    public void setCommandGroup(String commandGroup) {
        this.commandGroup = commandGroup;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getPlane() {
        return plane;
    }

    public void setPlane(int plane) {
        this.plane = plane;
    }

    // SAT test: does an axis-aligned tile intersect the oriented bounding box?
    // cosR/sinR are the OBB's local X-axis components; cosA/sinA are their absolute values.
    private boolean tileIntersectsOBB(Tile tile, float cx, float cy,
                                       float hw, float hh,
                                       float cosR, float sinR,
                                       float cosA, float sinA) {
        Coordinate tileCenter = tile.getCenterPoint();
        float tx = tileCenter.x;
        float ty = tileCenter.y;
        float tHalf = tile.tileSize / 2f;

        // World X axis
        if (Math.abs(cx - tx) > hw * cosA + hh * sinA + tHalf) return false;
        // World Y axis
        if (Math.abs(cy - ty) > hw * sinA + hh * cosA + tHalf) return false;

        float dx = tx - cx;
        float dy = ty - cy;
        float tileProjHalf = tHalf * (cosA + sinA);

        // OBB local X axis
        if (Math.abs(dx * cosR + dy * sinR) > hw + tileProjHalf) return false;
        // OBB local Y axis
        if (Math.abs(-dx * sinR + dy * cosR) > hh + tileProjHalf) return false;

        return true;
    }
}

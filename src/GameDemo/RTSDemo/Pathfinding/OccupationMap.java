package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSUnit;
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
public class OccupationMap {
    public static ExecutorService occupationService = Executors.newFixedThreadPool(200);
    
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
                    && unit.team == team) {
                occupationTasks.add(occupationService.submit(() -> {
                    boolean isForInfantry = padding == 16;
                    Coordinate cachedPosition = UnitPositionCache.getCachedPosition(unit);
                    for(Coordinate coord : tileMap.getTilesNearPoint(getGridLocationOf(cachedPosition), (int)(unit.getWidthForPathing() * (isForInfantry ? .7 : .7)) + tileSize + padding)) {
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
            // todo add padding to this calculation
            if(plane == 0 && go instanceof KeyBuilding building && building.getHitbox() != null) {
                int keyBuildingPadding = padding + 50;
                Rectangle paddedRect = new Rectangle(
                    building.getPixelLocation().x - building.getWidth()/2 - keyBuildingPadding,
                    building.getPixelLocation().y - building.getHeight()/2 - keyBuildingPadding,
                    building.getWidth() + (keyBuildingPadding * 2),
                    building.getHeight() + (keyBuildingPadding * 2)
                );
                
                List<Tile> affectedTiles = tileMap.getTilesIntersectingRectangle(paddedRect);
                affectedTiles.forEach(tile -> occupiedMap.put(tile, true));
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
    
    
}

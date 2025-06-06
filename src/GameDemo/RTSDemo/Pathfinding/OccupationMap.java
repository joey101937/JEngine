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
    
    public ConcurrentHashMap<Tile, Boolean> occupiedMap = new ConcurrentHashMap<>();
    
    public void updateOccupationMap(Game game) {
        occupiedMap.clear();
        Collection<Future<?>> occupationTasks = new LinkedList<>();
        Tile[][] tileGrid = tileMap.tileGrid;
        
        for(GameObject2 go : game.getAllObjects()){
            if(go instanceof RTSUnit unit && !(go instanceof Landmine) && (!unit.commandGroup.equals(commandGroup) || unit.isRubble) && unit.isSolid && unit.plane == plane && unit.team == team) {
                occupationTasks.add(occupationService.submit(() -> {
                    for(Coordinate coord : tileMap.getTilesNearPoint(unit.getPixelLocation(), (int)(unit.getWidth() * 1.2) + Tile.tileSize + padding)) {
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
                    building.getPixelLocation().x - keyBuildingPadding,
                    building.getPixelLocation().y - keyBuildingPadding,
                    building.getWidth() + (keyBuildingPadding * 2),
                    building.getHeight() + (keyBuildingPadding * 2)
                );
                
                List<Tile> affectedTiles = tileMap.getTilesIntersectingRectangle(paddedRect);
                affectedTiles.forEach(tile -> occupiedMap.put(tile, true));
            }
        }
        
        Handler.waitForAllJobs(occupationTasks);
    }
    
    
    public OccupationMap(int padding, String commandGroup, int team, int plane, TileMap map) {
        this.padding = padding;
        this.commandGroup = commandGroup;
        this.tileMap = map;
        this.team = team;
        this.plane = plane;
    }
    
    public Boolean isTileBlocked(Tile t) {
        return occupiedMap.getOrDefault(t, Boolean.FALSE) || (this.plane < 2 && TerrainTileMap.getCurrentTerrainTileMap().isTileBlocked(t));
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

package GameDemo.RTSDemo.Pathfinding;

import Framework.Coordinate;
import Framework.CoreLoop.Handler;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Units.Landmine;
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
                    for(Coordinate coord : tileMap.getTilesNearPoint(unit.getPixelLocation(), unit.getWidth() + Tile.tileSize + padding)) {
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
               List<Coordinate> vertices = Main.jMap(List.of(building.getHitbox().vertices), x -> x.copy().add(building.getPixelLocation().x, building.getPixelLocation().y));
               List<Tile> topBorder = tileMap.getTilesIntersectingLine(vertices.get(0), vertices.get(1));
               topBorder.forEach(coord -> occupiedMap.put(tileGrid[coord.x][coord.y], true));
               
               List<Tile> bottomBorder = tileMap.getTilesIntersectingLine(vertices.get(2), vertices.get(3));
               bottomBorder.forEach(coord -> occupiedMap.put(tileGrid[coord.x][coord.y], true));
               
               List<Tile> leftBorder = tileMap.getTilesIntersectingLine(vertices.get(0), vertices.get(2));
               leftBorder.forEach(coord -> occupiedMap.put(tileGrid[coord.x][coord.y], true));
               
               List<Tile> rightBorder = tileMap.getTilesIntersectingLine(vertices.get(1), vertices.get(3));
               rightBorder.forEach(coord -> occupiedMap.put(tileGrid[coord.x][coord.y], true));
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

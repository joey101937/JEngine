package GameDemo.TileMaker;

import Framework.AsyncInputHandler;
import Framework.Coordinate;
import GameDemo.TileMaker.Tiles.BlueTile;
import java.awt.event.MouseEvent;

/**
 *
 * @author guydu
 */
public class TileInputHandler extends AsyncInputHandler {
    Tile hoveredTile = null;

    @Override
    public void onMouseMoved(MouseEvent e) {
        Coordinate mouseLocationInWorld = getLocationOfMouseEvent(e);
        Tile newHoveredTile = getTileAtLocation(mouseLocationInWorld);
        System.out.println("new HoveredTile " + newHoveredTile);
        if(hoveredTile != null) hoveredTile.setIsSelected(false);
        newHoveredTile.setIsSelected(true);
        hoveredTile = newHoveredTile;
    }
    

    @Override
    public void onMousePressed(MouseEvent e) {
        if(hoveredTile == null)  return;
        Tile t = new BlueTile(hoveredTile.location.x, hoveredTile.location.y);
        t.gridLocation = hoveredTile.gridLocation.copy();
        t.setTranslucent(hoveredTile.isTanslucent());
        TileMaker.tileGrid[t.gridLocation.x][t.gridLocation.y] = t;
    }
    
    
    public static Tile getTileAtLocation(Coordinate location) {
        int x = (int) (location.x / TileMaker.TILE_SIZE);
        int y = (int) (location.y / TileMaker.TILE_SIZE);
        
        if (x >= 0 && x < TileMaker.tileGrid.length && y >= 0 && y < TileMaker.tileGrid[0].length) {
            return TileMaker.tileGrid[x][y];
        }
        
        return null;
    }

}

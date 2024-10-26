package GameDemo.TileMaker;

import Framework.AsyncInputHandler;
import Framework.Coordinate;
import java.awt.event.MouseEvent;

/**
 *
 * @author guydu
 */
public class TileInputHandler extends AsyncInputHandler {

    @Override
    public void onMouseMoved(MouseEvent e) {
        Coordinate mouseLocationInWorld = getLocationOfMouseEvent(e);
        Tile hoveredTile = getTileAtLocation(mouseLocationInWorld);
        
        // Reset all tiles to not selected
        for (Tile[] row : TileMaker.tileGrid) {
            for (Tile tile : row) {
                tile.setIsSelected(false);
            }
        }
        
        // Set the hovered tile to selected
        if (hoveredTile != null) {
            hoveredTile.setIsSelected(true);
        }
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

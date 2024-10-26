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
    }
    
    
    public static Tile getTileAtLocation(Coordinate location) {
        // todo
        return null;
    }

}

package GameDemo.TileMaker;

import Framework.AsyncInputHandler;
import Framework.Camera;
import Framework.Coordinate;
import Framework.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author guydu
 */
public class TileInputHandler extends AsyncInputHandler {
    Tile hoveredTile = null;
    public static boolean wDown = false, aDown = false, sDown = false, dDown = false;

    @Override
    public void onMouseMoved(MouseEvent e) {
        Coordinate mouseLocationInWorld = getLocationOfMouseEvent(e);
        Tile newHoveredTile = getTileAtLocation(mouseLocationInWorld);
        if(hoveredTile != null) hoveredTile.setIsSelected(false);
        newHoveredTile.setIsSelected(true);
        hoveredTile = newHoveredTile;
    }
    
    @Override
    public void onMousePressed(MouseEvent e) {
        if(hoveredTile == null)  return;
        Tile t = TileMaker.tilePicker.getSelectedTile().createCopy(hoveredTile.location.x, hoveredTile.location.y);
        t.gridLocation = hoveredTile.gridLocation.copy();
        TileMaker.tilemap.tileGrid[t.gridLocation.x][t.gridLocation.y] = t;
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        // todo
    }
    
    public static Tile getTileAtLocation(Coordinate location) {
        int x = (int) (location.x / TileMaker.TILE_SIZE);
        int y = (int) (location.y / TileMaker.TILE_SIZE);
        
        if (x >= 0 && x < TileMaker.tilemap.tileGrid.length && y >= 0 && y < TileMaker.tilemap.tileGrid[0].length) {
            return TileMaker.tilemap.tileGrid[x][y];
        }
        
        return null;
    }

    @Override
    public void tick() {
        Camera cam = getHostGame().getCamera();
        double xVelocity = 0;
        double yVelocity = 0;
        if (wDown) {
            yVelocity += 1;
        }
        if (sDown) {
            yVelocity -= 1;
        }
        if (aDown) {
            xVelocity += 1;
        }
        if (dDown) {
            xVelocity -= 1;
        }

        cam.xVel = xVelocity;
        cam.yVel = yVelocity;
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> {
                wDown = true;
                sDown = false;
            }
            case KeyEvent.VK_A -> {
                aDown = true;
                dDown = false;
            }
            case KeyEvent.VK_S -> {
                sDown = true;
                wDown = false;
                if(e.isControlDown()) {
                    TileMaker.taskBar.handleSaveClick();
                }
            }
            case KeyEvent.VK_D -> {
                dDown = true;
                aDown = false;
            }
            case KeyEvent.VK_ESCAPE -> {
                Window.setFullscreenWindowed(false);
            }
            case KeyEvent.VK_EQUALS -> {
                Window.setFullscreenWindowed(true);
            }
            case KeyEvent.VK_E -> {
                if(e.isControlDown()) {
                    TileRenderer.exportAsImage();
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wDown = false;
            case KeyEvent.VK_A -> aDown = false;
            case KeyEvent.VK_S -> sDown = false;
            case KeyEvent.VK_D -> dDown = false;
        }
    }
}

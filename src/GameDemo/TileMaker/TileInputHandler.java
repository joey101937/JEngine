package GameDemo.TileMaker;

import Framework.AsyncInputHandler;
import Framework.Camera;
import Framework.Coordinate;
import Framework.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guydu
 */
public class TileInputHandler extends AsyncInputHandler {
    Tile hoveredTile = null;
    public static boolean wDown = false, aDown = false, sDown = false, dDown = false;
    private UndoManager undoManager = new UndoManager();
    private boolean isDragging = false;
    private List<Coordinate> modifiedTiles = new ArrayList<>();

    @Override
    public void onMouseMoved(MouseEvent e) {
        Coordinate mouseLocationInWorld = getLocationOfMouseEvent(e);
        Tile newHoveredTile = getTileAtLocation(mouseLocationInWorld);
        if(hoveredTile != null) hoveredTile.setIsSelected(false);
        if(newHoveredTile != null) newHoveredTile.setIsSelected(true);
        hoveredTile = newHoveredTile;
    }
    
    @Override
    public void onMousePressed(MouseEvent e) {
        isDragging = true;
        modifiedTiles.clear();
        changeTile(e);
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (isDragging) {
            changeTile(e);
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        isDragging = false;
        if (!modifiedTiles.isEmpty()) {
            undoManager.addUndoAction(modifiedTiles);
        }
    }
    
    private void changeTile(MouseEvent e) {
        Coordinate mouseLocationInWorld = getLocationOfMouseEvent(e);
        Tile tileAtLocation = getTileAtLocation(mouseLocationInWorld);
        if(tileAtLocation == null) return;
        
        Tile selectedTile = TileMaker.tilePicker.getSelectedTile();
        if(selectedTile == null) return;
        
        Coordinate gridLocation = tileAtLocation.gridLocation;
        Tile oldTile = TileMaker.tilemap.tileGrid[gridLocation.x][gridLocation.y];
        Tile newTile = selectedTile.createCopy(tileAtLocation.location.x, tileAtLocation.location.y);
        newTile.gridLocation = gridLocation.copy();
        TileMaker.tilemap.tileGrid[gridLocation.x][gridLocation.y] = newTile;
        
        if (!modifiedTiles.contains(gridLocation)) {
            modifiedTiles.add(gridLocation);
            undoManager.addUndoableAction(gridLocation, oldTile);
        }
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
            case KeyEvent.VK_Z -> {
                if(e.isControlDown()) {
                    undoManager.undo();
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

package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.InputHandler;
import Framework.Main;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TileInputHandler extends InputHandler {
    private UndoManager undoManager = new UndoManager();
    private boolean isDragging = false;
    private List<Coordinate> modifiedTiles = new ArrayList<>();

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            undoManager.undo();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Implement key release handling if needed
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        changeTile(e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        isDragging = true;
        modifiedTiles.clear();
        changeTile(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isDragging = false;
        if (!modifiedTiles.isEmpty()) {
            undoManager.addUndoAction(modifiedTiles);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging) {
            changeTile(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Implement mouse move handling if needed
    }

    private void changeTile(int mouseX, int mouseY) {
        Coordinate mouseLocation = new Coordinate(mouseX, mouseY);
        mouseLocation.add(TileMaker.game.getCamera().getLocation());
        int gridX = (int) (mouseLocation.x / TileMaker.TILE_SIZE);
        int gridY = (int) (mouseLocation.y / TileMaker.TILE_SIZE);

        if (gridX >= 0 && gridX < TileMaker.tileGrid.length && gridY >= 0 && gridY < TileMaker.tileGrid[0].length) {
            Tile selectedTile = TileMaker.tilePicker.getSelectedTile();
            if (selectedTile != null) {
                Tile oldTile = TileMaker.tileGrid[gridX][gridY];
                TileMaker.tileGrid[gridX][gridY] = selectedTile.createCopy(gridX * TileMaker.TILE_SIZE, gridY * TileMaker.TILE_SIZE);
                TileMaker.tileGrid[gridX][gridY].gridLocation = new Coordinate(gridX, gridY);
                
                Coordinate tileCoord = new Coordinate(gridX, gridY);
                if (!modifiedTiles.contains(tileCoord)) {
                    modifiedTiles.add(tileCoord);
                    undoManager.addUndoableAction(tileCoord, oldTile);
                }
            }
        }
    }
}

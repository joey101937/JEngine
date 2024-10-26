package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.InputHandler;
import Framework.Main;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TileInputHandler extends InputHandler {

    @Override
    public void keyPressed(KeyEvent e) {
        // Implement key press handling if needed
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Implement key release handling if needed
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Coordinate mouseLocation = new Coordinate(e.getX(), e.getY());
        mouseLocation.add(TileMaker.game.getCamera().getLocation());
        int gridX = (int) (mouseLocation.x / TileMaker.TILE_SIZE);
        int gridY = (int) (mouseLocation.y / TileMaker.TILE_SIZE);

        if (gridX >= 0 && gridX < TileMaker.tileGrid.length && gridY >= 0 && gridY < TileMaker.tileGrid[0].length) {
            Tile selectedTile = TileMaker.tilePicker.getSelectedTile();
            if (selectedTile != null) {
                TileMaker.tileGrid[gridX][gridY] = selectedTile.createCopy(gridX * TileMaker.TILE_SIZE, gridY * TileMaker.TILE_SIZE);
                TileMaker.tileGrid[gridX][gridY].gridLocation = new Coordinate(gridX, gridY);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Implement mouse press handling if needed
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Implement mouse release handling if needed
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Implement mouse drag handling if needed
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Implement mouse move handling if needed
    }
}

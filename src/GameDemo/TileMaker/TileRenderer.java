package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.IndependentEffect;
import static GameDemo.TileMaker.TileMaker.tileGrid;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class TileRenderer extends IndependentEffect {

    @Override
    public void render(Graphics2D g) {
        Coordinate currentCoordinate = null;
        Composite originalComposite = g.getComposite();
        for(int y = 0; y <  tileGrid[0].length; y++) {
            for(int x = 0; x <  tileGrid.length; x++) {
               currentCoordinate =  tileGrid[x][y].location;
                if(!TileMaker.game.getCamera().getFieldOfView().contains(currentCoordinate.x, currentCoordinate.y)) continue;
                if(tileGrid[x][y].isTanslucent()) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    renderTile(g, tileGrid[x][y]);
                     g.setComposite(originalComposite);
                } else {
                    renderTile(g, tileGrid[x][y]);
                }
            }
        }
    }
    
    private void renderTile(Graphics2D g, Tile t) {
        if(t.isSelected()) {
             g.drawImage(t.getSelectedSprite().getCurrentVolatileImage(), t.location.x, t.location.y, null);
        } else {
            g.drawImage(t.getMainSprite().getCurrentVolatileImage(), t.location.x, t.location.y, null);
        }
    }

    @Override
    public void tick() {
    
    }
    
    /**
     * exports the current tile grid as a .png image. Tile translucency is ignored.
     * it is saved as TileExport.png in the export folder
     */
    public static void exportAsImage() {
        
    }
    
}

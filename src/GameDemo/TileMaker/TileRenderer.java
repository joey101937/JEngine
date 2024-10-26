package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.IndependentEffect;
import static GameDemo.TileMaker.TileMaker.tileGrid;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

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
                if(tileGrid[x][y].isTranslucent()) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    renderTile(g, tileGrid[x][y]);
                     g.setComposite(originalComposite);
                } else {
                    renderTile(g, tileGrid[x][y]);
                }
            }
        }
    }
    
    private static final int BORDER_WIDTH = 2;
    private static final Color BORDER_COLOR = Color.WHITE;

    private void renderTile(Graphics2D g, Tile t) {
        g.drawImage(t.getSprite().getCurrentVolatileImage(), t.location.x, t.location.y, null);
        
        if (t.isSelected()) {
            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(BORDER_WIDTH));
            g.drawRect(t.location.x, t.location.y, TileMaker.TILE_SIZE, TileMaker.TILE_SIZE);
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
        int width = TileMaker.tileGrid.length * TileMaker.TILE_SIZE;
        int height = TileMaker.tileGrid[0].length * TileMaker.TILE_SIZE;
        
        BufferedImage exportImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = exportImage.createGraphics();
        
        for (int y = 0; y < TileMaker.tileGrid[0].length; y++) {
            for (int x = 0; x < TileMaker.tileGrid.length; x++) {
                Tile tile = TileMaker.tileGrid[x][y];
                int drawX = x * TileMaker.TILE_SIZE;
                int drawY = y * TileMaker.TILE_SIZE;
                g.drawImage(tile.getSprite().getCurrentVolatileImage(), drawX, drawY, null);
            }
        }
        
        g.dispose();
        
        File exportFolder = new File("export");
        if (!exportFolder.exists()) {
            exportFolder.mkdir();
        }
        
        File outputFile = new File(exportFolder, "TileExport.png");
        try {
            ImageIO.write(exportImage, "png", outputFile);
            System.out.println("Tile grid exported successfully to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error exporting tile grid: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}

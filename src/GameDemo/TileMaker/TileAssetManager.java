package GameDemo.TileMaker;

import Framework.GraphicalAssets.Graphic;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author guydu
 */
public class TileAssetManager {
    public static BufferedImage tileBackground;
    public static BufferedImage tileGreen, tileBlue, tileYellow, tileBrown;
    public static BufferedImage tileGreenSelected, tileBlueSelected, tileYellowSelected, tileBrownSelected;
    
    static {
        
        try {
            tileBackground = Graphic.load("DemoAssets/TankGame/grassTerrain_mega3.png");
            tileGreen = Graphic.load("DemoAssets/Tiles/tileGreen.png");
            tileBlue = Graphic.load("DemoAssets/Tiles/tileBlue.png");
            tileYellow = Graphic.load("DemoAssets/Tiles/tileYellow.png");
            tileBrown = Graphic.load("DemoAssets/Tiles/tileBrown.png");
            tileGreenSelected = addWhiteBorder(tileGreen);
            tileBlueSelected = addWhiteBorder(tileBlue);
            tileYellowSelected = addWhiteBorder(tileYellow);
            tileBrownSelected = addWhiteBorder(tileBrown);
        } catch (IOException ex) {
            System.out.println("error loading tile assets");
        }
    }
    
    
    /**
     * Returns a copy of the given buffered image except with a 2px white border around it
     * @param input input
     * @return new buffered image
     */
    private static BufferedImage addWhiteBorder(BufferedImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        int newWidth = width + 4;
        int newHeight = height + 4;
        
        BufferedImage output = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        
        // Create graphics object for drawing
        Graphics2D g2d = output.createGraphics();
        
        // Set white color for border
        g2d.setColor(Color.WHITE);
        
        // Draw white rectangle (border)
        g2d.fillRect(0, 0, newWidth, newHeight);
        
        // Draw original image in the center
        g2d.drawImage(input, 2, 2, null);
        
        // Dispose of graphics object
        g2d.dispose();
        
        return output;
    }
    
    
}

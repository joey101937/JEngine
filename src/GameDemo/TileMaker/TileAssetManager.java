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
    
    static {
        
        try {
            tileBackground = Graphic.load("DemoAssets/TankGame/grassTerrain_mega3.png");
            tileGreen = Graphic.load("DemoAssets/Tiles/tileGreen.png");
            tileBlue = Graphic.load("DemoAssets/Tiles/tileBlue.png");
            tileYellow = Graphic.load("DemoAssets/Tiles/tileYellow.png");
            tileBrown = Graphic.load("DemoAssets/Tiles/tileBrown.png");
        } catch (IOException ex) {
            System.out.println("error loading tile assets");
        }
    }
   
}

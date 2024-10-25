package GameDemo.TileMaker;

import Framework.GraphicalAssets.Graphic;
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
            tileGreenSelected = addWhiteBorder(tileBlue);
            tileGreenSelected = addWhiteBorder(tileYellow);
            tileGreenSelected = addWhiteBorder(tileBrown);
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
        return null; // todo
    }
}

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
    
    static {
        
        try {
            tileBackground = Graphic.load("DemoAssets/TankGame/grassTerrain_mega3.png");
        } catch (IOException ex) {
            System.out.println("error loading tile assets");
        }
    }
   
}

package GameDemo.TileMaker;

import Framework.GraphicalAssets.Sprite;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author guydu
 */
public class Tileset {
    public static ArrayList<Tile> library = new ArrayList<>();
    
    public static void initialize(String path) {
        try {
            loadLibrary(path);
            int detectedTileSize = library.get(0).getSprite().getWidth();
            TileMaker.TILE_SIZE = detectedTileSize;
            for(Tile tile : library) {
                Sprite s = tile.getSprite();
                if(s.getWidth() != detectedTileSize) {
                    System.out.println("incorrect width: " + s.getSignature());
                }
                if(s.getHeight() != detectedTileSize){
                    System.out.println("incorrect height " + s.getSignature());
                }
            }
        } catch (Exception ex) {
            System.out.println("exception while loading tile library");
        }
    }
    
    public static Tile getByName(String name) {
        for(Tile t : library) {
            if(t.getSprite().getSignature() == name) {
                return t;
            }
        }
        System.out.println("no tile by name " + name);
        return null;
    }
    
    
    /**
     * loads tile library from directory
     * @param directory 
     */
    private static void loadLibrary(String directory) throws IOException{
        File dir = new File(directory);
        for(File f : dir.listFiles()) {
            BufferedImage img = ImageIO.read(f);
            Sprite s = new Sprite(img);
            s.setSignature(f.getName());
            Tile t = new Tile(0, 0);
            t.setSprite(s);
            library.add(t);
        }
    }

}

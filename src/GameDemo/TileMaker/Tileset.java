package GameDemo.TileMaker;

import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
            if(t.getSprite().getSignature().equals(name)) {
                return t;
            }
        }
        System.out.println("no tile by name " + name);
        return null;
    }
    
    /**
     * loads tile library from directory and its subdirectories
     * @param directory 
     */
    private static void loadLibrary(String directory) throws IOException {
        File dir = new File(directory);
        loadLibraryRecursive(dir, "");
    }

    private static void loadLibraryRecursive(File dir, String prefix) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                loadLibraryRecursive(f, prefix + f.getName() + " - ");
            } else if (f.getName().toLowerCase().endsWith(".png")) {
                BufferedImage img = ImageIO.read(f);
                Sprite s = new Sprite(img);
                s.setSignature(prefix + f.getName());
                Tile t = new Tile();
                t.setSprite(s);
                library.add(t);
            }
        }
    }

    public static void saveTileMap(TileMap tileMap, String mapName) {
        File exportDir = new File("export");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File saveFile = new File(exportDir, mapName + ".tilemap");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(tileMap);
            System.out.println("TileMap saved successfully to: " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving TileMap: " + e.getMessage());
        }
    }

    public static TileMap loadTileMap() {
        JFileChooser fileChooser = new JFileChooser("export");
        fileChooser.setDialogTitle("Select TileMap file to import");
        fileChooser.setFileFilter(new FileNameExtensionFilter("TileMap files", "tilemap"));

        int filepickerResult = fileChooser.showOpenDialog(null);
        if (filepickerResult != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected");
            return null;
        }

        File saveFile = fileChooser.getSelectedFile();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            TileMap tileMap = (TileMap) ois.readObject();
            System.out.println("TileMap loaded successfully from: " + saveFile.getAbsolutePath());
            return tileMap;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading TileMap: " + e.getMessage());
            return null;
        }
    }
}

package GameDemo.TileMaker;

import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileReader;
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

    public static void exportTileGridToCSV(Tile[][] tileGrid, String mapName) {
        File exportDir = new File("export");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File csvFile = new File(exportDir, mapName + ".csv");

        try (FileWriter writer = new FileWriter(csvFile)) {
            for (int y = 0; y < tileGrid.length; y++) {
                for (int x = 0; x < tileGrid[y].length; x++) {
                    Tile tile = tileGrid[y][x];
                    String tileFileName = tile.getSprite().getSignature();
                    writer.append(tileFileName);
                    if (x < tileGrid[y].length - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
            System.out.println("CSV file exported successfully to: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error exporting CSV file: " + e.getMessage());
        }
    }

    public static Tile[][] importTileGridFromCSV() {
        JFileChooser fileChooser = new JFileChooser("export");
        fileChooser.setDialogTitle("Select CSV file to import");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected");
            return null;
        }

        File csvFile = fileChooser.getSelectedFile();
        ArrayList<ArrayList<Tile>> tileGrid = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tileNames = line.split(",");
                ArrayList<Tile> row = new ArrayList<>();
                for (String tileName : tileNames) {
                    Tile tile = getByName(tileName);
                    if (tile == null) {
                        System.err.println("Warning: Tile not found for name: " + tileName);
                        tile = library.get(0); // Use the first tile as a default
                    }
                    row.add(tile.createCopy());
                }
                tileGrid.add(row);
            }
            System.out.println("CSV file imported successfully from: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error importing CSV file: " + e.getMessage());
            return null;
        }

        // Convert ArrayList<ArrayList<Tile>> to Tile[][]
        Tile[][] result = new Tile[tileGrid.size()][];
        for (int i = 0; i < tileGrid.size(); i++) {
            result[i] = tileGrid.get(i).toArray(new Tile[0]);
        }

        return result;
    }

}

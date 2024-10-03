import GameDemo.RTSDemo.RTSAssetManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AssetExporter {

    public static void main(String[] args) {
        // Main method can be used to test the exportImages function
        System.out.println("Asset Exporter initialized.");
        exportImages(RTSAssetManager.infantryBazookaDie, "BazookaDeathpUpdated");
    }

    /**
     * Exports an array of BufferedImages as PNG files to the specified path.
     * 
     * @param images Array of BufferedImages to be exported
     * @param path   The directory path where the images will be saved
     */
    public static void exportImages(BufferedImage[] images, String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (int i = 0; i < images.length; i++) {
            String fileName = String.format("image_%03d.png", i);
            File outputFile = new File(directory, fileName);

            try {
                ImageIO.write(images[i], "png", outputFile);
                System.out.println("Saved: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving image: " + fileName);
                e.printStackTrace();
            }
        }
    }
}

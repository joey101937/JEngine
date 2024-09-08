/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import Framework.Main;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Represents a graphical asset that can be displayed to the screen
 *
 * @author Joseph
 */
public interface Graphic {
    
    /**
     * used for identifying the graphic
     * @return string signature
     */
    public String getSignature();
    
    /**
     * used for identifying the graphic
     * @param s new signuature
     */
    public void setSignature(String s);

    /**
     * gets either image of sprite or current frame of animated sequence
     *
     * @return either image of sprite or current frame of animated sequence
     */
    public Image getCurrentImage();

    /**
     * weather or not the asset is animated. sprite = false, sequence = true.
     *
     * @return weather or not the asset is animated.
     */
    public boolean isAnimated();

    /**
     * releases memory from asset and stops animaion thread
     */
    public void destroy();

    /**
     * creates a duplicate asset based on this graphic object
     *
     * @return Sprite or Sequence based on this
     */
    public Graphic copy();

    /**
     * Scales asset by a certain amount from its current scale
     *
     * @param d amount to scale by
     */
    public void scale(double d);

    /**
     * Scales asset to a certain percentage size relative to original size
     *
     * @param d percentage of original size to scale to
     */
    public void scaleTo(double d);

    /**
     * gets current size percentage of image relative to original size AS OF
     * LAST TICK
     *
     * @return current size percentage of image relative to original size
     */
    public double getScale();

    /**
     * flips the graphic horizontally
     */
    public void mirrorHorizontal();

    /**
     * flips the graphic vertically
     */
    public void mirrorVertical();

    /**
     * returns a scaled copy of the image
     * scales over 16 steps
     * @param before
     * @param scaleAmount
     * @return
     */
    public static Image scaleImage(Image before, double scaleAmount) {
        if(scaleAmount == 1) return before;
        
        boolean isSmallImage = before.getWidth() < 200 || before.getHeight() < 200;
        
        int numScales = isSmallImage ? 1 : 1;
        double stepAmount = Math.pow(scaleAmount, 1.0/numScales);
        
        Image output = before;
        
        for(int i = 0; i < numScales; i++) {
            output = scaleImageDirect(output, stepAmount);
        }
        
        return output;
    }
    
    /**
     * returns a scaled copy of the image
     * scales once straight to the target value
     * @param before
     * @param scaleAmount
     * @return
     */
    private static Image scaleImageDirect(Image before, double scaleAmount) {
        // Calculate the new dimensions
        double newWidth = before.getWidth() * scaleAmount;
        double newHeight = before.getHeight() * scaleAmount;

        // Create a writable image with the new dimensions
        WritableImage scaledImage = new WritableImage((int) newWidth, (int) newHeight);

        // Create a canvas to draw the scaled image
        Canvas canvas = new Canvas(newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw the original image onto the canvas with scaling
        gc.scale(scaleAmount, scaleAmount);
        gc.drawImage(before, 0, 0);

        // Copy the canvas content into the writable image
        scaledImage = canvas.snapshot(null, scaledImage);

        return scaledImage;
    }

        /**
     * returns a scaled copy of the image
     * scales once straight to the target value
     * @param before
     * @param scaleAmountX
     * @param scaleAmountY
     * @return
     */
    public static Image scaleImageDirect(Image before, double scaleAmountX, double scaleAmountY) {
        // Calculate the new dimensions
        double newWidth = before.getWidth() * scaleAmountX;
        double newHeight = before.getHeight() * scaleAmountY;

        // Create a writable image with the new dimensions
        WritableImage scaledImage = new WritableImage((int) newWidth, (int) newHeight);

        // Create a canvas to draw the scaled image
        Canvas canvas = new Canvas(newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw the original image onto the canvas with scaling
        gc.scale(scaleAmountX, scaleAmountY);
        gc.drawImage(before, 0, 0);

        // Copy the canvas content into the writable image
        scaledImage = canvas.snapshot(null, scaledImage);

        return scaledImage;
    }

    
    
    /**
     * returns a horizontally mirrored copy of the image
     *
     * @param before
     * @return
     */
    public static Image mirrorHorizontal(Image originalImage) {
       // Create a writable image with the same dimensions as the original
        WritableImage mirroredImage = new WritableImage(
            (int) originalImage.getWidth(), 
            (int) originalImage.getHeight()
        );

        // Create a canvas to draw the mirrored image
        Canvas canvas = new Canvas(originalImage.getWidth(), originalImage.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Apply horizontal flip by scaling -1 on the x-axis
        gc.scale(-1, 1);

        // Draw the original image on the flipped canvas
        gc.drawImage(originalImage, -originalImage.getWidth(), 0);

        // Copy the canvas content into the writable image
        mirroredImage = canvas.snapshot(null, mirroredImage);

        return mirroredImage;
    }

    /**
     * returns a horizontally mirrored copy of the image
     *
     * @param before
     * @return
     */
    public static Image mirrorVertical(Image originalImage) {
      // Create a writable image with the same dimensions as the original
        WritableImage mirroredImage = new WritableImage(
            (int) originalImage.getWidth(), 
            (int) originalImage.getHeight()
        );

        // Create a canvas to draw the mirrored image
        Canvas canvas = new Canvas(originalImage.getWidth(), originalImage.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Apply horizontal flip by scaling -1 on the x-axis
        gc.scale(1, -1);

        // Draw the original image on the flipped canvas
        gc.drawImage(originalImage, -originalImage.getWidth(), 0);

        // Copy the canvas content into the writable image
        mirroredImage = canvas.snapshot(null, mirroredImage);

        return mirroredImage;
    }
    
    /**
     * gets the RGB integer code from the given image at given coordinates
     * @param image Image
     * @param x x coord
     * @param y y coord
     * @return integer representation of RGB value. Similar to BufferedImage.getRGB(x,y)
     */
    public static int getRGBA(Image image, int x, int y) {
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader != null) {
            Color color = pixelReader.getColor(x, y);
            int r = (int) (color.getRed() * 255);
            int g = (int) (color.getGreen() * 255);
            int b = (int) (color.getBlue() * 255);
            int a = (int) (color.getOpacity() * 255);

            return (a << 24) | (r << 16) | (g << 8) | b;
        } else {
            throw new IllegalArgumentException("PixelReader is null for the provided image.");
        }
    }
    
    /**
     * gets int represntation of color
     * @param color color obj
     * @return 
     */
    public static int getRGBA(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        int a = (int) (color.getOpacity() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    public static Color generateColor(int rgba) {
        int r = (rgba >> 16) & 0xFF;
        int g = (rgba >> 8) & 0xFF;
        int b = rgba & 0xFF;
        int a = (rgba >> 24) & 0xFF;

        // Normalize the RGB and Alpha values to the range [0.0, 1.0]
        return Color.rgb(r, g, b, a / 255.0);
    }
    
    /**
     * returns a Image loaded from the given filename, located in assets
     * folder.
     * @param filename name of file including extension
     * @return buffered image render
     * @throws IOException if file cannot be found or loaded
     */
    public static Image load(String filename) throws IOException {
        try {
            return new Image(Main.assets + filename);
        } catch (Exception e) {
            System.out.println("Exception while trying to load " + filename);
           throw e;
        }
    }
    
    /**
     *  loads a sprite sequence from given directory
     * @param filename name of folder to load
     * @return list of files in directory
     * @throws IOException if there is a problem
     */ 
    public static Image[] loadSequence(String filename) throws IOException{
        filename = Main.assets + filename;
        ArrayList<Image> a = new ArrayList<>();
        ArrayList<File> children = new ArrayList<>();
        for(File f : new File(filename).listFiles()){
            children.add(f);
        }
        children.sort(null);
        for(File child : children){
            System.out.println("loading " + child.getPath().substring(6)); //to remove the redundant /Assets
           a.add(load(child.getPath().substring(6)));
        }
        Image[] output = new Image[a.size()];
        for(Image b : a){
            output[a.indexOf(b)]=b;
        }
        return output;
    }
    
    /**
     * Extracts a subimage from the given image.
     *
     * @param originalImage The original image to extract from.
     * @param x The x coordinate of the upper-left corner of the subimage.
     * @param y The y coordinate of the upper-left corner of the subimage.
     * @param width The width of the subimage.
     * @param height The height of the subimage.
     * @return A new Image object representing the extracted subimage.
     */
    public static Image getSubImage(Image originalImage, int x, int y, int width, int height) {
        // Ensure the subimage dimensions are within the bounds of the original image
        if (x < 0 || y < 0 || x + width > originalImage.getWidth() || y + height > originalImage.getHeight()) {
            throw new IllegalArgumentException("Subimage bounds are outside the original image.");
        }

        // Get the PixelReader for reading the original image
        PixelReader pixelReader = originalImage.getPixelReader();

        // Create a WritableImage to hold the subimage
        WritableImage subImage = new WritableImage(pixelReader, x, y, width, height);

        return subImage;
    }
    
    public static Image[] loadSequenceBouncing(String filename) throws IOException {
        Image[] forwards = loadSequence(filename);
        Image[] backwards = new Image[forwards.length];
        for(int i = forwards.length -1; i >=0; i--) {
            var imageToCopy = forwards[forwards.length-i - 1];
            backwards[i] = getSubImage(imageToCopy, 0, 0, (int)imageToCopy.getWidth(), (int)imageToCopy.getHeight());
        }
        
        return Main.arrayConcatenate(forwards, backwards);
    }
}

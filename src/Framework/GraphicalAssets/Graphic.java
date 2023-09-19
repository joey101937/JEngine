/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import Framework.Main;
import Framework.Window;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Represents a graphical asset that can be displayed to the screen
 *
 * @author Joseph
 */
public interface Graphic {

    /**
     * gets either image of sprite or current frame of animated sequence
     *
     * @return either image of sprite or current frame of animated sequence
     */
    public BufferedImage getCurrentImage();

    /**
     * gets either image of sprite or current frame of animated sequence
     *
     * @return either image of sprite or current frame of animated sequence
     */
    public VolatileImage getCurrentVolatileImage();

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
     *
     * @param before
     * @param scaleAmount
     * @return
     */
    public static BufferedImage scaleImage(BufferedImage before, double scaleAmount) {
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage((int) (w * scaleAmount), (int) (h * scaleAmount), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scaleAmount, scaleAmount);
        AffineTransformOp scaleOp
                = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }

    /**
     * returns a horizontally mirrored copy of the image
     *
     * @param before
     * @return
     */
    public static BufferedImage mirrorHorizontal(BufferedImage before) {
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-before.getWidth(), 0));
        AffineTransformOp scaleOp
                = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }

    /**
     * returns a horizontally mirrored copy of the image
     *
     * @param before
     * @return
     */
    public static BufferedImage mirrorVertical(BufferedImage before) {
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -before.getHeight()));
        AffineTransformOp scaleOp
                = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }

    public static VolatileImage getVolatileFromBuffered(BufferedImage bi) {
        VolatileImage output = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = Window.frame != null ? Window.frame.getGraphicsConfiguration() : ge.getDefaultScreenDevice().getDefaultConfiguration();
        output = gc.createCompatibleVolatileImage(bi.getWidth(), bi.getHeight(), Transparency.TRANSLUCENT);
        output.validate(gc);
        Graphics2D g2d = output.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        // clear rect here maybe
        g2d.drawImage(bi, 0, 0, null);
        g2d.dispose();
        return output;
    }

    /**
     * returns valid volatile image given the volatile image and the buffered
     * image it was based on. if the volatile image has been lost, it creates a
     * new volatile image based on the passed buffered image
     *
     * @param vi volatile image
     * @param source buffered backup
     * @return
     */
    public static VolatileImage getValidatedVolatileImage(VolatileImage vi, BufferedImage source) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = Window.frame.getGraphicsConfiguration();
        int status = vi.validate(gc);
        if (vi.contentsLost() || status != 0) {
            if (Main.debugMode) {
                System.out.println("Graphic- volatile image contents lost");
            }
            return getVolatileFromBuffered(source);
        } else {
            return vi;
        }
    }
    
    /**
     * returns a bufferedImage loaded from the given filename, located in assets
     * folder.
     * @param filename name of file including extension
     * @return buffered image render
     * @throws IOException if file cannot be found or loaded
     */
    public static BufferedImage load(String filename) throws IOException {
        return ImageIO.read(new File(Main.assets + filename));
    }
    
    /**
     *  loads a sprite sequence from given directory
     * @param filename name of folder to load
     * @return list of files in directory
     * @throws IOException if there is a problem
     */ 
    public static BufferedImage[] loadSequence(String filename) throws IOException{
        filename = Main.assets + filename;
        ArrayList<BufferedImage> a = new ArrayList<>();
        ArrayList<File> children = new ArrayList<>();
        for(File f : new File(filename).listFiles()){
            children.add(f);
        }
        children.sort(null);
        for(File child : children){
            System.out.println("loading " + child.getPath().substring(6)); //to remove the redundant /Assets
           a.add(load(child.getPath().substring(6)));
        }
        BufferedImage[] output = new BufferedImage[a.size()];
        for(BufferedImage b : a){
            output[a.indexOf(b)]=b;
        }
        return output;
    }
}

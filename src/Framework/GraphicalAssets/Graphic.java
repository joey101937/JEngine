/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

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
     * returns valid volatile image given the volatile image and the buffered image it was based on.
     * if the volatile image has been lost, it creates a new volatile image based on the passed buffered image
     * @param vi volatile image
     * @param source buffered backup
     * @return 
     */
    public static VolatileImage getValidatedVolatileImage(VolatileImage vi, BufferedImage source) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = Window.frame.getGraphicsConfiguration();
        int status = vi.validate(gc);
        if(vi.contentsLost() || status != 0) {
            System.out.println("image contents lost");
            return getVolatileFromBuffered(source);
        }
        else return vi;
    }
}

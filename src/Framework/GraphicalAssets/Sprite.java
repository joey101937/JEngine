/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Represents an image used to be rendered in the world
 * @author Joseph
 */
public class Sprite implements Graphic{
    private BufferedImage image;
     private double scale = 1;
    
     /**
      * gets raw underlying image
      * @return underlying image
      */
    public BufferedImage getImage(){
        return image;
    }
    
    /**
     * sets underlying image
     * @param b new underlying image
     */
    public void setImage(BufferedImage b){
        image= b;
    }
    
   
    public Sprite(BufferedImage bi){
        image = bi;
    }
    
    @Override
    public Sprite copy() {
        Sprite output = new Sprite(image);
        output.scale = scale;
        return output;
    }

    @Override
    public double getScale(){
        return scale;
    }
    
    /**
     * image by a given amount
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        image = scaleImage(image, d);
        scale *= d;
    }
    
    /**
     * gets height of image in px
     * @return number of pixels tall
     */
    public int getHeight(){
        return image.getHeight();
    }
    /**
     * gets width of image in px
     * @return number of pixels wide
     */
    public int getWidth(){
        return image.getWidth();
    }

    /**
     * sets the scale of image to a given scale
     * @param d new value to be scale, relative to the default scale of image
     */
    @Override
    public void scaleTo(double d) {
        image = scaleImage(image, d / scale);
        scale = d;
    }

    private BufferedImage scaleImage(BufferedImage before, double scaleAmount) {   
        if (before == null) return null;
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage((int)(w*scaleAmount), (int)(h*scaleAmount), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scaleAmount, scaleAmount);
        AffineTransformOp scaleOp
                = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }
    
    /**
     * destroys this sprite and releases memory
     * do not call unless you know what your doing
     */
    @Override
    public void destroy(){
        image.flush();
        image = null;
        try {
            finalize();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public BufferedImage getCurrentImage() {
        return getImage();
    }
    @Override
    public boolean isAnimated(){
        return false;
    }
}

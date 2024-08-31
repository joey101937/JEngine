/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 * Represents an image used to be rendered in the world
 * @author Joseph
 */
public class Sprite implements Graphic{
    private BufferedImage image;

    private VolatileImage volatileImage;
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
        volatileImage = Graphic.getVolatileFromBuffered(b);
    }
    
   
    public Sprite(BufferedImage bi){
        image = bi;
        volatileImage = Graphic.getVolatileFromBuffered(image);
    }
       
    public Sprite(BufferedImage bi, double inputScale){
        image = bi;
        this.scale = inputScale;
        volatileImage = Graphic.getVolatileFromBuffered(image);
    }
    
    @Override
    public VolatileImage getCurrentVolatileImage() {
        if(volatileImage != null && image != null) {
            volatileImage = Graphic.getValidatedVolatileImage(volatileImage, image);
            return volatileImage;
        } else {
            return null;
        }
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
        image = Graphic.scaleImage(image, d);
        volatileImage = Graphic.getVolatileFromBuffered(image);
        scale *= d;
    }
    
    @Override
    public void mirrorHorizontal () {
        image = Graphic.mirrorHorizontal(image);
        volatileImage = Graphic.getVolatileFromBuffered(image);
    }
    
    @Override
    public void mirrorVertical () {
        image = Graphic.mirrorVertical(image);
        volatileImage = Graphic.getVolatileFromBuffered(image);
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
        image = Graphic.scaleImage(image, d / scale);
        volatileImage = Graphic.getVolatileFromBuffered(image);
        scale = d;
    }
    
    /**
     * destroys this sprite and releases memory
     * do not call unless you know what your doing
     */
    @Override
    public void destroy(){
        image = null;
        volatileImage = null;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an image used to be rendered in the world
 * @author Joseph
 */
public class Sprite {
    private BufferedImage image;
     private double scale = 1;
    
    public BufferedImage getImage(){
        return image;
    }
    
    public void setImage(BufferedImage b){
        image= b;
    }
    
   
    public Sprite(BufferedImage bi){
        image = bi;
    }
    
    public Sprite copy() {
        Sprite output = new Sprite(image);
        output.scale = scale;
        return output;
    }

    public double getScale(){
        return scale;
    }
    
    /**
     * image by a given amount
     * @param d multiplier to scale by
     */
    public void scale(double d) {
        image = scaleImage(image, d);
        scale *= d;
    }

    /**
     * sets the scale of image to a given scale
     * @param d new value to be scale, relative to the default scale of image
     */
    public void scaleTo(double d) {
        image = scaleImage(image, d / scale);
        scale = d;
    }

    private BufferedImage scaleImage(BufferedImage before, double scaleAmount) {
      
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
    
    protected void destroy(){
        image.flush();
        try {
            finalize();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}

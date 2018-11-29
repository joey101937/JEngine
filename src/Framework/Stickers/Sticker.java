/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;


import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;

/**
 * Renders an image at a location for a given length of time
 * @author Joseph
 */
public class Sticker implements Runnable{
    protected Game hostGame = null;
    public volatile BufferedImage image;
    public Coordinate spawnLocation = new Coordinate(0,0);
    protected Coordinate renderLocation = new Coordinate(0,0);
    public boolean disabled = false;
    public int timeToRender;
    protected double scale = 1;
    protected static int numSticker = 0; //id for sticker, used for profiling threads
    /**
     * @param g Game to add to
     * @param i Image to display
     * @param c location to display
     * @param duration how long to display
     */
    public Sticker(Game g, BufferedImage i, Coordinate c, int duration){
        image = i;
        hostGame = g;
        spawnLocation = new Coordinate(c);//where we want the sticker
        timeToRender = duration; //topleft location of sticker used to put center on spawnLocation
        g.visHandler.stickers.add(this);
        Thread t = new Thread(this);
        t.setName("Sticker timer " + numSticker++);
        t.start();
    }
    /**
     * calibrates the render location to center the image on spawn location
     * @param toRender 
     */
    protected void centerCoordinate(BufferedImage toRender) {
        try{
        renderLocation.x = spawnLocation.x - toRender.getWidth() / 2;
        renderLocation.y = spawnLocation.y - toRender.getHeight() / 2;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void render(Graphics2D g) {
        centerCoordinate(image);
        if (spawnLocation.x < 0 || spawnLocation.y < 0) {
            disable();     //if the coordinates are bad, dont render
        }
        if (!disabled) {
            if (image != null) {
                g.drawImage(image, renderLocation.x, renderLocation.y, null);
                             
            }
        }
    }

    public void disable() {
        disabled = true;
        while(hostGame.visHandler.stickers.contains(this)){
            try{
                hostGame.visHandler.stickers.remove(this);
            }catch(ConcurrentModificationException cme){
                cme.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Main.wait(timeToRender);
        disable();
    }
    
    protected static BufferedImage scaleImage(BufferedImage before, double scaleAmount) {
      
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
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    public void scale(double d) {
        image = scaleImage(image,d);
        scale*=d;
    }
    
    /**
     * sets the scale of all frames of this sequence to a given scale
     * @param d new value to be scale, relative to the default scale of the images
     */
    public void scaleTo(double d) {
        image = scaleImage(image,d/scale);
        scale = d;
    }

    /**
     * returns current scaling of this sequence
     * @return
     */
    public double getScale() {
        return scale;
    }

}

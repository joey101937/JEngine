/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;


import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;

/**
 * Renders an image at a location for a given length of time
 * @author Joseph
 */
public class Sticker {
    protected Game hostGame = null;
    protected GameObject2 host = null; //used if attached to a game object
    public volatile BufferedImage image;
    public Coordinate spawnLocation = new Coordinate(0,0); //canter of where we want the sticker
    protected Coordinate renderLocation = new Coordinate(0,0); //top left position of sticker, to allow the center to be on spawnLocation
    public volatile boolean disabled = false;
    public volatile int timeToRender;
    protected double scale = 1;
    protected static int numSticker = 0; //id for sticker, used for profiling threads
    protected final int ID = numSticker++;
    public final Long creationTime;
    public double rotation;
    
    /**
     * @param g Game to add to
     * @param i Image to display
     * @param c location to display
     * @param duration how long to display IN MILLIS
     */
    public Sticker(Game g, BufferedImage i, Coordinate c, int duration){
        image = i;
        hostGame = g;
        spawnLocation = new Coordinate(c);//where we want the sticker
        timeToRender = duration; //topleft location of sticker used to put center on spawnLocation
        g.visHandler.stickers.add(this);
        creationTime = System.currentTimeMillis();
    }
    /**
     * Calibrates the render location to center the image on spawn location
     * @param toRender image to adjust renderLocation based on
     */
    protected void centerCoordinate(BufferedImage toRender) {
        try{
        if(spawnLocation==null)return;
        renderLocation.x = spawnLocation.x - toRender.getWidth() / 2;
        renderLocation.y = spawnLocation.y - toRender.getHeight() / 2;
        }catch(Exception e){
           e.printStackTrace();
        }
    }

    public synchronized void render(Graphics2D g) {
        AffineTransform old = g.getTransform();
        try {
            if (host != null && host.isAlive()) {
                spawnLocation = host.getPixelLocation();
            }
            centerCoordinate(image);
            g.rotate(Math.toRadians(rotation), spawnLocation.x, spawnLocation.y);
            if (spawnLocation == null) {
                return;
            }
            if (spawnLocation.x < 0 || spawnLocation.y < 0) {
                disable();     //if the coordinates are bad, dont render
            }
            if (!disabled) {
                if (image != null) {
                    g.drawImage(image, renderLocation.x, renderLocation.y, null);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        g.setTransform(old);
    }

    public synchronized void disable() {
        disabled = true;
        host=null;
        while(hostGame.visHandler.stickers.contains(this)){
            try{
                hostGame.visHandler.stickers.remove(this);
            }catch(ConcurrentModificationException cme){
                cme.printStackTrace();
            }
        }
       if(image!=null)image.flush();
       image=null;
       renderLocation=null;
       spawnLocation=null;
        try {
            finalize();
        } catch (Throwable ex) {
          ex.printStackTrace();
        }
    }

    /**
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    public void scale(double d) {
        image = Graphic.scaleImage(image,d);
        scale*=d;
    }
    
    /**
     * sets the scale of all frames of this sequence to a given scale
     * @param d new value to be scale, relative to the default scale of the images
     */
    public void scaleTo(double d) {
        image = Graphic.scaleImage(image,d/scale);
        scale = d;
    }

    /**
     * returns current scaling of this sequence
     * @return  current scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * makes this sticker move with a given GameObject. If this gameObject dies, the sticker is disabled
     * @param go GameObject to follow. If null, the sticker stops moving
     */
    public void attachTo(GameObject2 go){
        host = go;
        if(go.getHostGame()!=null){
            if (!go.getHostGame().visHandler.stickers.contains(this)) {
                this.hostGame = go.getHostGame();
                go.getHostGame().visHandler.stickers.add(this);
            }
            
        }
    }
}

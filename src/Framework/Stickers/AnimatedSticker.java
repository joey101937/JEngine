/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;


import Framework.Coordinate;
import Framework.Game;
import Framework.GraphicalAssets.Sequence;
import java.awt.Graphics2D;
import java.awt.image.VolatileImage;

/**
 * Animated sticker
 * @author Joseph
 */
public class AnimatedSticker extends Sticker{
    public Sequence sequence;
    public static int defaultFrameDelay = 40;
    
    
    /**
     * @param g Game to spawn in
     * @param s Sequence to be played for animation
     * @param c location to display
     * @param duration how long to display
     */
    public AnimatedSticker(Game g, Sequence s, Coordinate c, int duration) {
        super(g, null, c, duration);
        if(s==null){
            System.out.println("Sticker attempted to be made with null image");
            this.disable();
            return;
        }
        sequence = s.copyMaintainSource();
        if(defaultFrameDelay > 0) sequence.setFrameDelay(defaultFrameDelay);
    }
    

    @Override
    public void render(Graphics2D g) {
        if(System.currentTimeMillis() > creationTime + timeToRender) {
            disable();
            return;
        }
        Graphics2D gToUse = (Graphics2D)g.create();
        try {
            if (sequence == null) {
                return;
            }
            image = sequence.getCurrentImage();
            gToUse.rotate(Math.toRadians(rotation), spawnLocation.x, spawnLocation.y);
            if (spawnLocation.x < 0 || spawnLocation.y < 0) {
                disable();     //if the coordinates are bad, dont render
            }
            if (!disabled) {
                if (image != null) {
                   VolatileImage toRender = sequence.getCurrentVolatileFrame();
                   gToUse.drawImage(toRender, spawnLocation.x-toRender.getWidth()/2 , spawnLocation.y-toRender.getHeight()/2,null); //draws frmae centered on pixelLocation
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        scale *=d;
        sequence.scale(d);
    }
    
    /**
     * sets the scale of all frames of this sequence to a given scale
     * @param d new value to be scale, relative to the default scale of the images
     */
    @Override
    public void scaleTo(double d) {
        scale = d;
        sequence.scaleTo(d);
    }
    /**
     * returns current scaling of this sequence
     * @return current scale
     */
    @Override
    public double getScale() {
        return scale;
    }
    

    @Override
    public void disable() {
        sequence = null;
        super.disable();
    }

}

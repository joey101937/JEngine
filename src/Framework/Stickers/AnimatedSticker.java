/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;


import Framework.Coordinate;
import Framework.Game;
import Framework.GraphicalAssets.Graphic;
import Framework.Main;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Animated sticker
 * @author Joseph
 */
public class AnimatedSticker extends Sticker{
    public volatile BufferedImage[] sprites;
    public volatile int frameCount = 0;
    public volatile int currentFrame = 0;
    public volatile int frameDuration = 40;
    
    
    /**
     * @param g Game to spawn in
     * @param i array of Bufferedimages to be played for animation
     * @param c location to display
     * @param duration how long to display
     */
    public AnimatedSticker(Game g, BufferedImage[] i, Coordinate c, int duration) {
          super(g, null, c, duration);
          BufferedImage[] toUse = new BufferedImage[i.length];
          for(int index = 0; index < i.length; index++){
              toUse[index] = i[index];
          }
          i=toUse;
        if(i==null){
            System.out.println("Sticker attempted to be made with null image");
            this.disable();
            return;
        }
        sprites = i;
        frameCount = i.length;
        new AnimationHelper(this);
    }
    
    /**
     * sets current frame back to 0, restarting animation
     */
    public void resetCurrentFrame(){
        currentFrame = 0;
    }

    @Override
    public synchronized void render(Graphics2D g) {
        Graphics2D gToUse = (Graphics2D)g.create();
        try {
            if (sprites == null) {
                return;
            }
            try {
                image = sprites[currentFrame];
            } catch (ArrayIndexOutOfBoundsException e) {
                //sometimes thread scheduler will increase frame beyond maximum
                System.out.println("Animated sticker FrameIndexOutOfBounds, likely its fine");
                resetCurrentFrame();
                image = sprites[currentFrame];
            }
            centerCoordinate(image);
            gToUse.rotate(Math.toRadians(rotation), spawnLocation.x, spawnLocation.y);
            if (spawnLocation.x < 0 || spawnLocation.y < 0) {
                disable();     //if the coordinates are bad, dont render
            }
            if (!disabled) {
                if (image != null) {
                    gToUse.drawImage(image, renderLocation.x, renderLocation.y, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AnimationHelper implements Runnable {

        public AnimatedSticker host;

        public AnimationHelper(AnimatedSticker as){
            host = as;
            Thread t = new Thread(this);
            t.setName("Sticker animator " + numSticker);
            t.start();
        }

        @Override
        public void run() {
           while(!host.disabled){
               Main.wait(host.frameDuration);
               currentFrame++;
               if(currentFrame>=frameCount){
                  host.resetCurrentFrame();
               }
           }
        }
    }
    
    
    /**
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        for(int i = 0; i < sprites.length; i++){
            sprites[i] = Graphic.scaleImage(sprites[i],d);
        }
        scale *=d;
    }
    
    /**
     * sets the scale of all frames of this sequence to a given scale
     * @param d new value to be scale, relative to the default scale of the images
     */
    @Override
    public void scaleTo(double d) {
        for(int i = 0; i < sprites.length; i++){
            sprites[i] = Graphic.scaleImage(sprites[i],d/scale);
        }
        scale = d;
    }
    /**
     * returns current scaling of this sequence
     * @return current scale
     */
    @Override
    public double getScale() {
        return scale;
    }
    
    
        /**
     * reverses the animation sequence. 
     * first frame becomes last frame and vice versa
     */
    public void reverse(){
        BufferedImage[] newFrames = new BufferedImage[sprites.length];
        for(int i =0 ; i<sprites.length;i++){
            newFrames[sprites.length - (i + 1)] = sprites[i];
        }
        sprites = newFrames;
    }

    @Override
    public synchronized void disable() {
        if (sprites != null) {
            for (BufferedImage bi : sprites) {
                bi.flush();
                bi = null;
            }
            sprites = null;
        }
        super.disable();
    }

}

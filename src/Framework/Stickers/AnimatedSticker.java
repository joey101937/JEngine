/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;


import Framework.Coordinate;
import Framework.Main;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Animated sticker
 * @author Joseph
 */
public class AnimatedSticker extends Sticker{
    public BufferedImage[] sprites;
    public int frameCount = 0;
    public int currentFrame = 0;
    public int frameDuration = 40;
    
    
    /**
     * @param i array of Bufferedimages to be played for animation
     * @param c location to display
     * @param duration how long to display
     */
    public AnimatedSticker(BufferedImage[] i, Coordinate c, int duration) {
        super(null, c, duration);
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
        try{
            image = sprites[currentFrame];
        }catch(ArrayIndexOutOfBoundsException e){
            //sometimes thread scheduler will increase frame beyond maximum
            e.printStackTrace();
            resetCurrentFrame();
            image = sprites[currentFrame];
        }
        
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
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;

import Framework.Coordinate;
import Framework.Game;
import java.awt.image.BufferedImage;

/**
 * An animated sticker that disables itself after one animation cycle
 * Low duration may still end the animation early
 * @author Joseph
 */
public class OnceThroughSticker extends AnimatedSticker{
    
    
    /**
     * creates a once through sticker with a maximum duration
     * @param g game to spawn in
     * @param i array to hold frames of animation
     * @param c location in world
     * @param duration Max duration of this sticker
     */
    public OnceThroughSticker(Game g, BufferedImage[] i, Coordinate c, int duration) {
        super(g, i, c, duration);
    }
    
    /**
     * creates a once through sticker without maximum lifetime
     * @param g game to spawn in
     * @param i array to hold frames of animation
     * @param c location in world
     */
    public OnceThroughSticker(Game g, BufferedImage[] i, Coordinate c){
        super(g,i,c,999999);
    }
    
    @Override
    public void resetCurrentFrame(){
        currentFrame=0;
        disable();
    }
    
    
}

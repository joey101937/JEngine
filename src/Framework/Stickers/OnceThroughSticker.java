/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;

import Framework.Coordinate;
import Framework.Game;
import Framework.GraphicalAssets.Sequence;
import Framework.Main;
import java.awt.Graphics2D;

/**
 * An animated sticker that disables itself after one animation cycle
 * Low duration may still end the animation early
 * @author Joseph
 */
public class OnceThroughSticker extends AnimatedSticker{
    
    
    /**
     * creates a once through sticker with a maximum duration
     * @param g game to spawn in
     * @param s array to hold frames of animation
     * @param c location in world
     * @param duration Max duration of this sticker
     */
    public OnceThroughSticker(Game g, Sequence s, Coordinate c, int duration) {
        super(g, s, c, duration);
    }
    
    /**
     * creates a once through sticker without maximum lifetime
     * @param g game to spawn in
     * @param s sequence holding frames of animation
     * @param c location in world
     */
    public OnceThroughSticker(Game g, Sequence s, Coordinate c){
        super(g,s,c,999999);
    }
    
    @Override
    public void render(Graphics2D g) {
        if(sequence == null) {
            if(Main.debugMode) {
                System.out.println("Sticker attempting to render without sequence");
            }
            return;
        }
        if(System.currentTimeMillis() > creationTime + (sequence.getFrameCount() * sequence.getFrameDelay())) {
            disable();
            return;
        }
        super.render(g);
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Stickers;

import Framework.Coordinate;
import java.awt.image.BufferedImage;

/**
 * An animated sticker that disables itself after one animation cycle
 * Low duration may still end the animation early
 * @author Joseph
 */
public class OnceThroughSticker extends AnimatedSticker{
    
    public OnceThroughSticker(BufferedImage[] i, Coordinate c, int duration) {
        super(i, c, duration);
    }
    
    @Override
    public void resetCurrentFrame(){
        currentFrame=0;
        disable();
    }
}

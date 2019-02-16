/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;

/**
 * This class represents a visual effect not tied to any GameObject2 nor location
 * @author Joseph
 */
public abstract class IndependentEffect {
    /**
     * renders something to the game it has been applied to
     * @param g  
     */
    public abstract void render(Graphics2D g);
    /**
     * runs whenever a game this is applied to ticks. 
     */
    public abstract void tick();
}

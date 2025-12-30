/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.CoreLoop.Renderable;
import java.awt.Graphics2D;

/**
 * This class represents a visual effect not tied to any GameObject2 nor location
 * @author Joseph
 */
public abstract class IndependentEffect implements Renderable, java.io.Serializable{
    private static final long serialVersionUID = 1L;

    public int getZLayer() {
        return 10;
    };
    /**
     * renders something to the game it has been applied to
     * @param g
     */
    public abstract void render(Graphics2D g);
    /**
     * runs whenever a game this is applied to ticks.
     */
    public abstract void tick();

    /**
     * Called after deserialization to restore transient state.
     * Override this method in subclasses to restore any transient fields
     * or perform project-specific restoration logic (e.g., restoring selections).
     * Default implementation does nothing.
     *
     * @param game The game this effect has been added to
     */
    public void onPostDeserialization(Game game) {
        // Override in subclasses if needed
    }

    /**
     * Determines whether this effect should be serialized and saved.
     * Override this method to return false for effects that should persist
     * across save/load without being serialized (e.g., singleton managers).
     * Default implementation returns true.
     *
     * @return true if this effect should be saved, false if it should be skipped
     */
    public boolean shouldSerialize() {
        return true;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements.Examples;

import Framework.Game;
import Framework.GameObject2;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public abstract class SimpleRenderHelper {
    /**
     * Overrides the simple render of minimap example UI element for a particular item
     * @param item GameObeject to simple render
     */
    public abstract void simpleRender(GameObject2 item, Graphics2D g);

    /**
     * Called once after every object has been simple-rendered, with the graphics
     * still scaled to world coordinates. Lets a helper draw an overlay on top of
     * all objects (e.g. a fog-of-war layer). Default is a no-op.
     * @param game the game being rendered on the minimap
     * @param g graphics scaled to world coordinates
     */
    public void postRender(Game game, Graphics2D g) {
    }
}

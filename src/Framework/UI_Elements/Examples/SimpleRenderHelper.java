/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements.Examples;

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
}

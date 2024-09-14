/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.GameObject2;
import Framework.UI_Elements.Examples.SimpleRenderHelper;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class SimpleRenderHelperRTS extends SimpleRenderHelper {

    @Override
    public void simpleRender(GameObject2 go, Graphics2D g) {
        if(go instanceof RTSUnit unit && !unit.isRubble) {
            Color originalColor = g.getColor();
            if(unit.team == 0) {
                g.setColor(Color.green);
            } else {
                g.setColor(Color.red);
            }
            int shorterSide = Math.min(go.getWidth(), go.getHeight());
            g.fillOval(go.getPixelLocation().x - shorterSide/2, go.getPixelLocation().y - shorterSide/2, shorterSide, shorterSide);

        }
    }
    
}

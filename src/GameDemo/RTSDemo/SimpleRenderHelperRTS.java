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
            int longerSide = Math.max(go.getWidth(), go.getHeight());
            int borderDiameter = longerSide + 64;
            g.setColor(Color.BLACK);
            g.fillOval(go.getPixelLocation().x - borderDiameter/2, go.getPixelLocation().y - borderDiameter/2, borderDiameter, borderDiameter);
            if(unit.team == 0) {
                g.setColor(Color.green);
            } else {
                g.setColor(Color.red);
            }
            g.fillOval(go.getPixelLocation().x - longerSide/2, go.getPixelLocation().y - longerSide/2, longerSide, longerSide);
        }
    }
    
}

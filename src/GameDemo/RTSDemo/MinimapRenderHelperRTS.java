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
public class MinimapRenderHelperRTS extends SimpleRenderHelper {

    @Override
    public void simpleRender(GameObject2 go, Graphics2D g) {
        if(go instanceof RTSUnit unit && !unit.isRubble) {
            Color originalColor = g.getColor();
            int longerSide = Math.max(go.getWidth(), go.getHeight());
            int borderDiameter = longerSide + 64;
            g.setColor(Color.BLACK);
            g.fillOval(go.getPixelLocation().x - borderDiameter/2, go.getPixelLocation().y - borderDiameter/2, borderDiameter, borderDiameter);
            g.setColor(RTSUnit.getColorFromTeam(unit.team));
            g.fillOval(go.getPixelLocation().x - longerSide/2, go.getPixelLocation().y - longerSide/2, longerSide, longerSide);
        }
        if (go instanceof MinimapRenderable mr) {
            int longerSide = Math.min(go.getWidth(), go.getHeight());
            int borderDiameter = longerSide + 64;
            Color fillColor = mr.getMinimapColor();
            if (mr.getMinimapShape() == MinimapRenderable.Shape.RECTANGLE) {
                g.setColor(Color.BLACK);
                g.fillRect(go.getPixelLocation().x - borderDiameter/2, go.getPixelLocation().y - borderDiameter/2, borderDiameter, borderDiameter);
                g.setColor(fillColor);
                g.fillRect(go.getPixelLocation().x - longerSide/2, go.getPixelLocation().y - longerSide/2, longerSide, longerSide);
            } else {
                g.setColor(Color.BLACK);
                g.fillOval(go.getPixelLocation().x - borderDiameter/2, go.getPixelLocation().y - borderDiameter/2, borderDiameter, borderDiameter);
                g.setColor(fillColor);
                g.fillOval(go.getPixelLocation().x - longerSide/2, go.getPixelLocation().y - longerSide/2, longerSide, longerSide);
            }
        }
    }
    
}

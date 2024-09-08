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
//        if(go instanceof RTSUnit) {
//            Color originalColor = g.getColor();
//            if(((RTSUnit) go).team == 0) {
//                g.setColor(Color.green);
//            } else {
//                g.setColor(Color.red);
//            }
//            g.fillOval(go.getPixelLocation().x - go.getWidth()/2, go.getPixelLocation().y - go.getHeight()/2, go.getWidth(), go.getHeight());
//            g.setColor(originalColor);
//        }
    }
    
}

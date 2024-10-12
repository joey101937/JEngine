/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import static GameDemo.RTSDemo.SelectionBoxEffect.uncontrollableColor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 *
 * @author guydu
 */
public class SelectionBoxEffectAir extends IndependentEffect {
    
    @Override
    public int getZLayer() {
        // under ground units
        return 10;
    }

    @Override
    public void render(Graphics2D g) {
         drawSelectionCirclesAir(g);
    }

    @Override
    public void tick() {
       
    }
    
     private void drawSelectionCirclesAir(Graphics2D g) {
        g.setStroke(new BasicStroke(3));
        List<GameObject2> gos = RTSGame.game.getAllObjects();
        for (GameObject2 go : gos) {
            if (go instanceof RTSUnit unit) {
                if(unit.plane < 2) continue;
                g.setColor(SelectionBoxEffect.selectionColor);
                if (ExternalCommunicator.isMultiplayer && ExternalCommunicator.localTeam != unit.team) {
                    g.setColor(uncontrollableColor);
                }
                if (unit.isSelected() && !unit.isRubble) {
                    Coordinate coord = unit.getPixelLocation();
                    int sideLength = Math.max(unit.getWidth(), unit.getHeight());
                    g.drawOval(coord.x - sideLength / 2, coord.y - sideLength / 2, sideLength, sideLength);
                    var desiredLoc = unit.getDesiredLocation();
                    if (desiredLoc != null && Coordinate.distanceBetween(coord, desiredLoc) > sideLength / 2) {
                        Coordinate lineStart = Coordinate.nearestPointOnCircle(coord, desiredLoc, sideLength / 2);
                        g.drawLine(lineStart.x, lineStart.y, desiredLoc.x, desiredLoc.y);
                    }
                }
            }
        }
    }
    
}

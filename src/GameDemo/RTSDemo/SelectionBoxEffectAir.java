/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import static GameDemo.RTSDemo.SelectionBoxEffect.uncontrollableColor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class SelectionBoxEffectAir extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private transient Game game;
    private transient Stroke stroke3 = new BasicStroke(3);

    public SelectionBoxEffectAir(Game g) {
        game = g;
    }

    @Override
    public void onPostDeserialization(Game g) {
        // Restore game reference
        this.game = g;
        stroke3 = new BasicStroke(3);
    }
    
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
        g.setStroke(stroke3);
        for (RTSUnit unit : SelectionBoxEffect.selectedUnits) {
            if(unit.plane < 2) continue;
            if (unit.isSelected() && !unit.isRubble) {
                Color ringColor = SelectionBoxEffect.selectionColor;
                if (ExternalCommunicator.isMultiplayer && ExternalCommunicator.localTeam != unit.team) {
                    ringColor = uncontrollableColor;
                }
                Coordinate coord = unit.getRenderLocation().toCoordinate();
                int sideLength = Math.max(unit.getWidth(), unit.getHeight());
                VolatileImage ring = SelectionBoxEffect.getRingSprite(sideLength, ringColor);
                g.drawImage(ring, coord.x - ring.getWidth() / 2, coord.y - ring.getHeight() / 2, null);
                var desiredLoc = unit.getDesiredLocation();
                if (desiredLoc != null && Coordinate.distanceBetween(coord, desiredLoc) > sideLength / 2) {
                    g.setColor(ringColor);
                    Coordinate lineStart = Coordinate.nearestPointOnCircle(coord, desiredLoc, sideLength / 2);
                    g.drawLine(lineStart.x, lineStart.y, desiredLoc.x, desiredLoc.y);
                }
            }
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * draws the green rectangular selection box
 *
 * @author Joseph
 */
public class SelectionBoxEffect extends IndependentEffect {

    private static volatile Rectangle selectionZone = null;
    public static Set<RTSUnit> selectedUnits = new HashSet<>();
    public static final Color uncontrollableColor = new Color(.5f, .5f, .5f, .8f);

    @Override
    public int getZLayer() {
        // under ground units
        return 0;
    }

    @Override
    public void render(Graphics2D g) {
        drawSelectionBox(g);
        drawSelectionCirclesGround(g);
    }

    @Override
    public void tick() {
        Coordinate downLoc = RTSInput.getMouseDownLocation();
        Coordinate dragLoc = RTSInput.getMouseDraggedLocation();
        if (downLoc != null && dragLoc != null) {
            selectedUnits.forEach(x -> x.setSelected(false));
            selectedUnits.clear();
        }
        if (selectionZone != null) {
            for (GameObject2 go : RTSGame.game.getPreciseObjectsInArea(selectionZone)) {
                if (go instanceof RTSUnit) {
                    ((RTSUnit) go).setSelected(true);
                    selectedUnits.add((RTSUnit) go);
                }
            }
        }
    }

    private void drawSelectionBox(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        Color originalColor = g.getColor();
        g.setColor(Color.green);
        Coordinate downLoc = RTSInput.getMouseDownLocation();
        Coordinate dragLoc = RTSInput.getMouseDraggedLocation();
        if (downLoc != null && dragLoc != null) {
            int mlX = dragLoc.x;
            int mlY = dragLoc.y;
            int width = Math.abs(mlX - downLoc.x);
            int height = Math.abs(mlY - downLoc.y);
            boolean down = false;
            boolean up = false;
            boolean right = false;
            boolean left = false;
            int buffer = 2;     //minimum distance the cursor must move before the selection box is drawn
            if (downLoc.x > mlX + buffer) {
                left = true;
            }
            if (downLoc.x < mlX - buffer) {
                right = true;
            }
            if (downLoc.y < mlY - buffer) {
                down = true;
            }
            if (downLoc.y > mlY + buffer) {
                up = true;
            }
            if (left && up) {
                selectionZone = new Rectangle(mlX, mlY, width, height);
            }
            if (left && down) {
                selectionZone = new Rectangle(mlX, mlY - height, width, height);
            }
            if (right && down) {
                selectionZone = new Rectangle(downLoc.x, downLoc.y, width, height);
            }
            if (right && up) {
                selectionZone = new Rectangle(downLoc.x, downLoc.y - height, width, height);
            }
            if (selectionZone != null) {
                g.drawRect(selectionZone.x, selectionZone.y, selectionZone.width, selectionZone.height);
            }
        } else {
            selectionZone = null;
        }
    }

    private void drawSelectionCirclesGround(Graphics2D g) {
        g.setStroke(new BasicStroke(3));
        List<GameObject2> gos = RTSGame.game.getAllObjects();
        for (GameObject2 go : gos) {
            if (go instanceof RTSUnit unit) {
                if(unit.plane > 1) continue;
                g.setColor(Color.green);
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

    public static Rectangle getSelectionZone() {
        if (selectionZone == null) {
            return null;
        }
        return new Rectangle(selectionZone.x, selectionZone.y, selectionZone.width, selectionZone.height);
    }

}

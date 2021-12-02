/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.IndependentEffect;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * draws the green rectangular selection box
 *
 * @author Joseph
 */
public class SelectionBoxEffect extends IndependentEffect {

    private static volatile Rectangle selectionZone = null;
    public static ArrayList<RTSUnit> selectedUnits = new ArrayList<>();

    @Override
    public void render(Graphics2D g) {
        drawSelectionBox(g);       
    }

    @Override
    public void tick() {
        Coordinate downLoc = RTSInput.getMouseDownLocation();
        Coordinate dragLoc = RTSInput.getMouseDraggedLocation();
        if(downLoc != null && dragLoc != null) {
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
    
    private void drawSelectionBox(Graphics g) {
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
                selectionZone= new Rectangle(mlX, mlY - height, width, height);
            }
            if (right && down) {
                selectionZone= new Rectangle(downLoc.x, downLoc.y, width, height);
            }
            if (right && up) {
                selectionZone= new Rectangle(downLoc.x, downLoc.y - height, width, height);
            }
            if(selectionZone!=null)g.drawRect(selectionZone.x, selectionZone.y, selectionZone.width, selectionZone.height);
        }else{
            selectionZone=null;
        }
    }
    
    public static Rectangle getSelectionZone(){
        if(selectionZone == null)return null;
        return new Rectangle(selectionZone.x, selectionZone.y, selectionZone.width, selectionZone.height);
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.InputHandler;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class RTSInput extends InputHandler {
    private static Coordinate mouseDownLocation = null;
    private static Coordinate mouseDraggedLocation = null;
    
    @Override
    public void mousePressed(MouseEvent e){
        if(e.getButton() == 1) { //1 means left click
            System.out.println("test");
            for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                u.setSelected(false);
            }
            SelectionBoxEffect.selectedUnits.clear();
            mouseDownLocation = locationOfMouseEvent(e);
            mouseDraggedLocation = locationOfMouseEvent(e);
        }else if(e.getButton()==3){ //3 means right click
            for(RTSUnit u : SelectionBoxEffect.selectedUnits){
                u.setDesiredLocation(locationOfMouseEvent(e));
            }
        }

    }
    
    @Override
    public void mouseClicked(MouseEvent e){
        //selects one nidevidual unit at a clicked point
        ArrayList<GameObject2> grabbed =RTSGame.game.getObjectsIntersecting(new Hitbox(locationOfMouseEvent(e).toDCoordinate(),5));
        for(GameObject2 go : grabbed){
            if(go instanceof RTSUnit){
                RTSUnit unit = (RTSUnit)go;
                unit.setSelected(true);
                SelectionBoxEffect.selectedUnits.add(unit);
                return;
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e){
        mouseDownLocation = null;
        mouseDraggedLocation = null;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseDraggedLocation = locationOfMouseEvent(e);
    }

    /**
     * gets the location the mouse was first pressed down if its currenlty pressed down
     * @return 
     */
    public static Coordinate getMouseDownLocation(){
        if(mouseDownLocation==null){
            return null;
        }
        return mouseDownLocation.copy();
    }
    /**
     * gets location of mouse if currently being dragged
     * @return 
     */
    public static Coordinate getMouseDraggedLocation(){
        if(mouseDraggedLocation==null){
            return null;
        }
        return mouseDraggedLocation;
    }
    
    @Override
    public void keyPressed(KeyEvent e){
        //s is stop command
        if(e.getKeyChar()=='s'){
            for(RTSUnit u : SelectionBoxEffect.selectedUnits){
                u.setDesiredLocation(u.getPixelLocation());
            }
        }
    }
}

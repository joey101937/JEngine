/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Camera;
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
    public void mouseExited(MouseEvent e){
        getHostGame().getCamera().xVel=0;
        getHostGame().getCamera().yVel=0;
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
        panCamera(e);
    }

    @Override
    public void mouseMoved(MouseEvent e){
        panCamera(e);
    }
    
    private void panCamera(MouseEvent e){
        boolean up = false, down=false, left=false, right=false;
        Coordinate loc = locationOfMouseEvent(e);
        Camera cam = getHostGame().getCamera();
        loc.subtract(cam.getWorldLocation());
        if(loc.y < cam.getFieldOfView().height*.13) up=true; //top 10% of screen to scroll up
        if(loc.y > cam.getFieldOfView().height*.87) down =true;
        if(loc.x < cam.getFieldOfView().width*.13) left=true;
        if(loc.x > cam.getFieldOfView().width*.87) right = true;
        if(up){
            cam.yVel=1;
        }else if(down){
            cam.yVel=-1;
        }else{
            cam.yVel=0;
        }
        if(left){
            cam.xVel=1;
        }else if(right){
            cam.xVel=-1;
        }else{
            cam.xVel=0;
        }
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
    /**
     * WASD camera movement plus hotkey commands
     * @param e 
     */
    @Override
    public void keyPressed(KeyEvent e) {
        Camera cam = getHostGame().getCamera();
        switch (e.getKeyChar()) {
            case 'x':       //x for stop command
                for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                    u.setDesiredLocation(u.getPixelLocation());
                }
                break;
            case 'w':
                cam.yVel=1;
                break;
            case 'a':
                cam.xVel=1;
                break;
            case 's':
                cam.yVel=-1;
                break;
            case 'd':
                cam.xVel=-1;
                break;
            default:
                return;
        }
    }
    @Override
    public void keyReleased(KeyEvent e){
         Camera cam = getHostGame().getCamera();
        switch (e.getKeyChar()) {
            case 'w':
                if(cam.yVel>0){
                cam.yVel=0;
                }
                break;
            case 'a':
                if(cam.xVel>0){
                    cam.xVel=0;
                }
                break;
            case 's':
                if(cam.yVel<0){
                cam.yVel=0;
                }
                break;
            case 'd':
                 if(cam.xVel<0){
                    cam.xVel=0;
                }
                break;
            default:
                return;
        }
    }
    
}

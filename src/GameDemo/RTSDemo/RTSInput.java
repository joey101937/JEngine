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
import Framework.AsyncInputHandler;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Joseph
 */
public class RTSInput extends AsyncInputHandler {
    private static Coordinate mouseDownLocation = null;
    private static Coordinate mouseDraggedLocation = null;
    
    public static boolean wDown = false, aDown = false, sDown = false, dDown = false;
    
    @Override
    public void tick(){
        Camera cam = getHostGame().getCamera();
        double xVelocity = 0;
        double yVelocity = 0;
        if(wDown) yVelocity += 1;
        if(sDown) yVelocity -= 1;
        if(aDown) xVelocity += 1;
        if(dDown) xVelocity -= 1;
        
        cam.xVel = xVelocity;
        cam.yVel = yVelocity;
    }
    
    private Coordinate averageLocation(ArrayList<RTSUnit> input) {
        List<RTSUnit> livingMembers = input.stream().filter(x -> x.isAlive()).collect(Collectors.toList());
        Coordinate output = new Coordinate(0,0);
        livingMembers.forEach((item) -> {
            output.x += item.getPixelLocation().x;
            output.y += item.getPixelLocation().y;
        });
        output.x /= livingMembers.size();
        output.y /= livingMembers.size();
        return output;
    }
    
    @Override
    public void onMousePressed(MouseEvent e){
            if (e.getButton() == 1) { //1 means left click
            for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                u.setSelected(false);
            }
            SelectionBoxEffect.selectedUnits.clear();
            mouseDownLocation = locationOfMouseEvent(e);
            mouseDraggedLocation = locationOfMouseEvent(e);
        }else if(e.getButton()==3){ //3 means right click
            if(e.isControlDown()) {
                // all move to exact position of mouse click
                for(RTSUnit u : SelectionBoxEffect.selectedUnits){
                    u.setDesiredLocation(locationOfMouseEvent(e));
                }
            } else {
                // formation move
                Coordinate target = locationOfMouseEvent(e);
                Coordinate avgStartLocation = averageLocation(SelectionBoxEffect.selectedUnits);
                for(RTSUnit u : SelectionBoxEffect.selectedUnits){
                    Coordinate offset = new Coordinate(avgStartLocation.x - u.getPixelLocation().x, avgStartLocation.y - u.getPixelLocation().y);
                    u.setDesiredLocation(target.offsetBy(offset));
                }
            }
        }

    }
    
    @Override
    public void onMouseExited(MouseEvent e){
        getHostGame().getCamera().xVel=0;
        getHostGame().getCamera().yVel=0;
    }
    
    @Override
    public void onMouseClicked(MouseEvent e){
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
    public void onMouseReleased(MouseEvent e){
        mouseDownLocation = null;
        mouseDraggedLocation = null;
    }
    
    @Override
    public void onMouseDragged(MouseEvent e) {
        mouseDraggedLocation = locationOfMouseEvent(e);
        panCamera(e);
    }

    @Override
    public void onMouseMoved(MouseEvent e){
        // panCamera(e);
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
    public void onKeyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'x' -> {
                //x for stop command
                for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                    u.setDesiredLocation(u.getPixelLocation());
                }
            }
            case 'w' -> {
                wDown = true;
                sDown = false;
            }
            case 'a' -> {
                aDown = true;
                dDown = false;
            }
            case 's' -> {
                sDown = true;
                wDown = false;
            }
            case 'd' -> {
                dDown = true;
                aDown = false;
            }
        }
    }
    @Override
    public void onKeyReleased(KeyEvent e){
        switch (e.getKeyChar()) {
            case 'w' -> wDown = false;
            case 'a' -> aDown = false;
            case 's' -> sDown = false;
            case 'd' -> dDown = false;
        }
    }
    
}

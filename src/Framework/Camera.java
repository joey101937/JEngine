/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Controls the viewing frame location for the user
 * @author joey
 */
public class Camera {
    /**Topleft coordinate of the rendering window relative to topleft of canvas*/
    public DCoordinate location = new DCoordinate(0,0); //location of camera, 0,0 is top left. NOTE these will be negative
    public int camSpeed = 3;//how fast the camera moves
    public double xVel, yVel;  //camera velocity. Change in position each render
    private boolean readyToUpdate = false; //render only runs after a tick
    public GameObject2.MovementType movementType = GameObject2.MovementType.SpeedRatio;
    public boolean disableMovement = false;
    public int tickNumber = 0; //for debug usage
    public Game hostGame;
    private boolean trackingGameObject = false; //weather or not the camera is free or if the camera is tracking a target object
    private GameObject2 target = null;
    
    
    /**
     * determines if the camera is following its target.
     * note if set to false, this does not change target, only makes the camera
     * not follow that target anymore
     * @param b new value of tracking flag
     */
    public void setIsTackingTarget(boolean b){
        trackingGameObject = b;
    }
    /**
     * @return weather or not the camera is tracking its target. Note this does
     * NOT return weather or not the camera has a target to begin with
     */
    public boolean isTrackingTarget(){
        return trackingGameObject;
    }
    /**
     * returns the gameobject this camera is targeting.
     * Note the camera may not be actively following this target (check with
     * isTrackingTarget). 
     * @return target object; returns null if none
     */
    public GameObject2 getTarget(){
        return target;
    }
    
    
    public Camera(Game g){
        hostGame = g;
    }
    
    
    public void render(Graphics2D g){
        g.translate(location.x, location.y); //this runs regardless of ticks because it keeps the camera location still (g resets to 0,0 every render)
        if(!readyToUpdate) return;
        if(!disableMovement){
            updateLocation(g);
        }
        constrainCameraToWorld();
        readyToUpdate = false;
    }
    public void tick(){
        readyToUpdate=true;
        tickNumber++;
    }
   
    /**
     * updates the camera position based on either velocity or to follow target
     * @param g graphics for which this camera operates
     */
    private void updateLocation(Graphics2D g) {
        if(disableMovement)return;
        if (trackingGameObject && target != null) {
            location.x = -target.location.x + Game.windowWidth/Game.resolutionScaleX / 2;
            location.y = -target.location.y + Game.windowHeight/Game.resolutionScaleY / 2;           
            return;
        }
        switch (movementType) {
            case SpeedRatio:
            case RotationBased:
                double delta = 0.0;
                double totalVelocity = Math.abs(xVel) + Math.abs(yVel);
                if (totalVelocity != 0) {
                    delta = Math.abs(camSpeed / totalVelocity);
                }
                location.x += xVel * delta;
                location.y += yVel * delta;
                g.translate(location.x - g.getTransform().getTranslateX(), location.y - g.getTransform().getTranslateY());
                break;
            case RawVelocity:
                location.x += xVel;
                location.y += yVel;
                g.translate(location.x - g.getTransform().getTranslateX(), location.y - g.getTransform().getTranslateY());
                break;
        }
    }
    
    private void constrainCameraToWorld(){
        if(location.x > 0) location.x = 0;
        if(location.y > 0) location.y = 0;
        
        if(-location.x + hostGame.windowWidth/Game.resolutionScaleX > hostGame.getWorldWidth()) location.x = -1 * (hostGame.getWorldWidth()- hostGame.windowWidth/Game.resolutionScaleX);
        if(-location.y + hostGame.windowHeight/Game.resolutionScaleY > hostGame.getWorldHeight()) location.y = -1 * (hostGame.getWorldHeight() - hostGame.windowHeight/Game.resolutionScaleY);
        
        if(location.x > 0) location.x = 0;
        if(location.y > 0) location.y = 0;
    }
    
    /**
     * attempts to focus camera on a game object
     * @param obj  object to center on
     */
    public void setTarget(GameObject2 obj){
        trackingGameObject=true;
        location.x = -obj.location.x + Game.windowWidth/2;
        location.y = -obj.location.y + Game.windowHeight/2;
        target = obj;
    }
    
    public Rectangle getFieldOfView(){
        return new Rectangle((int)-location.x,(int)-location.y,(int)(Game.windowWidth/Game.resolutionScaleX),(int)(Game.windowHeight/Game.resolutionScaleY));
    }
    
}

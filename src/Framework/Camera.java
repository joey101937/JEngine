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
    /**Topleft coordinate of the rendering window relative to topleft of canvas NOTE this will be negative*/
    protected DCoordinate location = new DCoordinate(0,0); //location of camera, 0,0 is top left. NOTE these will be negative
    public int camSpeed = 3;//how fast the camera moves
    public double xVel, yVel;  //camera velocity. Change in position each render
    public GameObject2.MovementType movementType = GameObject2.MovementType.SpeedRatio;
    public boolean disableMovement = false;
    public int tickNumber = 0; //for debug usage
    public Game hostGame;
    private boolean trackingGameObject = false; //weather or not the camera is free or if the camera is tracking a target object
    private GameObject2 target = null;
    
    
    /**
     * gets location of this camera's top left point
     * @return 
     */
    public Coordinate getPixelLocation(){
        return location.toCoordinate().copy();
    }

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
     * Note the camera may not
     * be actively following this target (check with isTrackingTarget).
     *
     * @return target object; returns null if none
     */
    public GameObject2 getTarget() {
        return target;
    }

    public Camera(Game g) {
        hostGame = g;
    }

    /**
     * runs at the beginning of game render to keep camera at correct location
     * @param g runs at the beginning of game render to keep camera at correct location
     */
    public void render(Graphics2D g) {
        g.translate(location.x, location.y); //this runs regardless of ticks because it keeps the camera location still (g resets to 0,0 every render)   
    }

    public void tick() {
        tickNumber++;
        updateLocation();
        constrainCameraToWorld();
    }

    /**
     * updates the camera position based on either velocity or to follow target
     * @param g graphics for which this camera operates
     */
    private void updateLocation() {
        if (disableMovement) {
            return;
        }
        if (trackingGameObject && target != null) {
            location.x = -target.location.x + (hostGame.windowWidth / Game.resolutionScaleX) / hostGame.getZoom() / 2;
            location.y = -target.location.y + (hostGame.windowHeight / Game.resolutionScaleY) / hostGame.getZoom() / 2;
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
                break;
            case RawVelocity:
                location.x += xVel;
                location.y += yVel;          
                break;
        }
         constrainCameraToWorld();
    }
    /**
     * keeps camera from going out of bounds
     */
    private void constrainCameraToWorld(){
        if(location.x > 0) location.x = 0;
        if(location.y > 0) location.y = 0;
        
        if(-location.x + hostGame.windowWidth/Game.resolutionScaleX/hostGame.getZoom() > hostGame.getWorldWidth()) location.x = -1 * (hostGame.getWorldWidth()- (hostGame.windowWidth/Game.resolutionScaleX)/hostGame.getZoom());
        if(-location.y + hostGame.windowHeight/Game.resolutionScaleY/hostGame.getZoom() > hostGame.getWorldHeight()) location.y = -1 * (hostGame.getWorldHeight() - (hostGame.windowHeight/Game.resolutionScaleY)/hostGame.getZoom());
        
        if(location.x > 0) location.x = 0;
        if(location.y > 0) location.y = 0;
    }
    
    /**
     * attempts to focus camera on a game object
     * @param obj  object to center on
     */
    public void setTarget(GameObject2 obj){
        trackingGameObject=true;
        location.x = -obj.location.x + hostGame.windowWidth/2;
        location.y = -obj.location.y + hostGame.windowHeight/2;
        target = obj;
    }
    
    /**
     * returns rectangle object that represents the field of view of this camera
     * @return rectangle object that represents the field of view of this camera
     */
    public Rectangle getFieldOfView(){
        return new Rectangle((int)-location.x,(int)-location.y,(int)(hostGame.windowWidth/Game.resolutionScaleX/hostGame.getZoom()),(int)(hostGame.windowHeight/Game.resolutionScaleY/hostGame.getZoom()));
    }
    
    /**
     * returns the location in the gameworld where the top-left corner of the window is
     * @return the location in the gameworld where the top-left corner of the window is
     */
    public DCoordinate getWorldLocation(){
        return new DCoordinate(-location.x,-location.y);
    }
    
    /**
     * gets point at which the camera is currently centered on
     * @return 
     */
    public Coordinate getCenterPoint(){
        Coordinate out = this.getWorldLocation().toCoordinate();
        out.x+=hostGame.windowWidth/2;
        out.y+=hostGame.windowHeight/2;
        return out;
    }
    
    /**
     * Instantly pans camera to center on given cooridinate 
     * @param point point to go to
     */
    public void centerOn(Coordinate point) {
        if(isTrackingTarget())return;
        location.x = -point.x + (hostGame.windowWidth / 2)/hostGame.getZoom()/Game.resolutionScaleX;
        location.y = -point.y + (hostGame.windowHeight / 2)/hostGame.getZoom()/Game.resolutionScaleY;
        constrainCameraToWorld();
    }
}

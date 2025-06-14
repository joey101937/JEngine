/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import static Framework.GameObject2.MovementType.RawVelocity;
import static Framework.GameObject2.MovementType.RotationBased;
import static Framework.GameObject2.MovementType.SpeedRatio;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Controls the viewing frame location for the user
 * @author joey
 */
public class Camera {
    /**Topleft coordinate of the rendering window relative to topleft of canvas NOTE this will be negative*/
    public DCoordinate location = new DCoordinate(0,0); //location of camera, 0,0 is top left. NOTE these will be negative
    public int camSpeed = 3;//how fast the camera moves
    public double xVel, yVel;  //camera velocity. Change in position each render
    public GameObject2.MovementType movementType = GameObject2.MovementType.SpeedRatio;
    public boolean disableMovement = false;
    public int tickNumber = 0; //for debug usage
    public Game hostGame;
    private boolean trackingGameObject = false; //weather or not the camera is free or if the camera is tracking a target object
    private GameObject2 target = null;
    private DCoordinate renderLocation = new DCoordinate(0,0);
    
    
    /**
     * gets location of this camera's top left point
     * @return 
     */
    public Coordinate getPixelLocation(){
        return location.toCoordinate();
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
    
    public DCoordinate getRenderLocation() {
        return renderLocation.copy();
    }
    
    /**
     * returns negative value because camera coords are negative
     * @return 
     */
    private DCoordinate calcRenderLocation(){
        DCoordinate renderOffset = new DCoordinate(0,0);
        if(Main.enableLerping) {
            if(this.isTrackingTarget() && this.target != null) {
                Coordinate targetPosistion = target.getRenderLocation().invert();
                DCoordinate centeringOffset = new DCoordinate((double)hostGame.windowWidth/2, (double)hostGame.windowHeight/2).scale(1/hostGame.getZoom());
                
                return constrainCameraToWorld(targetPosistion.add(centeringOffset).toDCoordinate());
            } else {
                // camera lerping coming soon
                // renderOffset.add(getMovementNextTick().scale(hostGame.getPercentThroughTick())).invert();
            }
        }
        return constrainCameraToWorld(renderOffset.add(location));
    }

    /**
     * runs at the beginning of game render to keep camera at correct location
     * @param g runs at the beginning of game render to keep camera at correct location
     */
    public void render(Graphics2D g) {
        this.renderLocation = calcRenderLocation();
        g.translate(this.renderLocation.x, this.renderLocation.y); //this runs regardless of ticks because it keeps the camera location still (g resets to 0,0 every render)   
    }

    public void tick() {
        tickNumber++;
        updateLocation();
        constrainCameraToWorld(location);
    }
    
    public DCoordinate getMovementNextTick() {
        DCoordinate toMove = new DCoordinate(0,0);
        if (disableMovement) {
            return toMove;
        }
        
        switch (movementType) {
            case SpeedRatio:
            case RotationBased:
                double delta = 0.0;
                double totalVelocity = Math.abs(xVel) + Math.abs(yVel);
                if (totalVelocity != 0) {
                    delta = Math.abs(camSpeed / totalVelocity);
                }
                toMove.x += xVel * delta;
                toMove.y += yVel * delta;
                break;
            case RawVelocity:
                toMove.x += xVel;
                toMove.y += yVel;          
                break;
        }
        return toMove;
    }

    /**
     * updates the camera position based on either velocity or to follow target
     */
    private void updateLocation() {
        if (disableMovement) {
            return;
        }
        if (trackingGameObject && target != null) {
            var targetLocation = target.getLocation();
            location.x = -targetLocation.x + (hostGame.windowWidth / Game.resolutionScaleX) / hostGame.getZoom() / 2;
            location.y = -targetLocation.y + (hostGame.windowHeight / Game.resolutionScaleY) / hostGame.getZoom() / 2;
            location = constrainCameraToWorld(location);
            return;
        }
        location.add(getMovementNextTick());
        location = constrainCameraToWorld(location);
    }
    /**
     * returns copy of input. if the input is out of bounds, the copy will be adjusted to be in bounds
     */
    private DCoordinate constrainCameraToWorld(DCoordinate input){
        DCoordinate value = input.copy();
        if(value.x > 0) value.x = 0;
        if(value.y > 0) value.y = 0;
        
        if(-value.x + hostGame.windowWidth/Game.resolutionScaleX/hostGame.getZoom() > hostGame.getWorldWidth()) value.x = -1 * (hostGame.getWorldWidth()- (hostGame.windowWidth/Game.resolutionScaleX)/hostGame.getZoom());
        if(-value.y + hostGame.windowHeight/Game.resolutionScaleY/hostGame.getZoom() > hostGame.getWorldHeight()) value.y = -1 * (hostGame.getWorldHeight() - (hostGame.windowHeight/Game.resolutionScaleY)/hostGame.getZoom());
        
        if(value.x > 0) value.x = 0;
        if(value.y > 0) value.y = 0;
        
        return value;
    }
    
    /**
     * attempts to focus camera on a game object
     * @param obj  object to center on
     */
    public void setTarget(GameObject2 obj){
        trackingGameObject=true;
        var objLocation = obj.getLocation();
        location.x = -objLocation.x + hostGame.windowWidth/2;
        location.y = -objLocation.y + hostGame.windowHeight/2;
        target = obj;
    }
    
    /**
     * returns rectangle object that represents the field of view of this camera
     * @return rectangle object that represents the field of view of this camera
     */
    public Rectangle getFieldOfView(){
        return new Rectangle(
                (int)-location.x,
                (int)-location.y,
                (int)(hostGame.windowWidth/Game.resolutionScaleX/hostGame.getZoom()),
                (int)(hostGame.windowHeight/Game.resolutionScaleY/hostGame.getZoom())
        );
    }
    
    /**
     * returns the location in the gameworld where the top-left corner of the window is
     * @return the location in the gameworld where the top-left corner of the window is
     */
    public DCoordinate getWorldLocation(){
        return new DCoordinate(-location.x,-location.y);
    }
    
    /**
     * Returns the top left corner's world coord when rendering. This will match getWorldLocation
     * unless the camera is lerping (not yet implemented). If you want to consistently render something relative to the camera, use this.
     * @return location in world where the camera is rendering
     */
    public DCoordinate getWorldRenderLocation() {
        return getRenderLocation().invert();
    };
    
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
        location = constrainCameraToWorld(location);
    }
}

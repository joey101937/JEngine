/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameDemo.GameObject2;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Controls the viewing frame location for the user
 * @author joey
 */
public class Camera {
    /**Topleft coordinate of the rendering window relative to topleft of canvas*/
    public static DCoordinate location = new DCoordinate(0,0); //location of camera, 0,0 is top left. NOTE these will be negative
    public static int camSpeed = 3;//how fast the camera moves
    public static double xVel, yVel;  //camera velocity. Change in position each render
    private static boolean readyToUpdate = false; //render only runs after a tick
    public static GameObject2.MovementType movementType = GameObject2.MovementType.SpeedRatio;
    public static boolean disableMovement = false;
    public static int tickNumber = 0; //for debug usage
    
    public static void render(Graphics2D g){
        g.translate(Camera.location.x, Camera.location.y); //this runs regardless of ticks because it keeps the camera location still (it resets to 0,0 every render)
        if(!readyToUpdate) return;
        if(!disableMovement){
           updateLocation(g);
           focusOn(Window.mainWindow.currentGame.testObject); //keep camera following our sample character
        }
        constrainCameraToWorld();
        readyToUpdate = false;
    }
    public static void tick(){
        readyToUpdate=true;
        tickNumber++;
    }
   
    /**
     * updates the camera position based on camera velocity
     * @param g graphics for which this camera operates
     */
    private static void updateLocation(Graphics2D g) {
        switch (movementType) {
            case SpeedRatio:
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
    
    private static void constrainCameraToWorld(){
        if(location.x > 0) location.x = 0;
        if(location.y > 0) location.y = 0;
        if(-location.x + Game.windowWidth > Game.worldWidth) location.x = -1 * (Game.worldWidth- Game.windowWidth);
        if(-location.y + Game.windowHeight > Game.worldHeight) location.y = -1 * (Game.worldHeight - Game.windowHeight);
    }
    
    /**
     * attempts to focus camera on a game object
     * @param obj 
     */
    public static void focusOn(GameObject2 obj){
        location.x = -obj.location.x + Game.windowWidth/2;
        location.y = -obj.location.y + Game.windowHeight/2;
    }
    
    public static Rectangle getFieldOfView(){
        return new Rectangle((int)-location.x,(int)-location.y,Game.windowWidth,Game.windowHeight);
    }
    
}

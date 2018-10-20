/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;

/**
 * Controls the viewing frame location for the user
 * @author joey
 */
public class Camera {
    /**Topleft coordinate of the rendering window relative to topleft of canvas*/
    public static DCoordinate location = new DCoordinate(0,0); 
    public static int camSpeed = 3;//how fast the camera moves
    public static double xVel, yVel;  //camera velocity. Change in position each render
    private static boolean readyToUpdate = false; //render only runs after a tick
    public static void render(Graphics2D g){
        g.translate(Camera.location.x, Camera.location.y); //this runs regardless because it keeps the camera location still (it resets to 0,0 every render)
        if(!readyToUpdate) return;
        double delta = 0.0;
        double totalVelocity = Math.abs(xVel) + Math.abs(yVel);
        if(totalVelocity!=0)delta = Math.abs(camSpeed/totalVelocity);
        location.x+=xVel*delta;
        location.y+=yVel*delta;
         g.translate(location.x - g.getTransform().getTranslateX(), location.y - g.getTransform().getTranslateY());
        if(location.x>0)location.x=0; //prevent camera from going out of bounds 
        if(location.y>0)location.y=0;
        readyToUpdate = false;
        System.out.println("CAMERA: " + xVel + " " + yVel + " " + delta);
    }
    public static void tick(){
        readyToUpdate=true;
    }
}

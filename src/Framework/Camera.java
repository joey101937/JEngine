/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;

/**
 *
 * @author joey
 */
public class Camera {
    /**Topleft coordinate of the rendering window relative to topleft of canvas*/
    public static Coordinate location = new Coordinate(0,0); 
    public static int xVel, yVel;  //camera velocity. Change in position each render
    public static void render(Graphics2D g){
        g.translate(Camera.location.x, Camera.location.y);
        g.translate(xVel, yVel);
        location.x+=xVel;
        location.y+=yVel;
        if(location.x>0)location.x=0; //prevent camera from going out of bounds
        if(location.y>0)location.y=0;
        
    }
}

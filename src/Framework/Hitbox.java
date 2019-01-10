/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import static Framework.Hitbox.Type.box;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * Htbox class represents either a 2D polygon or circle in the world, able to 
 * detect when they intersect eachother and are therefore used for collision detection
 * Hitboxes are either standalone or attached to gameobject2s.
 * 
 * polygonal hitboxes take 4 coordinate vertices. these are offsets if connected to a gameobject or raw locations if not
 * circular hitboxes take only a radius parameter.
 * 
 * PERFORMANCE NOTE: Circular hitboxes are significantly faster to process than polygonal
 * @author joey
 */
public class Hitbox {

    public void setVertices(Coordinate[] verts) {
        this.vertices = verts;
    }

    public static enum Type {
        box, circle
    };
    public GameObject2 host;
    public Game hostGame;
    public Type type = box;
    private double rotation = 0;
    private  DCoordinate staticCenter = new DCoordinate(0, 0); //center point for if there is no host
    public double radius = 0.0; //radius if circle hitbox
    //private double rotation = 0.0;
    /**
     * topLeft, topRight, botLeft, botRight
     */
    private Coordinate[] vertices = null;            //topLeft, topRight, botLeft, botRight
    private double farthestRange = 0; //stores the distance of the farthest vertex, used for optimising performance
    private double shortestRange = 9999999; //shortest distance from center to a flat side
    /**
     * used to set farthestRange. used for optimizing
     */
    private void setFarthestAndShortest(){
        for(Coordinate c : vertices){
            if(c.distanceFrom(getCenter())>farthestRange){
                farthestRange = c.distanceFrom(getCenter());
            }
            if(Math.abs(c.x)<shortestRange){
                shortestRange = Math.abs(c.x);
            }
            if(Math.abs(c.y)<shortestRange){
                shortestRange=Math.abs(c.y);
            }
        }
    }
    
    public double getRotation(){
        return rotation;
    }
    public void rotate(double degrees){
        rotation+=degrees;
    }
    public void rotateTo(double newRotation){
        rotate(newRotation-rotation);
    }
    
    
    public DCoordinate getCenter(){
        if(host==null)return staticCenter;
        else return host.location.copy();
    }
    

    /**
     * Creates a circular hitbox at location given with radius given
     *
     * @param c location hitbox is centered on
     * @param radius radius of circle
     */
    public Hitbox(DCoordinate c, double radius) {
        staticCenter = c;
        this.radius = radius;
        this.type = Type.circle;
    }

    /**
     * creates a rectangle hitbox with given corner points topLeft, topRight,
     * botLeft, botRight. In that order!
     * @param given an array of 4 coordinates that represents the vertices of the polygon
     * 
     */
    public Hitbox(Coordinate[] given) {
        if (given.length != 4) {
            System.out.println("ERROR: Bad argument for hitbox vertices- length should be 4");
        }
        for (Coordinate cor : given) {
            if (given == null) {
                System.out.println("WARNING CREATING HITBOX WITH NULL VERTEX");
            }
        }
        //set center to avg point
        int avgX = 0, avgY = 0;
        for (Coordinate c : given) {
            avgX += c.x;
            avgY += c.y;
        }
        staticCenter = new DCoordinate(avgX/4, avgY/4);
        for (Coordinate c : given) {
            c = c.copy();//dont modify given coordinates
            c.subtract(staticCenter);
        }
        
        vertices = given;
        type = Type.box;
        setFarthestAndShortest();
    }

    /**
     * creates circular hitbox attached to gameObject
     *
     * @param go host unit
     * @param radius radius of circle
     */
    public Hitbox(GameObject2 go, double radius) {
        host = go;
        type = Type.circle;
        this.radius = radius;
    }

    /**
     * Creates a rectangle hitbox attached to game object NOTE VERTICES ARE NOW
     * OFFSETS NOT POINTS
     *
     * @param go host unit
     * @param verts offset locations of corners relative to go location. Ie:
     * (1,1) is location of go + 1 to the right and 1 up
     */
    public Hitbox(GameObject2 go, Coordinate[] verts) {
        this.vertices = verts;
        host = go;
        type = Type.box;
        setFarthestAndShortest();
    }

    private Coordinate adjustForRotation(Coordinate c) {
        int oldX = c.x;
        int oldY = c.y;
        double rotation = Math.toRadians(getRotation());
        int x = (int) (oldX * Math.cos(rotation) - oldY * Math.sin(rotation));
        int y = (int) (oldX * Math.sin(rotation) + oldY * Math.cos(rotation));
        return new Coordinate(x,y);
    }

    /**
     * coordinates for leftside line, used with box type
     * @return leftside line coordinates x1,y1,x2,y2
     */
    private double[] leftSide() {
        Coordinate hostLoc = new Coordinate(getCenter());      
        double[] output = {adjustForRotation(vertices[0]).x+ hostLoc.x, adjustForRotation(vertices[0]).y+ hostLoc.y, adjustForRotation(vertices[2]).x+ hostLoc.x, adjustForRotation(vertices[2]).y+ hostLoc.y};
        return output;
    }

    /**
     * coordinates for topside line used with box type
     * @return  line coordinates x1,y1,x2,y2
     */
    private double[] topSide() {
        Coordinate hostLoc = new Coordinate(getCenter());    
        double[] output = {adjustForRotation(vertices[0]).x+ hostLoc.x,adjustForRotation(vertices[0]).y+ hostLoc.y, adjustForRotation(vertices[1]).x+ hostLoc.x, adjustForRotation(vertices[1]).y+ hostLoc.y};
        return output;
    }

    /**
     * coordinates for right side line used with box type
     * @return  line coordinates x1,y1,x2,y2
     */
    private double[] rightSide() {
        Coordinate hostLoc = new Coordinate(getCenter());    
        double[] output = {adjustForRotation(vertices[1]).x+ hostLoc.x, adjustForRotation(vertices[1]).y+ hostLoc.y, adjustForRotation(vertices[3]).x+ hostLoc.x,adjustForRotation(vertices[3]).y+ hostLoc.y};
        return output;
    }

    /**
     * coordinates for bottom side line used with box type
     * @return  line coordinates x1,y1,x2,y2
     */
    private double[] botSide() {
        Coordinate hostLoc = new Coordinate(getCenter());    
        double[] output = { adjustForRotation(vertices[2]).x+ hostLoc.x,  adjustForRotation(vertices[2]).y+ hostLoc.y,  adjustForRotation(vertices[3]).x+ hostLoc.x,  adjustForRotation(vertices[3]).y+ hostLoc.y};
        return output;
    }

    /**
     * weather or not the given two lines intersect
     * @param line1 first line to eval
     * @param line2 second line to eval
     * @return weather or not the two lines intersect
     */
    private boolean linesIntersect(double[] line1, double[] line2) {
        return Line2D.linesIntersect(line1[0], line1[1], line1[2], line1[3], line2[0], line2[1], line2[2], line2[3]);
    }

    /**
     * returns true if this hitbox touches the given hitbox
     * @param other hitbox to compare to
     * @return weather or not they overlap
     */
    public boolean intersects(Hitbox other) {
        if(other==null || other==this){
            return false;
        }
        double distance = getCenter().distanceFrom(other.getCenter());
        //box on box collision
        if(this.type==Type.box && other.type==Type.box){
            if(distance>this.farthestRange && distance > other.farthestRange)return false; //too far to possibly intersect
        if (linesIntersect(rightSide(), other.leftSide()) || linesIntersect(rightSide(), other.rightSide())
                || linesIntersect(rightSide(), other.topSide()) || linesIntersect(rightSide(), other.botSide())) {
            //our right side intersects one of their lines
            return true;
        }
        if (linesIntersect(leftSide(), other.leftSide()) || linesIntersect(leftSide(), other.rightSide())
                || linesIntersect(leftSide(), other.topSide()) || linesIntersect(leftSide(), other.botSide())) {
            //our left side intersects one of their lines
            return true;
        }
        if (linesIntersect(topSide(), other.leftSide()) || linesIntersect(topSide(), other.rightSide())
                || linesIntersect(topSide(), other.topSide()) || linesIntersect(topSide(), other.botSide())) {
            //our top side intersects one of their lines
            return true;
        }
        if (linesIntersect(botSide(), other.leftSide()) || linesIntersect(botSide(), other.rightSide())
                || linesIntersect(botSide(), other.topSide()) || linesIntersect(botSide(), other.botSide())) {
            //our bot side intersects one of their lines
            return true;
        }
        return false;
        }
        if (this.type == Type.box && other.type == Type.circle) {
            //box on circle collision
            if (distance <= other.radius + this.shortestRange) {
                return true;
            }
            for (Coordinate c : vertices) {
                Coordinate corner = this.adjustForRotation(c);
                corner.add(getCenter());
                if (corner.distanceFrom(other.getCenter()) <= other.radius) {
                    return true;
                }
            }
            return false;
        }
         if (this.type == Type.circle && other.type == Type.box) {
             //circle on box collision
            if (distance <= radius + other.shortestRange) {
                return true;
            }
            for (Coordinate c : other.vertices) {
                Coordinate corner = other.adjustForRotation(c);
                corner.add(other.getCenter());
                if (corner.distanceFrom(getCenter()) <= radius) {
                    return true;
                }
            }
            return false;
        }
        if(this.type==Type.circle && other.type == Type.circle){
            if(distance < radius + other.radius){
                return true;
            }
            return false;
        }
        System.out.println("error with intersecting, types are " + type + " " + other.type);
        return false;
    }
    /**
     * if this hitbox would intersect another hitbox if it was moved based on input
     * @param other hitbox to compare to
     * @param velocity contains the amount to move this hitbox
     * @return weather or not the hitbox would be intersecting another if moved
     */
    public boolean intersectsIfMoved(Hitbox other, Coordinate velocity) {
        double saftyScaler = 2; //how much we scale the velocity to account for extramovement
                                   //large scaler = less chance of overlap but farther apart units must stay
        staticCenter.x += velocity.x * saftyScaler;
        staticCenter.y += velocity.y * saftyScaler;
        if (host != null) {
            host.location.x += velocity.x * saftyScaler;
            host.location.y += velocity.y * saftyScaler;
        }
        boolean result = intersects(other);
            if (host != null) {
            host.location.x -= velocity.x * saftyScaler;
            host.location.y -= velocity.y * saftyScaler;
        }
         staticCenter.x -= velocity.x * saftyScaler;
        staticCenter.y -= velocity.y * saftyScaler;
        return result;
    }
    
    
    
    public boolean intersectsIfRotated(Hitbox other, double possibleRotation){
        rotate(possibleRotation);
        boolean result = intersects(other);
        rotate(-possibleRotation);
        return result;
    }    
    


    public void render(Graphics2D g){
        Color col = g.getColor();
        if(this.type == Type.box){ 
            //render all sides
            if(host==null){
                g.drawLine((int) leftSide()[0], (int) leftSide()[1], (int) leftSide()[2], (int) leftSide()[3]);
                g.drawLine((int) rightSide()[0], (int) rightSide()[1], (int) rightSide()[2], (int) rightSide()[3]);
                g.drawLine((int) topSide()[0], (int) topSide()[1], (int) topSide()[2], (int) topSide()[3]);
                g.drawLine((int) botSide()[0], (int) botSide()[1], (int) botSide()[2], (int) botSide()[3]);
            } else {
                if(!host.isSolid){
                    g.setColor(Color.blue);
                }
                //render all sides based on host location
                Coordinate hostLoc = host.getPixelLocation();
                g.drawLine((int) leftSide()[0], (int) leftSide()[1], (int) leftSide()[2], (int) leftSide()[3]);
                g.drawLine((int) rightSide()[0], (int) rightSide()[1], (int) rightSide()[2], (int) rightSide()[3]);
                g.drawLine((int) topSide()[0], (int) topSide()[1], (int) topSide()[2], (int) topSide()[3]);
                g.drawLine((int) botSide()[0], (int) botSide()[1], (int) botSide()[2], (int) botSide()[3]);             
            }
        } else if (type == Type.circle) {
            if(host!=null && !host.isSolid){
                g.setColor(Color.blue);
            }
            g.drawOval((int) (getCenter().x-radius), (int) (getCenter().y-radius), (int) radius*2, (int) radius*2);
        }
        g.setColor(col);
    }

}

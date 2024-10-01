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
    public Coordinate[] vertices = null;            //topLeft, topRight, botLeft, botRight
    private double farthestRange = 0; //stores the distance of the farthest vertex, used for optimising performance
    private double shortestRange = 9999999; //shortest distance from center to a flat side
    private String vertsKey_midpoint = ""; // string made up of vert coordinates to detect changes for recalculating midpoiunt
    private String vertsKey_shortest_farthest = ""; // string made up of vert coordinates to detect changes for recalculating shortest/longest

    private Coordinate[] cachedMidpoints;
    
    private String generateVertsKey () {
        if(vertices == null) return "";
        String output = "";
        for(Coordinate c : vertices) {
            output += c;
        }
        return output;
    }
    
    private Coordinate[] getMidpoints() {
        String thisKey = generateVertsKey();
        if(thisKey.equals(vertsKey_midpoint)) return cachedMidpoints;
        Coordinate[] sideMidpoints = new Coordinate[4];
        sideMidpoints[0] = new Coordinate( // top midpoint
            (vertices[0].x + vertices[1].x) /2,
            (vertices[0].y + vertices[1].y) /2
        );
        sideMidpoints[1] = new Coordinate( // right midpoint
            (vertices[1].x + vertices[3].x) /2,
            (vertices[1].y + vertices[3].y) /2
        );
        sideMidpoints[2] = new Coordinate( // bottom midpoint
            (vertices[2].x + vertices[3].x) /2,
            (vertices[2].y + vertices[3].y) /2
        );
        sideMidpoints[3] = new Coordinate( // left midpoint
            (vertices[2].x + vertices[0].x) /2,
            (vertices[2].y + vertices[0].y) /2
        );
        cachedMidpoints = sideMidpoints;
        vertsKey_midpoint = thisKey;
        return sideMidpoints;
    }
    
    /**
     * used to set farthestRange. used for optimizing
     */
    protected final void updateFarthestAndShortest(){
        if(type == Type.circle) {
            shortestRange = radius;
            farthestRange = radius;
            return;
        }
        String thisVertsKey = generateVertsKey();
        if(thisVertsKey.equals(vertsKey_shortest_farthest)) return; // already up to date
        if(vertices == null) return;
        for(Coordinate c : vertices){
            c = c.copy();
            c.add(getCenter());
            double distance = c.distanceFrom(getCenter());
            if(distance>farthestRange){
                farthestRange = distance;
            }
            if(distance<shortestRange){
                shortestRange = distance;
            }
        }
        Coordinate[] sideMidpoints = getMidpoints();
        for(Coordinate c : sideMidpoints){
            c = c.copy();
            c.add(getCenter());
            double distance = c.distanceFrom(getCenter());
            if(distance > farthestRange){
                farthestRange = distance;
            }
            if(distance < shortestRange){
                shortestRange = distance;
            }
            if(distance < shortestRange){
                shortestRange = distance;
            }
        }
        vertsKey_shortest_farthest = thisVertsKey;
    }
    
    /**
     * given a circle, find the closest point on that circle to a nother target point
     * @param target target point
     * https://www.youtube.com/watch?v=aHaFwnqH5CU
     * @return 
     */
    private DCoordinate findClosestPointOnCircleFromPoint(DCoordinate target) {
        DCoordinate center = getCenter();
        DCoordinate targetOffset = target.copy().subtract(center);
        double angle = Math.atan(targetOffset.y / targetOffset.x);
        double x = Math.cos(angle) * radius;
        double y = Math.sin(angle) * radius;
        
        if (targetOffset.x < 0) {
            x *= -1;
            y *= -1;
        }
        return new DCoordinate(x + center.x, y + center.y);
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
        else return host.getCenterForCollisionSliding().copy();
    }
    
    public void setStaticCenter(DCoordinate sc) {
        this.staticCenter = sc;
    }
    
    private DCoordinate[] getRenderVerts() {
        double[] leftSide = leftSide();
        double[] rightSide = rightSide();
        DCoordinate[] renderVerts = new DCoordinate[4];
        renderVerts[0] = new DCoordinate(leftSide[0],leftSide[1]);
        renderVerts[1] = new DCoordinate(leftSide[2],leftSide[3]);
        renderVerts[2] = new DCoordinate(rightSide[0],rightSide[1]);
        renderVerts[3] = new DCoordinate(rightSide[2],rightSide[3]);
        return renderVerts;
    }
    
    public boolean containsPoint(Coordinate p){
        if(type == Type.box){
            DCoordinate[] renderVerts = getRenderVerts();
            boolean leftCheck = false,
                    rightCheck = false,
                    aboveCheck = false,
                    belowCheck = false;
            for(DCoordinate dc : renderVerts) {
                if(dc.x > p.x) rightCheck = true;
                if(dc.x < p.x) leftCheck = true;
                if(dc.y > p.y) aboveCheck = true;
                if(dc.y < p.y) belowCheck = true;
            }
            return rightCheck && leftCheck && belowCheck && aboveCheck;
              //return p.distanceFrom(getCenter())<=(this.shortestRange);
        }else{
            //circle
            return p.distanceFrom(getCenter())<=(radius);
        }
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
        updateFarthestAndShortest();
    }
    
        /**
     * Creates a circular hitbox at location given with radius given
     *
     * @param c location hitbox is centered on
     * @param radius radius of circle
     */
    public Hitbox(Coordinate c, double radius) {
        staticCenter = c.toDCoordinate();
        this.radius = radius;
        this.type = Type.circle;
        updateFarthestAndShortest();
    }

    /**
     * creates a rectangle hitbox with given corner points topLeft, topRight,
     * botLeft, botRight. In that order!
     * @param given an array of 4 coordinates that represents the vertices of the polygon
     * 
     */
    public Hitbox(Coordinate[] given) {
        if (given.length != 4) {
            throw new IllegalArgumentException("ERROR: Bad argument for hitbox vertices array: length should be 4, not " + given.length);
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
        Coordinate[] given2 = new Coordinate[given.length];
        for(int i = 0 ; i < given.length; i++){
            given2[i]=given[i].copy().subtract(staticCenter);
        }
        vertices = given2;
        type = Type.box;
        updateFarthestAndShortest();
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
        updateFarthestAndShortest();
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
        Coordinate centerLoc = new Coordinate(getCenter());      
        double[] output = {adjustForRotation(vertices[0]).x+ centerLoc.x, adjustForRotation(vertices[0]).y+ centerLoc.y, adjustForRotation(vertices[2]).x+ centerLoc.x, adjustForRotation(vertices[2]).y+ centerLoc.y};
        return output;
    }

    /**
     * coordinates for topside line used with box type
     * @return  line coordinates x1,y1,x2,y2
     */
    private double[] topSide() {
        Coordinate centerLoc = new Coordinate(getCenter());    
        double[] output = {adjustForRotation(vertices[0]).x+ centerLoc.x,adjustForRotation(vertices[0]).y+ centerLoc.y, adjustForRotation(vertices[1]).x+ centerLoc.x, adjustForRotation(vertices[1]).y+ centerLoc.y};
        return output;
    }

    /**
     * coordinates for right side line used with box type
     * @return  line coordinates x1,y1,x2,y2
     */
    private double[] rightSide() {
        Coordinate centerLoc = new Coordinate(getCenter());    
        double[] output = {adjustForRotation(vertices[1]).x+ centerLoc.x, adjustForRotation(vertices[1]).y+ centerLoc.y, adjustForRotation(vertices[3]).x+ centerLoc.x,adjustForRotation(vertices[3]).y+ centerLoc.y};
        return output;
    }

    /**
     * coordinates for bottom side line used with box type
     * @return  line coordinates x1,y1,x2,y2
     */
    private double[] botSide() {
        Coordinate centerLoc = new Coordinate(getCenter());    
        double[] output = { adjustForRotation(vertices[2]).x+ centerLoc.x,  adjustForRotation(vertices[2]).y+ centerLoc.y,  adjustForRotation(vertices[3]).x+ centerLoc.x,  adjustForRotation(vertices[3]).y+ centerLoc.y};
        return output;
    }

    /**
     * weather or not the given two lines intersect
     * @param line1 first line to eval
     * @param line2 second line to eval
     * @return weather or not the two lines intersect
     */
    private static boolean linesIntersect(double[] line1, double[] line2) {
        return Line2D.linesIntersect(line1[0], line1[1], line1[2], line1[3], line2[0], line2[1], line2[2], line2[3]);
    }
    
        
    private static boolean isBetween(double i, double a, double b) {
        return (i <= a && i >= b) || (i >= a && i <= b);
    }
    
    /**
     * Weather or not this hitbox intersects wit given line
     * @param points 4 length array of doubles that represents two points in a line
     * x1,y1,x2,y2
     * @return if the given line intersects with any sides of this hitbox.
     */
    public boolean intersectsWithLine(double[] points) {
        updateFarthestAndShortest();
        if (points == null) {
            throw new RuntimeException("Bad argument: line to compare is null");
        }
        if (points.length != 4) {
            throw new RuntimeException("Bad argument: param must be length 4, was " + points.length);
        }
        
        DCoordinate p1 = new DCoordinate(points[0], points[1]);
        DCoordinate p2 = new DCoordinate(points[2], points[3]);

        if (type == Type.box) {
            //first check if the points are too close
            //if that is uncertain, do straight intersection test
            if (p1.distanceFrom(getCenter()) <= this.shortestRange || p2.distanceFrom(getCenter()) <= this.shortestRange) {
                return true;
            }
            return linesIntersect(rightSide(), points) || linesIntersect(leftSide(), points)
                    || linesIntersect(topSide(), points) || linesIntersect(rightSide(), points);
        } else if (type == Type.circle) {
            /*
            uses this solution to determien whether or not our circle intersects given line
            https://stackoverflow.com/a/44975251
            resources 
            https://www.youtube.com/watch?v=YyO7ASwNpFQ
            https://www.youtube.com/watch?v=kTZhEM4ap-M
            */
            // turn line into aX + bY = c
            double a = p1.y - p2.y;
            double b = p2.x - p1.x;
            double c = p2.x * p1.y - p1.x * p2.y;
            double x = getCenter().x;
            double y = getCenter().y;
            double r = radius;

            // In general a quadratic is written as: Ax^2 + Bx + C = 0
            // (a^2 + b^2)x^2 + (2abY - 2ac + - 2b^2X)x + (b^2X^2 + b^2Y^2 - 2bcY + c^2 - b^2r^2) = 0
            double A = a * a + b * b;
            double B = 2 * a * b * y - 2 * a * c - 2 * b * b * x;
            double C = b * b * x * x + b * b * y * y - 2 * b * c * y + c * c - b * b * r * r;

            // Use quadratic formula x = (-b +- sqrt(a^2 - 4ac))/2a to find the 
            // roots of the equation (if they exist).
            double D = B * B - 4 * A * C;
            double x1, y1, x2, y2;
            double EPS = .0000001;

            // Handle vertical line case with b = 0
            if (Math.abs(b) < EPS) {
                // Line equation is ax + by = c, but b = 0, so x = c/a
                x1 = c / a;
                // No intersection
                if (Math.abs(x - x1) > r) {
                    return false;
                }
                // Vertical line is tangent to circle
                if (Math.abs((x1 - r) - x) < EPS || Math.abs((x1 + r) - x) < EPS) {
                     // point of intersection is (x1,y). make sure that value is in line segment
                    return isBetween(x1, points[0], points[2]) && isBetween(y, points[1], points[3]);
                }
                double dx = Math.abs(x1 - x);
                double dy = Math.sqrt(r * r - dx * dx);
                // Vertical line cuts through circle
                // points are (x1, y+dy) and (x1,y-dy)
                return isBetween(x1, points[0], points[2])
                        && (isBetween(y+dy, points[1], points[3]) || isBetween(y-dy, points[1], points[3])); // make sure that value is in line segment

            } else if (Math.abs(D) < EPS) { // Line is tangent to circle
                x1 = -B / (2 * A);
                y1 = (c - a * x1) / b;
                // intersects at (x1, y1)
                return isBetween(x1, points[0], points[2]) && isBetween(y1, points[1], points[3]); // make sure that value is in line segment
            } else if (D < 0) { // No intersection
                return false;
            } else {
                D = Math.sqrt(D);
                x1 = (-B + D) / (2 * A);
                y1 = (c - a * x1) / b;
                
                x2 = (-B - D) / (2 * A);
                y2 = (c - a * x2) / b;
                // intersects at (x1, y1), and (x2, y2);
                // make sure that x value is in line segment
                return (isBetween(x1, points[0], points[2]) && isBetween(y1, points[1], points[3]))
                        || (isBetween(x2, points[0], points[2]) && isBetween(y2, points[1], points[3]));
            }
        } else { // neither box nor circle
            return false;
        }
    }
    
    private static boolean doCircleAndBoxInterset(Hitbox circle, Hitbox rect) {
        double distance = rect.getCenter().distanceFrom(circle.getCenter());
        if (distance - circle.radius > rect.farthestRange) {
            return false; // too far to possibly touch
        }
        if (distance <= circle.radius + rect.shortestRange) {
            return true;
        }
        
        // check for circle contained in large polygon
        double[] lineComingOutOfCircle = {circle.getCenter().x, circle.getCenter().y, 0, 0}; // line from circle center to 0,0
        int intersections = 0;
        if(linesIntersect(lineComingOutOfCircle, rect.topSide())) intersections++;
        if(linesIntersect(lineComingOutOfCircle, rect.botSide())) intersections++;
        if(linesIntersect(lineComingOutOfCircle, rect.rightSide())) intersections++;
        if(linesIntersect(lineComingOutOfCircle, rect.leftSide())) intersections++;
        if(intersections == 1) return true; // if the circle is inside the polygon, there will be exacatly 1 intersection
        
        //check for border intersect
        boolean lineIntersection =
                  circle.intersectsWithLine(rect.leftSide())
                || circle.intersectsWithLine(rect.rightSide())
                || circle.intersectsWithLine(rect.topSide())
                || circle.intersectsWithLine(rect.botSide());
        return lineIntersection;
    }
    
    private static boolean doBoxAndBoxIntersect(Hitbox a, Hitbox b) {
        double distance = a.getCenter().distanceFrom(b.getCenter());
        if (distance - b.farthestRange > a.farthestRange) {
            return false; //too far to possibly intersect
        }
        if (distance <= a.shortestRange + b.shortestRange) {
            return true; //must be touching
        }

        for (int i =0; i < a.vertices.length; i++) {
            Coordinate vert = a.vertices[i];
            Coordinate adjustedVert = a.adjustForRotation(vert.copy()).add(a.getCenter());
            
            double[] leftSideOfBox = b.leftSide();
            // draw line from each vert to origin. if any of the lines intersect other hitbox exactly one time
            // then it must be inside that other hitbox
            double[] lineComingOut = {adjustedVert.x, adjustedVert.y, 0, 0}; // line from circle center to 0,0int intersections = 0;
            int intersections = 0;
            if (linesIntersect(lineComingOut, b.topSide())) {
                intersections++;
            }
            if (linesIntersect(lineComingOut, b.botSide())) {
                intersections++;
            }
            if (linesIntersect(lineComingOut, b.rightSide())) {
                intersections++;
            }
            if (linesIntersect(lineComingOut, b.leftSide())) {
                intersections++;
            }
            if (intersections == 1) {
                return true;
            }
        }
        for (Coordinate vert : b.vertices) {
            Coordinate adjustedVert = b.adjustForRotation(vert.copy()).add(b.getCenter());
            // draw line from each vert to origin. if any of the lines intersect other hitbox exactly one time
            // then it must be inside that other hitbox
            double[] lineComingOut = {adjustedVert.x, adjustedVert.y, 0, 0}; // line from circle center to 0,0int intersections = 0;
            int intersections = 0;
            if (linesIntersect(lineComingOut, a.topSide())) {
                intersections++;
            }
            if (linesIntersect(lineComingOut, a.botSide())) {
                intersections++;
            }
            if (linesIntersect(lineComingOut, a.rightSide())) {
                intersections++;
            }
            if (linesIntersect(lineComingOut, a.leftSide())) {
                intersections++;
            }
            if (intersections == 1) {
                return true;
            }
        }
        
        return false;
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
            return doBoxAndBoxIntersect(this, other);
        }
        if (this.type == Type.box && other.type == Type.circle) { //box on circle collision
            return doCircleAndBoxInterset(other, this);
        }
         if (this.type == Type.circle && other.type == Type.box) {
            return doCircleAndBoxInterset(this, other);
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
    public  boolean intersectsIfMoved(Hitbox other, Coordinate velocity) {
        Hitbox simulatedHitbox;
        if(this.type == Type.box) {
            Coordinate[] simulatedVerts = new Coordinate[4];
            for (int i = 0; i < 4; i++) {
                simulatedVerts[i] = adjustForRotation(vertices[i]);
                simulatedVerts[i].add(velocity);
                simulatedVerts[i].add(getCenter());
            }            
            simulatedHitbox = new Hitbox(simulatedVerts);
        } else {
            DCoordinate simulatedCenter = getCenter().copy();
            simulatedCenter.add(velocity);
            simulatedHitbox = new Hitbox(simulatedCenter, radius);
        }
        boolean result = simulatedHitbox.intersects(other);
        return result;
    }
    
    
    
    public boolean intersectsIfRotated(Hitbox other, double possibleRotation){
        rotate(possibleRotation);
        updateFarthestAndShortest();
        boolean result = intersects(other);
        rotate(-possibleRotation);
        updateFarthestAndShortest();
        return result;
    }    
    


    public void render(Graphics2D g){
        Color col = g.getColor();
        if(host!=null && !host.isSolid){
                g.setColor(Color.blue);
        }
        if(this.type == Type.box){ 
            //render all sides
            g.drawLine((int) leftSide()[0], (int) leftSide()[1], (int) leftSide()[2], (int) leftSide()[3]);
            g.drawLine((int) rightSide()[0], (int) rightSide()[1], (int) rightSide()[2], (int) rightSide()[3]);
            g.drawLine((int) topSide()[0], (int) topSide()[1], (int) topSide()[2], (int) topSide()[3]);
            g.drawLine((int) botSide()[0], (int) botSide()[1], (int) botSide()[2], (int) botSide()[3]);
        } else if (type == Type.circle) {
            g.drawOval((int) (getCenter().x-radius), (int) (getCenter().y-radius), (int) radius*2, (int) radius*2);
        }
        g.setColor(col);
    }

    public double getFarthestRange() {
        return farthestRange;
    }
    
    public double getShortestRange() {
        return shortestRange;
    }
    
}

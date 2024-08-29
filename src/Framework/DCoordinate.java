/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

/**
 * Coordinate that uses doubles rather than integers
 *
 * @author Joseph
 */
public class DCoordinate {

    public double x, y;

    public static double distanceBetween(DCoordinate a, DCoordinate b) {
        return Math.sqrt(Math.pow((b.x - a.x), 2) + Math.pow(b.y - a.y, 2));
    }

    public double distanceFrom(DCoordinate other) {
        return Math.sqrt(Math.pow((other.x - this.x), 2) + Math.pow(other.y - this.y, 2));
    }

    public double distanceFrom(Coordinate other) {
        return Math.sqrt(Math.pow((other.x - this.x), 2) + Math.pow(other.y - this.y, 2));
    }

    public DCoordinate(Coordinate c) {
        x = c.x;
        y = c.y;
    }

    public DCoordinate(DCoordinate c) {
        x = c.x;
        y = c.y;
    }

    public DCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public DCoordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DCoordinate){
           DCoordinate other = (DCoordinate)o;
           return other.x == x && other.y == y;
       } else if (o instanceof Coordinate) {
           DCoordinate other = ((Coordinate)o).toDCoordinate();
           return other.x == x && other.y == y;
       } else {
           return super.equals(o);
       }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    public static Coordinate sum(Coordinate a, Coordinate b) {
        return new Coordinate(a.x + b.x, a.y + b.y);
    }

    public DCoordinate add(DCoordinate other) {
        x += other.x;
        y += other.y;
        return this;
    }

    public DCoordinate add(Coordinate other) {
        x += other.x;
        y += other.y;
        return this;
    }
    
    
    public DCoordinate add(Double x, Double y) {
        this.x += x;
        this.y += y;
        return this;
    }
    

    public DCoordinate subtract(Coordinate other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public DCoordinate subtract(DCoordinate other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public DCoordinate getInverse() {
        return new DCoordinate(-x, -y);
    }

    public DCoordinate copy() {
        return new DCoordinate(this);
    }

    /**
     * angle we must turn from start location (assuming no initial rotation) to
     * 'face' the end location. Note we add 90 degrees because top of sprite is
     * considered the face of the sprite, remove that if the sprite is facing
     * right
     *
     * @param start original point
     * @param end point we want to look at
     * @return angle to turn assuming top of sprite is forwards
     */
    public static Double angleFrom(DCoordinate start, DCoordinate end) {
        if (end.x == start.x) {
            //on top of eachother
            if (end.y < start.y) {
                return 0.0; //directly up or down
            } else {
                return 180.0;
            }
        } else {
            double adjacent = end.x - start.x;
            double opposite = end.y - start.y;
            double angle = Math.toDegrees(Math.atan2(opposite, adjacent)) + 90;
            return angle;
        }
    }

    /**
     * angle we must turn from start location (assuming no initial rotation) to
     * 'face' the end location. Note we add 90 degrees because top of sprite is
     * considered the face of the sprite, remove that if the sprite is facing
     * right
     *
     * @param start original point
     * @param end point we want to look at
     * @return angle to turn assuming top of sprite is forwards
     */
    public static Double angleFrom(Coordinate start, Coordinate end) {
        if (end.x == start.x) {
            //on top of eachother
            if (end.y < start.y) {
                return 0.0; //directly up or down
            } else {
                return 180.0;
            }
        } else {
            double adjacent = end.x - start.x;
            double opposite = end.y - start.y;
            double angle = Math.toDegrees(Math.atan2(opposite, adjacent)) + 90;
            return angle;
        }
    }

    public DCoordinate scale(double s) {
        x *= s;
        y *= s;
        return this;
    }

    /**
     * adjusts this coordinate to reflect the location it would be at if it were
     * to be rotated about the origin by a given degree
     *
     * @param degree degree of rotation about the origin
     */
    public void adjustForRotation(double degree) {
        double oldX = x;
        double oldY = y;
        double rotation = Math.toRadians(degree);
        x = (oldX * Math.cos(rotation) - oldY * Math.sin(rotation));
        y = (oldX * Math.sin(rotation) + oldY * Math.cos(rotation));
    }

    public void rotateAboutPoint(Coordinate point, double degrees) {
        rotateAboutPoint(point.toDCoordinate(), degrees);
    }

    public void rotateAboutPoint(DCoordinate point, double degrees) {
        DCoordinate offset = this.copy();
        offset.subtract(point.copy());
        offset.adjustForRotation(degrees);
        offset.add(point);
        x = offset.x;
        y = offset.y;
    }
    
    
    public DCoordinate offsetBy(Coordinate other) {
      return new DCoordinate(x - other.x, y - other.y);  
    }
    
    public DCoordinate offsetBy(DCoordinate other) {
        return new DCoordinate((double)x - other.x, (double)y - other.y);
    }

    /**
     * returns a coordinate whos x and y values have been rotated around the
     * orgin
     *
     * @param c coordinate to use
     * @param r degree of rotation
     * @return updated coordinate
     */
    public static DCoordinate adjustForRotation(DCoordinate c, double r) {
        double oldX = c.x;
        double oldY = c.y;
        double rotation = Math.toRadians(r);
        double x = (oldX * Math.cos(rotation) - oldY * Math.sin(rotation));
        double y = (oldX * Math.sin(rotation) + oldY * Math.cos(rotation));
        return new DCoordinate(x, y);
    }

    public Coordinate toCoordinate() {
        return new Coordinate(this);
    }

    /**
     * scales this Coordinate by -1
     */
    public DCoordinate invert() {
        x *= -1;
        y *= -1;
        return this;
    }
}

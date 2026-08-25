package Framework;

import java.io.Serializable;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Coordinate holds values for position; Functions as a 2D vector.
 *
 * @author Joseph
 */
public class Coordinate implements Serializable{

    public int x, y;

    /*
     * Squared by multiplication rather than Math.pow: multiplication and sqrt are exactly
     * specified by IEEE 754 and so give the same distance on every machine, which lockstep
     * multiplayer depends on, while Math.pow may differ by a unit in the last place between
     * implementations. See DCoordinate for the same reasoning.
     */
    public static double distanceBetween(Coordinate a, Coordinate b) {
        double dx = b.x - a.x, dy = b.y - a.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceFrom(Coordinate other) {
        double dx = other.x - this.x, dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceFrom(DCoordinate other) {
        double dx = other.x - this.x, dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Coordinate(Coordinate c) {
        x = c.x;
        y = c.y;
    }

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(DCoordinate c) {
        x = (int) c.x;
        y = (int) c.y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
       if(o instanceof Coordinate){
           Coordinate other = (Coordinate)o;
           return other.x == x && other.y == y;
       } else if (o instanceof DCoordinate) {
           Coordinate other = ((DCoordinate)o).toCoordinate();
           return other.x == x && other.y == y;
       } else {
           return super.equals(o);
       }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.y;
        return hash;
    }

    public static Coordinate sum(Coordinate a, Coordinate b) {
        return new Coordinate(a.x + b.x, a.y + b.y);
    }

    public Coordinate add(DCoordinate other) {
        x += other.x;
        y += other.y;
        return this;
    }

    public Coordinate add(Coordinate other) {
        x += other.x;
        y += other.y;
        return this;
    }
    
    public Coordinate add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Coordinate subtract(Coordinate other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public Coordinate subtract(DCoordinate other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public Coordinate getInverse() {
        return new Coordinate(-x, -y);
    }

    public Coordinate copy() {
        return new Coordinate(this);
    }

    public Coordinate scale(double s) {
        x *= s;
        y *= s;
        return this;
    }

    public Coordinate rotateAboutPoint(Coordinate point, double degrees) {
        rotateAboutPoint(point.toDCoordinate(), degrees);
        return this;
    }

    public Coordinate rotateAboutPoint(DCoordinate point, double degrees) {
        DCoordinate offset = this.copy().toDCoordinate();
        offset.subtract(point.copy());
        offset.adjustForRotation(degrees);
        offset.add(point);
        x = (int) offset.x;
        y = (int) offset.y;
        return this;
    }
    
    
    public Coordinate offsetBy(Coordinate other) {
      return new Coordinate(x - other.x, y - other.y);  
    }
    
    public DCoordinate offsetBy(DCoordinate other) {
        return new DCoordinate((double)x - other.x, (double)y - other.y);
    }
    

    /**
     * adjusts this coordinate to reflect the location it would be at if it were
     * to be rotated about the origin by a given degree
     *
     * @param degree degree of rotation about the origin
     * @return this.
     */
    public Coordinate adjustForRotation(double degree) {
        int oldX = x;
        int oldY = y;
        double rotation = Math.toRadians(degree);
        x = (int) (oldX * StrictMath.cos(rotation) - oldY * StrictMath.sin(rotation));
        y = (int) (oldX * StrictMath.sin(rotation) + oldY * StrictMath.cos(rotation));
        return this;
    }
    
    public static Coordinate nearestPointOnCircle(Coordinate origin, Coordinate target, double radius) {
        double dx = target.x - origin.x;
        double dy = target.y - origin.y;

        // Calculate the distance from the origin to the target
        double distanceToTarget = Math.sqrt(dx * dx + dy * dy);

        // Normalize the direction vector (dx, dy)
        double directionX = dx / distanceToTarget;
        double directionY = dy / distanceToTarget;

        // Calculate the nearest point on the circle
        double nearestX = origin.x + radius * directionX;
        double nearestY = origin.y + radius * directionY;

        return new Coordinate((int)nearestX, (int)nearestY);
    }

    /**
     * returns a coordinate whos x and y values have been rotated around the
     * orgin
     *
     * @param c coordinate to use
     * @param r degree of rotation
     * @return updated coordinate
     */
    public static Coordinate adjustForRotation(Coordinate c, double r) {
        int oldX = c.x;
        int oldY = c.y;
        double rotation = Math.toRadians(r);
        int x = (int) (oldX * StrictMath.cos(rotation) - oldY * StrictMath.sin(rotation));
        int y = (int) (oldX * StrictMath.sin(rotation) + oldY * StrictMath.cos(rotation));
        return new Coordinate(x, y);
    }

    public DCoordinate toDCoordinate() {
        return new DCoordinate(this);
    }

    /**
     * scales this Coordinate by -1
     * @return this scaled
     */
    public Coordinate invert() {
        x *= -1;
        y *= -1;
        return this;
    }
}

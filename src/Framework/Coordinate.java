package Framework;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * Coordinate holds values for position; Functions as a 2D vector. 
 * @author Joseph
 */
public class Coordinate {
    public int x, y;
    
    public static double distanceBetween(Coordinate a, Coordinate b){
        return Math.sqrt(Math.pow((b.x-a.x), 2)+Math.pow(b.y-a.y, 2));
    }
    
    
    public double distanceFrom(Coordinate other) {
        return Math.sqrt(Math.pow((other.x - this.x), 2) + Math.pow(other.y - this.y, 2));
    }

    public double distanceFrom(DCoordinate other) {
        return Math.sqrt(Math.pow((other.x - this.x), 2) + Math.pow(other.y - this.y, 2));
    }

    public Coordinate(Coordinate c) {
        x = c.x;
        y = c.y;
    }
    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }
    public Coordinate(DCoordinate c){
        x = (int)c.x;
        y = (int)c.y;
    }
    
    @Override
    public String toString(){
        return "("+x+","+y+")";
    }
    
    
    @Override
    public boolean equals(Object o){
        try{
            Coordinate other = (Coordinate)o;
            return (x==other.x && y==other.y);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.y;
        return hash;
    }

    
    public static void main(String[] args){
        
        
        Coordinate one = new Coordinate(1,1);
        
        Coordinate two = new Coordinate(1,1);
        
        System.out.println(one==two);
        System.out.println(one.equals(two));
    }
    
    
    
    
    public static Coordinate sum(Coordinate a, Coordinate b) {
        return new Coordinate(a.x + b.x, a.y + b.y);
    }

    public void add(DCoordinate other) {
        x += other.x;
        y += other.y;
    }

    public void add(Coordinate other) {
        x += other.x;
        y += other.y;
    }

    public void subtract(Coordinate other) {
        x -= other.x;
        y -= other.y;
    }

    public void subtract(DCoordinate other) {
        x -= other.x;
        y -= other.y;
    }

    public Coordinate getInverse(){
        return new Coordinate(-x,-y);
    }
    
    public Coordinate copy(){
        return new Coordinate(this);
    }
    

    public void scale(double s){
        x*=s;
        y*=s;
    }

    public void rotateAboutPoint(Coordinate point, double degrees) {
        rotateAboutPoint(point.toDCoordinate(), degrees);
    }

    public void rotateAboutPoint(DCoordinate point, double degrees) {
        DCoordinate offset = this.copy().toDCoordinate();
        offset.subtract(point.copy());
        offset.adjustForRotation(degrees);
        offset.add(point);
        x = (int)offset.x;
        y = (int)offset.y;
    }

    
    /**
     * adjusts this coordinate to reflect the location it would be at if it were
     * to be rotated about the origin by a given degree
     *
     * @param degree degree of rotation about the origin
     */
    public void adjustForRotation(double degree) {
        int oldX = x;
        int oldY = y;
        double rotation = Math.toRadians(degree);
        x = (int) (oldX * Math.cos(rotation) - oldY * Math.sin(rotation));
        y = (int) (oldX * Math.sin(rotation) + oldY * Math.cos(rotation));
    }


    /**
     * returns a coordinate whos x and y values have been rotated around the orgin
     * @param c coordinate to use
     * @param r degree of rotation
     * @return updated coordinate
     */
    public static Coordinate adjustForRotation(Coordinate c, double r) {
        int oldX = c.x;
        int oldY = c.y;
        double rotation = Math.toRadians(r);
        int x = (int) (oldX * Math.cos(rotation) - oldY * Math.sin(rotation));
        int y = (int) (oldX * Math.sin(rotation) + oldY * Math.cos(rotation));
        return new Coordinate(x, y);
    }

    public DCoordinate toDCoordinate() {
        return new DCoordinate(this);
    }
    
    /**
     * scales this Coordinate by -1
     */
    public void invert(){
        x*=-1;
        y*=-1;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

/**
 * Coordinate that uses doubles rather than integers
 * @author Joseph
 */
public class DCoordinate {
    public double x, y;
    
    public static double distanceBetween(DCoordinate a, DCoordinate b){
        return Math.sqrt(Math.pow((b.x-a.x), 2)+Math.pow(b.y-a.y, 2));
    }
    
    public double distanceFrom(DCoordinate other){
        return Math.sqrt(Math.pow((other.x-this.x), 2)+Math.pow(other.y-this.y, 2));
    }
    
    public double distanceFreom(Coordinate other){
        return Math.sqrt(Math.pow((other.x-this.x), 2)+Math.pow(other.y-this.y, 2));
    }
    
    public DCoordinate(Coordinate c){
        x = c.x;
        y = c.y;
    }
     public DCoordinate(DCoordinate c){
        x = c.x;
        y = c.y;
    }
    public DCoordinate(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString(){
        return "("+x+","+y+")";
    }
    
    
    @Override
    public boolean equals(Object o){
        try{
            DCoordinate other = (DCoordinate)o;
            return (x==other.x && y==other.y);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }


    
    public static void main(String[] args){
        Coordinate one = new Coordinate(1,1);
        Coordinate two = new Coordinate(1,1);
        System.out.println(one.equals(two));
    }
    
    public static Coordinate sum(Coordinate a, Coordinate b){
        return new Coordinate(a.x+b.x,a.y+b.y);
    }
    
    public static Double angleFrom(DCoordinate start, DCoordinate end){
        if(end.x==start.x){
            //on top of eachother
            if(end.y<start.y){
                return 0.0; //directly up
            }else{
                return 180.0; //directly down
            }
        }else{
            double adjacent = 0.0;
            double opposite = 0.0;
            adjacent = end.x - start.x;
            opposite = end.y - start.y;
            double angle = Math.toDegrees(Math.atan2(opposite, adjacent));
            return angle;
        }
    }
}

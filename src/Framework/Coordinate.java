package Framework;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * Coordinate holds values for position
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
        System.out.println(one.equals(two));
    }
    
    public static Coordinate sum(Coordinate a, Coordinate b){
        return new Coordinate(a.x+b.x,a.y+b.y);
    }
    
     public void add(DCoordinate other){
        x+=other.x;
        y+=other.y;
    }
        public void add(Coordinate other){
        x+=other.x;
        y+=other.y;
    }
}

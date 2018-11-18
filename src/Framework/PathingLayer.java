/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * represented by an image, uses colors to determine pathing area
 * @author Joseph
 */
public class PathingLayer {
    public static final Color groundColor = new Color(0,255,0);
    public static final Color waterColor = new Color(0,0,255);
    public static final Color impassColor = new Color(0,0,0);
    
    public static enum Type {ground, water, impass};
    
    public BufferedImage source = null;
    private Type[][] map;
    private boolean mapGenerated = false;
    private boolean sourceInternalized = false;
    /**
     * converts pixels in source to cooresponding colors to make it easier to see
     * in debug view
     */
    public void internalizeSource(){
        if(sourceInternalized) return;
        for(int i = 0; i < source.getWidth(); i++){
            for(int j = 0; j < source.getHeight(); j++){
                if(getTypeAt(new Coordinate(i,j)) == Type.impass){
                    source.setRGB(i, j, Color.black.getRGB());                
                }
            }
        }
        sourceInternalized = true;
        System.out.println("done");
    }
    
    /**
     * Gets pathing type at current Location
     * Uses generated map if available, else evaluates pixel first
     * @param c coordinate to evaluate
     * @return pathing type
     */
    public Type getTypeAt(Coordinate c) {
        if (mapGenerated) {
            return map[c.x][c.y];
        } else {
            return getType(source.getRGB(c.x, c.y));
        }
    }
  
    
    /**
     * constructor, creates pathing layer based on given image
     * @param image image to create based on
     */
    public PathingLayer(BufferedImage image){
        source = image;
    }
    
    /**
     * generates a 2d array map for quick reference but takes a long time.
     * This speeds up future getTypeAt method calls but requires alot of memory.
     */
    public void generateMap(){
        map = new Type[source.getWidth()][source.getHeight()];
        for(int i = 0; i < source.getWidth(); i++){
            for(int j = 0; j < source.getHeight(); j++){
                map[i][j] = getType(source.getRGB(i, j));
            }
        }
        mapGenerated = true;
    }
    
    
    
    /**
     * returns the pathing type that corresponds to the given rgb code.
     * If none is found, defaults to impass.
     * @param c rgb code
     * @return pathing type
     */
    private static Type getType(int c){
        if(c==groundColor.getRGB()){
            return Type.ground;
        }else if(c==waterColor.getRGB()){
            return Type.water;
        }else if(c==impassColor.getRGB()){
            return Type.impass;
        }else{
           // System.out.println("no path found for color " + new Color(c));
            return Type.impass;
        }
    }
    
}

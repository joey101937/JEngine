/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.GraphicalAssets.Graphic;
import java.util.HashMap;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import static Framework.GraphicalAssets.Graphic.getRGBA;

/**
 * represented by an image, uses colors to determine pathing area
 * @author Joseph
 */
public class PathingLayer {
    
    /**
     * constructor, creates pathing layer based on given image
     * @param image image to create based on
     */
    protected PathingLayer(Image image) {
        source = image;
        initLegend();
    }
    
    
    /**
     * loads the legend with build in terrain types
     */
    private void initLegend(){
       legend.put(getRGBA(Color.rgb(0, 0, 0)), Type.impass);
       legend.put(getRGBA(Color.rgb(255, 0, 0)), Type.hostile);
       legend.put(getRGBA(Color.rgb(0, 255, 0)), Type.ground);
       legend.put(getRGBA(Color.rgb(0, 0, 255)), Type.water);
    }

    /**
     * represents a terrain type. To add your own custom terrain types, create
     * a class that extends this class, and then use assignColor method to link
     * it to your pathing layer (Game.getPathingLayer). Note the color set in 
     * this class is used to desplay in debug mode and internally. The color you
     * link it to is used when parsing the source image to figure out what type
     * is where
     */
    public static class Type{
        /**
         * name of this type of terrain
         */
        public final String name;
        /**
         * This type of terrain will show up as this color when in debug mode
         */
        public final Color color;
        public Type(String name, Color c){
            this.name = name;
            color = c;
        }        
        public static final Type ground = new Type("ground",Color.GREEN);
        public static final Type water = new Type("water",Color.BLUE);
        public static final Type hostile = new Type("hostile",Color.RED);
        public static final Type impass = new Type("impass",Color.BLACK);
    }
    
    /**
     * Links a terrain type to a given color in this layer's legend
     * @param c Pixels in the source file with this color will direct to the given Type
     * @param t Type to be yielded
     */
    public void assignColor(Color c, Type t){
        if(mapGenerated){
            throw new RuntimeException("Unable to assign color to legend of PathingLayer once map has been generated");
        }
        legend.put(getRGBA(c), t);
    }

    
    private Image source = null;
    private HashMap<Integer,Type> legend = new HashMap<>();
    private Type[][] map;
    private boolean mapGenerated = false;
    private boolean sourceInternalized = false;
        
    /**
     * converts pixels in source to cooresponding colors to make it easier to see
     * in debug view. 
     */
    public void internalizeSource(){
//        if(sourceInternalized) return;
//        for(int i = 0; i < source.getWidth(); i++){
//            for(int j = 0; j < source.getHeight(); j++){
//                source.setRGB(i, j, getTypeAt(new Coordinate(i,j)).color.getRGB());
//            }
//        }
//        sourceInternalized = true;
//        System.out.println("done");
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage writableImage = new WritableImage(width, height);

        PixelReader pixelReader = source.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);

                if (map[argb] != null) {
                    Color fxColor = getTypeAt(x,y).color;

                    // Convert JavaFX Color to ARGB value
                    int newArgb = getRGBA(fxColor);

                    pixelWriter.setArgb(x, y, newArgb);
                } else {
                    // If the color is not found in the map, keep the original color
                    pixelWriter.setArgb(x, y, argb);
                }
            }
        }

        source = writableImage;
    }
    
    /**
     * Gets pathing type at current Location
     * Uses generated map if available,
     * else evaluates pixel first
     *
     * @param c coordinate to evaluate
     * @return pathing type
     */
    public Type getTypeAt(Coordinate c) {
        try {
            if (mapGenerated) {
                return map[c.x][c.y];
            } else {
                return getType(getRGBA(source, c.x, c.y));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("trying to get pathing layer outside of world " + c);
            return Type.impass;
        }
    }
    
    public Type getTypeAt(int x, int y) {
        return this.getTypeAt(new Coordinate(x,y));
    }

    public Image getSource() {
        return source;
    }

    public void setSource(Image source) {
        if(this.mapGenerated){
            throw new RuntimeException("Cannot change source once map has been generated");
        }
        this.source = source;
    }

    
    /**
     * generates a 2d array map for quick reference using the current legend
     * but takes a long time. This speeds up future getTypeAt method calls
     * but requires alot of memory. ONCE DONE LEGEND AND SOURCE WILL BE UNCHANGABLE
     */
    public void generateMap(){
        map = new Type[(int)source.getWidth()][(int)source.getHeight()];
        for(int i = 0; i < source.getWidth(); i++){
            for(int j = 0; j < source.getHeight(); j++){
                map[i][j] = getType(getRGBA(source, j, j));
            }
        }
        mapGenerated = true;
    }

    /**
     * returns the pathing type that corresponds to the given rgb code. If none
     * is found, defaults to impass.
     *
     * @param c rgb code
     * @return pathing type
     */
    private Type getType(int c) {
        for (int code : legend.keySet()) {
            if (c == code) {
                return legend.get(code);
            }
        }
        //no color found, so return impass
        return Type.impass;
    }

    /**
     * returns the pathing type that corresponds to the given color. If none is
     * found, defaults to impass.
     * @param c rgb code
     * @return pathing type
     */
    private Type getType(Color c) {
        for (int code : legend.keySet()) {
            if (getRGBA(c) == code) {
                return legend.get(code);
            }
        }
        //no color found, so return impass
        return Type.impass;
    }


}

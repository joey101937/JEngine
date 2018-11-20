/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author Joseph
 */
public abstract class SpriteManager {

    public static boolean initialized = false;
    /*--------------------------------------------------------*/
    public static BufferedImage up;
    public static BufferedImage pathingLayer;
    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] birdySequence;
    public static BufferedImage[] sampleChar_idle, sampleChar_walkUp, sampleChar_walkDown, sampleChar_walkLeft, sampleChar_walkRight;
    /*--------------------------------------------------------*/
    
    /*--------------------------------------------------------*/
    
    
    /**
     * Loads all image assets into static variables for use in the project
     * Use before calling on any image variable
     */
    public static void initialize(){
        if(initialized)return;
        try{
           //this is where we load sprites
           explosionSequence = loadSequence("explosionSequence");
           birdySequence = loadSequence("birdySequence");
           up = load("upSprite.png");
           sampleChar_idle = loadSequence("SampleCharacter/Idle");
           sampleChar_walkUp = loadSequence("SampleCharacter/WalkUp");
           sampleChar_walkDown = loadSequence("SampleCharacter/WalkDown");
           sampleChar_walkLeft = loadSequence("SampleCharacter/WalkLeft");
           sampleChar_walkRight = loadSequence("SampleCharacter/WalkRight");
           pathingLayer = load("terrainBG-PATHING.png");
           initialized=true;
        }catch(Exception e){
            e.printStackTrace();
            Main.display("Error loading all assets. Please Verify Assets folder.");
        }
    }

    /**
     * returns a bufferedImage loaded from the given filename, located in assets
     * folder.
     * @param filename name of file including extension
     * @return buffered image render
     * @throws IOException if file cannot be found or loaded
     */
    private static BufferedImage load(String filename) throws IOException {
        return ImageIO.read(new File(Main.assets + filename));
    }
    
    /**
     *  loads a sprite sequence from given directory
     * @param filename name of folder to load
     * @return list of files in directory
     * @throws IOException if there is a problem
     */ 
    private static BufferedImage[] loadSequence(String filename) throws IOException{
        filename = Main.assets + filename;
        ArrayList<BufferedImage> a = new ArrayList<>();
        ArrayList<File> children = new ArrayList<>();
        for(File f : new File(filename).listFiles()){
            children.add(f);
        }
        children.sort(null);
        for(File child : children){
            System.out.println("loading " + child.getPath().substring(6)); //to remove the redundant /Assets
           a.add(load(child.getPath().substring(6)));
        }
        BufferedImage[] output = new BufferedImage[a.size()];
        for(BufferedImage b : a){
            output[a.indexOf(b)]=b;
        }
        return output;
    }
}

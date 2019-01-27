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
 * This class acts as a central hub for accessing exterior assets; particularly
 * for images. You dont have to use it but it makes things easy to have all in
 * one place
 * @author Joseph
 */
public abstract class SpriteManager {
    public static boolean initialized = false;
    //THESE  ARE GLOBAL FIELDS THAT ARE USED TO STORE AND ACCESS ASSETS
    /*--------------------------------------------------------*/
    //SANDBOX ASSETS
    public static BufferedImage up;
    public static BufferedImage pathingLayer;
    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] birdySequence;
    public static BufferedImage[] sampleChar_idle, sampleChar_walkUp, sampleChar_walkDown, sampleChar_walkLeft, sampleChar_walkRight;
    
    //TANK ASSETS
    public static BufferedImage tankChasis;
    public static BufferedImage tankTurret;
    public static BufferedImage bullet;
    public static BufferedImage[] tankFireAnimation;
    public static BufferedImage dirtBG;
    
    //SPACE ASSETS
    public static BufferedImage spaceBG;
    public static BufferedImage spaceBG2;
    public static BufferedImage spaceship;
    
    //PLATFORMER ASSETS
    public static BufferedImage platformBG;
    public static BufferedImage platformPathing;
    
    /*--------------------------------------------------------*/
    static{
        initialize();
    }
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
           
           tankChasis = load("TankGame/tankChasis.png");
           tankTurret = load("TankGame/tankTurret.png");
           bullet = load("TankGame/bullet.png");
           tankFireAnimation = loadSequence("TankGame/turretFireSequence");
           dirtBG = load("TankGame/dirtBG.png");
           
           spaceBG = load("spacebg.png");
           spaceBG2 = load("spacebg2.png");
           spaceship = load("spaceship.png");
           
           platformBG = load("platformer/platformer.png");
           platformPathing = load("platformer/platformPATHING.png");
           initialized=true;
        }catch(Exception e){
            e.printStackTrace();
            Main.display("Error loading all assets. Please Verify Assets folder.");
            System.exit(1);
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

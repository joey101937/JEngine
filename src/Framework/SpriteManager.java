/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import static Framework.GraphicalAssets.Graphic.load;
import static Framework.GraphicalAssets.Graphic.loadSequence;
import static Framework.GraphicalAssets.Graphic.loadSequenceBouncing;
import javafx.scene.image.Image;

/**
 * This class acts as a central hub for accessing exterior assets used in the demos; particularly
 * for images. 
 * NOTE: You dont have to use it but it makes things easy to have all in
 * one place
 * @author Joseph
 */
public abstract class SpriteManager {
    public static boolean initialized = false;
    //THESE  ARE GLOBAL FIELDS THAT ARE USED TO STORE AND ACCESS ASSETS
    /*--------------------------------------------------------*/
    
    //SANDBOX ASSETS
    public static Image terrainBG;
    public static Image up;
    public static Image pathingLayer;
    public static Image[] explosionSequence;
    public static Image[] explosionSequenceSmall;
    public static Image[] birdySequence;
    public static Image[] sampleChar_idle, sampleChar_walkUp, sampleChar_walkDown, sampleChar_walkLeft, sampleChar_walkRight;
    public static Image localizedLight;
    public static Image fog;
    public static Image[] fogSequence;
    public static Image car;

    
    //TANK ASSETS
    public static Image tankChasis;
    public static Image tankTurret;
    public static Image bullet;
    public static Image[] tankFireAnimation;
    public static Image dirtBG;
    public static Image dirtBGNight;

    
    //SPACE ASSETS
    public static Image spaceBG;
    public static Image spaceBG2;
    public static Image spaceship;
    public static Image evilShip;
    public static Image bolt;
    
    //PLATFORMER ASSETS
    public static Image platformBG;
    public static Image platformPathing;
    
    //SIDESCROLL GAME ASSETS
    public static Image[] minotaurIdle_Right;
    public static Image[] minotaurIdle_Left; 
    public static Image[] minotaurRun_Right;
    public static Image[] minotaurRun_Left;
    public static Image[] minotaurSwing_Right;
    public static Image[] minotaurSwing_Left;
    public static Image[] minotaurDeath_Right;
    public static Image SSBackground;
    public static Image terrain260x125, terrain585x120;
    public static Image barrel;
    public static Image[] barrelDeath;
    
    //TOWN ASSETS
    public static Image townOutside;
    public static Image townOutsidePathing;
    public static Image buildingInterior;
    public static Image buildingInteriorPathing;

    
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
           
           terrainBG = load("DemoAssets/terrainBG.png");
           explosionSequence = loadSequence("DemoAssets/explosionSequence");
           explosionSequenceSmall = loadSequence("DemoAssets/explosionSequence_small");
           birdySequence = loadSequence("DemoAssets/birdySequence");
           up = load("DemoAssets/upSprite.png");
           sampleChar_idle = loadSequence("DemoAssets/SampleCharacter/Idle");
           sampleChar_walkUp = loadSequence("DemoAssets/SampleCharacter/WalkUp");
           sampleChar_walkDown = loadSequence("DemoAssets/SampleCharacter/WalkDown");
           sampleChar_walkLeft = loadSequence("DemoAssets/SampleCharacter/WalkLeft");
           sampleChar_walkRight = loadSequence("DemoAssets/SampleCharacter/WalkRight");
           pathingLayer = load("DemoAssets/terrainBG-PATHING.png");
           localizedLight = load("DemoAssets/localizedLight2.png");
           fog = load("DemoAssets/fog.png");
           fogSequence = loadSequenceBouncing("DemoAssets/fogSequence");
           car = load("DemoAssets/topDownCarShadowDarkGrit.png");
           
           
           tankChasis = load("DemoAssets/TankGame/tankChasis.png");
           tankTurret = load("DemoAssets/TankGame/tankTurret.png");
           bullet = load("DemoAssets/TankGame/bullet.png");
           tankFireAnimation = loadSequence("DemoAssets/TankGame/turretFireSequence");
           dirtBG = load("DemoAssets/TankGame/dirtBG.png");
           dirtBGNight = load("DemoAssets/TankGame/dirtBGNight.png");
           
           spaceBG = load("DemoAssets/spacebg.png");
           spaceBG2 = load("DemoAssets/spacebg2.png");
           spaceship = load("DemoAssets/spaceship.png");
           evilShip = load("DemoAssets/evilSpaceShip.png");
           bolt = load("DemoAssets/bolt.png");
           
           platformBG = load("DemoAssets/Platformer/platformer.png");
           platformPathing = load("DemoAssets/Platformer/platformPATHING.png");
           
           minotaurIdle_Right = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurIdle_Right");
           minotaurIdle_Left = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurIdle_Left");
           minotaurRun_Right = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurRun_Right");
           minotaurRun_Left = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurRun_Left");
           minotaurSwing_Right = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurSwing_Right");
           minotaurSwing_Left = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurSwing_Left");
           minotaurDeath_Right = loadSequence("DemoAssets/SideScroll/Minotaur/MinotaurDeath_Right");
           SSBackground = load("DemoAssets/SideScroll/Terrain/DesertBG.png");
           terrain260x125 = load("DemoAssets/SideScroll/Terrain/terrain260x125.png");
           terrain585x120 = load("DemoAssets/SideScroll/Terrain/terrain585x120.png");
           barrel = load("DemoAssets/SideScroll/Barrel/barrel.png");
           barrelDeath = loadSequence("DemoAssets/SideScroll/Barrel/barrelBreak");
           
           townOutside = load("DemoAssets/town/outside.png");
           townOutsidePathing = load("DemoAssets/town/outside_pathing.png");
           buildingInterior = load("DemoAssets/town/interior1.png");
           buildingInteriorPathing = load("DemoAssets/town/interior1_PATHING.png");
           
           initialized=true;
        }catch(Exception e){
            e.printStackTrace();
            Main.display("Error loading all assets. Please Verify Assets folder.");
            System.exit(1);
        }
    }
}

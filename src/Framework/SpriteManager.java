/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import static Framework.GraphicalAssets.Graphic.load;
import static Framework.GraphicalAssets.Graphic.loadSequence;
import static Framework.GraphicalAssets.Graphic.loadSequenceBouncing;
import java.awt.image.BufferedImage;

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
    public static BufferedImage terrainBG;
    public static BufferedImage up;
    public static BufferedImage pathingLayer;
    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] explosionSequenceSmall;
    public static BufferedImage[] birdySequence;
    public static BufferedImage[] sampleChar_idle, sampleChar_walkUp, sampleChar_walkDown, sampleChar_walkLeft, sampleChar_walkRight;
    public static BufferedImage localizedLight;
    public static BufferedImage fog;
    public static BufferedImage[] fogSequence;
    public static BufferedImage car;

    
    //TANK ASSETS
    public static BufferedImage tankChasis;
    public static BufferedImage tankTurret;
    public static BufferedImage tankChasis2;
    public static BufferedImage tankTurret2;
    public static BufferedImage[] tankFireAnimation2;
    public static BufferedImage bullet;
    public static BufferedImage[] tankFireAnimation;
    public static BufferedImage dirtBG;
    public static BufferedImage dirtBGNight;
    public static BufferedImage grassBG;
    public static BufferedImage grassBGMega;
    public static BufferedImage hellicopter;
    public static BufferedImage hellicopterShadow;
    public static BufferedImage[] hellicopterAttack;
    public static BufferedImage missile;
    public static BufferedImage missileShadow;

    
    //SPACE ASSETS
    public static BufferedImage spaceBG;
    public static BufferedImage spaceBG2;
    public static BufferedImage spaceship;
    public static BufferedImage evilShip;
    public static BufferedImage bolt;
    
    //PLATFORMER ASSETS
    public static BufferedImage platformBG;
    public static BufferedImage platformPathing;
    
    //SIDESCROLL GAME ASSETS
    public static BufferedImage[] minotaurIdle_Right;
    public static BufferedImage[] minotaurIdle_Left; 
    public static BufferedImage[] minotaurRun_Right;
    public static BufferedImage[] minotaurRun_Left;
    public static BufferedImage[] minotaurSwing_Right;
    public static BufferedImage[] minotaurSwing_Left;
    public static BufferedImage[] minotaurDeath_Right;
    public static BufferedImage SSBackground;
    public static BufferedImage terrain260x125, terrain585x120;
    public static BufferedImage barrel;
    public static BufferedImage[] barrelDeath;
    
    //TOWN ASSETS
    public static BufferedImage townOutside;
    public static BufferedImage townOutsidePathing;
    public static BufferedImage buildingInterior;
    public static BufferedImage buildingInteriorPathing;

    
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
           tankFireAnimation = loadSequence("DemoAssets/TankGame/turretFireSequence");
           tankChasis2 = load("DemoAssets/TankGame/tank1-hull.png");
           tankTurret2 = load("DemoAssets/TankGame/tank1-turret.png");
           tankFireAnimation2 = loadSequence("DemoAssets/TankGame/tank1Fire");
           bullet = load("DemoAssets/TankGame/bullet.png");
           dirtBG = load("DemoAssets/TankGame/dirtBG.png");
           dirtBGNight = load("DemoAssets/TankGame/dirtBGNight.png");
           grassBG = load("DemoAssets/TankGame/grassTerrain.png");
           // uncomment to load this large bg file 
           // grassBGMega = load("DemoAssets/TankGame/grassTerrain_mega.png");
           hellicopter = load("DemoAssets/TankGame/copter/base.png");
           hellicopterShadow = load("DemoAssets/TankGame/copter/shadow.png");
           hellicopterAttack = loadSequence("DemoAssets/TankGame/copter/fire");
           missile = load("DemoAssets/TankGame/copter/missile.png");
           missileShadow = load("DemoAssets/TankGame/copter/missileShadow.png");


                      
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

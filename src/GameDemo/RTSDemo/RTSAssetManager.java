package GameDemo.RTSDemo;

import static Framework.GraphicalAssets.Graphic.load;
import static Framework.GraphicalAssets.Graphic.loadSequence;
import java.awt.image.BufferedImage;

public abstract class RTSAssetManager {
    public static boolean initialized = false;

    // RTS ASSETS
    public static BufferedImage tankChasis;
    public static BufferedImage tankTurret, tankTurretShadow;
    public static BufferedImage tankHullDamaged;
    public static BufferedImage tankTurretDamaged;
    public static BufferedImage[] tankFireAnimation;
    public static BufferedImage[] tankFireAnimationDamaged;
    public static BufferedImage bullet, bullet2;
    public static BufferedImage dirtBG;
    public static BufferedImage dirtBGNight;
    public static BufferedImage grassBG;
    public static BufferedImage grassBGMega;
    public static BufferedImage hellicopter;
    public static BufferedImage hellicopterShadow;
    public static BufferedImage hellicopterDestroyed;
    public static BufferedImage[] hellicopterAttack;
    public static BufferedImage missile, yellowMissile;
    public static BufferedImage missileShadow, yellowMissileShadow;
    public static BufferedImage tankDeadHull;
    public static BufferedImage tankDeadHullShadow;
    public static BufferedImage tankShadow;
    public static BufferedImage tankDeadTurret;
    public static BufferedImage[] tankHullDeathAni;
    public static BufferedImage[] tankTurretDeathAni;
    public static BufferedImage lightTankHull, lightTankTurret, lightTankShadow, lightTankHullDamaged, lightTankTurretDamaged, 
                lightTankHullDestroyed, lightTankTurretDestroyed, lightTankDeathShadow;
    public static BufferedImage[] lightTankFire, lightTankFireDamaged;
    
    public static BufferedImage[] infantryLegsRun, infantryRifleIdle, infantryRifleFire, infantryRifleMoving, infantryBazookaIdle, infantryBazookaFire;
    public static BufferedImage infantryLegs, infantryShadow;
    public static BufferedImage tankSelectionImage, lightTankSelectionImage, riflemanSelectionImage, bazookamanSelectionImage, hellicopterSelectionImage;

    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] explosionSequenceSmall;
    public static BufferedImage[] impactCone;

    static {
        initialize();
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        try {
            explosionSequence = loadSequence("DemoAssets/explosionSequence");
            explosionSequenceSmall = loadSequence("DemoAssets/explosionSequence_small");
            impactCone = loadSequence("DemoAssets/TankGame/impact");

            tankChasis = load("DemoAssets/TankGame/tank1-hull.png");
            tankTurret = load("DemoAssets/TankGame/tank1-turret.png");
            tankTurretShadow = load("DemoAssets/TankGame/tank1-turret-shadow.png");
            tankFireAnimation = loadSequence("DemoAssets/TankGame/tank1Fire");
            bullet = load("DemoAssets/TankGame/bullet.png");
            bullet2 = load("DemoAssets/TankGame/bullet2.png");
            dirtBG = load("DemoAssets/TankGame/dirtBG.png");
            dirtBGNight = load("DemoAssets/TankGame/dirtBGNight.png");
            grassBG = load("DemoAssets/TankGame/grassTerrain_mega3.png");
            hellicopter = load("DemoAssets/TankGame/copter/base.png");
            hellicopterDestroyed = load("DemoAssets/TankGame/copter/hellicopterDestroyed.png");
            hellicopterShadow = load("DemoAssets/TankGame/copter/shadow.png");
            hellicopterAttack = loadSequence("DemoAssets/TankGame/copter/fire");
            missile = load("DemoAssets/TankGame/missile.png");
            missileShadow = load("DemoAssets/TankGame/missileShadow.png");
            yellowMissile = load("DemoAssets/TankGame/yellowMissile.png");
            yellowMissileShadow = load("DemoAssets/TankGame/yellowMissileShadow.png");
            tankDeadHull = load("DemoAssets/TankGame/destroyedHull.png");
            tankDeadHullShadow = load("DemoAssets/TankGame/destroyedHullShadow.png");
            tankShadow = load("DemoAssets/TankGame/tankShadow.png");
            tankHullDamaged = load("DemoAssets/TankGame/tankHullDamaged.png");
            tankTurretDamaged = load("DemoAssets/TankGame/tankTurretDamaged.png");
            tankFireAnimationDamaged = loadSequence("DemoAssets/TankGame/tank1FireDamaged");
            tankDeadTurret = load("DemoAssets/TankGame/destroyedTurret.png");
            tankHullDeathAni = loadSequence("DemoAssets/TankGame/tankHullDeath");
            tankTurretDeathAni = loadSequence("DemoAssets/TankGame/tankTurretDeath");
            
            lightTankHull = load("DemoAssets/TankGame/lightTank/lightTankHull.png");
            lightTankTurret = load("DemoAssets/TankGame/lightTank/lightTankTurret.png");
            lightTankShadow = load("DemoAssets/TankGame/lightTank/lightTankShadow.png");
            lightTankFire = loadSequence("DemoAssets/TankGame/lightTank/fire");
            lightTankFireDamaged = loadSequence("DemoAssets/TankGame/lightTank/fireDamaged");
            lightTankTurretDamaged = load("DemoAssets/TankGame/lightTank/lightTankTurretDamaged.png");
            lightTankHullDamaged = load("DemoAssets/TankGame/lightTank/lightTankHullDamaged.png");  
            lightTankTurretDestroyed= load("DemoAssets/TankGame/lightTank/lightTankTurretDestroyed.png");
            lightTankHullDestroyed= load("DemoAssets/TankGame/lightTank/lightTankHullDestroyed.png");  
            lightTankDeathShadow= load("DemoAssets/TankGame/lightTank/lightTankDeathShadow.png");  
            
            infantryLegsRun = loadSequence("DemoAssets/TankGame/Infantry/feet/run");
            infantryRifleIdle = loadSequence("DemoAssets/TankGame/Infantry/rifle/idle");
            infantryBazookaIdle = loadSequence("DemoAssets/TankGame/Infantry/bazooka/idle");
            infantryRifleMoving = loadSequence("DemoAssets/TankGame/Infantry/rifle/move");
            infantryLegs = load("DemoAssets/TankGame/Infantry/feet/idle/survivor-idle_0.png");
            infantryRifleFire = loadSequence("DemoAssets/TankGame/Infantry/rifle/shoot");
            infantryBazookaFire = loadSequence("DemoAssets/TankGame/Infantry/bazooka/shoot");
            infantryShadow = load("DemoAssets/TankGame/Infantry/infantryShadow.png");

            tankSelectionImage = load("DemoAssets/TankGame/tankSelectionImage.png");
            lightTankSelectionImage = load("DemoAssets/TankGame/lightTank/lightTankSelectionImage.png");
            hellicopterSelectionImage = load("DemoAssets/TankGame/copter/hellicopterSelectionImage.png");
            riflemanSelectionImage = load("DemoAssets/TankGame/infantry/rifleSelectionImage.png");
            bazookamanSelectionImage = load("DemoAssets/TankGame/infantry/bazookaSelectionImage.png");

            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading RTS assets. Please verify Assets folder.");
            System.exit(1);
        }
    }
}

package GameDemo.RTSDemo;

import static Framework.GraphicalAssets.Graphic.load;
import static Framework.GraphicalAssets.Graphic.loadSequence;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RTSAssetManager {

    private static boolean initialized = false;
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // RTS ASSETS
    public static BufferedImage tankChasis, tankChasisRed;
    public static BufferedImage tankTurret, tankTurretRed, tankTurretShadow;
    public static BufferedImage tankHullDamaged, tankHullDamagedRed;
    public static BufferedImage tankTurretDamaged, tankTurretDamagedRed;
    public static BufferedImage[] tankFireAnimation, tankFireAnimationRed;
    public static BufferedImage[] tankFireAnimationDamaged, tankFireAnimationDamagedRed;
    public static BufferedImage bullet, bullet2;
    public static BufferedImage grassBG, grassBGDark, rtsPathing;
    public static BufferedImage hellicopter, hellicopterRed;
    public static BufferedImage hellicopterShadow;
    public static BufferedImage hellicopterDestroyed, hellicopterDestroyedRed;
    public static BufferedImage[] hellicopterAttack, hellicopterAttackRed;
    public static BufferedImage missile, yellowMissile;
    public static BufferedImage missileShadow, yellowMissileShadow;
    public static BufferedImage tankDeadHull;
    public static BufferedImage tankDeadHullShadow;
    public static BufferedImage tankShadow;
    public static BufferedImage tankDeadTurret;
    public static BufferedImage[] tankHullDeathAni;
    public static BufferedImage[] tankTurretDeathAni;
    public static BufferedImage lightTankHull, lightTankHullRed, lightTankTurret, lightTankTurretRed, lightTankShadow,
            lightTankHullDamaged, lightTankHullDamagedRed, lightTankTurretDamaged, lightTankTurretDamagedRed,
            lightTankHullDestroyed, lightTankTurretDestroyed, lightTankDeathShadow;
    public static BufferedImage[] lightTankFire, lightTankFireRed, lightTankFireDamaged, lightTankFireDamagedRed;

    public static BufferedImage[] infantryLegsRun;
    public static BufferedImage[] infantryRifleIdle, infantryRifleIdleRed;
    public static BufferedImage[] infantryRifleFire, infantryRifleFireRed;
    public static BufferedImage[] infantryBazookaIdle, infantryBazookaIdleRed;
    public static BufferedImage[] infantryBazookaFire, infantryBazookaFireRed;
    public static BufferedImage[] infantryBazookaDie, infantryBazookaDieRed;
    public static BufferedImage[] infantryRifleDie, infantryRifleDieRed;
    public static BufferedImage infantryRifleDead, infantryRifleDeadRed;
    public static BufferedImage infantryBazookaDead, infantryBazookaDeadRed;
    public static BufferedImage infantryLegs, infantryShadow;
    public static BufferedImage tankSelectionImage, lightTankSelectionImage, riflemanSelectionImage, bazookamanSelectionImage, hellicopterSelectionImage;

    public static BufferedImage building, buildingShadow;

    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] explosionSequenceSmall;
    public static BufferedImage[] impactCone;

    public static BufferedImage landmine, landmineRed, landmineSelectionImage, landmineBlast;

    public static BufferedImage layMineButton, layMineButtonHover;
    public static BufferedImage digInButton;
    public static BufferedImage digOutButton;
    public static BufferedImage frontalArmorButton;
    public static BufferedImage heatSeekersButton;
    public static BufferedImage infantryHelmetButton;


    public static BufferedImage immobilizationIcon, shieldIcon;
    
    public static BufferedImage JEngineIconLoading;
    
    public static BufferedImage sandbagsForTank;

    public static void initialize() {
        if (initialized) {
            return;
        }
        try {
            CompletableFuture<Void> future = CompletableFuture.allOf(
                loadExplosionAssets(),
                loadTankAssets(),
                loadHelicopterAssets(),
                loadLightTankAssets(),
                loadInfantryAssets(),
                loadLandmineAssets(),
                loadSelectionImages(),
                loadUtilityAssets(),
                loadMapAssets1(),
                loadMapAssets2(),
                loadMapAssets3(),
                loadButtonAssets()
            );

            future.thenRun(() -> {
                preloadUnits();
                initialized = true;
                System.out.println("All assets loaded successfully.");
            }).exceptionally(ex -> {
                System.out.println("Error loading RTS assets. Please verify Assets folder.");
                ex.printStackTrace();
                System.exit(1);
                return null;
            });

            future.join(); // Wait for all assets to load
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading RTS assets. Please verify Assets folder.");
            System.exit(1);
        }
    }

    private static CompletableFuture<Void> loadExplosionAssets() {
        return CompletableFuture.runAsync(() -> {
            explosionSequence = loadSequence("DemoAssets/explosionSequence");
            explosionSequenceSmall = loadSequence("DemoAssets/explosionSequence_small");
            impactCone = loadSequence("DemoAssets/TankGame/impact");
        }, executor);
    }
    
     private static CompletableFuture<Void> loadMapAssets1() {
        return CompletableFuture.runAsync(() -> { 
                grassBG = load("DemoAssets/TankGame/terrainPlayground.png");  // load("DemoAssets/TankGame/grassTerrain_mega3.png");
            }, executor);
        }
     
    private static CompletableFuture<Void> loadMapAssets2() {
        return CompletableFuture.runAsync(() -> {
            grassBGDark = load("DemoAssets/TankGame/terrainPlayground.png"); // load("DemoAssets/TankGame/grassTerrain_mega3_dark.png");
        }, executor);
    }
    
    private static CompletableFuture<Void> loadMapAssets3() {
        return CompletableFuture.runAsync(() -> {
            rtsPathing = load("DemoAssets/TankGame/terrainPlaygroundPathing.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadTankAssets() {
        return CompletableFuture.runAsync(() -> {
            tankChasis = load("DemoAssets/TankGame/tank1-hull.png");
            tankChasisRed = greenToRed(tankChasis);
            tankTurret = load("DemoAssets/TankGame/tank1-turret.png");
            tankTurretRed = greenToRed(tankTurret);
            tankTurretShadow = load("DemoAssets/TankGame/tank1-turret-shadow.png");
            tankFireAnimation = loadSequence("DemoAssets/TankGame/tank1Fire");
            tankFireAnimationRed = greenToRed(tankFireAnimation);
            bullet = load("DemoAssets/TankGame/bullet.png");
            bullet2 = load("DemoAssets/TankGame/bullet2.png");
            tankDeadHull = load("DemoAssets/TankGame/destroyedHull.png");
            tankDeadHullShadow = load("DemoAssets/TankGame/destroyedHullShadow.png");
            tankShadow = load("DemoAssets/TankGame/tankShadow.png");
            tankHullDamaged = load("DemoAssets/TankGame/tankHullDamaged.png");
            tankHullDamagedRed = greenToRed(tankHullDamaged);
            tankTurretDamaged = load("DemoAssets/TankGame/tankTurretDamaged.png");
            tankTurretDamagedRed = greenToRed(tankTurretDamaged);
            tankFireAnimationDamaged = loadSequence("DemoAssets/TankGame/tank1FireDamaged");
            tankFireAnimationDamagedRed = greenToRed(tankFireAnimationDamaged);
            tankDeadTurret = load("DemoAssets/TankGame/destroyedTurret.png");
            tankHullDeathAni = loadSequence("DemoAssets/TankGame/tankHullDeath");
            tankTurretDeathAni = loadSequence("DemoAssets/TankGame/tankTurretDeath");
            sandbagsForTank = load("DemoAssets/TankGame/sandbagsForTank.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadHelicopterAssets() {
        return CompletableFuture.runAsync(() -> {
            hellicopter = load("DemoAssets/TankGame/copter/base.png");
            hellicopterRed = blueToRed(hellicopter);
            hellicopterDestroyed = load("DemoAssets/TankGame/copter/hellicopterDestroyed.png");
            hellicopterDestroyedRed = blueToRed(hellicopterDestroyed);
            hellicopterShadow = load("DemoAssets/TankGame/copter/shadow.png");
            hellicopterAttack = loadSequence("DemoAssets/TankGame/copter/fire");
            hellicopterAttackRed = blueToRed(hellicopterAttack);
            missile = load("DemoAssets/TankGame/missile.png");
            missileShadow = load("DemoAssets/TankGame/missileShadow.png");
            yellowMissile = load("DemoAssets/TankGame/yellowMissile.png");
            yellowMissileShadow = load("DemoAssets/TankGame/yellowMissileShadow.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadLightTankAssets() {
        return CompletableFuture.runAsync(() -> {
            lightTankHull = load("DemoAssets/TankGame/lightTank/lightTankHull.png");
            lightTankHullRed = greenToRed(lightTankHull);
            lightTankTurret = load("DemoAssets/TankGame/lightTank/lightTankTurret.png");
            lightTankTurretRed = greenToRed(lightTankTurret);
            lightTankShadow = load("DemoAssets/TankGame/lightTank/lightTankShadow.png");
            lightTankFire = loadSequence("DemoAssets/TankGame/lightTank/fire");
            lightTankFireRed = greenToRed(lightTankFire);
            lightTankFireDamaged = loadSequence("DemoAssets/TankGame/lightTank/fireDamaged");
            lightTankFireDamagedRed = greenToRed(lightTankFireDamaged);
            lightTankTurretDamaged = load("DemoAssets/TankGame/lightTank/lightTankTurretDamaged.png");
            lightTankTurretDamagedRed = greenToRed(lightTankTurretDamaged);
            lightTankHullDamaged = load("DemoAssets/TankGame/lightTank/lightTankHullDamaged.png");
            lightTankHullDamagedRed = greenToRed(lightTankHullDamaged);
            lightTankTurretDestroyed = load("DemoAssets/TankGame/lightTank/lightTankTurretDestroyed.png");
            lightTankHullDestroyed = load("DemoAssets/TankGame/lightTank/lightTankHullDestroyed.png");
            lightTankDeathShadow = load("DemoAssets/TankGame/lightTank/lightTankDeathShadow.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadInfantryAssets() {
        return CompletableFuture.runAsync(() -> {
            infantryLegs = load("DemoAssets/TankGame/Infantry/feet/idle/survivor-idle_0.png");
            infantryLegsRun = loadSequence("DemoAssets/TankGame/Infantry/feet/run");
            infantryRifleIdle = loadSequence("DemoAssets/TankGame/Infantry/rifle/idle");
            infantryRifleIdleRed = darkToRed(infantryRifleIdle);
            infantryBazookaIdle = loadSequence("DemoAssets/TankGame/Infantry/bazooka/idle");
            infantryBazookaIdleRed = darkToRed(infantryBazookaIdle);
            infantryRifleFire = loadSequence("DemoAssets/TankGame/Infantry/rifle/shoot");
            infantryRifleFireRed = darkToRed(infantryRifleFire);
            infantryRifleDie = loadSequence("DemoAssets/TankGame/Infantry/rifle/die");
            infantryRifleDieRed = darkToRed(infantryRifleDie);
            infantryRifleDead = load("DemoAssets/TankGame/Infantry/rifle/dead.png");
            infantryRifleDeadRed = darkToRed(infantryRifleDead);
            infantryBazookaFire = loadSequence("DemoAssets/TankGame/Infantry/bazooka/shoot");
            infantryBazookaFireRed = darkToRed(infantryBazookaFire);
            infantryShadow = load("DemoAssets/TankGame/Infantry/infantryShadow.png");
            infantryBazookaDie = loadSequence("DemoAssets/TankGame/Infantry/bazooka/die");
            infantryBazookaDieRed = darkToRed(infantryBazookaDie);
            infantryBazookaDead = load("DemoAssets/TankGame/Infantry/bazooka/dead.png");
            infantryBazookaDeadRed = darkToRed(infantryBazookaDead);
        }, executor);
    }

    private static CompletableFuture<Void> loadLandmineAssets() {
        return CompletableFuture.runAsync(() -> {
            landmine = load("DemoAssets/TankGame/landmine.png");
            landmineRed = greenToRed(landmine);
            landmineSelectionImage = load("DemoAssets/TankGame/landmineSelectionImage.png");
            landmineBlast = load("DemoAssets/TankGame/landmineBlast.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadSelectionImages() {
        return CompletableFuture.runAsync(() -> {
            tankSelectionImage = load("DemoAssets/TankGame/tankSelectionImage.png");
            lightTankSelectionImage = load("DemoAssets/TankGame/lightTank/lightTankSelectionImage.png");
            hellicopterSelectionImage = load("DemoAssets/TankGame/copter/hellicopterSelectionImage.png");
            riflemanSelectionImage = load("DemoAssets/TankGame/infantry/rifleSelectionImage.png");
            bazookamanSelectionImage = load("DemoAssets/TankGame/infantry/bazookaSelectionImage.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadUtilityAssets() {
        return CompletableFuture.runAsync(() -> {
            building = load("DemoAssets/TankGame/building.png");
            buildingShadow = load("DemoAssets/TankGame/buildingShadow.png");
            immobilizationIcon = load("DemoAssets/TankGame/immobilizationIcon.png");
            shieldIcon = load("DemoAssets/TankGame/shieldIcon.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadButtonAssets() {
        return CompletableFuture.runAsync(() -> {
            layMineButton = load("DemoAssets/TankGame/Buttons/layMineButton.png");
            layMineButtonHover = load("DemoAssets/TankGame/Buttons/layMineButtonHover.png");
            JEngineIconLoading = load("DemoAssets/JEngineIconLoading.png");
            digInButton = load("DemoAssets/TankGame/Buttons/digInUnhovered.png");
            digOutButton = load("DemoAssets/TankGame/Buttons/digOutUnhovered.png");
            frontalArmorButton = load("DemoAssets/TankGame/Buttons/frontalArmor.png");
            heatSeekersButton = load("DemoAssets/TankGame/Buttons/heatSeekers.png");
            infantryHelmetButton = load("DemoAssets/TankGame/Buttons/infantryHelmet.png");
        }, executor);
    }

    /**
     * instantiates a unit of each type to load static variables and set static
     * scale
     */
    private static void preloadUnits() {
        System.out.println("preloading units");
        new TankUnit(0, 0);
        new LightTank(0, 0, 0);
        new Rifleman(0, 0, 0);
        new Bazookaman(0, 0, 0);
        new Hellicopter(0, 0, 0);
        new Landmine(0, 0, 0);
        System.out.println("done preloading");
    }

    public static BufferedImage[] greenToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = greenToRed(input[i]);
        }
        return out;
    }

    public static BufferedImage greenToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() > (prevColor.getRed() + prevColor.getBlue()) * .5) {
                    int newRed = Math.min(255, (int) (prevColor.getGreen() * 1.5));
                    int newGreen = (int) (prevColor.getRed() * .75);
                    int newBlue = (int) (prevColor.getBlue() * .75);
                    Color newColor = new Color(newRed, newGreen, newBlue);
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage blueToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() > (prevColor.getRed() + prevColor.getGreen()) * .5) {
                    int newRed = Math.min(255, (int) (prevColor.getBlue() * 1.5));
                    int newGreen = (int) (prevColor.getRed() * .75);
                    int newBlue = (int) (prevColor.getGreen() * .75);
                    Color newColor = new Color(newRed, newGreen, newBlue);
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] blueToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = blueToRed(input[i]);
        }
        return out;
    }

    public static BufferedImage darkToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300) {
                    int newRed = 0;
                    if (prevColor.getRed() > 0) {
                        newRed = Math.min(255, (int) (prevColor.getRed() + 50 * 1.5));
                    }
                    int newGreen = (int) (prevColor.getRed());
                    int newBlue = (int) (prevColor.getGreen());
                    Color newColor = new Color(newRed, newGreen, newBlue, prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = darkToRed(input[i]);
        }
        return out;
    }

    public static BufferedImage[] removeBlue(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = removeBlue(input[i]);
        }
        return out;
    }

    public static BufferedImage removeBlue(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() > prevColor.getRed() && prevColor.getBlue() > prevColor.getGreen()) {
                    int newRed = prevColor.getRed();
                    int newGreen = prevColor.getGreen();
                    int newBlue = Math.min(prevColor.getRed(), prevColor.getGreen());
                    Color newColor = new Color(newRed, newGreen, newBlue, prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage removeGreenOutright(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen()> prevColor.getRed() && prevColor.getGreen()> prevColor.getBlue() && prevColor.getGreen() > 50) {
                    int newRed = 0;
                    int newGreen = 0;
                    int newBlue = 0;
                    Color newColor = new Color(newRed, newGreen, newBlue, 0);
                    bi.setRGB(x, y, newColor.getRGB());
                } else {
                    Color newColor = new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha());
                    bi.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        return bi;
    }
}

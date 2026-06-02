package GameDemo.RTSDemo;

import static Framework.GraphicalAssets.Graphic.load;
import static Framework.GraphicalAssets.Graphic.loadSequence;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Apache;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;
import GameDemo.RTSDemo.Units.Truck;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RTSAssetManager {

    private static boolean initialized = false;
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // RTS ASSETS
    public static BufferedImage tankChasis, tankChasisRed, tankChasisYellow;
    public static BufferedImage tankTurret, tankTurretRed, tankTurretYellow;
    public static BufferedImage tankHullDamaged, tankHullDamagedRed, tankHullDamagedYellow;
    public static BufferedImage tankTurretDamaged, tankTurretDamagedRed, tankTurretDamagedYellow;
    public static BufferedImage[] tankFireAnimation, tankFireAnimationRed, tankFireAnimationYellow;
    public static BufferedImage[] tankFireAnimationDamaged, tankFireAnimationDamagedRed, tankFireAnimationDamagedYellow;
    public static BufferedImage bullet, bullet2;
    public static BufferedImage grassBG, grassBGDark, rtsPathing;
    public static BufferedImage hellicopter, hellicopterRed, hellicopterYellow;
    public static BufferedImage hellicopterBlades, hellicopterBladesRed, hellicopterBladesYellow;
    public static BufferedImage hellicopterShadow;
    public static BufferedImage hellicopterDestroyed, hellicopterDestroyedRed, hellicopterDestroyedYellow;
    public static BufferedImage[] hellicopterAttack, hellicopterAttackRed, hellicopterAttackYellow;
    public static BufferedImage chopperDead, chopperRubble, chopperDeathShadow;

    public static BufferedImage apache, apacheRed, apacheYellow;
    public static BufferedImage apacheBlades, apacheBladesRed, apacheBladesYellow;
    public static BufferedImage apacheShadow;
    public static BufferedImage apacheDestroyed, apacheDestroyedRed, apacheDestroyedYellow;
    public static BufferedImage[] apacheAttack, apacheAttackRed, apacheAttackYellow;
    public static BufferedImage apacheEmptyPods, apacheEmptyPodsRed, apacheEmptyPodsYellow;
    public static BufferedImage apacheMissileProjectile;
    public static BufferedImage apacheDockedMissile;
    public static BufferedImage missile, yellowMissile;
    public static BufferedImage missileShadow, yellowMissileShadow;
    public static BufferedImage tankDeadHull;
    public static BufferedImage[] tankDeath;
    public static BufferedImage tankDeadHullShadow;
    public static BufferedImage tankShadow;
    public static BufferedImage tankDeadTurret;
    public static BufferedImage[] tankHullDeathAni;
    public static BufferedImage[] tankTurretDeathAni;
    public static BufferedImage lightTankHull, lightTankHullRed, lightTankHullYellow,
            lightTankTurret, lightTankTurretRed, lightTankTurretYellow,
            lightTankHullDamaged, lightTankHullDamagedRed, lightTankHullDamagedYellow,
            lightTankTurretDamaged, lightTankTurretDamagedRed, lightTankTurretDamagedYellow,
            lightTankHullDestroyed, lightTankTurretDestroyed, lightTankDeathShadow;
    public static BufferedImage[] lightTankFire, lightTankFireRed, lightTankFireYellow,
            lightTankFireDamaged, lightTankFireDamagedRed, lightTankFireDamagedYellow;

    public static BufferedImage[] infantryLegsRun;
    public static BufferedImage[] infantryRifleIdle, infantryRifleIdleRed, infantryRifleIdleYellow;
    public static BufferedImage[] infantryRifleFire, infantryRifleFireRed, infantryRifleFireYellow;
    public static BufferedImage[] infantryBazookaIdle, infantryBazookaIdleRed, infantryBazookaIdleYellow;
    public static BufferedImage[] infantryBazookaFire, infantryBazookaFireRed, infantryBazookaFireYellow;
    public static BufferedImage[] infantryBazookaDie, infantryBazookaDieRed, infantryBazookaDieYellow;
    public static BufferedImage[] infantryRifleDie, infantryRifleDieRed, infantryRifleDieYellow;
    public static BufferedImage infantryRifleDead, infantryRifleDeadRed, infantryRifleDeadYellow;
    public static BufferedImage infantryBazookaDead, infantryBazookaDeadRed, infantryBazookaDeadYellow;
    public static BufferedImage infantryLegs, infantryShadow;
    public static BufferedImage tankSelectionImage, lightTankSelectionImage, riflemanSelectionImage, bazookamanSelectionImage, hellicopterSelectionImage, apacheSelectionImage;

    public static BufferedImage building;

    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] explosionSequenceSmall;
    public static BufferedImage[] impactCone;
    public static BufferedImage[] smallImpact;

    public static BufferedImage landmine, landmineRed, landmineYellow, landmineSelectionImage, landmineBlast;

    public static BufferedImage layMineButton, layMineButtonHover;
    public static BufferedImage digInButton;
    public static BufferedImage digOutButton;
    public static BufferedImage frontalArmorButton;
    public static BufferedImage heatSeekersButton;
    public static BufferedImage infantryHelmetButton;
    public static BufferedImage landButton;
    public static BufferedImage flyButton;
    public static BufferedImage loadButton;
    public static BufferedImage unloadButton;


    public static BufferedImage immobilizationIcon, shieldIcon;
    
    public static BufferedImage JEngineIconLoading;
    
    public static BufferedImage sandbagsForTank, sandbagsForTankDamaged;
    
    public static BufferedImage truckHull, truckHullRed, truckHullYellow,
            truckHullDamaged, truckHullDamagedRed, truckHullDamagedYellow,
            truckRubble, truckRubbleRed, truckRubbleYellow, truckDeathShadow;
    public static BufferedImage truckWheel;
    public static BufferedImage transportHeli, transportHeliRed, transportHeliYellow;
    public static BufferedImage transportHeliRoof, transportHeliRoofRed, transportHeliRoofYellow;

    public synchronized static void initialize() {
        if (initialized) {
            return;
        }
        try {
            CompletableFuture<Void> future = CompletableFuture.allOf(
                loadExplosionAssets(),
                loadTankAssets(),
                loadHelicopterAssets(),
                loadApacheAssets(),
                loadLightTankAssets(),
                loadInfantryAssets(),
                loadLandmineAssets(),
                loadTruckAssets(),
                loadTransportHeliAssets(),
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
            grassBGDark = load("DemoAssets/TankGame/terrainPlayground.png"); 
            // grassBGDark = load("DemoAssets/TankGame/terrainPlayground_dark.png");
        }, executor);
    }
    
    private static CompletableFuture<Void> loadMapAssets3() {
        return CompletableFuture.runAsync(() -> {
            rtsPathing = load("DemoAssets/TankGame/terrainPlaygroundPathing.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadTankAssets() {
        return CompletableFuture.runAsync(() -> {
            tankChasis = load("DemoAssets/TankGame/newTank/newHull2.png");
            tankChasisRed = greenToRed(tankChasis);
            tankChasisYellow = greenToYellow(tankChasis);
            tankTurret = load("DemoAssets/TankGame/newTank/newTurret.png");
            tankTurretRed = greenToRed(tankTurret);
            tankTurretYellow = greenToYellow(tankTurret);
            tankFireAnimation = loadSequence("DemoAssets/TankGame/newTank/fireAnimation");
            tankFireAnimationRed = greenToRed(tankFireAnimation);
            tankFireAnimationYellow = greenToYellow(tankFireAnimation);
            bullet = load("DemoAssets/TankGame/bullet.png");
            bullet2 = load("DemoAssets/TankGame/bullet2.png");
            tankDeadHullShadow = load("DemoAssets/TankGame/destroyedHullShadow.png");
            tankHullDamaged = load("DemoAssets/TankGame/newTank/newHull2Damaged.png");
            tankHullDamagedRed = greenToRed(tankHullDamaged);
            tankHullDamagedYellow = greenToYellow(tankHullDamaged);
            tankTurretDamaged = load("DemoAssets/TankGame/newTank/newTurretDamaged.png");
            tankTurretDamagedRed = greenToRed(tankTurretDamaged);
            tankTurretDamagedYellow = greenToYellow(tankTurretDamaged);
            tankFireAnimationDamaged = loadSequence("DemoAssets/TankGame/newTank/fireAnimationDamaged");
            tankFireAnimationDamagedRed = greenToRed(tankFireAnimationDamaged);
            tankFireAnimationDamagedYellow = greenToYellow(tankFireAnimationDamaged);
            tankDeadTurret = load("DemoAssets/TankGame/newTank/newTurretRubble.png");
            tankDeadHull = load("DemoAssets/TankGame/newTank/newHullRubble.png");
            sandbagsForTank = load("DemoAssets/TankGame/sandbagsForTank.png");
            sandbagsForTankDamaged = load("DemoAssets/TankGame/sandbagsForTankDamaged.png");
            tankDeath = loadSequence("DemoAssets/TankGame/newTank/deathAnimation1");
        }, executor);
    }

    private static CompletableFuture<Void> loadHelicopterAssets() {
        return CompletableFuture.runAsync(() -> {
            hellicopter = load("DemoAssets/TankGame/copter/newChopperFrames/newChopper3Cleaned.png");
            hellicopterRed = greenToRed(hellicopter);
            hellicopterYellow = greenToYellow(hellicopter);
            hellicopterDestroyed = load("DemoAssets/TankGame/copter/hellicopterDestroyed.png");
            hellicopterDestroyedRed = greenToRed(hellicopterDestroyed);
            hellicopterDestroyedYellow = greenToYellow(hellicopterDestroyed);
            hellicopterShadow = load("DemoAssets/TankGame/copter/shadow.png");
            hellicopterAttack = loadSequence("DemoAssets/TankGame/copter/newFire");
            hellicopterAttackRed = greenToRed(hellicopterAttack);
            hellicopterAttackYellow = greenToYellow(hellicopterAttack);
            hellicopterBlades = load("DemoAssets/TankGame/copter/newChopperFrames/blades.png");
            hellicopterBladesRed = greenToRed(hellicopterBlades);
            hellicopterBladesYellow = greenToYellow(hellicopterBlades);
            missile = load("DemoAssets/TankGame/copter/newMissile.png");
            missileShadow = load("DemoAssets/TankGame/missileShadow.png");
            yellowMissile = load("DemoAssets/TankGame/yellowMissile.png");
            yellowMissileShadow = load("DemoAssets/TankGame/yellowMissileShadow.png");
            chopperDead = load("DemoAssets/TankGame/copter/newChopperFrames/newChopperDead.png");
            chopperRubble = load("DemoAssets/TankGame/copter/newChopperFrames/newChopperRubble.png");
            chopperDeathShadow = load("DemoAssets/TankGame/copter/newChopperFrames/newChopperDeathShadow.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadApacheAssets() {
        return CompletableFuture.runAsync(() -> {
            apache = load("DemoAssets/TankGame/apache/newChopperFrames/newChopper3Cleaned.png");
            apacheRed = greenToRed(apache);
            apacheYellow = greenToYellow(apache);
            apacheDestroyed = load("DemoAssets/TankGame/apache/apacheDestroyed.png");
            apacheDestroyedRed = greenToRed(apacheDestroyed);
            apacheDestroyedYellow = greenToYellow(apacheDestroyed);
            apacheShadow = load("DemoAssets/TankGame/apache/shadow.png");
            apacheAttack = loadSequence("DemoAssets/TankGame/apache/newFire");
            apacheAttackRed = greenToRed(apacheAttack);
            apacheAttackYellow = greenToYellow(apacheAttack);
            apacheBlades = load("DemoAssets/TankGame/apache/newChopperFrames/blades.png");
            apacheBladesRed = greenToRed(apacheBlades);
            apacheBladesYellow = greenToYellow(apacheBlades);
            apacheEmptyPods = load("DemoAssets/TankGame/apache/apacheEmptyPods.png");
            apacheEmptyPodsRed = greenToRed(apacheEmptyPods);
            apacheEmptyPodsYellow = greenToYellow(apacheEmptyPods);
            apacheMissileProjectile = load("DemoAssets/TankGame/apache/apacheMissile.png");
            apacheDockedMissile = load("DemoAssets/TankGame/apache/dockedMissile.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadLightTankAssets() {
        return CompletableFuture.runAsync(() -> {
            lightTankHull = load("DemoAssets/TankGame/newLightTank/hull.png");
            lightTankHullRed = greenToRed(lightTankHull);
            lightTankHullYellow = greenToYellow(lightTankHull);
            lightTankTurret = load("DemoAssets/TankGame/newLightTank/turret.png");
            lightTankTurretRed = greenToRed(lightTankTurret);
            lightTankTurretYellow = greenToYellow(lightTankTurret);
            lightTankFire = loadSequence("DemoAssets/TankGame/newLightTank/fire");
            lightTankFireRed = greenToRed(lightTankFire);
            lightTankFireYellow = greenToYellow(lightTankFire);
            lightTankFireDamaged = loadSequence("DemoAssets/TankGame/newLightTank/fire_damaged");
            lightTankFireDamagedRed = greenToRed(lightTankFireDamaged);
            lightTankFireDamagedYellow = greenToYellow(lightTankFireDamaged);
            lightTankTurretDamaged = load("DemoAssets/TankGame/newLightTank/turret_damaged.png");
            lightTankTurretDamagedRed = greenToRed(lightTankTurretDamaged);
            lightTankTurretDamagedYellow = greenToYellow(lightTankTurretDamaged);
            lightTankHullDamaged = load("DemoAssets/TankGame/newLightTank/hull_damaged.png");
            lightTankHullDamagedRed = greenToRed(lightTankHullDamaged);
            lightTankHullDamagedYellow = greenToYellow(lightTankHullDamaged);
            lightTankTurretDestroyed = load("DemoAssets/TankGame/newLightTank/turret_destroyed.png");
            lightTankHullDestroyed = load("DemoAssets/TankGame/newLightTank/hull_destroyed.png");
            lightTankDeathShadow = load("DemoAssets/TankGame/newLightTank/deathShadow.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadInfantryAssets() {
        return CompletableFuture.runAsync(() -> {
            infantryLegs = load("DemoAssets/TankGame/Infantry/feet/idle/survivor-idle_0.png");
            infantryLegsRun = loadSequence("DemoAssets/TankGame/Infantry/feet/run");
            infantryRifleIdle = loadSequence("DemoAssets/TankGame/Infantry/rifle/idle");
            infantryRifleIdleRed = darkToRed(infantryRifleIdle);
            infantryRifleIdleYellow = darkToYellow(infantryRifleIdle);
            infantryBazookaIdle = loadSequence("DemoAssets/TankGame/Infantry/bazooka/idle");
            infantryBazookaIdleRed = darkToRed(infantryBazookaIdle);
            infantryBazookaIdleYellow = darkToYellow(infantryBazookaIdle);
            infantryRifleFire = loadSequence("DemoAssets/TankGame/Infantry/rifle/shoot");
            infantryRifleFireRed = darkToRed(infantryRifleFire);
            infantryRifleFireYellow = darkToYellow(infantryRifleFire);
            infantryRifleDie = loadSequence("DemoAssets/TankGame/Infantry/rifle/die");
            infantryRifleDieRed = darkToRed(infantryRifleDie);
            infantryRifleDieYellow = darkToYellow(infantryRifleDie);
            infantryRifleDead = load("DemoAssets/TankGame/Infantry/rifle/dead.png");
            infantryRifleDeadRed = darkToRed(infantryRifleDead);
            infantryRifleDeadYellow = darkToYellow(infantryRifleDead);
            infantryBazookaFire = loadSequence("DemoAssets/TankGame/Infantry/bazooka/shoot");
            infantryBazookaFireRed = darkToRed(infantryBazookaFire);
            infantryBazookaFireYellow = darkToYellow(infantryBazookaFire);
            infantryShadow = load("DemoAssets/TankGame/Infantry/infantryShadow.png");
            infantryBazookaDie = loadSequence("DemoAssets/TankGame/Infantry/bazooka/die");
            infantryBazookaDieRed = darkToRed(infantryBazookaDie);
            infantryBazookaDieYellow = darkToYellow(infantryBazookaDie);
            infantryBazookaDead = load("DemoAssets/TankGame/Infantry/bazooka/dead.png");
            infantryBazookaDeadRed = darkToRed(infantryBazookaDead);
            infantryBazookaDeadYellow = darkToYellow(infantryBazookaDead);
            smallImpact = loadSequence("DemoAssets/TankGame/smallImpact");
        }, executor);
    }

    private static CompletableFuture<Void> loadLandmineAssets() {
        return CompletableFuture.runAsync(() -> {
            landmine = load("DemoAssets/TankGame/landmine.png");
            landmineRed = greenToRed(landmine);
            landmineYellow = greenToYellow(landmine);
            landmineSelectionImage = load("DemoAssets/TankGame/landmineSelectionImage.png");
            landmineBlast = load("DemoAssets/TankGame/landmineBlast.png");
        }, executor);
    }
    
    
    private static CompletableFuture<Void> loadTruckAssets() {
        return CompletableFuture.runAsync(() -> {
            truckHull = load("DemoAssets/TankGame/truck/hull.png");
            truckHullRed = greenToRed(truckHull);
            truckHullYellow = greenToYellow(truckHull);
            truckHullDamaged = load("DemoAssets/TankGame/truck/hull_damaged.png");
            truckHullDamagedRed = greenToRed(truckHullDamaged);
            truckHullDamagedYellow = greenToYellow(truckHullDamaged);
            truckWheel = load("DemoAssets/TankGame/truck/tire.png");
            truckRubble = load("DemoAssets/TankGame/truck/greenRubble2.png");
            truckRubbleRed = greenToRed(truckRubble);
            truckRubbleYellow = greenToYellow(truckRubble);
            truckDeathShadow = load("DemoAssets/TankGame/truck/deathShadow.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadTransportHeliAssets() {
        return CompletableFuture.runAsync(() -> {
            transportHeli = load("DemoAssets/TankGame/transportHeli/body.png");
            transportHeliRed = greenToRed(transportHeli);
            transportHeliYellow = greenToYellow(transportHeli);
            transportHeliRoof = load("DemoAssets/TankGame/transportHeli/transportHeliRoof.png");
            transportHeliRoofRed = greenToRed(transportHeliRoof);
            transportHeliRoofYellow = greenToYellow(transportHeliRoof);
        }, executor);
    }


    private static CompletableFuture<Void> loadSelectionImages() {
        return CompletableFuture.runAsync(() -> {
            tankSelectionImage = load("DemoAssets/TankGame/tankSelectionImage.png");
            lightTankSelectionImage = load("DemoAssets/TankGame/newLightTank/selectionImage.png");
            hellicopterSelectionImage = load("DemoAssets/TankGame/copter/hellicopterSelectionImage.png");
            apacheSelectionImage = load("DemoAssets/TankGame/apache/apacheSelectionImage.png");
            riflemanSelectionImage = load("DemoAssets/TankGame/infantry/rifleSelectionImage.png");
            bazookamanSelectionImage = load("DemoAssets/TankGame/infantry/bazookaSelectionImage.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadUtilityAssets() {
        return CompletableFuture.runAsync(() -> {
            building = load("DemoAssets/TankGame/building.png");
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
            loadButton = load("DemoAssets/TankGame/Buttons/loadButton.png");
            unloadButton = load("DemoAssets/TankGame/Buttons/unloadButton.png");
            landButton = load("DemoAssets/TankGame/Buttons/landButton.png");
            flyButton = load("DemoAssets/TankGame/Buttons/flyButton.png");
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
        new Apache(0, 0, 0);
        new Landmine(0, 0, 0);
        new Truck(0,0,0);
        new TransportHelicopter(0, 0, 0);
        RTSUnitIdHelper.reset();
        System.out.println("done preloading.");
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
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5) {
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

    public static BufferedImage[] greenToYellow(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = greenToYellow(input[i]);
        }
        return out;
    }

    public static BufferedImage greenToYellow(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5) {
                    int newRed = Math.min(255, (int)(prevColor.getGreen() * 1.6));
                    int newGreen = Math.min(255, (int)(prevColor.getGreen() * 1.24));
                    int newBlue = Math.min(255, (int)(prevColor.getBlue() * 0.24));
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
                if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300 &&  prevColor.getRed() < 30 + prevColor.getGreen() + prevColor.getBlue() ) {
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

    public static BufferedImage darkToYellow(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300
                        && prevColor.getRed() < 30 + prevColor.getGreen() + prevColor.getBlue()
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() > 30 ) {
                    int prevYellow = (prevColor.getRed() + prevColor.getGreen())/2;
                    int newRed = Math.min(255, (int) (50 + prevYellow * 1.5));
                    int newGreen = Math.min(255, (int)(20 + prevYellow * 1.5));
                    int newBlue = prevColor.getGreen();
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

    public static BufferedImage[] darkToYellow(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = darkToYellow(input[i]);
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

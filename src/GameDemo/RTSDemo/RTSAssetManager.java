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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RTSAssetManager {

    private static boolean initialized = false;
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // ── Base (team-neutral) assets ────────────────────────────────────────────
    public static BufferedImage tankChasis;
    public static BufferedImage tankTurret;
    public static BufferedImage tankHullDamaged;
    public static BufferedImage tankTurretDamaged;
    public static BufferedImage[] tankFireAnimation;
    public static BufferedImage[] tankFireAnimationDamaged;
    public static BufferedImage bullet, bullet2;
    public static BufferedImage grassBG, rtsPathing;
    public static BufferedImage hellicopter;
    public static BufferedImage hellicopterBlades;
    public static BufferedImage hellicopterDestroyed;
    public static BufferedImage[] hellicopterAttack;
    public static BufferedImage chopperDead, chopperRubble, chopperDeathShadow;

    public static BufferedImage apache;
    public static BufferedImage apacheBlades;
    public static BufferedImage apacheDestroyed;
    public static BufferedImage[] apacheAttack;
    public static BufferedImage apacheEmptyPods;
    public static BufferedImage apacheMissileProjectile;
    public static BufferedImage apacheDockedMissile;
    public static BufferedImage missile, yellowMissile;
    public static BufferedImage missileShadow, yellowMissileShadow;
    public static BufferedImage tankDeadHull;
    public static BufferedImage[] tankDeath;
    public static BufferedImage tankDeadHullShadow;
    public static BufferedImage tankDeadTurret;
    public static BufferedImage lightTankHull, lightTankTurret,
            lightTankHullDamaged, lightTankTurretDamaged,
            lightTankHullDestroyed, lightTankTurretDestroyed, lightTankDeathShadow;
    public static BufferedImage[] lightTankFire, lightTankFireDamaged;

    public static BufferedImage[] infantryLegsRun;
    public static BufferedImage[] infantryRifleIdle;
    public static BufferedImage[] infantryRifleFire;
    public static BufferedImage[] infantryBazookaIdle;
    public static BufferedImage[] infantryBazookaFire;
    public static BufferedImage[] infantryBazookaDie;
    public static BufferedImage[] infantryRifleDie;
    public static BufferedImage infantryRifleDead;
    public static BufferedImage infantryBazookaDead;
    public static BufferedImage infantryLegs, infantryShadow;
    public static BufferedImage tankSelectionImage, lightTankSelectionImage, riflemanSelectionImage, bazookamanSelectionImage, hellicopterSelectionImage, apacheSelectionImage;

    public static BufferedImage building;

    public static BufferedImage hangarBase;
    public static BufferedImage hangarFloor;
    public static BufferedImage metalShack;
    public static BufferedImage buildingGreen1;
    public static BufferedImage buildingGreen1Extras;
    public static BufferedImage orangeWoodHouse;
    public static BufferedImage orangeWoodHouseExtras;
    public static BufferedImage greenShippingContainer;
    public static BufferedImage propaneTank;
    public static BufferedImage bush1;
    public static BufferedImage bush2;
    public static BufferedImage bush3;
    public static BufferedImage tree1;
    public static BufferedImage tree1Shadow;
    public static BufferedImage tree2;
    public static BufferedImage tree2Shadow;
    public static BufferedImage tree3;
    public static BufferedImage tree3Shadow;

    public static BufferedImage[] explosionSequence;
    public static BufferedImage[] explosionSequenceSmall;
    public static BufferedImage[] impactCone;
    public static BufferedImage[] smallImpact;

    public static BufferedImage landmine, landmineSelectionImage, landmineBlast;

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

    public static BufferedImage truckHull, truckHullDamaged, truckRubble, truckDeathShadow;
    public static BufferedImage truckWheel;
    public static BufferedImage transportHeli, transportHeliRoof;

    // ── Team-colored asset maps (populated by precomputeTeamAssets) ───────────
    private static final Map<Integer, BufferedImage>   tankChasisMap            = new HashMap<>();
    private static final Map<Integer, BufferedImage>   tankTurretMap            = new HashMap<>();
    private static final Map<Integer, BufferedImage>   tankHullDamagedMap       = new HashMap<>();
    private static final Map<Integer, BufferedImage>   tankTurretDamagedMap     = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> tankFireAnimMap          = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> tankFireAnimDamagedMap   = new HashMap<>();

    private static final Map<Integer, BufferedImage>   lightTankHullMap         = new HashMap<>();
    private static final Map<Integer, BufferedImage>   lightTankTurretMap       = new HashMap<>();
    private static final Map<Integer, BufferedImage>   lightTankHullDamagedMap  = new HashMap<>();
    private static final Map<Integer, BufferedImage>   lightTankTurretDamagedMap = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> lightTankFireMap         = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> lightTankFireDamagedMap  = new HashMap<>();

    private static final Map<Integer, BufferedImage>   hellicopterBodyMap       = new HashMap<>();
    private static final Map<Integer, BufferedImage>   hellicopterDestroyedMap  = new HashMap<>();
    private static final Map<Integer, BufferedImage>   hellicopterBladesMap     = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> hellicopterAttackMap     = new HashMap<>();

    private static final Map<Integer, BufferedImage>   apacheBodyMap            = new HashMap<>();
    private static final Map<Integer, BufferedImage>   apacheEmptyPodsMap       = new HashMap<>();
    private static final Map<Integer, BufferedImage>   apacheDestroyedMap       = new HashMap<>();
    private static final Map<Integer, BufferedImage>   apacheBladesMap          = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> apacheAttackMap          = new HashMap<>();

    private static final Map<Integer, BufferedImage>   transportHeliBodyMap     = new HashMap<>();
    private static final Map<Integer, BufferedImage>   transportHeliRoofMap     = new HashMap<>();

    private static final Map<Integer, BufferedImage>   truckHullMap             = new HashMap<>();
    private static final Map<Integer, BufferedImage>   truckHullDamagedMap      = new HashMap<>();
    private static final Map<Integer, BufferedImage>   truckRubbleMap           = new HashMap<>();

    private static final Map<Integer, BufferedImage>   landmineMap              = new HashMap<>();

    private static final Map<Integer, BufferedImage[]> rifleIdleMap             = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> rifleFireMap             = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> rifleDieMap              = new HashMap<>();
    private static final Map<Integer, BufferedImage>   rifleDeadMap             = new HashMap<>();

    private static final Map<Integer, BufferedImage[]> bazookaIdleMap           = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> bazookaFireMap           = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> bazookaDieMap            = new HashMap<>();
    private static final Map<Integer, BufferedImage>   bazookaDeadMap           = new HashMap<>();

    // ── Team color transform ──────────────────────────────────────────────────

    public static BufferedImage applyTeamTransform(BufferedImage src, int team) {
        return switch (team) {
            case -1 -> greenToSilver(src);
            case 1 -> greenToRed(src);
            case 2 -> greenToCharcoal(src);
            case 3 -> greenToTan(src);
            case 4 -> greenToArctic(src);
            default -> src;
        };
    }

    public static BufferedImage[] applyTeamTransform(BufferedImage[] src, int team) {
        return switch (team) {
            case -1 -> greenToSilver(src);
            case 1 -> greenToRed(src);
            case 2 -> greenToCharcoal(src);
            case 3 -> greenToTan(src);
            case 4 -> greenToArctic(src);
            default -> src;
        };
    }

    public static BufferedImage applyInfantryTeamTransform(BufferedImage src, int team) {
        return switch (team) {
            case -1 -> darkToSilver(src);
            case 1 -> darkToRed(src);
            case 2 -> darkToCharcoal(src);
            case 3 -> darkToTan(src);
            case 4 -> darkToArctic(src);
            default -> src;
        };
    }

    public static BufferedImage[] applyInfantryTeamTransform(BufferedImage[] src, int team) {
        return switch (team) {
            case -1 -> darkToSilver(src);
            case 1 -> darkToRed(src);
            case 2 -> darkToCharcoal(src);
            case 3 -> darkToTan(src);
            case 4 -> darkToArctic(src);
            default -> src;
        };
    }

    // ── Precompute all team-colored assets for the active teams ───────────────

    public static void precomputeTeamAssets(Set<Integer> activeTeams) {
        for (int team : activeTeams) {
            tankChasisMap.put(team,           applyTeamTransform(tankChasis, team));
            tankTurretMap.put(team,           applyTeamTransform(tankTurret, team));
            tankHullDamagedMap.put(team,      applyTeamTransform(tankHullDamaged, team));
            tankTurretDamagedMap.put(team,    applyTeamTransform(tankTurretDamaged, team));
            tankFireAnimMap.put(team,         applyTeamTransform(tankFireAnimation, team));
            tankFireAnimDamagedMap.put(team,  applyTeamTransform(tankFireAnimationDamaged, team));

            lightTankHullMap.put(team,          applyTeamTransform(lightTankHull, team));
            lightTankTurretMap.put(team,        applyTeamTransform(lightTankTurret, team));
            lightTankHullDamagedMap.put(team,   applyTeamTransform(lightTankHullDamaged, team));
            lightTankTurretDamagedMap.put(team, applyTeamTransform(lightTankTurretDamaged, team));
            lightTankFireMap.put(team,          applyTeamTransform(lightTankFire, team));
            lightTankFireDamagedMap.put(team,   applyTeamTransform(lightTankFireDamaged, team));

            hellicopterBodyMap.put(team,      applyTeamTransform(hellicopter, team));
            hellicopterDestroyedMap.put(team, applyTeamTransform(hellicopterDestroyed, team));
            hellicopterBladesMap.put(team,    applyTeamTransform(hellicopterBlades, team));
            hellicopterAttackMap.put(team,    applyTeamTransform(hellicopterAttack, team));

            apacheBodyMap.put(team,       applyTeamTransform(apache, team));
            apacheEmptyPodsMap.put(team,  applyTeamTransform(apacheEmptyPods, team));
            apacheDestroyedMap.put(team,  applyTeamTransform(apacheDestroyed, team));
            apacheBladesMap.put(team,     applyTeamTransform(apacheBlades, team));
            apacheAttackMap.put(team,     applyTeamTransform(apacheAttack, team));

            transportHeliBodyMap.put(team, applyTeamTransform(transportHeli, team));
            transportHeliRoofMap.put(team, applyTeamTransform(transportHeliRoof, team));

            truckHullMap.put(team,        applyTeamTransform(truckHull, team));
            truckHullDamagedMap.put(team, applyTeamTransform(truckHullDamaged, team));
            truckRubbleMap.put(team,      applyTeamTransform(truckRubble, team));

            landmineMap.put(team, applyTeamTransform(landmine, team));

            rifleIdleMap.put(team, applyInfantryTeamTransform(infantryRifleIdle, team));
            rifleFireMap.put(team, applyInfantryTeamTransform(infantryRifleFire, team));
            rifleDieMap.put(team,  applyInfantryTeamTransform(infantryRifleDie, team));
            rifleDeadMap.put(team, applyInfantryTeamTransform(infantryRifleDead, team));

            bazookaIdleMap.put(team, applyInfantryTeamTransform(infantryBazookaIdle, team));
            bazookaFireMap.put(team, applyInfantryTeamTransform(infantryBazookaFire, team));
            bazookaDieMap.put(team,  applyInfantryTeamTransform(infantryBazookaDie, team));
            bazookaDeadMap.put(team, applyInfantryTeamTransform(infantryBazookaDead, team));
        }
    }

    // ── Getters for team-colored assets ───────────────────────────────────────

    public static BufferedImage   getTankChasis(int team)           { return tankChasisMap.get(team); }
    public static BufferedImage   getTankTurret(int team)           { return tankTurretMap.get(team); }
    public static BufferedImage   getTankHullDamaged(int team)      { return tankHullDamagedMap.get(team); }
    public static BufferedImage   getTankTurretDamaged(int team)    { return tankTurretDamagedMap.get(team); }
    public static BufferedImage[] getTankFireAnim(int team)         { return tankFireAnimMap.get(team); }
    public static BufferedImage[] getTankFireAnimDamaged(int team)  { return tankFireAnimDamagedMap.get(team); }

    public static BufferedImage   getLightTankHull(int team)          { return lightTankHullMap.get(team); }
    public static BufferedImage   getLightTankTurret(int team)        { return lightTankTurretMap.get(team); }
    public static BufferedImage   getLightTankHullDamaged(int team)   { return lightTankHullDamagedMap.get(team); }
    public static BufferedImage   getLightTankTurretDamaged(int team) { return lightTankTurretDamagedMap.get(team); }
    public static BufferedImage[] getLightTankFire(int team)          { return lightTankFireMap.get(team); }
    public static BufferedImage[] getLightTankFireDamaged(int team)   { return lightTankFireDamagedMap.get(team); }

    public static BufferedImage   getHellicopterBody(int team)      { return hellicopterBodyMap.get(team); }
    public static BufferedImage   getHellicopterDestroyed(int team) { return hellicopterDestroyedMap.get(team); }
    public static BufferedImage   getHellicopterBlades(int team)    { return hellicopterBladesMap.get(team); }
    public static BufferedImage[] getHellicopterAttack(int team)    { return hellicopterAttackMap.get(team); }

    public static BufferedImage   getApacheBody(int team)       { return apacheBodyMap.get(team); }
    public static BufferedImage   getApacheEmptyPods(int team)  { return apacheEmptyPodsMap.get(team); }
    public static BufferedImage   getApacheDestroyed(int team)  { return apacheDestroyedMap.get(team); }
    public static BufferedImage   getApacheBlades(int team)     { return apacheBladesMap.get(team); }
    public static BufferedImage[] getApacheAttack(int team)     { return apacheAttackMap.get(team); }

    public static BufferedImage getTransportHeliBody(int team) { return transportHeliBodyMap.get(team); }
    public static BufferedImage getTransportHeliRoof(int team) { return transportHeliRoofMap.get(team); }

    public static BufferedImage getTruckHull(int team)        { return truckHullMap.get(team); }
    public static BufferedImage getTruckHullDamaged(int team) { return truckHullDamagedMap.get(team); }
    public static BufferedImage getTruckRubble(int team)      { return truckRubbleMap.get(team); }

    public static BufferedImage getLandmine(int team) { return landmineMap.get(team); }

    public static BufferedImage[] getRifleIdle(int team) { return rifleIdleMap.get(team); }
    public static BufferedImage[] getRifleFire(int team) { return rifleFireMap.get(team); }
    public static BufferedImage[] getRifleDie(int team)  { return rifleDieMap.get(team); }
    public static BufferedImage   getRifleDead(int team) { return rifleDeadMap.get(team); }

    public static BufferedImage[] getBazookaIdle(int team) { return bazookaIdleMap.get(team); }
    public static BufferedImage[] getBazookaFire(int team) { return bazookaFireMap.get(team); }
    public static BufferedImage[] getBazookaDie(int team)  { return bazookaDieMap.get(team); }
    public static BufferedImage   getBazookaDead(int team) { return bazookaDeadMap.get(team); }

    // ── Initialization ────────────────────────────────────────────────────────

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
                loadHangarAssets(),
                loadMapAssets1(),
                loadMapAssets2(),
                loadButtonAssets()
            );

            CompletableFuture<Void> allDone = future.thenRun(() -> {
                precomputeTeamAssets(RTSGame.activeTeams);
                preloadUnits();
                initialized = true;
                System.out.println("All assets loaded successfully.");
            });
            allDone.exceptionally(ex -> {
                System.out.println("Error loading RTS assets. Please verify Assets folder.");
                ex.printStackTrace();
                System.exit(1);
                return null;
            });

            allDone.join();
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
            grassBG = load("DemoAssets/TankGame/terrainPlaygroundHighground130.png");
        }, executor);
    }


    private static CompletableFuture<Void> loadMapAssets2() {
        return CompletableFuture.runAsync(() -> {
            rtsPathing = load("DemoAssets/TankGame/terrainPlaygroundPathing.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadTankAssets() {
        return CompletableFuture.runAsync(() -> {
            tankChasis = load("DemoAssets/TankGame/newTank/newHull2.png");
            tankTurret = load("DemoAssets/TankGame/newTank/newTurret.png");
            tankFireAnimation = loadSequence("DemoAssets/TankGame/newTank/fireAnimation");
            bullet = load("DemoAssets/TankGame/bullet.png");
            bullet2 = load("DemoAssets/TankGame/bullet2.png");
            tankDeadHullShadow = load("DemoAssets/TankGame/destroyedHullShadow.png");
            tankHullDamaged = load("DemoAssets/TankGame/newTank/newHull2Damaged.png");
            tankTurretDamaged = load("DemoAssets/TankGame/newTank/newTurretDamaged.png");
            tankFireAnimationDamaged = loadSequence("DemoAssets/TankGame/newTank/fireAnimationDamaged");
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
            hellicopterDestroyed = load("DemoAssets/TankGame/copter/hellicopterDestroyed.png");
            hellicopterAttack = loadSequence("DemoAssets/TankGame/copter/newFire");
            hellicopterBlades = load("DemoAssets/TankGame/copter/newChopperFrames/blades.png");
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
            apacheDestroyed = load("DemoAssets/TankGame/apache/apacheDestroyed.png");
            apacheAttack = loadSequence("DemoAssets/TankGame/apache/newFire");
            apacheBlades = load("DemoAssets/TankGame/apache/newChopperFrames/blades.png");
            apacheEmptyPods = load("DemoAssets/TankGame/apache/apacheEmptyPods.png");
            apacheMissileProjectile = load("DemoAssets/TankGame/apache/apacheMissile.png");
            apacheDockedMissile = load("DemoAssets/TankGame/apache/dockedMissile.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadLightTankAssets() {
        return CompletableFuture.runAsync(() -> {
            lightTankHull = load("DemoAssets/TankGame/newLightTank/hull.png");
            lightTankTurret = load("DemoAssets/TankGame/newLightTank/turret.png");
            lightTankFire = loadSequence("DemoAssets/TankGame/newLightTank/fire");
            lightTankFireDamaged = loadSequence("DemoAssets/TankGame/newLightTank/fire_damaged");
            lightTankTurretDamaged = load("DemoAssets/TankGame/newLightTank/turret_damaged.png");
            lightTankHullDamaged = load("DemoAssets/TankGame/newLightTank/hull_damaged.png");
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
            infantryBazookaIdle = loadSequence("DemoAssets/TankGame/Infantry/bazooka/idle");
            infantryRifleFire = loadSequence("DemoAssets/TankGame/Infantry/rifle/shoot");
            infantryRifleDie = loadSequence("DemoAssets/TankGame/Infantry/rifle/die");
            infantryRifleDead = load("DemoAssets/TankGame/Infantry/rifle/dead.png");
            infantryBazookaFire = loadSequence("DemoAssets/TankGame/Infantry/bazooka/shoot");
            infantryShadow = load("DemoAssets/TankGame/Infantry/infantryShadow.png");
            infantryBazookaDie = loadSequence("DemoAssets/TankGame/Infantry/bazooka/die");
            infantryBazookaDead = load("DemoAssets/TankGame/Infantry/bazooka/dead.png");
            smallImpact = loadSequence("DemoAssets/TankGame/smallImpact");
        }, executor);
    }

    private static CompletableFuture<Void> loadLandmineAssets() {
        return CompletableFuture.runAsync(() -> {
            landmine = load("DemoAssets/TankGame/landmine.png");
            landmineSelectionImage = load("DemoAssets/TankGame/landmineSelectionImage.png");
            landmineBlast = load("DemoAssets/TankGame/landmineBlast.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadTruckAssets() {
        return CompletableFuture.runAsync(() -> {
            truckHull = load("DemoAssets/TankGame/truck/hull.png");
            truckHullDamaged = load("DemoAssets/TankGame/truck/hull_damaged.png");
            truckWheel = load("DemoAssets/TankGame/truck/tire.png");
            truckRubble = load("DemoAssets/TankGame/truck/greenRubble2.png");
            truckDeathShadow = load("DemoAssets/TankGame/truck/deathShadow.png");
        }, executor);
    }

    private static CompletableFuture<Void> loadTransportHeliAssets() {
        return CompletableFuture.runAsync(() -> {
            transportHeli = load("DemoAssets/TankGame/transportHeli/body.png");
            transportHeliRoof = load("DemoAssets/TankGame/transportHeli/transportHeliRoof.png");
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

    private static CompletableFuture<Void> loadHangarAssets() {
        return CompletableFuture.runAsync(() -> {
            hangarBase = load("DemoAssets/TankGame/buildings/hangarBase.png");
            hangarFloor = load("DemoAssets/TankGame/buildings/hangarFloor.png");
            metalShack = load("DemoAssets/TankGame/buildings/metalShack.png");
            buildingGreen1 = load("DemoAssets/TankGame/buildings/buildingGreen1.png");
            buildingGreen1Extras = load("DemoAssets/TankGame/buildings/buildingGreen1Extras.png");
            orangeWoodHouse = load("DemoAssets/TankGame/buildings/orangeWoodHouse.png");
            orangeWoodHouseExtras = load("DemoAssets/TankGame/buildings/orangeWoodHouseExtras.png");
            greenShippingContainer = load("DemoAssets/TankGame/buildings/greenShippingContainer.png");
            propaneTank = load("DemoAssets/TankGame/buildings/propaneTank.png");
            bush1 = load("DemoAssets/TankGame/buildings/bush1.png");
            bush2 = load("DemoAssets/TankGame/buildings/bush2.png");
            bush3 = load("DemoAssets/TankGame/buildings/bush3.png");
            tree1 = load("DemoAssets/TankGame/buildings/tree1.png");
            tree1Shadow = load("DemoAssets/TankGame/buildings/tree1Shadow.png");
            tree2 = load("DemoAssets/TankGame/buildings/tree2.png");
            tree2Shadow = load("DemoAssets/TankGame/buildings/tree2Shadow.png");
            tree3 = load("DemoAssets/TankGame/buildings/tree3.png");
            tree3Shadow = load("DemoAssets/TankGame/buildings/tree3Shadow.png");
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

    private static void preloadUnits() {
        System.out.println("preloading units");
        new TankUnit(0, 0);
        new LightTank(0, 0, 0);
        new Rifleman(0, 0, 0);
        new Bazookaman(0, 0, 0);
        new Hellicopter(0, 0, 0);
        new Apache(0, 0, 0);
        new Landmine(0, 0, 0);
        new Truck(0, 0, 0);
        new TransportHelicopter(0, 0, 0);
        RTSUnitIdHelper.reset();
        System.out.println("done preloading.");
    }

    // ── Color transform methods ───────────────────────────────────────────────

    public static BufferedImage[] greenToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = greenToRed(input[i]);
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
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] greenToYellow(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = greenToYellow(input[i]);
        return out;
    }

    public static BufferedImage greenToYellow(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5) {
                    int newRed   = Math.min(255, (int)(prevColor.getGreen() * 1.6));
                    int newGreen = Math.min(255, (int)(prevColor.getGreen() * 1.24));
                    int newBlue  = Math.min(255, (int)(prevColor.getBlue()  * 0.24));
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
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
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] blueToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = blueToRed(input[i]);
        return out;
    }

    public static BufferedImage darkToRed(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300
                        && prevColor.getRed() < 30 + prevColor.getGreen() + prevColor.getBlue()) {
                    int newRed = prevColor.getRed() > 0 ? Math.min(255, (int)(prevColor.getRed() + 50 * 1.5)) : 0;
                    int newGreen = prevColor.getRed();
                    int newBlue = prevColor.getGreen();
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToRed(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = darkToRed(input[i]);
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
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() > 30) {
                    int prevYellow = (prevColor.getRed() + prevColor.getGreen()) / 2;
                    int newRed   = Math.min(255, (int)(50 + prevYellow * 1.5));
                    int newGreen = Math.min(255, (int)(20 + prevYellow * 1.5));
                    int newBlue  = prevColor.getGreen();
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToYellow(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = darkToYellow(input[i]);
        return out;
    }

    public static BufferedImage greenToCharcoal(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() < 500) {
                    int dark = Math.min(140, (int)(prevColor.getGreen() * 0.72));
                    int newRed   = (int)(dark * 0.85);
                    int newGreen = dark;
                    int newBlue  = Math.min(255, (int)(dark * 1.15));
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] greenToCharcoal(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = greenToCharcoal(input[i]);
        return out;
    }

    public static BufferedImage darkToCharcoal(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() < 500) {
                    int dark = Math.min(120, (int)(prevColor.getGreen() * 0.65));
                    bi.setRGB(x, y, new Color((int)(dark * 0.85), dark, Math.min(255, (int)(dark * 1.15)), prevColor.getAlpha()).getRGB());
                } else if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300
                        && prevColor.getRed() < 30 + prevColor.getGreen() + prevColor.getBlue()
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() > 30) {
                    int newRed   = Math.max(0, prevColor.getRed() - 5);
                    int newGreen = prevColor.getGreen();
                    int newBlue  = Math.min(255, prevColor.getBlue() + 12);
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToCharcoal(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = darkToCharcoal(input[i]);
        return out;
    }

    public static BufferedImage greenToTan(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5) {
                    int newRed   = Math.min(255, (int)(prevColor.getGreen() * 1.25));
                    int newGreen = Math.min(255, (int)(prevColor.getGreen() * 1.10));
                    int newBlue  = Math.min(255, (int)(prevColor.getGreen() * 0.72));
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    int newRed   = Math.min(255, (int)( prevColor.getRed() * 1.05 ));
                    int newGreen = Math.min(255, (int)(prevColor.getGreen() * 1.05 ));
                    int newBlue  = Math.min(255, (int)(10 + prevColor.getBlue() * 1.25 ));
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } 
            }
        }
        return bi;
    }

    public static BufferedImage[] greenToTan(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = greenToTan(input[i]);
        return out;
    }

    public static BufferedImage darkToTan(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5) {
                    int gray = Math.min(255, (int)(prevColor.getGreen() * 0.75));
                    bi.setRGB(x, y, new Color(gray, gray, gray, prevColor.getAlpha()).getRGB());
                } else if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300
                        && prevColor.getRed() < 30 + prevColor.getGreen() + prevColor.getBlue()
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() > 30) {
                    int brightness = prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue();
                    int newRed   = Math.min(255, brightness / 3 + 45);
                    int newGreen = Math.min(255, brightness / 3 + 30);
                    int newBlue  = Math.min(255, brightness / 3 + 8);
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToTan(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = darkToTan(input[i]);
        return out;
    }

    public static BufferedImage greenToArctic(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() < 500) {
                    int bright = Math.min(255, (int)(prevColor.getGreen() * 2.35));
                    int newRed   = bright;
                    int newGreen = bright;
                    int newBlue  = Math.min(255, (int)(bright * 1.1));
                    bi.setRGB(x, y, new Color(newRed, newGreen, newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] greenToArctic(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = greenToArctic(input[i]);
        return out;
    }

    // todo refine this function
    public static BufferedImage darkToArctic(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                int brightness = prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue();
                int maxC = Math.max(prevColor.getRed(), Math.max(prevColor.getGreen(), prevColor.getBlue()));
                int minC = Math.min(prevColor.getRed(), Math.min(prevColor.getGreen(), prevColor.getBlue()));
                int saturation = maxC - minC;
                int avg = brightness / 3;
                boolean greenTinted = prevColor.getGreen() - 8 > prevColor.getRed() && prevColor.getGreen() > prevColor.getBlue();
                if (saturation < 20 && brightness > 100 && (prevColor.getBlue() - 2 > prevColor.getGreen() * .8 && prevColor.getBlue() + 2 < prevColor.getGreen() * 1.2)) {
                    int base = brightness / 3;
                    int bright = Math.min(220, base * 3 + 90);
                    bi.setRGB(x, y, new Color(bright, bright, Math.min(255, (int)(bright * 1.04)), prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToArctic(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = darkToArctic(input[i]);
        return out;
    }

    public static BufferedImage greenToSilver(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() < 500) {
                    int base = Math.min(210, (int)(prevColor.getGreen() * 1.35));
                    bi.setRGB(x, y, new Color(base, base, Math.min(255, (int)(base * 1.06)), prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] greenToSilver(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = greenToSilver(input[i]);
        return out;
    }

    public static BufferedImage darkToSilver(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getGreen() - 10 > (prevColor.getRed() + prevColor.getBlue()) * .5
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() < 500) {
                    int base = Math.min(190, (int)(prevColor.getGreen() * 1.2));
                    bi.setRGB(x, y, new Color(base, base, Math.min(255, (int)(base * 1.06)), prevColor.getAlpha()).getRGB());
                } else if (prevColor.getBlue() + prevColor.getRed() + prevColor.getGreen() < 300
                        && prevColor.getRed() < 30 + prevColor.getGreen() + prevColor.getBlue()
                        && prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue() > 30) {
                    int avg = (prevColor.getRed() + prevColor.getGreen() + prevColor.getBlue()) / 3;
                    int newVal = Math.min(200, avg + 55);
                    bi.setRGB(x, y, new Color(newVal, newVal, Math.min(255, (int)(newVal * 1.06)), prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }

    public static BufferedImage[] darkToSilver(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = darkToSilver(input[i]);
        return out;
    }

    public static BufferedImage[] removeBlue(BufferedImage[] input) {
        BufferedImage[] out = new BufferedImage[input.length];
        for (int i = 0; i < out.length; i++) out[i] = removeBlue(input[i]);
        return out;
    }

    public static BufferedImage removeBlue(BufferedImage input) {
        BufferedImage bi = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgba = input.getRGB(x, y);
                Color prevColor = new Color(rgba, true);
                if (prevColor.getBlue() > prevColor.getRed() && prevColor.getBlue() > prevColor.getGreen()) {
                    int newBlue = Math.min(prevColor.getRed(), prevColor.getGreen());
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), newBlue, prevColor.getAlpha()).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
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
                if (prevColor.getGreen() > prevColor.getRed() && prevColor.getGreen() > prevColor.getBlue() && prevColor.getGreen() > 50) {
                    bi.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
                } else {
                    bi.setRGB(x, y, new Color(prevColor.getRed(), prevColor.getGreen(), prevColor.getBlue(), prevColor.getAlpha()).getRGB());
                }
            }
        }
        return bi;
    }
}

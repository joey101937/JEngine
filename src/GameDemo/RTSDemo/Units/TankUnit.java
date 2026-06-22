/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GameObject2.MovementType;
import Framework.Push;
import java.util.HashMap;
import java.util.Map;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.DigInButton;
import GameDemo.RTSDemo.Buttons.DigOutButton;
import GameDemo.RTSDemo.Buttons.FrontalArmorButton;
import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.FogOfWar.DirectionalVisionProvider;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a tank gameobject. Tank class is the chasis
 * 
 * Has directional armor, hull mounted secondary weapon with restricted firing params, and ability to deploy sandbags
 *
 * @author Joseph
 */
public class TankUnit extends RTSUnit implements DirectionalVisionProvider {
    
    public static final double attackFrequency = 2.5;
    public static double speed = RTSGame.tickAdjust(2.15);

    public Turret turret;
    public final static double VISUAL_SCALE = .50;
    public long weaponCooldownExpiresAtTick = 0;
    public long lastTickTakenDamage = 0;
    public boolean sandbagActive = false;
    public int sandbagUsesRemaining = 2;
    public Sandbag sandbag = new Sandbag(this);
    public DigInButton digInButton;
    public DigOutButton digOutButton;

    private final Map<String, Push> lastInfantryPushPerTarget = new HashMap<>();
    private double hullRotationSpeed = 0.0;

    // Hull machine gun
    public static final Damage staticHullMGDamage = new Damage(4);
    public static final double HULL_MG_ATTACK_FREQUENCY = 1.5;
    public Damage hullMGDamage;
    public long hullMGCooldownExpiresAtTick = 0;
    public int hullMGMuzzleOffsetX = 15;
    public int hullMGMuzzleForwardOffset = -2;
    private long[] hullMGFlashTicks = {-9999, -9999, -9999};

    // State tracking for in-progress actions (survives serialization)
    private boolean isDiggingIn = false;
    private boolean isDiggingOut = false;
    private long digActionStartTick = 0;
    private long fadeoutScheduledAtTick = 0;
    private long destructionScheduledAtTick = 0;

    public static volatile Sequence hullMGImpact = null;

    // Team-neutral sprites
    public static volatile Sprite rubbleHullSprite = null;
    public static volatile Sprite rubbleTurretSprite = null;
    public static volatile Sprite deathShadow = null;
    public static volatile Sprite shadow = null;
    public static volatile Sprite turretShadow = null;
    public static volatile Sprite sandbagSprite = null;
    public static volatile Sprite sandbagDamagedSprite = null;
    public static volatile Sprite sandbagShadow = null;
    public static volatile Sequence deathFadeout;
    public static volatile Sequence tankDeathAnimation = null;

    // Team-colored sprite/sequence maps
    private static final Map<Integer, Sprite>    chasisSpriteMap       = new HashMap<>();
    private static final Map<Integer, Sprite>    turretSpriteMap       = new HashMap<>();
    private static final Map<Integer, Sprite>    hullDamagedSpriteMap  = new HashMap<>();
    private static final Map<Integer, Sprite>    turretDamagedSpriteMap = new HashMap<>();
    private static final Map<Integer, Sequence>  fireAnimMap           = new HashMap<>();
    private static final Map<Integer, Sequence>  fireAnimDamagedMap    = new HashMap<>();

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (!chasisSpriteMap.isEmpty()) return;

        tankDeathAnimation = new Sequence(RTSAssetManager.tankDeath, "deathAnimation");
        tankDeathAnimation.setFrameDelay(35);
        rubbleHullSprite = new Sprite(RTSAssetManager.tankDeadHull);
        rubbleTurretSprite = new Sprite(RTSAssetManager.tankDeadTurret);
        deathShadow = new Sprite(RTSAssetManager.tankDeadHullShadow);
        deathFadeout = Sequence.createFadeout(RTSAssetManager.tankDeadHullShadow, 40);
        deathFadeout.setSignature("fadeout");
        shadow = Sprite.generateShadowSprite(RTSAssetManager.tankChasis, .8);
        turretShadow = Sprite.generateShadowSprite(RTSAssetManager.tankTurret, .8);
        sandbagSprite = new Sprite(RTSAssetManager.sandbagsForTank, "sandbagsForTank");
        sandbagDamagedSprite = new Sprite(RTSAssetManager.sandbagsForTankDamaged, "sandbagsForTankDamaged");
        sandbagShadow = Sprite.generateShadowSprite(RTSAssetManager.sandbagsForTank, .7);
        shadow.applyAlphaEdgeBlurSelf(8);
        turretShadow.applyAlphaEdgeBlurSelf(3);
        sandbagSprite.applyAlphaEdgeBlurSelf(1);
        sandbagDamagedSprite.applyAlphaEdgeBlurSelf(1);
        List.of(deathShadow, shadow).forEach(x -> x.scaleTo(VISUAL_SCALE));
        hullMGImpact = new Sequence(RTSAssetManager.smallImpact);
        hullMGImpact.scaleTo(3);
        hullMGImpact.setFrameDelay(30);

        for (int team : RTSGame.activeTeams) {
            Sprite chassis = new Sprite(RTSAssetManager.getTankChasis(team));
            chassis.applyAlphaEdgeBlurSelf(1);
            chasisSpriteMap.put(team, chassis);

            Sprite turret = new Sprite(RTSAssetManager.getTankTurret(team));
            turret.applyAlphaEdgeBlurSelf(1);
            turretSpriteMap.put(team, turret);

            Sprite hullDamaged = new Sprite(RTSAssetManager.getTankHullDamaged(team));
            hullDamaged.setSignature("damagedHull");
            hullDamagedSpriteMap.put(team, hullDamaged);

            Sprite turretDamaged = new Sprite(RTSAssetManager.getTankTurretDamaged(team));
            turretDamaged.setSignature("damagedTurret");
            turretDamagedSpriteMap.put(team, turretDamaged);

            Sequence fire = new Sequence(RTSAssetManager.getTankFireAnim(team), "tankFire");
            fire.setSignature("fireAnimation");
            fire.setFrameDelay(35);
            fire.applyAlphaEdgeBlurSelf(1);
            fireAnimMap.put(team, fire);

            Sequence fireDamaged = new Sequence(RTSAssetManager.getTankFireAnimDamaged(team));
            fireDamaged.setSignature("fireAnimation");
            fireDamaged.setFrameDelay(35);
            fireAnimDamagedMap.put(team, fireDamaged);
        }

        // Bake the tank's permanent tint into its sprites once.
        // Hull graphics get brightness .9 + saturation .9; turret graphics get saturation .9.
        for (Sprite s : chasisSpriteMap.values())        { s.setBrightness(0.9); s.setSaturation(0.9); }
        for (Sprite s : hullDamagedSpriteMap.values())   { s.setBrightness(0.9); s.setSaturation(0.9); }
        for (Sprite s : turretSpriteMap.values())        { s.setSaturation(0.9); }
        for (Sprite s : turretDamagedSpriteMap.values()) { s.setSaturation(0.9); }
        rubbleHullSprite.setBrightness(0.9);   rubbleHullSprite.setSaturation(0.9);
        rubbleTurretSprite.setSaturation(0.9);
        tankDeathAnimation.setBrightness(0.9); tankDeathAnimation.setSaturation(0.9);
        deathFadeout.setBrightness(0.9);       deathFadeout.setSaturation(0.9);

        List.of(
                deathShadow,
                shadow
        ).forEach(x -> x.scaleTo(VISUAL_SCALE));

        hullMGImpact = new Sequence(RTSAssetManager.smallImpact);
        hullMGImpact.scaleTo(3);
        hullMGImpact.setFrameDelay(30);
    }

    public Sprite getHullSprite() {
        boolean isDamaged = currentHealth > 0 && currentHealth < maxHealth / 3;
        return isDamaged ? hullDamagedSpriteMap.get(team) : chasisSpriteMap.get(team);
    }

    @Override
    public void onAnimationCycle() {
        if ("fadeout".equals(getGraphic().getSignature())) {
            this.isInvisible = true;
        }
        
        if("deathAnimation".equals(getGraphic().getSignature())) {
            this.setGraphic(rubbleHullSprite);
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;
        if (isSolid) {
            drawShadow(g, shadow, 5, 9);
        }
        super.render(g);
        if (!isRubble) {
            renderHullMGFlash(g);
        }
    }

    @Override
    protected void applyHullRotation(double desiredRotation) {
        double maxSpeed = getEffectiveRotationSpeed(desiredRotation);
        final double accel = RTSGame.tickAdjust(0.15);
        double targetSpeed = Math.abs(desiredRotation) < 0.01 ? 0.0 : Math.copySign(maxSpeed, desiredRotation);

        if (hullRotationSpeed < targetSpeed) {
            hullRotationSpeed = Math.min(hullRotationSpeed + accel, targetSpeed);
        } else if (hullRotationSpeed > targetSpeed) {
            hullRotationSpeed = Math.max(hullRotationSpeed - accel, targetSpeed);
        }

        if (Math.abs(desiredRotation) <= Math.abs(hullRotationSpeed)) {
            rotate(desiredRotation);
            hullRotationSpeed = 0;
        } else {
            rotate(hullRotationSpeed);
        }
    }

    @Override
    protected double getEffectiveRotationSpeed(double desiredRotation) {
        double angle = Math.abs(desiredRotation);
        return rotationSpeed * Math.max(0.1, Math.min(1.0, angle / 30.0));
    }

    @Override
    public double getSpeed() {
        Coordinate nextWaypoint = getNextWaypoint();
        if (nextWaypoint == null || isCloseEnoughToDesired()) {
            return super.getSpeed();
        }
        double angle = Math.abs(rotationNeededToFace(nextWaypoint));
        double angleFactor = Math.max(0.0, Math.min(1.0, (90.0 - angle) / 60.0));
        return super.getSpeed() * angleFactor;
    }

    public void tick() {
        super.tick();

        // Check for scheduled destruction
        if (destructionScheduledAtTick > 0 && getHostGame().getGameTickNumber() >= destructionScheduledAtTick) {
            this.destroy();
            return;
        }

        // Check for scheduled fadeout
        if (fadeoutScheduledAtTick > 0 && getHostGame().getGameTickNumber() >= fadeoutScheduledAtTick) {
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "transientExplosion"), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.setZLayer(-100);
            this.turret.isInvisible = true;
            destructionScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 3);
            fadeoutScheduledAtTick = 0;
            return;
        }

        // Check weapon cooldown expiration
        if (weaponCooldownExpiresAtTick > 0 && getHostGame().getGameTickNumber() >= weaponCooldownExpiresAtTick) {
            weaponCooldownExpiresAtTick = 0;
        }
        if (hullMGCooldownExpiresAtTick > 0 && getHostGame().getGameTickNumber() >= hullMGCooldownExpiresAtTick) {
            hullMGCooldownExpiresAtTick = 0;
        }

        if (currentHealth > 0 && !isRubble) {
            this.setGraphic(getHullSprite());
        }

        // Check for in-progress dig actions
        if (isDiggingIn && getHostGame().getGameTickNumber() >= digActionStartTick + (RTSGame.desiredTPS * 5)) {
            deploySandbagDirect();
            isDiggingIn = false;
        }
        if (isDiggingOut && getHostGame().getGameTickNumber() >= digActionStartTick + (RTSGame.desiredTPS * 5)) {
            pickUpSandbag();
            isDiggingOut = false;
        }

        if (!isRubble && !isImmobilized && velocity.y == 0 && rotationSpeed > 0) {
            tickHullMachineGun();
        }
    }

    @Override
    public int getWidth() {
        // consistent width so that width is not tied to animation frame
        return (int)(chasisSpriteMap.get(0).getWidth() * VISUAL_SCALE);
    }

    @Override
    public int getHeight() {
        // consistent height so that width is not tied to animation frame
        return (int)(chasisSpriteMap.get(0).getHeight() * VISUAL_SCALE);
    }

    @Override
    public int getWidthForPathing() {
        return sandbagActive ? getWidth() : getSideLength() /2;
    }

    /*
    sets up the tank values
     */
    public TankUnit(Coordinate c) {
        super(c, 0);
        init();
    }

    public TankUnit(int x, int y) {
        super(x, y, 0);
        init();
    }

    public TankUnit(int x, int y, int team) {
        super(x, y, team);
        init();
    }

    private void init() {
        isSolid = true;
        preventOverlap = true;
        setScale(VISUAL_SCALE);
        turretShadow.scaleTo(VISUAL_SCALE);
        shadow.scaleTo(VISUAL_SCALE);
        Sprite chassSprite = getHullSprite();
        this.setGraphic(chassSprite);
        this.movementType = MovementType.RotationBased;
        turret = new Turret(new Coordinate(0, 0));
        this.addSubObject(sandbag);
        this.addSubObject(turret);
        this.maxHealth = 210;//tanks can take 4 shots
        this.currentHealth = maxHealth;
        this.baseSpeed = speed;
        initializeButtons();
        this.rotationSpeed = RTSGame.tickAdjust(1.4);
        this.cargoSize = 8;
        this.sightRadius = 300;
        hullMGDamage = staticHullMGDamage.copy(this);
        // tint (brightness/saturation .9) is baked into the sprites in initGraphics()
    }

    private void initializeButtons() {
        digInButton = new DigInButton(this);
        digOutButton = new DigOutButton(this);
        addButton(digInButton);
        addButton(digOutButton);
        addButton(new FrontalArmorButton(this));
    }


    @Override
    public void onPostDeserialization() {
        // Restore graphics after deserialization
        this.setGraphic(isRubble ? rubbleHullSprite : getHullSprite());
        if (turret != null) {
            turret.setGraphic(isRubble ? rubbleTurretSprite : turret.getTurretSprite());
        }
        if (sandbag != null) {
            sandbag.setGraphic(sandbagSprite);
        }
        // Restore button transient fields after deserialization
        for (CommandButton button : getButtons()) {
            button.restoreTransientFields();
        }
    }

    private void tickHullMachineGun() {
        RTSUnit infantryTarget = nearestVisibleEnemyInfantryInRange();
        if (infantryTarget == null) return;
        double angleToTarget = rotationNeededToFace(infantryTarget.getPixelLocation());
        applyHullRotation(angleToTarget);
        if (Math.abs(angleToTarget) <= 14) {
            fireHullMG(infantryTarget);
        }
    }

    private RTSUnit nearestVisibleEnemyInfantryInRange() {
        double closest = range + 1;
        RTSUnit found = null;
        for (var go : getHostGame().getObjectsNearPoint(getPixelLocation(), range)) {
            if (!(go instanceof RTSUnit unit)) continue;
            if (unit.team == team || unit.isRubble || !unit.isInfantry || !unit.isVisible(team)) continue;
            double dist = distanceFrom(unit);
            if (dist < closest) {
                closest = dist;
                found = unit;
            }
        }
        return found;
    }

    private void fireHullMG(RTSUnit target) {
        if (hullMGCooldownExpiresAtTick > 0) return;
        hullMGCooldownExpiresAtTick = getHostGame().getGameTickNumber() + (long)(RTSGame.desiredTPS * HULL_MG_ATTACK_FREQUENCY);
        long fireTick = getHostGame().getGameTickNumber();
        hullMGFlashTicks[0] = fireTick;
        hullMGFlashTicks[1] = fireTick + RTSGame.tickAdjust(14);
        hullMGFlashTicks[2] = fireTick + RTSGame.tickAdjust(24);
        if (isOnScreen()) {
            RTSSoundManager.get().play(RTSSoundManager.RIFLEMAN_ATTACK, Main.generateRandomDoubleLocally(.55f, .63f), Main.generateRandomIntLocally(0, 20));
        } else {
            RTSSoundManager.get().play(RTSSoundManager.RIFLEMAN_ATTACK, Main.generateRandomDoubleLocally(.4f, .48f), Main.generateRandomIntLocally(0, 20));
        }
        boolean hit0 = performHullMGAttack(target, 0);
        boolean hit1 = performHullMGAttack(target, 1);
        boolean hit2 = performHullMGAttack(target, 2);
        createHullMGImpactVisual(target, 0, hit0);
        createHullMGImpactVisual(target, RTSGame.tickAdjust(14), hit1);
        createHullMGImpactVisual(target, RTSGame.tickAdjust(24), hit2);
    }

    private boolean performHullMGAttack(RTSUnit target, int callNum) {
        if (Main.generateDeterministicRandomInt(0, 100, callNum) > target.getDodgeChance()) {
            hullMGDamage.launchLocation = getPixelLocation();
            hullMGDamage.impactLoaction = getPixelLocation();
            target.takeDamage(hullMGDamage);
            return true;
        }
        return false;
    }

    private void createHullMGImpactVisual(RTSUnit target, int tickDelay, boolean hit) {
        addTickDelayedEffect(tickDelay, c -> {
            Coordinate impactLocation = target.getPixelLocation();
            int scatter = hit ? target.getWidth() / 3 : target.getWidth() / 2 + 22;
            impactLocation.x += Main.generateRandomInt(-scatter, scatter);
            impactLocation.y += Main.generateRandomInt(-scatter, scatter);
            Sequence visual = hit ? Rifleman.smallImpact.copyMaintainSource() : hullMGImpact.copyMaintainSource();
            OnceThroughSticker s = new OnceThroughSticker(getHostGame(), visual, impactLocation);
            s.rotation = Main.generateRandomInt(0, 360);
        });
    }

    private void renderHullMGFlash(Graphics2D g) {
        long currentTick = getHostGame().getGameTickNumber();
        boolean anyVisible = false;
        for (long flashTick : hullMGFlashTicks) {
            if (currentTick - flashTick <= 4) { anyVisible = true; break; }
        }
        if (!anyVisible) return;
        DCoordinate muzzleOffset = new DCoordinate(hullMGMuzzleOffsetX, -(getHeight() / 2.0 + hullMGMuzzleForwardOffset));
        muzzleOffset.adjustForRotation(getRotationRealTime());
        Coordinate renderLoc = getRenderLocation();
        int mx = (int)(renderLoc.x + muzzleOffset.x);
        int my = (int)(renderLoc.y + muzzleOffset.y);
        Composite oldComposite = g.getComposite();
        Color oldColor = g.getColor();
        for (long flashTick : hullMGFlashTicks) {
            long ticksSince = currentTick - flashTick;
            if (ticksSince < 0 || ticksSince > 4) continue;
            float fadeAlpha = 1.0f - (ticksSince / 5.0f);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.5f));
            g.setColor(new Color(255, 220, 50));
            g.fillOval(mx - 4, my - 4, 8, 8);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.7f));
            g.setColor(new Color(255, 140, 0));
            g.fillOval(mx - 2, my - 2, 5, 5);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            g.setColor(Color.WHITE);
            g.fillOval(mx - 1, my - 1, 3, 3);
        }
        g.setColor(oldColor);
        g.setComposite(oldComposite);
    }

    //when a tank tries to fire, it first checks if its turret is still firing.
    //if not, tell the turret to fire at target location
    public void fire(Coordinate target) {
        if (weaponCooldownExpiresAtTick > 0 || target.distanceFrom(getLocation()) < getHeight() * 3 / 5 || Math.abs(turret.rotationNeededToFace(target)) > 1) { //limited to one shot per 60 ticks
            return;
        }
        weaponCooldownExpiresAtTick = getHostGame().getGameTickNumber() + (int) (RTSGame.desiredTPS * attackFrequency);
        System.out.println("" + this.ID + " located at " + this.getLocationAsOfLastTick()+"/" + this.getLocation()+ "/" + this.getPixelLocation() + " firing on tick " + getHostGame().getGameTickNumber() + " at " + currentTarget.ID + " located at " + currentTarget.getLocationAsOfLastTick()+"/" + currentTarget.getLocation()+ "/" + currentTarget.getPixelLocation());
        turret.onFire(target);
    }

    public class Turret extends SubObject {

        private double turretRotationSpeed = 0.0;
        private double previousHullRotation = Double.NaN;

        public Sequence getFireSequence() {
            boolean isDamaged = currentHealth > 0 && currentHealth < maxHealth / 3;
            return isDamaged ? fireAnimDamagedMap.get(team).copyMaintainSource() : fireAnimMap.get(team).copyMaintainSource();
        }

        public Sprite getTurretSprite() {
            boolean isDamaged = currentHealth > 0 && currentHealth < maxHealth / 3;
            return isDamaged ? turretDamagedSpriteMap.get(team) : turretSpriteMap.get(team);
        }

        public Turret(Coordinate offset) {
            super(offset);
            setScale(VISUAL_SCALE);
            this.setGraphic(getTurretSprite());
        }

        /*
        fires the gun at the location.
        first, play the firing animation on the gun, then create a small explosion
        effect for the muzzleflash, then create the bullet object and spawn it
        into the game world
         */
        public void onFire(Coordinate target) {
            setGraphic(getFireSequence().copyMaintainSource());
            try {
                String soundEffect = RTSSoundManager.TANK_ATTACK;
                if (isOnScreen()) {
                    RTSSoundManager.get().play(
                        soundEffect,
                        Main.generateRandomDoubleLocally(.69, .74),
                        Main.generateRandomIntLocally(0, 40));
                } else {
                    RTSSoundManager.get().play(
                        soundEffect,
                        Main.generateRandomDoubleLocally(.62, .67),
                        Main.generateRandomIntLocally(0, 40));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            DCoordinate muzzelLocation = new DCoordinate(0, 0);
            muzzelLocation.y -= getFireSequence().frames[0].getHeight() * 2 / 5;
            muzzelLocation = DCoordinate.adjustForRotation(muzzelLocation, getRotationRealTime());
            muzzelLocation.add(getPixelLocation());
            RTSUnit targetUnit = ((RTSUnit) this.getHost()).currentTarget;
            int longestSide = Math.max(targetUnit.getWidth(), targetUnit.getHeight());
            Coordinate offset = new Coordinate(
                    Main.generateRandomIntFromSeed(-longestSide / 3, longestSide / 3, getHostGame().getGameTickNumber() + (int) getLocationAsOfLastTick().x),
                    Main.generateRandomIntFromSeed(-longestSide / 3, longestSide / 3, getHostGame().getGameTickNumber() + (int) getLocationAsOfLastTick().y)
            );
            target.add(offset);
            TankBullet bullet = new TankBullet(muzzelLocation, target.toDCoordinate(), ((RTSUnit) this.getHost()).getPreferredTargetId());
            bullet.shooter = this.getHost();
            System.out.println("tankbullet created at " + muzzelLocation + " target " + target.toDCoordinate() + " from " + getHost().ID);
            getHostGame().addObject(bullet);
        }

        /*
        this runs whenever an animation cycle ends.
        here we use it to tell the gank when its ready to fire again and
        also to reset the object back to using the regular turret sprite
         */
        @Override
        public void onAnimationCycle() {
            if (getGraphic().getSignature().equals("fireAnimation")) {
                setGraphic(getTurretSprite());
            }
            if (isRubble) {
                setGraphic(rubbleTurretSprite);
            }
        }

        //tank turret tick
        @Override
        public void tick() {
            super.tick();
            if (isRubble) {
                return;
            }
            if (currentHealth > 0 && !getGraphic().isAnimated()) {
                this.setGraphic(getTurretSprite());
            }

            // Mirror hull rotation immediately — turret is physically mounted on the hull
            double currentHullRotation = getHost().getRotationRealTime();
            if (!Double.isNaN(previousHullRotation)) {
                double hullDelta = currentHullRotation - previousHullRotation;
                if (hullDelta > 180) hullDelta -= 360;
                else if (hullDelta < -180) hullDelta += 360;
                rotate(hullDelta);
            }
            previousHullRotation = currentHullRotation;

            RTSUnit preferred = ((RTSUnit) getHost()).getPreferredTargetIfInRange();
            RTSUnit enemy = preferred != null ? preferred : nearestEnemyInRange();
            ((RTSUnit) getHost()).currentTarget = enemy;

            double desiredRotation;
            double maxSpeed = RTSGame.tickAdjust(2);
            if (enemy == null) {
                desiredRotation = getHost().getRotationRealTime() - getRotationRealTime();
                if (desiredRotation > 180) desiredRotation -= 360;
                else if (desiredRotation < -180) desiredRotation += 360;
            } else {
                desiredRotation = rotationNeededToFace(enemy.getPixelLocation());
            }

            final double accel = RTSGame.tickAdjust(0.15);
            double targetSpeed = Math.abs(desiredRotation) < 0.01 ? 0.0 : Math.copySign(maxSpeed, desiredRotation);

            if (turretRotationSpeed < targetSpeed) {
                turretRotationSpeed = Math.min(turretRotationSpeed + accel, targetSpeed);
            } else if (turretRotationSpeed > targetSpeed) {
                turretRotationSpeed = Math.max(turretRotationSpeed - accel, targetSpeed);
            }

            if (Math.abs(desiredRotation) <= Math.abs(turretRotationSpeed)) {
                rotate(desiredRotation);
                turretRotationSpeed = 0;
            } else {
                rotate(turretRotationSpeed);
            }

            if (enemy != null) {
                ((TankUnit) getHost()).fire(enemy.getPixelLocation());
            }
        }

        // turret render
        @Override
        public void render(Graphics2D g) {
            if (!((RTSUnit) getHost()).shouldRender()) return;
            if (getHost().isSolid) {
                AffineTransform old = g.getTransform();
                VolatileImage toRender = turretShadow.getCurrentVolatileImage();
                int renderX = getRenderLocation().x - toRender.getWidth() / 2;
                int renderY = getRenderLocation().y - toRender.getHeight() / 2;
                int shadowOffsetY = 2;
                int shadowOffsetX = 1;
                g.rotate(Math.toRadians(getRotationRealTime()), getRenderLocation().x + shadowOffsetX, getRenderLocation().y + shadowOffsetY);
                g.drawImage(toRender, renderX, renderY + shadowOffsetY, null);
                g.setTransform(old);
            }
            super.render(g);
        }

    }
    
    public class Sandbag extends SubObject {
        public TankUnit hull;
        public static final double SANDBAG_SCALE = .56;
        
        public Sandbag(TankUnit t) {
            super(new Coordinate(0,0));
            hull = t;
            this.setGraphic(sandbagSprite);
            this.setRenderBelow(false);
            this.setScale(SANDBAG_SCALE);
            sandbagShadow.scaleTo(SANDBAG_SCALE);
        }
        
        @Override
        public void tick() {
            long ticksSinceLastDamaged = (getHostGame().getGameTickNumber() - lastTickTakenDamage);
            if(ticksSinceLastDamaged > RTSGame.desiredTPS * 20 && sandbagUsesRemaining < 1) {
                sandbagUsesRemaining = 2;
            }
            this.isSolid = hull.sandbagActive && hull.sandbagUsesRemaining > 0;
            this.isInvisible = !hull.sandbagActive;
            this.setRotation(getHost().getRotation());
            if(this.hull.sandbagUsesRemaining < 1 && !this.getGraphic().getSignature().equals("sandbagsForTankDamaged")) {
                this.setGraphic(sandbagDamagedSprite);
            }
            if(this.hull.sandbagUsesRemaining > 0 && !this.getGraphic().getSignature().equals("sandbagsForTank")) {
                System.out.println("setting back" + this.hull.sandbagUsesRemaining);
                this.setGraphic(sandbagSprite);
            }
        }
        
        @Override
        public void render(Graphics2D g) {
            if (!hull.shouldRender()) return;
            if(!isInvisible) {
                drawShadow(g, sandbagShadow, 2, 3);
            }
            super.render(g);
        }
        
    }

    @Override
    public void onCollide(GameObject2 other, boolean myTick) {
        super.onCollide(other, myTick);
        if (!myTick || isRubble) return;
        tryPushInfantry(other);
    }

    private void tryPushInfantry(GameObject2 other) {
        if (!(other instanceof RTSUnit unit && unit.isInfantry)) return;

        Push existing = lastInfantryPushPerTarget.get(other.ID);
        if (existing != null && !existing.isExpired()) return;

        DCoordinate myLoc = getLocationAsOfLastTick();
        DCoordinate otherLoc = other.getLocationAsOfLastTick();
        double dx = otherLoc.x - myLoc.x;
        double dy = otherLoc.y - myLoc.y;
        if (dx == 0 && dy == 0) dx = 1;

        Push push = new Push(dx, dy, RTSGame.tickAdjust(4.0), 3.0, 20,
                p -> { p.speed *= 0.82; p.strength *= 0.82; });
        other.addPush(push);
        lastInfantryPushPerTarget.put(other.ID, push);
    }

    @Override
    public void die() {
        if (this.isRubble) {
            return;
        }
        OnceThroughSticker deathExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "transientExplosion"), getPixelLocation());
        this.isRubble = true;
        this.team = -1;
        this.setBaseSpeed(0);
        this.setDesiredLocation(this.getPixelLocation());
        this.setGraphic(tankDeathAnimation);
        turret.setGraphic(rubbleTurretSprite);
        if(isOnScreen()) {
            RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, Main.generateRandomDoubleLocally(.62, .66), 0);
        }
        fadeoutScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 10);
    }
    
    @Override
    public java.awt.image.BufferedImage getSelectionImage() {
        return RTSAssetManager.tankSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + TankBullet.staticDamage + "    Interval: " + attackFrequency+"s    Range: "+ range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground");
        return out;
    }
    
    @Override
    public double getDirectionalVisionHalfAngle() { return 40; }

    @Override
    public double getDirectionalRangeMultiplier() { return 2; }

    @Override
    public double getVisionFacingDegrees() { return getRotation(); }

    @Override
    public void takeDamage(Damage d) {
        lastTickTakenDamage = getHostGame().getGameTickNumber();
        Damage updatedDamage = d.copy();
        // sandbag reduces frontal damage over 20 from front 90 degrees
        if (sandbagActive
                && updatedDamage.getTotal() >= 20
                && updatedDamage.impactLoaction != null
                && Math.abs(rotationNeededToFace(updatedDamage.impactLoaction)) < 90
                && sandbagUsesRemaining > 0) {
            sandbagUsesRemaining--;
            updatedDamage.apAmount *= .25;
            updatedDamage.baseAmount *= .25;
            super.takeDamage(updatedDamage);
            return;
        }
        if (updatedDamage.impactLoaction != null && Math.abs(rotationNeededToFace(updatedDamage.impactLoaction)) < 41) {
            updatedDamage.baseAmount -= 5;
            if (updatedDamage.baseAmount < 0) {
                updatedDamage.baseAmount = 0;
            }
        }
        System.out.println(this + " taking damage on tick " + getHostGame().getGameTickNumber());
        super.takeDamage(updatedDamage);
    }
    
    public void startDeployingSandbags() {
        setImmobilized(true);
        isDiggingIn = true;
        digActionStartTick = getHostGame().getGameTickNumber();
    }

    public void startPickingUpSandbags() {
        isDiggingOut = true;
        digActionStartTick = getHostGame().getGameTickNumber();
    }
    
    @Override
    public void triggerAbility(int index, Coordinate target, String targetUnitId) {
        switch (index) {
            case 0 -> startDeployingSandbags();
            case 1 -> startPickingUpSandbags();
        }
    }

    public void deploySandbagDirect() {
        this.sandbagActive = true;
        setImmobilized(true);
    }
    
    public void pickUpSandbag() {
        this.sandbagActive = false;
        setImmobilized(false);
    }

}

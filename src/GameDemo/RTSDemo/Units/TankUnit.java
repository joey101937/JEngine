/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2.MovementType;
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
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a tank gameobject. Tank class is the chasis
 *
 * @author Joseph
 */
public class TankUnit extends RTSUnit {
    
    public static final double attackFrequency = 2.5;
    public static double speed = RTSGame.tickAdjust(2.15);

    public Turret turret;
    public final static double VISUAL_SCALE = 1.10;
    public long weaponCooldownExpiresAtTick = 0;
    public long lastTickTakenDamage = 0;
    public boolean sandbagActive = false;
    public int sandbagUsesRemaining = 2;
    public Sandbag sandbag = new Sandbag(this);
    public DigInButton digInButton;
    public DigOutButton digOutButton;

    // State tracking for in-progress actions (survives serialization)
    private boolean isDiggingIn = false;
    private boolean isDiggingOut = false;
    private long digActionStartTick = 0;
    private long fadeoutScheduledAtTick = 0;
    private long destructionScheduledAtTick = 0;

    // Modified buffered images for team color
    public static BufferedImage enemyTankChasisImage = RTSAssetManager.tankChasisRed;
    public static BufferedImage enemyTankTurretImage = RTSAssetManager.tankTurretRed;
    public static BufferedImage[] enemyTankFireAnimation = RTSAssetManager.tankFireAnimationRed;

    // sprites for reuse
    public static volatile Sprite chasisSpriteGreen = null;
    public static volatile Sprite chasisSpriteRed = null;
    public static volatile Sprite turretSpriteGreen = null;
    public static volatile Sprite turretSpriteRed = null;
    public static volatile Sprite rubbleHullSprite = null;
    public static volatile Sprite rubbleTurretSprite = null;
    public static volatile Sequence deathAnimationHull = null;
    public static volatile Sequence deathAnimationTurret = null;
    public static volatile Sprite deathShadow = null;
    public static volatile Sprite shadow = null;
    public static volatile Sprite turretShadow = null;
    public static volatile Sprite sandbagSprite = null;
    public static volatile Sprite sandbagDamagedSprite = null;
    public static volatile Sprite sandbagShadow = null;

    public static volatile Sprite tankHullDamagedGreen;
    public static volatile Sprite tankTurretDamagedGreen;
    public static volatile Sprite tankHullDamagedRed;
    public static volatile Sprite tankTurretDamagedRed;
    public static volatile Sequence tankFireAnimationDamagedGreen;
    public static volatile Sequence tankFireAnimationDamagedRed;

    public static volatile Sequence deathFadeout;

    public static volatile Sequence turretFireAnimationGreen = null; // new Sequence(RTSAssetManager.tankFireAnimation);
    public static volatile Sequence turretFireAnimationRed = null; // new Sequence(enemyTankFireAnimation);
    
    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (chasisSpriteGreen != null) {
            return;
        }
        chasisSpriteGreen = new Sprite(RTSAssetManager.tankChasis);
        chasisSpriteRed = new Sprite(enemyTankChasisImage);
        turretSpriteGreen = new Sprite(RTSAssetManager.tankTurret);
        turretSpriteRed = new Sprite(enemyTankTurretImage);
        turretFireAnimationGreen = new Sequence(RTSAssetManager.tankFireAnimation, "tankFireRed");
        turretFireAnimationRed = new Sequence(enemyTankFireAnimation, "redTankFire");
        rubbleHullSprite = new Sprite(RTSAssetManager.tankDeadHull);
        rubbleTurretSprite = new Sprite(RTSAssetManager.tankDeadTurret);
        deathAnimationHull = new Sequence(RTSAssetManager.tankHullDeathAni, "tankDeathAniHull");
        deathAnimationTurret = new Sequence(RTSAssetManager.tankTurretDeathAni);
        deathShadow = new Sprite(RTSAssetManager.tankDeadHullShadow);
        deathFadeout = Sequence.createFadeout(RTSAssetManager.tankDeadHullShadow, 40);
        deathFadeout.setSignature("fadeout");
        shadow = new Sprite(RTSAssetManager.tankShadow);
        turretShadow = new Sprite(RTSAssetManager.tankTurretShadow);

        tankHullDamagedGreen = new Sprite(RTSAssetManager.tankHullDamaged);
        tankTurretDamagedGreen = new Sprite(RTSAssetManager.tankTurretDamaged);
        tankHullDamagedRed = new Sprite(RTSAssetManager.tankHullDamagedRed);
        tankTurretDamagedRed = new Sprite(RTSAssetManager.tankTurretDamagedRed);
        tankFireAnimationDamagedGreen = new Sequence(RTSAssetManager.tankFireAnimationDamaged);
        tankFireAnimationDamagedRed = new Sequence(RTSAssetManager.tankFireAnimationDamagedRed);

        tankHullDamagedGreen.setSignature("damagedHull");
        tankTurretDamagedGreen.setSignature("damagedTurret");
        tankHullDamagedRed.setSignature("damagedHull");
        tankTurretDamagedRed.setSignature("damagedTurret");
        tankFireAnimationDamagedGreen.setSignature("fireAnimation");
        tankFireAnimationDamagedRed.setSignature("fireAnimation");
        turretFireAnimationGreen.setSignature("fireAnimation");
        turretFireAnimationRed.setSignature("fireAnimation");

        deathAnimationHull.setFrameDelay(60);
        deathAnimationTurret.setFrameDelay(60);
        
        sandbagSprite = new Sprite(RTSAssetManager.sandbagsForTank, "sandbagsForTank");
        sandbagDamagedSprite = new Sprite(RTSAssetManager.sandbagsForTankDamaged, "sandbagsForTankDamaged");
        
        sandbagShadow = Sprite.generateShadowSprite(RTSAssetManager.sandbagsForTank, .7);

        List.of(
                deathShadow,
                shadow
        ).forEach(x -> x.scaleTo(VISUAL_SCALE));
    }

    public Sprite getHullSprite() {
        if (currentHealth > 0 && currentHealth < maxHealth / 3) {
            return team == 0 ? tankHullDamagedGreen : tankHullDamagedRed;
        }
        return team == 0 ? chasisSpriteGreen : chasisSpriteRed;
    }

    @Override
    public void onAnimationCycle() {
        if ("tankDeathAniHull".equals(getGraphic().getSignature())) {
            this.setGraphic(rubbleHullSprite);
        }
        if ("fadeout".equals(getGraphic().getSignature())) {
            this.isInvisible = true;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (isSolid) {
            drawShadow(g, shadow, 5, 9);
        }
        if(isSelected()) {
            drawRubbleProximityIndicators(g);
        }
        super.render(g);
    }

    @Override
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
    }

    @Override
    public int getWidth() {
        // consistent width so that width is not tied to animation frame
        return chasisSpriteGreen.getWidth();
    }

    @Override
    public int getHeight() {
        // consistent height so that width is not tied to animation frame
        return chasisSpriteGreen.getHeight();
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

        public Sequence getFireSequence() {
            if (currentHealth > 0 && currentHealth < maxHealth / 3) {
                return team == 0 ? tankFireAnimationDamagedGreen.copyMaintainSource() : tankFireAnimationDamagedRed.copyMaintainSource();
            }
            return team == 0 ? turretFireAnimationGreen.copyMaintainSource() : turretFireAnimationRed.copyMaintainSource();
        }

        public Sprite getTurretSprite() {
            if (currentHealth > 0 && currentHealth < maxHealth / 3) {
                return team == 0 ? tankTurretDamagedGreen : tankTurretDamagedRed;
            }
            return team == 0 ? turretSpriteGreen : turretSpriteRed;
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
            TankBullet bullet = new TankBullet(muzzelLocation, target.toDCoordinate());
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
            // System.out.println(this + " " + this.ID);
            super.tick();
            if (isRubble) {
                return;
            }
            if (currentHealth > 0 && !getGraphic().isAnimated()) {
                this.setGraphic(getTurretSprite());
            }
            RTSUnit enemy = nearestEnemyInRange();
            ((RTSUnit) getHost()).currentTarget = enemy;
            if (enemy == null) {
                double desiredRotation = getHost().getRotationRealTime()- getRotationRealTime();
                if (desiredRotation > 180) {
                    desiredRotation -= 360;
                } else if (desiredRotation < -180) {
                    desiredRotation += 360;
                }
                double maxRotation = RTSGame.tickAdjust(5);
                if (Math.abs(desiredRotation) < maxRotation) {
                    rotate(desiredRotation);
                } else {
                    if (desiredRotation > 0) {
                        rotate(maxRotation);
                    } else {
                        rotate(-maxRotation);
                    }
                }
            } else {
                double desiredRotation = rotationNeededToFace(enemy.getPixelLocation());
                double maxRotation = RTSGame.tickAdjust(5);
                if (Math.abs(desiredRotation) < maxRotation) {
                    rotate(desiredRotation);
                } else {
                    if (desiredRotation > 0) {
                        rotate(maxRotation);
                    } else {
                        rotate(-maxRotation);
                    }
                }
                Coordinate targetPoint = enemy.getPixelLocation();
                ((TankUnit) getHost()).fire(targetPoint);
            }
        }

        // turret render
        @Override
        public void render(Graphics2D g) {
            if (getHost().isSolid) {
                AffineTransform old = g.getTransform();
                VolatileImage toRender = turretShadow.getCurrentVolatileImage();
                int renderX = getPixelLocation().x - toRender.getWidth() / 2;
                int renderY = getPixelLocation().y - toRender.getHeight() / 2;
                int shadowOffsetY = 3;
                int shadowOffsetX = 1;
                g.rotate(Math.toRadians(getRotationRealTime()), getPixelLocation().x + shadowOffsetX, getPixelLocation().y + shadowOffsetY);
                g.drawImage(toRender, renderX, renderY + shadowOffsetY, null);
                g.setTransform(old);
            }
            super.render(g);
        }

    }
    
    public class Sandbag extends SubObject {
        public TankUnit hull;
        
        public Sandbag(TankUnit t) {
            super(new Coordinate(0,0));
            hull = t;
            this.setGraphic(sandbagSprite);
            this.setRenderBelow(false);
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
            if(!isInvisible) {
                drawShadow(g, sandbagShadow, 2, 3);
            }
            super.render(g);
        }
        
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
        this.setGraphic(deathAnimationHull.copyMaintainSource());
        turret.setGraphic(deathAnimationTurret.copyMaintainSource());
        if(isOnScreen()) {
            RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, Main.generateRandomDoubleLocally(.62, .66), 0);
        }
        fadeoutScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 10);
    }
    
    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + TankBullet.staticDamage + "    Interval: " + attackFrequency+"s    Range: "+ range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground");
        return out;
    }
    
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
    
    public void deploySandbagDirect() {
        this.sandbagActive = true;
        setImmobilized(true);
    }
    
    public void pickUpSandbag() {
        this.sandbagActive = false;
        setImmobilized(false);
    }

}

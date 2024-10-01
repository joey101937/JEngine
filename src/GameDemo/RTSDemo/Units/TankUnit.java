/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GameObject2.MovementType;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a gank gameobject. Tank class is the chasis
 *
 * @author Joseph
 */
public class TankUnit extends RTSUnit {
    
    public static final double attackFrequency = 2.5;
    public static double speed = 2.15;

    public static SoundEffect launchSoundSource = new SoundEffect(new File(Main.assets + "Sounds/blast4.62.wav"));
    public static SoundEffect launchSoundSource2 = new SoundEffect(new File(Main.assets + "Sounds/blast4.6.wav"));
    // public static SoundEffect launchSoundSource = new SoundEffect(new File(Main.assets + "Sounds/gunshot.wav"));
    public Turret turret;
    public final static double VISUAL_SCALE = 1.10;
    public boolean weaponOnCooldown = false;

    // Modified buffered images for team color
    public static BufferedImage enemyTankChasisImage = greenToRed(RTSAssetManager.tankChasis);
    public static BufferedImage enemyTankTurretImage = greenToRed(RTSAssetManager.tankTurret);
    public static BufferedImage[] enemyTankFireAnimation = greenToRed(RTSAssetManager.tankFireAnimation);

    // sprites for reuse
    public static volatile Sprite chasisSpriteGreen = null; // new Sprite(RTSAssetManager.tankChasis2);
    public static volatile Sprite chasisSpriteRed = null; // new Sprite(enemyTankChasisImage);
    public static volatile Sprite turretSpriteGreen = null; // new Sprite(RTSAssetManager.tankTurret);
    public static volatile Sprite turretSpriteRed = null; // new Sprite(enemyTankTurretImage);
    public static volatile Sprite rubbleHullSprite = null;
    public static volatile Sprite rubbleTurretSprite = null;
    public static volatile Sequence deathAnimationHull = null;
    public static volatile Sequence deathAnimationTurret = null;
    public static volatile Sprite deathShadow = null;
    public static volatile Sprite shadow = null;
    public static volatile Sprite turretShadow = null;

    public static volatile Sprite tankHullDamagedGreen;
    public static volatile Sprite tankTurretDamagedGreen;
    public static volatile Sprite tankHullDamagedRed;
    public static volatile Sprite tankTurretDamagedRed;
    public static volatile Sequence tankFireAnimationDamagedGreen;
    public static volatile Sequence tankFireAnimationDamagedRed;

    public static volatile Sequence deathFadeout;

    public static volatile Sequence turretFireAnimationGreen = null; // new Sequence(RTSAssetManager.tankFireAnimation);
    public static volatile Sequence turretFireAnimationRed = null; // new Sequence(enemyTankFireAnimation);

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
        tankHullDamagedRed = new Sprite(greenToRed(RTSAssetManager.tankHullDamaged));
        tankTurretDamagedRed = new Sprite(greenToRed(RTSAssetManager.tankTurretDamaged));
        tankFireAnimationDamagedGreen = new Sequence(RTSAssetManager.tankFireAnimationDamaged);
        tankFireAnimationDamagedRed = new Sequence(greenToRed(RTSAssetManager.tankFireAnimationDamaged));

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
        
        List.of(chasisSpriteGreen,
                chasisSpriteRed,
                turretSpriteGreen,
                turretSpriteRed,
                turretFireAnimationGreen,
                turretFireAnimationRed,
                rubbleHullSprite,
                rubbleTurretSprite,
                deathShadow,
                shadow,
                deathAnimationHull,
                deathAnimationTurret,
                tankHullDamagedRed,
                tankHullDamagedGreen,
                tankTurretDamagedRed,
                tankFireAnimationDamagedRed,
                tankFireAnimationDamagedGreen,
                deathFadeout,
                tankTurretDamagedGreen
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
        if (currentHealth > 0 && !isRubble) {
            this.setGraphic(getHullSprite());
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
        initGraphics();
        setScale(VISUAL_SCALE); 
        Sprite chassSprite = getHullSprite();
        this.setGraphic(chassSprite);
        this.movementType = MovementType.RotationBased;
        turret = new Turret(new Coordinate(0, 0));
        this.addSubObject(turret);
        isSolid = true;
        preventOverlap = true;
        this.maxHealth = 210;//tanks can take 4 shots
        this.currentHealth = maxHealth;
        this.baseSpeed = speed;
    }

    //when a tank tries to fire, it first checks if its turret is still firing. 
    //if not, tell the turret to fire at target location
    public void fire(Coordinate target) {
        if (weaponOnCooldown || target.distanceFrom(location) < getHeight() * 3 / 5 || Math.abs(turret.rotationNeededToFace(target)) > 1) { //limited to one shot per 60 ticks
            return;
        }
        weaponOnCooldown = true;
        turret.onFire(target);
        this.addTickDelayedEffect((int) (Main.ticksPerSecond * attackFrequency), x -> {
            weaponOnCooldown = false;
        });
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
                SoundEffect sound = tickNumber%2==0 ? launchSoundSource : launchSoundSource2;
                if (sound.getNumCopiesPlaying() < 6) {
                    if (isOnScreen()) {
                        sound.playCopy(Main.generateRandomDoubleLocally(.6, .65));
                        addTickDelayedEffect(Main.ticksPerSecond / 2, c -> sound.changeNumCopiesPlaying(-1));
                    } else {
                        sound.playCopy(Main.generateRandomDoubleLocally(.5, .55));
                        addTickDelayedEffect(Main.ticksPerSecond / 2, c -> sound.changeNumCopiesPlaying(-1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Coordinate muzzelLocation = new Coordinate(0, 0);
            muzzelLocation.y -= getFireSequence().frames[0].getHeight() * 2 / 5;
            muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, getRotation());
            muzzelLocation.add(getPixelLocation());
            RTSUnit targetUnit = ((RTSUnit) this.getHost()).currentTarget;
            int longestSide = Math.max(targetUnit.getWidth(), targetUnit.getHeight());
            Coordinate offset = new Coordinate(Main.generateRandomInt(-longestSide / 3, longestSide / 3), Main.generateRandomInt(-longestSide / 3, longestSide / 3));
            target.add(offset);
//            addTickDelayedEffect(4, c -> {
//                new OnceThroughSticker(getHostGame(), TankBullet.explosionSmall, target);
//            });
            TankBullet bullet = new TankBullet(muzzelLocation.toDCoordinate(), target.toDCoordinate());
            bullet.shooter = this.getHost();
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
                double desiredRotation = getHost().getRotation() - getRotation();
                if (desiredRotation > 180) {
                    desiredRotation -= 360;
                } else if (desiredRotation < -180) {
                    desiredRotation += 360;
                }
                double maxRotation = 5;
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
                double maxRotation = 5;
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
                g.rotate(Math.toRadians(getRotation()), getPixelLocation().x + shadowOffsetX, getPixelLocation().y + shadowOffsetY);
                g.drawImage(toRender, renderX, renderY + shadowOffsetY, null);
                g.setTransform(old);
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
        addTickDelayedEffect(Main.ticksPerSecond * 10, c -> {
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "transientExplosion"), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.setZLayer(-1);
            this.turret.isInvisible = true;
            addTickDelayedEffect(Main.ticksPerSecond * 3, c2 -> {
                this.destroy();
            });
            // this.destroy();
        });
    }
    
    @Override
    public BufferedImage getSelectionImage() {
        return RTSAssetManager.tankSelectionImage;
    }
    
    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + TankBullet.DAMAGE + "    Interval: " + attackFrequency+"s    Range: "+ range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground");
        return out;
    }

}

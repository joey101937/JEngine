/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class LightTank extends RTSUnit {
    
    public static final double speed = 2.8;
    public static final double attackInterval = 1.6;

    public static double VISUAL_SCALE = 1.00;
    public static SoundEffect launchSoundSource = new SoundEffect(new File(Main.assets + "Sounds/gunshot.wav"));
    public static final Sprite hullSprite = new Sprite(SpriteManager.lightTankHull);
    public static final Sprite turretSprite = new Sprite(SpriteManager.lightTankTurret);
    public static final Sprite turretSpriteDamaged = new Sprite(SpriteManager.lightTankTurretDamaged);
    public static final Sprite redHullSprite = new Sprite(greenToRed(SpriteManager.lightTankHull));
    public static final Sprite redTurretSprite = new Sprite(greenToRed(SpriteManager.lightTankTurret));
    public static final Sprite redTurretSpriteDamaged = new Sprite(greenToRed(SpriteManager.lightTankTurretDamaged));
    public static final Sprite hullShadow = new Sprite(SpriteManager.lightTankShadow);
    public static final Sprite turretShadow = Sprite.generateShadowSprite(SpriteManager.lightTankTurret, .8);
    public static final Sprite hullSpriteDamaged = new Sprite(SpriteManager.lightTankHullDamaged);
    public static final Sprite redHullSpriteDamaged = new Sprite(greenToRed(SpriteManager.lightTankHullDamaged));
    public static final Sprite hullSpriteDestroyed = new Sprite(SpriteManager.lightTankHullDestroyed);
    public static final Sprite turretSpriteDestroyed = new Sprite(SpriteManager.lightTankTurretDestroyed);
    public static final Sequence fireSequence = new Sequence(SpriteManager.lightTankFire, "lightTankFire");
    public static final Sequence fireSequenceDamaged = new Sequence(SpriteManager.lightTankFireDamaged, "lightTankFireDamaged");
    public static final Sequence redFireSequence = new Sequence(greenToRed(SpriteManager.lightTankFire), "lightTankFireRed");
    public static final Sequence redFireSequenceDamaged = new Sequence(greenToRed(SpriteManager.lightTankFireDamaged), "lightTankDamagedFireRed");
    public static final Sequence deathFadeout = Sequence.createFadeout(SpriteManager.lightTankDeathShadow, 40);

    static {
        hullShadow.scaleTo(VISUAL_SCALE);
        deathFadeout.setSignature("deathFadeoutLightTank");
    }

    // instance fields
    public LightTankTurret turret;
    public boolean barrelCoolingDown = false;

    public LightTank(int x, int y, int team) {
        super(x, y, team);
        this.maxHealth = 140;
        this.currentHealth = maxHealth;
        this.setScale(VISUAL_SCALE);
        this.setGraphic(getHullSprite());
        turret = new LightTankTurret(new Coordinate(0, 0), this);
        this.addSubObject(turret);
        this.isSolid = true;
        this.setHitbox(new Hitbox(this, getWidth() / 2));
        this.range = 500;
        this.baseSpeed = speed;
    }

    @Override
    public void render(Graphics2D g) {
        if (isSolid) {
            drawShadow(g, hullShadow, 5, 9);
        }
        if(isSelected()){
            drawRubbleProximityIndicators(g);
        }
        super.render(g);
    }

    @Override
    public void tick() {
        super.tick();
        if (isRubble) {
            return;
        }
        populateNearbyEnemies();
        currentTarget = nearestEnemyGroundUnit;
        if (currentTarget != null && turret.rotationNeededToFace(currentTarget.getPixelLocation()) < 1 && !barrelCoolingDown) {
            fire(currentTarget);
        }
        setGraphic(getHullSprite());
    }

    public void fire(RTSUnit target) {
        barrelCoolingDown = true;
        if (launchSoundSource.getNumCopiesPlaying() < 10) {
            if (isOnScreen()) {
                launchSoundSource.playCopy(Main.generateRandomDoubleLocally(.6, .65));
                addTickDelayedEffect(Main.ticksPerSecond / 2, c -> launchSoundSource.changeNumCopiesPlaying(-1));
            } else {
                launchSoundSource.playCopy(Main.generateRandomDoubleLocally(.5, .55));
                addTickDelayedEffect(Main.ticksPerSecond / 2, c -> launchSoundSource.changeNumCopiesPlaying(-1));
            }
        }
        turret.setGraphic(getFireSequence());
        Coordinate muzzelLocation = new Coordinate(0, 0);
        muzzelLocation.y -= turretSprite.getHeight() * 2 / 5;
        muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, turret.getRotation());
        muzzelLocation.add(turret.getPixelLocation());
        Coordinate randomOffset = new Coordinate(Main.generateRandomInt(-target.getWidth() / 4, target.getWidth() / 4), Main.generateRandomInt(-target.getWidth() / 4, target.getWidth() / 4));
        LightTankBullet bullet = new LightTankBullet(muzzelLocation.toDCoordinate(), target.getLocationAsOfLastTick().add(randomOffset));
        bullet.shooter = this;
        getHostGame().addObject(bullet);
        addTickDelayedEffect((int) (Main.ticksPerSecond * attackInterval), c -> {
            this.barrelCoolingDown = false;
        });
    }

    public Sequence getFireSequence() {
        if (currentHealth > maxHealth * .33) {
            return team == 0 ? fireSequence.copyMaintainSource() : redFireSequence.copyMaintainSource();
        }
        return team == 0 ? fireSequenceDamaged.copyMaintainSource() : redFireSequenceDamaged.copyMaintainSource();
    }

    public Sprite getHullSprite() {
        if (isRubble) {
            return hullSpriteDestroyed;
        }
        if (currentHealth > maxHealth * .33) {
            return team == 0 ? hullSprite : redHullSprite;
        }
        return team == 0 ? hullSpriteDamaged : redHullSpriteDamaged;
    }

    @Override
    public int getWidth() {
        return hullSprite.getWidth();
    }

    @Override
    public int getHeight() {
        return (int) (hullSprite.getHeight() * .9);
    }

    @Override
    public void die() {
        if (this.isRubble) {
            return;
        }
        OnceThroughSticker deathExplosion = new OnceThroughSticker(getHostGame(), new Sequence(SpriteManager.explosionSequence, "lightTankDeathExplosion"), getPixelLocation());
        this.isRubble = true;
        this.team = -1;
        this.setBaseSpeed(0);
        this.setDesiredLocation(this.getPixelLocation());
        this.setGraphic(hullSpriteDestroyed);
        turret.setGraphic(turretSpriteDestroyed);
        addTickDelayedEffect(Main.ticksPerSecond * 10, c -> {
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(SpriteManager.explosionSequence, "lightTankDespawnExplosion"), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.setZLayer(-1);
            this.turret.isInvisible = true;
            addTickDelayedEffect(Main.ticksPerSecond * 3, c2 -> {
                this.destroy();
            });
        });
    }

    public class LightTankTurret extends SubObject {

        public LightTank hull;
        public double desiredRotationAngle = 0;

        public LightTankTurret(Coordinate offset, LightTank h) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(getTurretSprite());
            this.hull = h;
        }

        public void updateDesiredRotation() {
            if (hull.isRubble) {
                desiredRotationAngle = getRotation();
                return;
            }
            if (currentTarget == null) {
                desiredRotationAngle = hull.getRotation();
            } else {
                desiredRotationAngle = angleFrom(hull.currentTarget.getPixelLocation());
            }
        }

        @Override
        public void tick() {
            updateDesiredRotation();
            double maxRotationPerTick = 5;
            double desiredRotationAmount = desiredRotationAngle - getRotationRealTime();
            if (desiredRotationAmount > 180) {
                desiredRotationAmount -= 360;
            } else if (desiredRotationAmount < -180) {
                desiredRotationAmount += 360;
            }
            double toRotate = Math.clamp(desiredRotationAmount, -maxRotationPerTick, maxRotationPerTick);
            this.rotate(toRotate);
            if (!isAnimated() || isRubble) {
                setGraphic(getTurretSprite());
            }
        }

        @Override
        public void render(Graphics2D g) {
            if (getHost().isSolid && !isRubble) {
                AffineTransform old = g.getTransform();
                VolatileImage toRender = turretShadow.getCurrentVolatileImage();
                Coordinate pixelLocation = getPixelLocation().add(new Coordinate(2, 3));
                int renderX = pixelLocation.x - toRender.getWidth() / 2;
                int renderY = pixelLocation.y - toRender.getHeight() / 2;
                g.rotate(Math.toRadians(getRotation()), pixelLocation.x, pixelLocation.y);
                g.drawImage(toRender, renderX, renderY, null);
                g.setTransform(old);
            }
            super.render(g);
        }

        public Graphic getTurretSprite() {
            if (isRubble) {
                return turretSpriteDestroyed;
            }
            if (hull != null && hull.currentHealth > hull.maxHealth * .33) {
                return team == 0 ? turretSprite : redTurretSprite;
            }
            return team == 0 ? turretSpriteDamaged : redTurretSpriteDamaged;
        }

        @Override
        public void onAnimationCycle() {
            this.setGraphic(getTurretSprite());
        }

    }

    @Override
    public BufferedImage getSelectionImage() {
        return SpriteManager.lightTankSelectionImage;
    }
    
    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + LightTankBullet.DAMAGE + "    Interval: " + attackInterval+"s    Range: "+ range);
        out.add("Speed: " + speed + "    Targets: Ground");
        return out;
    }
}
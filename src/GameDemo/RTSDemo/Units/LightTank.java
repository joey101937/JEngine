
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.LayMineButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class LightTank extends RTSUnit {

    public static final double speed = 2.8;
    public static final double attackInterval = 1.6;

    public static double VISUAL_SCALE = 1.00;
    public static final Sprite hullSprite = new Sprite(RTSAssetManager.lightTankHull);
    public static final Sprite turretSprite = new Sprite(RTSAssetManager.lightTankTurret);
    public static final Sprite turretSpriteDamaged = new Sprite(RTSAssetManager.lightTankTurretDamaged);
    public static final Sprite redHullSprite = new Sprite(RTSAssetManager.lightTankHullRed);
    public static final Sprite redTurretSprite = new Sprite(RTSAssetManager.lightTankTurretRed);
    public static final Sprite redTurretSpriteDamaged = new Sprite(RTSAssetManager.lightTankTurretDamagedRed);
    public static final Sprite hullShadow = new Sprite(RTSAssetManager.lightTankShadow);
    public static final Sprite turretShadow = Sprite.generateShadowSprite(RTSAssetManager.lightTankTurret, .8);
    public static final Sprite hullSpriteDamaged = new Sprite(RTSAssetManager.lightTankHullDamaged);
    public static final Sprite redHullSpriteDamaged = new Sprite(RTSAssetManager.lightTankHullDamagedRed);
    public static final Sprite hullSpriteDestroyed = new Sprite(RTSAssetManager.lightTankHullDestroyed);
    public static final Sprite turretSpriteDestroyed = new Sprite(RTSAssetManager.lightTankTurretDestroyed);
    public static final Sequence fireSequence = new Sequence(RTSAssetManager.lightTankFire, "lightTankFire");
    public static final Sequence fireSequenceDamaged = new Sequence(RTSAssetManager.lightTankFireDamaged, "lightTankFireDamaged");
    public static final Sequence redFireSequence = new Sequence(RTSAssetManager.lightTankFireRed, "lightTankFireRed");
    public static final Sequence redFireSequenceDamaged = new Sequence(RTSAssetManager.lightTankFireDamagedRed, "lightTankDamagedFireRed");
    public static final Sequence deathFadeout = Sequence.createFadeout(RTSAssetManager.lightTankDeathShadow, 40);

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
        addButton(new LayMineButton(this));
        addButton(new LayMineButton(this));
        addButton(new LayMineButton(this));
        addButton(new LayMineButton(this));
    }

    @Override
    public void render(Graphics2D g) {
        if (isSolid) {
            drawShadow(g, hullShadow, 5, 9);
        }
        if (isSelected()) {
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
    
    public void playAttackSound() {
         String soundToPlay = RTSSoundManager.LIGHT_TANK_ATTACK;
         int offset = Main.generateRandomIntLocally(0, 20);
         
        System.out.println(soundToPlay);
         
         double volumeOnScreen = Main.generateRandomDoubleLocally(.70, .76);
         
         double volumeOffScreen = Main.generateRandomDoubleLocally(.64, .71);
         
        RTSSoundManager.get().play(
                soundToPlay,
                isOnScreen() ? volumeOnScreen : volumeOffScreen,
                offset);
    }

    public void fire(RTSUnit target) {
        barrelCoolingDown = true;
        playAttackSound();
        turret.setGraphic(getFireSequence());
        Coordinate muzzelLocation = new Coordinate(0, 0);
        muzzelLocation.y -= turretSprite.getHeight() * 2 / 5;
        muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, turret.getRotationRealTime());
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
        OnceThroughSticker deathExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "lightTankDeathExplosion"), getPixelLocation());
        this.isRubble = true;
        this.team = -1;
        this.setBaseSpeed(0);
        this.setDesiredLocation(this.getPixelLocation());
        this.setGraphic(hullSpriteDestroyed);
        turret.setGraphic(turretSpriteDestroyed);
        if(isOnScreen()) {
            RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, Main.generateRandomDoubleLocally(.62, .64), 0);
        }
        addTickDelayedEffect(Main.ticksPerSecond * 10, c -> {
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "lightTankDespawnExplosion"), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.setZLayer(-10);
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
                desiredRotationAngle = hull.getRotationRealTime();
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
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + LightTankBullet.staticDamage + "    Interval: " + attackInterval + "s    Range: " + range);
        out.add("Speed: " + speed + "    Targets: Ground");
        return out;
    }

    @Override
    public void triggerAbility(int index, Coordinate target) {
        if (index == 0) {
            getHostGame().addObject(new Landmine(target.x, target.y, team));
        }
    }
}

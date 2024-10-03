package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class Rifleman extends RTSUnit {

    public static final double VISUAL_SCALE = .2;
    public static final Sprite baseSprite = new Sprite(RTSAssetManager.infantryLegs);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.infantryShadow);
    public static final Sequence runningSequence = new Sequence(RTSAssetManager.infantryLegsRun);
    public static final Sequence attackSequence = new Sequence(RTSAssetManager.infantryRifleFire, "riflemanAttackSequence");
    public static final Sequence attackSequenceRed = new Sequence(RTSAssetManager.infantryRifleFireRed, "riflemanAttackSequence");
    public static final Sequence idleAnimation = new Sequence(RTSAssetManager.infantryRifleIdle, "riflemanIdle");
    public static final Sequence idleAnimationRed = new Sequence(RTSAssetManager.infantryRifleIdleRed, "redRiflemanIdle");
    public static final SoundEffect attackSound = new SoundEffect(new File(Main.assets + "Sounds/machinegun.au"));
    public boolean attackCoolingDown = false;
    public static final int damage = 6;
    public static final int attackFrequency = 1;

    static {
        runningSequence.setFrameDelay(35);
        shadowSprite.scaleTo(VISUAL_SCALE * 2);
        idleAnimation.scaleTo(VISUAL_SCALE);
        idleAnimationRed.scaleTo(VISUAL_SCALE);
        attackSequence.scaleTo(VISUAL_SCALE);
        attackSequenceRed.scale(VISUAL_SCALE);
        baseSprite.scaleTo(VISUAL_SCALE * .8);
        runningSequence.scaleTo(VISUAL_SCALE * .8);
    }

    // fields
    public RiflemanTurret turret = new RiflemanTurret(this);

    public Rifleman(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE * .8);
        this.setGraphic(baseSprite);
        this.addSubObject(this.turret);
        this.setZLayer(1);
        this.isSolid = true;
        this.setBaseSpeed(1.88);
        this.canAttackAir = true;
        this.rotationSpeed = 15;
        this.maxHealth = 200;
        this.currentHealth = 200;
        this.range = 500;
        isInfantry = true;
    }

    @Override
    public int getWidth() {
        return baseSprite.getWidth() + 24;
    }

    @Override
    public int getHeight() {
        return baseSprite.getHeight() + 24;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.velocity.y != 0 && !getGraphic().isAnimated()) {
            Sequence runInstance = runningSequence.copyMaintainSource();
            runInstance.advanceMs((int) (Math.random() * 1000));
            this.setGraphic(runInstance);
        }
        if (this.velocity.y == 0 && getGraphic().isAnimated()) {
            this.setGraphic(baseSprite);
        }
        if("riflemanAttackSequence".equals(turret.getGraphic().getSignature()) && currentTarget == null && !isRubble) {
            turret.setGraphic(turret.getIdleAnimation());
        }
    }

    public void fire(RTSUnit target) {
        if (attackCoolingDown || Math.abs(turret.rotationNeededToFace(target.getPixelLocation())) > 1) {
            return;
        }
        attackCoolingDown = true;
        if (attackSound.getNumCopiesPlaying() < 7) {
            if (isOnScreen()) {
                attackSound.playCopy(Main.generateRandomDoubleLocally(.55f, .63f), Main.generateRandomIntLocally(0, 10));
                addTickDelayedEffect(Main.ticksPerSecond, c -> attackSound.changeNumCopiesPlaying(-1));
            } else {
                attackSound.playCopy(Main.generateRandomDoubleLocally(.4f, .48f), Main.generateRandomIntLocally(0, 10));
                addTickDelayedEffect(Main.ticksPerSecond, c -> attackSound.changeNumCopiesPlaying(-1));
            }
        }
        turret.setGraphic(turret.getFireAnimation());
        target.takeDamage(damage);
        addTickDelayedEffect(Main.ticksPerSecond * attackFrequency, c -> {
            this.attackCoolingDown = false;
        });
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = getPixelLocation().x - toRender.getWidth() / 2;
        int renderY = getPixelLocation().y - toRender.getHeight() / 2;
        int shadowOffset = 4;
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
    }

    public class RiflemanTurret extends SubObject {

        public Rifleman hull;
        
        public Graphic getIdleAnimation () {
            return switch(hull.team) {
                case 0 -> idleAnimation;
                case 1 -> idleAnimationRed;
                default -> idleAnimation;
            };
        }
        
        public Graphic getFireAnimation() {
            return switch(hull.team) {
                case 0 -> attackSequence.copyMaintainSource();
                case 1 -> attackSequenceRed.copyMaintainSource();
                default -> attackSequence.copyMaintainSource();
            };
        }

        public RiflemanTurret(Rifleman r) {
            super(new Coordinate(0, 0));
            this.setScale(VISUAL_SCALE);
            this.hull = r;
            this.setGraphic(getIdleAnimation());
        }

        @Override
        public void tick() {
            // System.out.println(this + " " + this.ID);
            super.tick();
            if (isRubble) {
                return;
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
                hull.fire(enemy);
            }
        }

        /*
        this runs whenever an animation cycle ends.
        here we use it to tell the gank when its ready to fire again and
        also to reset the object back to using the regular turret sprite
         */
        @Override
        public void onAnimationCycle() {
            if (getGraphic().getSignature().equals("riflemanAttackSequence")) {
                setGraphic(getIdleAnimation());
            }
        }
    }
    
    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + damage + "    Interval: " + attackFrequency+"s    Range: "+ range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        return out;
    }

}

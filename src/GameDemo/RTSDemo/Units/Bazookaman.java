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
public class Bazookaman extends RTSUnit {

    public static final double VISUAL_SCALE = .2;
    public static final Sprite baseSprite = new Sprite(RTSAssetManager.infantryLegs);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.infantryShadow);
    public static final Sequence runningSequence = new Sequence(RTSAssetManager.infantryLegsRun, "bazookaRun");
    public static final Sequence attackSequence = new Sequence(RTSAssetManager.infantryBazookaFire, "bazookaFire");
    public static final Sequence attackSequenceRed = new Sequence(RTSAssetManager.infantryBazookaFireRed, "bazookaFireRed");
    public static final Sequence idleAnimation = new Sequence(RTSAssetManager.infantryBazookaIdle, "bazookaIdle");
    public static final Sequence idleAnimationRed = new Sequence(RTSAssetManager.infantryBazookaIdleRed, "bazookaIdleRed");
    public static final Sequence deathAnimation = new Sequence(RTSAssetManager.infantryBazookaDie, "BazookaDie");
    public static final Sequence deathAnimationRed = new Sequence(RTSAssetManager.infantryBazookaDieRed, "BazookaDieRed");
    public static final Sprite corpseSprite = new Sprite(RTSAssetManager.infantryBazookaDead);
    public static final Sprite corpseSpriteRed = new Sprite(RTSAssetManager.infantryBazookaDeadRed);
    public static final Sprite deadShadowSprite = Sprite.generateShadowSprite(RTSAssetManager.infantryBazookaDead, .8);
    public boolean attackCoolingDown = false;
    public static final SoundEffect attackSound = new SoundEffect(new File(Main.assets + "Sounds/bazooka.au"));
    public static final double attackInterval = 3;

    static {
        runningSequence.setFrameDelay(35);
        attackSequence.setSignature("attackSequence");
        attackSequenceRed.setSignature("attackSequence");
        attackSequence.setFrameDelay(30);
        attackSequenceRed.setFrameDelay(30);
        deathAnimation.setFrameDelay(30);
        deathAnimationRed.setFrameDelay(30);
        corpseSprite.setSignature("corpseSprite");
        corpseSpriteRed.setSignature("corpseSprite");
        deathAnimation.setLooping(false);
        deathAnimationRed.setLooping(false);
    }

    // fields
    public BazookamanTurret turret = new BazookamanTurret(this);

    public Bazookaman(int x, int y, int team) {
        super(x, y, team);
        shadowSprite.scaleTo(VISUAL_SCALE * 2); //shadow is small
        this.setScale(VISUAL_SCALE); // feet are small already
        this.setGraphic(baseSprite);
        this.maxHealth = 20;
        this.addSubObject(this.turret);
        this.setZLayer(1);
        this.isSolid = true;
        this.setBaseSpeed(1.88);
        this.canAttackAir = true;
        this.rotationSpeed = 15;
        this.maxHealth = 30;
        this.currentHealth = 30;
        this.range = 600;
        isInfantry = true;
    }

    @Override
    public int getWidth() {
        return (int)(baseSprite.getWidth() * getScale()) + 30;
    }

    @Override
    public int getHeight() {
        return (int)(baseSprite.getHeight() * getScale()) + 40;
    }

    @Override
    public void tick() {
        super.tick();
        if(isRubble && this.turret.getRenderOpacity() > 0) {
                this.turret.setRenderOpacity(turret.getRenderOpacity() - (1f/(Main.ticksPerSecond*5)));
        }
        if(this.isRubble) return;
        if (this.velocity.y != 0 && !getGraphic().isAnimated()) {
            Sequence runInstance = runningSequence.copyMaintainSource();
            runInstance.advanceMs((int) (Math.random() * 1000));
            this.setGraphic(runInstance);
        }
        if (this.velocity.y == 0 && getGraphic().isAnimated()) {
            this.setGraphic(baseSprite);
        }
    }

    public void fire(RTSUnit target) {
        if (attackCoolingDown || Math.abs(turret.rotationNeededToFace(target.getPixelLocation())) > 1) {
            return;
        }
        attackCoolingDown = true;
        if (attackSound.getNumCopiesPlaying() < 10) {
            if (isOnScreen()) {
                attackSound.playCopy(Main.generateRandomDoubleLocally(.64f, .68f), Main.generateRandomIntLocally(0, 10));
                addTickDelayedEffect(Main.ticksPerSecond / 2, c -> attackSound.changeNumCopiesPlaying(-1));
            } else {
                attackSound.playCopy(Main.generateRandomDoubleLocally(.6f, .64f), Main.generateRandomIntLocally(0, 10));
                addTickDelayedEffect(Main.ticksPerSecond / 2, c -> attackSound.changeNumCopiesPlaying(-1));
            }
        }
        turret.setGraphic((team == 0 ? attackSequence : attackSequenceRed).copyMaintainSource());
        addTickDelayedEffect(25, c -> {
            Coordinate offset = new Coordinate(5, -30);
            offset.adjustForRotation(turret.getRotation());
            getHostGame().addObject(new BazookaBullet(this, getPixelLocation().copy().add(offset), target));
        });
        addTickDelayedEffect((int) (Main.ticksPerSecond * attackInterval), c -> {
            this.attackCoolingDown = false;
        });
    }

    @Override
    public void render(Graphics2D g) {
        if(isRubble) return;
        super.render(g);
        if (shadowSprite == null) {
            System.out.println("shadow null for bazooka");
            return;
        }
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = getPixelLocation().x - toRender.getWidth() / 2;
        int renderY = getPixelLocation().y - toRender.getHeight() / 2;
        int shadowOffset = 10;
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
    }
    
    private Sequence getDeathAnimation() {
        return switch(team){
            case 0 -> deathAnimation.copyMaintainSource();
            case 1 -> deathAnimationRed.copyMaintainSource();
            default -> deathAnimation.copyMaintainSource();
        };
    }
    
    private Graphic getCorpseGraphic() {
        return switch(team) {
            case 0 -> corpseSprite;
            case 1 -> corpseSpriteRed;                
            default -> corpseSprite;
        };
    }
    
    @Override
    public void die() {
        this.setBaseSpeed(0);
        this.isRubble = true;
        this.isSolid = false;
        this.turret.setGraphic(getDeathAnimation());
        this.addTickDelayedEffect(Main.ticksPerSecond * 10, x-> {this.destroy();});
        this.setZLayer((int)(Math.random() * -50));
    }


    public class BazookamanTurret extends SubObject {
        
        public Graphic getIdleAnimation() {
            return switch(team) {
                case 0 -> idleAnimation;
                case 1 -> idleAnimationRed;
                default -> idleAnimation;
            };
        }

        public Bazookaman hull;

        public BazookamanTurret(Bazookaman r) {
            super(new Coordinate(0, 0));
            this.setScale(VISUAL_SCALE);
            this.hull = r;
            this.setGraphic(getIdleAnimation());
        }
        
        @Override
        public void render(Graphics2D g) {
            super.render(g);
        }

        @Override
        public void tick() {
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
            if (getGraphic().getSignature().equals("attackSequence")) {
                setGraphic(getIdleAnimation());
            }
            if(getGraphic().getSignature().contains("Die")) {
                this.setGraphic(hull.getCorpseGraphic());
            }
        }
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + BazookaBullet.damage + "    Interval: " + attackInterval + "s    Range: " + range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        return out;
    }

}

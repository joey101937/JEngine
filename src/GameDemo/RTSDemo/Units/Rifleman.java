package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.InfantryButton;
import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
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
public class Rifleman extends RTSUnit {

    public static final double VISUAL_SCALE = .2;
    public static final Sprite baseSprite = new Sprite(RTSAssetManager.infantryLegs);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.infantryShadow);
    public static final Sequence runningSequence = new Sequence(RTSAssetManager.infantryLegsRun);
    public static final Sequence attackSequence = new Sequence(RTSAssetManager.infantryRifleFire, "riflemanAttackSequence");
    public static final Sequence attackSequenceRed = new Sequence(RTSAssetManager.infantryRifleFireRed, "riflemanAttackSequence");
    public static final Sequence idleAnimation = new Sequence(RTSAssetManager.infantryRifleIdle, "riflemanIdle");
    public static final Sequence idleAnimationRed = new Sequence(RTSAssetManager.infantryRifleIdleRed, "redRiflemanIdle");
    public static final Sequence deathAnimation = new Sequence(RTSAssetManager.infantryRifleDie, "RiflemanDie");
    public static final Sequence deathAnimationRed = new Sequence(RTSAssetManager.infantryRifleDieRed, "RiflemanDieRed");
    public static final Sprite corpseSprite = new Sprite(RTSAssetManager.infantryRifleDead);
    public static final Sprite corpseSpriteRed = new Sprite(RTSAssetManager.infantryRifleDeadRed);
    public static final Sprite deadShadowSprite = Sprite.generateShadowSprite(RTSAssetManager.infantryRifleDead, .8);
    public static final Damage staticDamage = new Damage(6);
    public static final int attackFrequency = 1;

    public long attackCooldownExpiresAtTick = 0;
    public Damage damage = staticDamage.copy(this);

    static {
        runningSequence.setFrameDelay(35);
        shadowSprite.scaleTo(VISUAL_SCALE * 2);
        deadShadowSprite.scale(VISUAL_SCALE);
        deathAnimation.setFrameDelay(30);
        deathAnimationRed.setFrameDelay(30);
        corpseSprite.setSignature("corpseSprite");
        corpseSpriteRed.setSignature("corpseSprite");
        deathAnimation.setLooping(false);
        deathAnimationRed.setLooping(false);
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
        this.setBaseSpeed(RTSGame.tickAdjust(1.88));
        this.canAttackAir = true;
        this.rotationSpeed = RTSGame.tickAdjust(15);
        this.maxHealth = 20;
        this.currentHealth = 20;
        this.range = 500;
        isInfantry = true;
        this.minSpeedMultiplier = .8;
        this.minSpeedDistance = 25;
        this.maxSpeedDistance = 50;
        initializeButtons();
    }

    private void initializeButtons() {
        addButton(new InfantryButton(this));
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
        // Restore graphics after deserialization
        if (g != null && getGraphic() == null) {
            this.setGraphic(baseSprite);
            if (turret != null) {
                turret.setGraphic(turret.getIdleAnimation());
            }
        }
        // Restore button transient fields after deserialization
        if (g != null) {
            for (CommandButton button : getButtons()) {
                button.restoreTransientFields();
            }
        }
    }

    @Override
    public int getWidth() {
        return (int)((baseSprite.getWidth() * getScale()) + 24);
    }

    @Override
    public int getHeight() {
        return (int)((baseSprite.getHeight()* getScale()) + 24);
    }

    @Override
    public void tick() {
        super.tick();

        // Check attack cooldown expiration
        if (attackCooldownExpiresAtTick > 0 && tickNumber >= attackCooldownExpiresAtTick) {
            attackCooldownExpiresAtTick = 0;
        }

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
        if(isRubble && this.turret.getRenderOpacity() > 0) {
                this.turret.setRenderOpacity(turret.getRenderOpacity() - (1f/(Main.ticksPerSecond*5)));
        }
    }

    public void fire(RTSUnit target) {
        if (attackCooldownExpiresAtTick > 0 || Math.abs(turret.rotationNeededToFace(target.getPixelLocation())) > 1) {
            return;
        }
        attackCooldownExpiresAtTick = tickNumber + (Main.ticksPerSecond * attackFrequency);

            if (isOnScreen()) {
                 RTSSoundManager.get().play(
                         RTSSoundManager.RIFLEMAN_ATTACK,
                         Main.generateRandomDoubleLocally(.55f, .63f),
                         Main.generateRandomIntLocally(0, 20));
            } else {
                RTSSoundManager.get().play(
                         RTSSoundManager.RIFLEMAN_ATTACK,
                         Main.generateRandomDoubleLocally(.4f, .48f),
                         Main.generateRandomIntLocally(0, 20));
            }
        turret.setGraphic(turret.getFireAnimation());
        damage.launchLocation = getPixelLocation();
        damage.impactLoaction = getPixelLocation();
        target.takeDamage(damage);
    }

    @Override
    public void render(Graphics2D g) {
        if(!isRubble) {
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
            if(getGraphic().getSignature().contains("Die")) {
                this.setGraphic(hull.getCorpseGraphic());
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
        if((tickNumber % 4) == 0) {
            RTSSoundManager.get().play(RTSSoundManager.INFANTRY_DEATH, .6, 0);
        }
    }
}

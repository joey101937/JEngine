package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class BazookaBullet extends Projectile {
    
    public static Damage staticDamage = new Damage(23); // used for 

    public Damage damage = staticDamage.copy();

    public static Sprite missileSprite = new Sprite(RTSAssetManager.yellowMissile);
    public static Sprite shadowSprite = new Sprite(RTSAssetManager.yellowMissileShadow);
    public static Sequence explosionSmall = new Sequence(RTSAssetManager.explosionSequenceSmall, "bazookaExplosion");

    public RTSUnit shooter; //the object that launched this projectile
    public RTSUnit target;
    public RTSUnit collidedUnit;
    public double initialDistance;
    public Coordinate startPosition;
    public double maxRotationPerTick = 1;

    public long tickToDestroy = -1;
    public boolean hasCollided = false;

    private int ticksToReachStillTarget = 0;

    public BazookaBullet(RTSUnit shooter, Coordinate startingLocation, RTSUnit other) {
        super(startingLocation);
        this.setBaseSpeed(RTSGame.tickAdjust(10.0));
        this.shooter = shooter;
        damage.source = shooter;
        damage.launchLocation = startingLocation;
        this.target = other;
        this.setScale(.12);
        this.setGraphic(missileSprite);
        this.setZLayer(2);
        this.plane = other.plane;
        this.lookAt(target);
        this.movementType = MovementType.RotationBased;
        this.velocity = new DCoordinate(0, -1);
        shadowSprite.scaleTo(.12);
        this.initialDistance = distanceFrom(other);
        explosionSmall.scaleTo(.85);
        maxRange = 700;
        startPosition = startingLocation;
        ticksToReachStillTarget = (int) (distanceFrom(other.getPixelLocation()) / baseSpeed);
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
        // Restore graphics after deserialization
        if (g != null && getGraphic() == null) {
            this.setGraphic(missileSprite);
        }
    }

    @Override
    public void onCollide(GameObject2 other, boolean myTick) {
        if (other != shooter && other instanceof RTSUnit unit) {
            if (unit.isRubble) {
                if (startPosition.distanceFrom(unit.getPixelLocation()) < 100) {
                    // if shooting unit is next to the rubble, it can shoot over it
                    return;
                }
            }
            if (unit.isCloaked) {
                return;
            }
            if (unit.team == shooter.team) {
                return;
            } else if (!hasCollided) {
                hasCollided = true;
                int tickDelay = Main.generateRandomInt(2, 5);
                tickToDestroy = tickNumber + tickDelay;
                collidedUnit = unit;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickNumber == tickToDestroy) {
            this.destroy();
        }
        // lockon
        if (target.maxHealth > 50) {
            Coordinate updatedTarget = target.getPixelLocation();
            double desiredRotation = rotationNeededToFace(updatedTarget);

            if (Math.abs(desiredRotation) < RTSUnit.RUBBLE_PROXIMITY) {
                if (Math.abs(desiredRotation) < maxRotationPerTick) {
                    this.rotate(desiredRotation);
                } else {
                    this.rotate(Math.clamp(desiredRotation, -maxRotationPerTick, maxRotationPerTick));
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), getPixelLocation(true));
        if (collidedUnit != null) {
            damage.impactLoaction = getPixelLocation();
            collidedUnit.takeDamage(damage);
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = getPixelLocation().x - toRender.getWidth() / 2;
        int renderY = getPixelLocation().y - toRender.getHeight() / 2;
        int shadowOffset = 30;
        if (target.plane == 1) {
            shadowOffset = (int) ((distanceFrom(target) / initialDistance) * 100);
        } else if (target.plane > 1) {
            shadowOffset = Math.min((int) (((double) tickNumber / (double) ticksToReachStillTarget) * 65) + 30, 95);
        }
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
        super.render(g);
    }

}

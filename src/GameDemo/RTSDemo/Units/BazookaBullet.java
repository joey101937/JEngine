package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import java.util.HashSet;
import java.util.Set;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.BurnMarkEffect;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.HullBurnDecal;
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
    public String preferredTargetId = null;
    private final Set<String> ignoredUnitIds = new HashSet<>(); 

    public long tickToDestroy = -1;
    public boolean hasCollided = false;

    private int ticksToReachStillTarget = 0;

    public BazookaBullet(RTSUnit shooter, Coordinate startingLocation, RTSUnit other) {
        super(startingLocation);
        this.setBaseSpeed(RTSGame.tickAdjust(10.0));
        this.shooter = shooter;
        this.preferredTargetId = shooter.getPreferredTargetId();
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
    }

    @Override
    public void onPostDeserialization() {
        // Restore graphics after deserialization
        this.setGraphic(missileSprite);
    }

    @Override
    public void onCollide(GameObject2 other, boolean myTick) {
        if (other != shooter && other instanceof RTSUnit otherUnit) {
            if (otherUnit.isRubble) {
                if (startPosition.distanceFrom(otherUnit.getPixelLocation()) < 100) {
                    // if shooting unit is next to the rubble, it can shoot over it
                    return;
                }
            }
            if (otherUnit.isCloaked) {
                return;
            }
            if (otherUnit.team == shooter.team) {
                return;
            }
            if (ignoredUnitIds.contains(otherUnit.ID)) return;
            
            boolean preferOtherUnit = preferredTargetId != null && !otherUnit.ID.equals(preferredTargetId);
            int ignoreChance = otherUnit.isInfantry ? otherUnit.getDodgeChance() : 0; // bazooka cannot be dodged by vehicles
            if(preferOtherUnit) ignoreChance += 50;
            if (Main.generateDeterministicRandomInt(0, 99) < ignoreChance) {
                ignoredUnitIds.add(otherUnit.ID);
                return;
            }
            
            if (!hasCollided) {
                hasCollided = true;
                int tickDelay = Main.generateRandomIntFromSeed(2, 5, getHostGame().getGameTickNumber());
                tickToDestroy = getHostGame().getGameTickNumber() + tickDelay;
                collidedUnit = otherUnit;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getHostGame().getGameTickNumber() == tickToDestroy) {
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
        if (collidedUnit != null) {
            damage.impactLoaction = getPixelLocation();
            collidedUnit.takeDamage(damage);
            Coordinate impactLoc = collidedUnit.getNearestBodyPoint(getPixelLocation());
            OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), impactLoc);
            if (collidedUnit.isSoftTarget) {
                getHostGame().addIndependentEffect(new BurnMarkEffect(getHostGame(), impactLoc, 9, RTSGame.desiredTPS * 5));
            } else {
                Coordinate vehicleCenter = collidedUnit.getPixelLocation();
                Coordinate vehicleBurnPos = new Coordinate(
                    (int)(impactLoc.x * 0.8 + vehicleCenter.x * 0.2),
                    (int)(impactLoc.y * 0.8 + vehicleCenter.y * 0.2)
                );
                collidedUnit.addRenderHook(new HullBurnDecal(vehicleBurnPos, collidedUnit, 9, RTSGame.desiredTPS * 5));
            }
        } else {
            Coordinate missPos = getPixelLocation(true);
            OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), missPos);
            if (this.plane<2) getHostGame().addIndependentEffect(new BurnMarkEffect(getHostGame(), missPos, 9, RTSGame.desiredTPS * 5));
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
            shadowOffset = Math.min((int) (((double) getHostGame().getGameTickNumber() / (double) ticksToReachStillTarget) * 65) + 30, 95);
        }
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
        super.render(g);
    }

}

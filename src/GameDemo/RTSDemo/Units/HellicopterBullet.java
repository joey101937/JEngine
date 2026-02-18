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
public class HellicopterBullet extends Projectile {
    
    public static Damage staticDamage = new Damage(0, 10);

    public static final Sprite missileSprite = new Sprite(RTSAssetManager.yellowMissile);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.yellowMissileShadow);
    public static final Sequence explosionSmall = new Sequence(RTSAssetManager.explosionSequenceSmall, "explosionSmallHeli");
    
    public Damage damage = staticDamage.copy();
    public RTSUnit shooter; //the object that launched this projectile
    public RTSUnit target;
    public RTSUnit collidedUnit;
    public double initialDistance;
    public Coordinate startPosition;

    public long tickToDestroy = -1;
    public boolean hasCollided = false;

    public int maxSpeed = RTSGame.tickAdjust(14);
    public double minSpeed = RTSGame.tickAdjust(4.4);
    public double accellerationTime = RTSGame.desiredTPS * .7;
    public double accellerationStage;

    static {
        shadowSprite.scaleTo(.12);
    }

    public HellicopterBullet(RTSUnit shooter, Coordinate startingLocation, RTSUnit other) {
        super(startingLocation);
        System.out.println(this.ID + " created with startLocation " + startingLocation);
        this.setBaseSpeed(minSpeed);
        this.shooter = shooter;
        damage.source = shooter;
        damage.launchLocation = startingLocation;
        this.target = other;
        this.setScale(.12);
        this.setGraphic(missileSprite);
        int shortestSide = Math.min(other.getWidth(), other.getHeight());
        double offsetX = 0;//  Main.generateRandomDoubleFromSeed(-shortestSide/2, shortestSide / 2, getHostGame().getGameTickNumber() + (int)getLocation().x + (int)getLocation().y); // Main.generateRandomInt(-shortestSide / 2, shortestSide / 2);
        double offsetY = 0;// Main.generateRandomDoubleFromSeed(-shortestSide/2, shortestSide / 2, getHostGame().getGameTickNumber() + (int)getLocation().x + (int)getLocation().y); // Main.generateRandomInt(-shortestSide / 2, shortestSide / 2);
        this.setZLayer(11);
        this.plane = other.plane;

        DCoordinate randomOffset = new DCoordinate(offsetX, offsetY);
        this.launch(other.getLocation().copy().add(randomOffset));
        shadowSprite.scaleTo(.12);
        this.initialDistance = distanceFrom(other);
        explosionSmall.scaleTo(.85);
        minSpeed = 4; // Main.generateRandomDoubleFromSeed(3, 5, getHostGame().getGameTickNumber() + getPixelLocation().x + getPixelLocation().y); // 4; // Main.generateRandomInt(3, 5);
        maxSpeed = 16; // Main.generateRandomIntFromSeed(14,18, getHostGame().getGameTickNumber() + (int)getLocation().x + (int)getLocation().y); // 16; // Main.generateRandomInt(14, 18);
        if (plane == 1) {
            maxRange = 750;
        } else {
            maxRange = 1400;
        }
        startPosition = startingLocation;
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
        if (other != shooter && other instanceof RTSUnit unit) {
            if (unit.isRubble) {
                if (startPosition.distanceFrom(unit.getPixelLocation()) < RTSUnit.RUBBLE_PROXIMITY + 30) {
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
                System.out.println(""+this.ID+"collided with " + other.ID + " on tick " + getHostGame().getGameTickNumber());
                hasCollided = true;
                int tickDelay = 3; // Main.generateRandomInt(2, 5);
                tickToDestroy = getHostGame().getGameTickNumber() + tickDelay;
                collidedUnit = unit;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        double accellarationPercent = accellerationStage / accellerationTime;
        double speed = Main.clamp(maxSpeed * accellarationPercent, maxSpeed, minSpeed);
        this.setBaseSpeed(speed);
        accellerationStage++;
        if (getHostGame().getGameTickNumber() == tickToDestroy) {
            this.destroy();
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
        int shadowOffset = 90;
        if (target.plane < 2) {
            shadowOffset *= (distanceFrom(target) / initialDistance);
        }
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
        super.render(g);
    }

}

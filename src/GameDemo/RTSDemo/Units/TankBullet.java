/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.UtilityObjects.Projectile;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Stickers.OnceThroughSticker;
import Framework.Stickers.Sticker;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 *
 * @author Joseph
 */
public class TankBullet extends Projectile {
    public static Damage staticDamage = new Damage(46);
    public Damage damage = staticDamage.copy();
    public GameObject2 shooter; //the object that launched this projectile

    public static final Sequence bulletGraphic = new Sequence(new BufferedImage[]{RTSAssetManager.bullet}, "tankBulletGraphic");
    public static final Sequence explosionSmall = new Sequence(RTSAssetManager.explosionSequenceSmall, "explosionSmallTank");
    public static final Sprite shadow = Sprite.generateShadowSprite(RTSAssetManager.bullet, .3);
    private DCoordinate startPosition;

    public TankBullet(DCoordinate start, DCoordinate end) {
        super(start, end);
        bulletGraphic.scaleTo(.2); // scales parent to the same size as how the sequence will be used so we dont have to scale on the fly
        shadow.scaleTo(.2);
        setScale(.2);
        this.setGraphic(bulletGraphic.copyMaintainSource());
        baseSpeed = RTSGame.tickAdjust(26.0);
        this.setHitbox(new Hitbox(this, 0)); //sets this to se a circular hitbox. updateHitbox() method manages radius for us so we set it to 0 by default
        maxRange = 750;
        startPosition = start;
        damage.source = (RTSUnit)shooter;
        damage.launchLocation = startPosition.toCoordinate();
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
        // Restore graphics after deserialization
        if (g != null && getGraphic() == null) {
            this.setGraphic(bulletGraphic.copyMaintainSource());
        }
    }

    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (other == shooter) {
            return; //dont collde with the gameobject that launched this projectile
        }
        RTSUnit otherUnit = RTSUnit.getUnitFromUnknown(other);
        if (otherUnit != null) {
            if (shooter instanceof RTSUnit) {
                if (((RTSUnit) shooter).team == otherUnit.team) {
                    return; // no friendly fire
                }
            }
            if(otherUnit.isCloaked) {
                // ignore cloaked units
                return;
            }
            if (otherUnit.isRubble) {
                if (startPosition.distanceFrom(otherUnit.getPixelLocation()) < RTSUnit.RUBBLE_PROXIMITY) {
                    // if shooting unit is next to the rubble, it can shoot over it
                    return;
                }
            }
            damage.impactLoaction = getPixelLocation();
            otherUnit.takeDamage(damage);
            Coordinate impactLoc = Coordinate.nearestPointOnCircle(getPixelLocation(), other.getPixelLocation(), other.getWidth() * .25);
            OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), impactLoc);
            destroy();
        }
    }

    @Override
    public void onTimeOut() {
        OnceThroughSticker s = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), this.getPixelLocation());
    }

    /**
     * bullets just destroy when they go out of bounds
     */
    @Override
       public void onCollideWorldBorder(DCoordinate l) {
           onTimeOut();
           destroy();
       }  

    @Override
    public void render(Graphics2D g) {
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadow.getCurrentVolatileImage();
        int renderX = getPixelLocation().x - toRender.getWidth() / 2;
        int renderY = getPixelLocation().y - toRender.getHeight() / 2;
        int shadowOffset = 30 - (int)((tickNumber/(maxRange/baseSpeed)) * 30);
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
        super.render(g);
    }
}

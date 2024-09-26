/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.image.BufferedImage;

/**
 *
 * @author guydu
 */
public class LightTankBullet extends Projectile {
     public static final Sequence explosionTiny = new Sequence(SpriteManager.impactCone, "lightTankImpact");
     public static final int DAMAGE = 20;

    public GameObject2 shooter; //the object that launched this projectile

    public static final Sequence bulletGraphic = new Sequence(new BufferedImage[]{SpriteManager.bullet2}, "lightTankBullet");
    public static final Sprite shadow = Sprite.generateShadowSprite(SpriteManager.bullet2, .3);
    private DCoordinate startPosition;

    public LightTankBullet(DCoordinate start, DCoordinate end) {
        super(start, end);
        explosionTiny.scaleTo(.8);
        explosionTiny.setFrameDelay(20);
        bulletGraphic.setSignature("bullet graphic");
        bulletGraphic.scaleTo(.16); // scales parent to the same size as how the sequence will be used so we dont have to scale on the fly
        shadow.scaleTo(.16);
        setScale(.16);
        this.setGraphic(bulletGraphic.copyMaintainSource());
        baseSpeed = 26;
        this.setHitbox(new Hitbox(this, 0)); //sets this to se a circular hitbox. updateHitbox() method manages radius for us so we set it to 0 by default
        maxRange = 600;
        startPosition = start;
    }

    //when this runs into a creature, deal damage to it then destroy this projectile
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (other == shooter) {
            return; //dont collde with the gameobject that launched this projectile
        }
        if (other instanceof RTSUnit) {
            RTSUnit otherUnit = (RTSUnit) other;
            if (shooter instanceof RTSUnit) {
                if (((RTSUnit) shooter).team == otherUnit.team) {
                    return; // no friendly fire
                }
            }
            if (otherUnit.isRubble) {
                if (startPosition.distanceFrom(otherUnit.getPixelLocation()) < RTSUnit.RUBBLE_PROXIMITY) {
                    // if shooting unit is next to the rubble, it can shoot over it
                    return;
                }
            }
            otherUnit.takeDamage(DAMAGE);
            Coordinate impactLoc = Coordinate.nearestPointOnCircle(getPixelLocation(), other.getPixelLocation(), other.getWidth() * .6);
            OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionTiny.copyMaintainSource(), impactLoc);
            impactExplosion.rotation = DCoordinate.angleFrom(shooter.getPixelLocation(), other.getPixelLocation());
            destroy();
        }
    }

    @Override
    public void onTimeOut() {
        OnceThroughSticker s = new OnceThroughSticker(getHostGame(), explosionTiny.copyMaintainSource(), this.getPixelLocation());
    }

    /**
     * bullets just destroy when they go out of bounds
     */
    @Override
    public void constrainToWorld() {
        DCoordinate loc = location;
        if (loc.x < getHostGame().worldBorder) {
            onTimeOut();
            destroy();
        }
        if (loc.y < getHostGame().worldBorder) {
            onTimeOut();
            destroy();
        }
        if (loc.x > getHostGame().getWorldWidth() - getHostGame().worldBorder) {
            onTimeOut();
            destroy();
        }
        if (loc.y > getHostGame().getWorldHeight() - getHostGame().worldBorder) {
            onTimeOut();
            destroy();
        }
    }
}

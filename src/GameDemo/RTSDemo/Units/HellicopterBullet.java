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
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class HellicopterBullet extends Projectile {

    public static Sprite missileSprite = new Sprite(SpriteManager.missile);
    public static Sprite shadowSprite = new Sprite(SpriteManager.missileShadow);
    public static Sequence explosionSmall = new Sequence(SpriteManager.explosionSequenceSmall);

    public RTSUnit shooter; //the object that launched this projectile
    public RTSUnit target;
    public RTSUnit collidedUnit;
    public double initialDistance;

    public long tickToDestroy = -1;
    public boolean hasCollided = false;

    public int maxSpeed = 14;
    public double minSpeed = 4.4;
    public double accellerationTime = Main.ticksPerSecond * .7;
    public double accellerationStage;

    public HellicopterBullet(RTSUnit shooter, Coordinate startingLocation, RTSUnit other) {
        super(startingLocation);
        this.setBaseSpeed(minSpeed);
        this.shooter = shooter;
        this.target = other;
        this.setScale(.12);
        this.setGraphic(missileSprite);
        int shortestSide = Math.min(other.getWidth(), other.getHeight());
        double offsetX = Main.generateRandomInt(-shortestSide / 2, shortestSide / 2);
        double offsetY = Main.generateRandomInt(-shortestSide / 2, shortestSide / 2);
        this.setZLayer(11);
        this.plane = other.plane;

        DCoordinate randomOffset = new DCoordinate(offsetX, offsetY);
        this.launch(other.location.copy().add(randomOffset));
        shadowSprite.scaleTo(.12);
        this.initialDistance = distanceFrom(other);
        explosionSmall.scaleTo(.85);
        minSpeed = Main.generateRandomInt(3, 5);
        maxSpeed = Main.generateRandomInt(14, 18);
        if(plane == 1) maxRange = 750;
        else maxRange = 1400;
    }

    @Override
    public void onCollide(GameObject2 other, boolean myTick) {
        if (other != shooter && other instanceof RTSUnit unit) {
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
        double accellarationPercent = accellerationStage / accellerationTime;
        double speed = Main.clamp(maxSpeed * accellarationPercent, maxSpeed, minSpeed);
        this.setBaseSpeed(speed);
        accellerationStage++;
        if (tickNumber == tickToDestroy) {
            this.destroy();
        }
    }

    @Override
    public void onDestroy() {
        OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), getPixelLocation(true));
        if (collidedUnit != null) {
            collidedUnit.takeDamage(10);
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

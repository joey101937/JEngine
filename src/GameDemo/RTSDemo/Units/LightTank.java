/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.SpriteManager;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class LightTank extends RTSUnit {

    public static double VISUAL_SCALE = 1.00;

    public static final Sprite hullSprite = new Sprite(SpriteManager.lightTankHull);
    public static final Sprite turretSprite = new Sprite(SpriteManager.lightTankTurret);
    public static final Sprite redHullSprite = new Sprite(greenToRed(SpriteManager.lightTankHull));
    public static final Sprite redTurretSprite = new Sprite(greenToRed(SpriteManager.lightTankTurret));
    public static final Sprite hullShadow = new Sprite(SpriteManager.lightTankShadow);
    public static final Sprite turretShadow = Sprite.generateShadowSprite(SpriteManager.lightTankTurret, .8);


    static {
        hullShadow.scaleTo(VISUAL_SCALE);
    }
    
    // instance fields
    public LightTankTurret turret;

    public LightTank(int x, int y, int team) {
        super(x, y, team);
        this.maxHealth = 140;
        this.currentHealth = 140;
        this.setScale(VISUAL_SCALE);
        this.setGraphic(getHullSprite());
        turret = new LightTankTurret(new Coordinate(0, 0), this);
        this.addSubObject(turret);
        this.isSolid = true;
        this.setHitbox(new Hitbox(this, getWidth()/2));
        this.range = 500;
    }

    @Override
    public void render(Graphics2D g) {
        if (isSolid) {
            drawShadow(g, hullShadow, 5, 9);
        }
        super.render(g);
    }
    
    @Override
    public void tick() {
        super.tick();
        populateNearbyEnemies();
        currentTarget = nearestEnemyGroundUnit;
    }

    public Sprite getHullSprite() {
        return team == 0 ? hullSprite : redHullSprite;
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
            if(currentTarget == null) {
                desiredRotationAngle = hull.getRotation();
            } else {
                desiredRotationAngle = angleFrom(hull.currentTarget.getPixelLocation());
            }
            
            if(Math.abs(desiredRotationAngle - getRotation()) > 180) {
                desiredRotationAngle -= 360;
            }
        }

        @Override
        public void tick() {
            updateDesiredRotation();
            double maxRotationPerTick = 5;
            double toRotate = 0;
            if(Math.abs(getRotation() - desiredRotationAngle) < maxRotationPerTick) {
                toRotate = desiredRotationAngle - getRotation();
            } else {
                toRotate = Math.clamp(desiredRotationAngle - getRotation(), -maxRotationPerTick, maxRotationPerTick);
            }
            this.rotate(toRotate);
        }
        
        @Override
        public void render(Graphics2D g) {
            if (getHost().isSolid) {
                AffineTransform old = g.getTransform();
                VolatileImage toRender = turretShadow.getCurrentVolatileImage();
                Coordinate pixelLocation = getPixelLocation().add(new Coordinate(2,3));
                int renderX = pixelLocation.x - toRender.getWidth() / 2;
                int renderY = pixelLocation.y - toRender.getHeight() / 2;
                g.rotate(Math.toRadians(getRotation()), pixelLocation.x, pixelLocation.y );
                g.drawImage(toRender, renderX, renderY, null);
                g.setTransform(old);
            }
            super.render(g);
        }

        public Sprite getTurretSprite() {
            return team == 0 ? turretSprite : redTurretSprite;
        }

    }
}

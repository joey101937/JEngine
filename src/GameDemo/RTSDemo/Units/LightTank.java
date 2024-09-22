/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sprite;
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

    public static double VISUAL_SCALE = 1.04;

    public static final Sprite hullSprite = new Sprite(SpriteManager.lightTankHull);
    public static final Sprite turretSprite = new Sprite(SpriteManager.lightTankTurret);
    public static final Sprite redHullSprite = new Sprite(greenToRed(SpriteManager.lightTankHull));
    public static final Sprite redTurretSprite = new Sprite(greenToRed(SpriteManager.lightTankTurret));
    public static final Sprite hullShadow = Sprite.generateShadowSprite(SpriteManager.lightTankHull, .5);

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
    }

    @Override
    public void render(Graphics2D g) {
        if (isSolid) {
            AffineTransform old = g.getTransform();
            VolatileImage toRender = hullShadow.getCurrentVolatileImage();
            int renderX = getPixelLocation().x - toRender.getWidth() / 2;
            int renderY = getPixelLocation().y - toRender.getHeight() / 2;
            int shadowOffset = 7;
            g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
            g.drawImage(toRender, renderX, renderY + shadowOffset, null);
            g.setTransform(old);
        }
        super.render(g);
    }

    public Sprite getHullSprite() {
        return team == 0 ? hullSprite : redHullSprite;
    }

    public class LightTankTurret extends SubObject {

        public LightTank hull;

        public LightTankTurret(Coordinate offset, LightTank h) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(getTurretSprite());
            this.hull = h;
        }

        @Override
        public void tick() {
            this.setRotation(hull.getRotation());
        }

        public Sprite getTurretSprite() {
            return team == 0 ? turretSprite : redTurretSprite;
        }

    }
}

package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sprite;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class Truck extends RTSUnit{
    public static double VISUAL_SCALE = .38;
    public static double TRUCK_SPEED = RTSGame.tickAdjust(2.8);
    
    public static Sprite hullSprite = null;
    public static Sprite hullSpriteRed = null;
    public static Sprite hullSpriteDamaged = null;
    public static Sprite hullSpriteDamagedRed = null;
    public static Sprite hullSpriteDestroyed = null;
    public static Sprite hullShadow = null;
    public static Sprite wheelSprite = null;
    
    static {
        initGraphics();
    }
    
    public static void initGraphics() {
        hullSprite = new Sprite(RTSAssetManager.truckHull);
        hullSpriteRed = new Sprite(RTSAssetManager.truckHullRed);
        hullSpriteDamaged = new Sprite(RTSAssetManager.truckHullDamaged);
        hullSpriteDamagedRed = new Sprite(RTSAssetManager.truckHullDamagedRed);
        hullShadow = Sprite.generateShadowSprite(hullSprite.getImage(), .7);
        hullShadow.scaleTo(VISUAL_SCALE);
        hullSprite.applyAlphaEdgeBlurSelf(1);
        hullSpriteRed.applyAlphaEdgeBlurSelf(1);
        hullShadow.applyAlphaEdgeBlurSelf(2);
        
        wheelSprite = new Sprite(RTSAssetManager.truckWheel);
        wheelSprite.applyAlphaEdgeBlurSelf(1);
    }
    
    public final Sprite getHullSprite() {
         if (isRubble) {
            return hullSpriteDestroyed;
        }
        if (currentHealth > maxHealth * .33) {
             return team == 0 ? hullSprite : hullSpriteRed;
        }
        return team == 0 ? hullSpriteDamaged : hullSpriteDamagedRed;
    }
    

    public Truck(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(getHullSprite());
        this.isSolid = true;
        this.addSubObject(new TruckWheel(new Coordinate(-24, -51), this));
        this.addSubObject(new TruckWheel(new Coordinate(24, -51), this));
    }
    
    @Override
    public void tick() {
        super.tick();
        setGraphic(getHullSprite());
    }
    
    @Override
    public void render(Graphics2D g) {
        if (isSolid) {
            drawShadow(g, hullShadow, 5, 9);
        }
        if (isSelected()) {
            drawRubbleProximityIndicators(g);
        }
        super.render(g);
    }
    
    
    @Override
    public int getWidth() {
        return (int)(hullSprite.getWidth() * VISUAL_SCALE);
    }

    @Override
    public int getHeight() {
        return (int)(hullSprite.getHeight()* VISUAL_SCALE);
    }
    
    public static class TruckWheel extends SubObject{
        public Truck hull;
        public Coordinate wheelDesiredLocation = null;
        
        public TruckWheel (Coordinate offset, Truck t) {
            super(offset);
            hull = t;
            wheelDesiredLocation = t.getDesiredLocation();
            this.setGraphic(wheelSprite);
            this.setZLayer(0);
            this.setScale(VISUAL_SCALE);
            this.setRenderBelow(true);
        }
        
        @Override
        public void tick() {
            super.tick();
            if(hull.getDesiredLocation()!= null) {
              this.setRotation(hull.angleFrom(hull.getDesiredLocation()));
            }
        }
        
    }
    
}

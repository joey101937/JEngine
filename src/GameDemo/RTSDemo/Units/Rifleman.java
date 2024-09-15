
package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.io.File;

/**
 *
 * @author guydu
 */
public class Rifleman extends RTSUnit {
    public static double VISUAL_SCALE = .4;
    public static Sprite baseSprite = new Sprite(SpriteManager.infantryLegs);
    public static Sprite shadowSprite = new Sprite(SpriteManager.infantryShadow);
    public static Sequence runningSequence = new Sequence(SpriteManager.infantryLegsRun);
    public static Sequence attackSequence = new Sequence(SpriteManager.infantryRifleFire);
    public static Sequence idleAnimation = new Sequence(SpriteManager.infantryRifleIdle);
    // public static Sequence idleAnimation = new Sequence(greenToRed(SpriteManager.infantryRifleIdle));
    public static SoundEffect attackSound = new SoundEffect(new File(Main.assets + "Sounds/machinegun.au"));
    
    static {
        runningSequence.setFrameDelay(35);
    }
    
    // fields
    public RiflemanTurret turret = new RiflemanTurret(this);
    
    public Rifleman(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(baseSprite);
        this.maxHealth = 20;
        this.addSubObject(this.turret);
        this.setZLayer(1);
        this.isSolid = true;
        this.setBaseSpeed(1.88);
        this.canAttackAir = true;
        this.rotationSpeed = 15;
        this.maxHealth = 30;
        this.currentHealth = 30;
    }
    
    @Override
    public int getWidth() {
       return baseSprite.getWidth() + 10;
    }
    
    @Override
    public int getHeight() {
        return baseSprite.getHeight() + 10;
    }
    
    @Override
    public void tick() {
        super.tick();
        if(this.velocity.y != 0 && !getGraphic().isAnimated()) {
            this.setGraphic(runningSequence.copyMaintainSource());
        }
        if(this.velocity.y == 0 && getGraphic().isAnimated()) {
            this.setGraphic(baseSprite);
        }
    }
    
   @Override
   public void render(Graphics2D g) {
        super.render(g);
        shadowSprite.scaleTo(VISUAL_SCALE);
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = getPixelLocation().x - toRender.getWidth() / 2;
        int renderY = getPixelLocation().y - toRender.getHeight() / 2;
        int shadowOffset = 4;
        g.rotate(Math.toRadians(getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
   }
    
    
    
    public class RiflemanTurret extends SubObject{
        public Rifleman hull;
        
        
        public RiflemanTurret(Rifleman r) {
            super(new Coordinate(0,0));
            this.setScale(VISUAL_SCALE);
            this.hull = r;
            this.setGraphic(idleAnimation);
        }
        
        @Override
        public void tick() {
            super.tick();
            this.setRotation(hull.getRotation());
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SandboxDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.Hitbox;
import Framework.UtilityObjects.Projectile;
import Framework.GraphicalAssets.Sprite;
import Framework.DemoSpriteManager;
import Framework.Stickers.OnceThroughSticker;

/**
 * Simple implementation of projectile.
 * @author Joseph
 */
public class Bullet extends Projectile{
    private static int numBullet = 0;
    
    public Bullet(DCoordinate start, DCoordinate end) {
        super(start,end);
        this.preventOverlap=false;
        this.setGraphic(new Sprite(DemoSpriteManager.up));
        this.setHitbox(new Hitbox(this,0)); //sets this to se a circular hitbox. updateHitbox() method manages radius for us so we set it to 0 by default
        maxRange = 500;
        System.out.println("bullet: " + numBullet++);
    }
    
    
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick){
        if(other instanceof Creature){
            Creature c = (Creature)other;
            c.takeDamage(50); 
            destroy();
        }
    }
    
    //when this is destroyed, cause explosion
    @Override
    public void onDestroy(){
        OnceThroughSticker s = new OnceThroughSticker(getHostGame(), new Sequence(DemoSpriteManager.explosionSequence), this.getPixelLocation());
        s.scaleTo(getScale());
    }
    

}

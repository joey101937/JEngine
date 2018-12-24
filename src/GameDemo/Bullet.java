/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.Projectile;
import Framework.Sprite;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;

/**
 *
 * @author Joseph
 */
public class Bullet extends Projectile{
    
    public Bullet(DCoordinate start, DCoordinate end) {
        super(start,end);
        this.setAnimationFalse(new Sprite(SpriteManager.up));
        this.setHitbox(new Hitbox(this,0)); //sets this to se a circular hitbox. updateHitbox() method manages radius for us so we set it to 0 by default
        maxRange = 500;
    }
    
    
    //when this runs into a creature, deal damage to it then destroy this projectile
    @Override
    public void onCollide(GameObject2 other){
        if(other instanceof Creature){
            Creature c = (Creature)other;
            c.takeDamage(50); 
            destroy();
        }
    }
    
    //when this is destroyed, cause explosion
    @Override
    public void onDestroy(){
        OnceThroughSticker s = new OnceThroughSticker(hostGame, SpriteManager.explosionSequence,this.getPixelLocation());
        s.scaleTo(scale);
    }
    

}

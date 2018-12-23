/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TankDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.Projectile;
import Framework.Sequence;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import GameDemo.Creature;
import java.awt.image.BufferedImage;

/**
 *
 * @author Joseph
 */
public class TankBullet extends Projectile {

    public TankBullet(DCoordinate start, DCoordinate end) {
        super(start, end);
        this.setAnimationTrue(new Sequence(new BufferedImage[]{SpriteManager.bullet}));
        baseSpeed = 30;
        scale = .25;
        this.setHitbox(new Hitbox(this, 0)); //sets this to se a circular hitbox. updateHitbox() method manages radius for us so we set it to 0 by default
        maxRange = 500;
    }


    //when this runs into a creature, deal damage to it then destroy this projectile
    @Override
    public void onCollide(GameObject2 other){
        if(other instanceof Creature){
            Creature c = (Creature)other;
            c.takeDamage(50); 
        }
        destroy();
    }
    
    //when this is destroyed, cause explosion
    @Override
    public void onDestroy(){
        OnceThroughSticker s = new OnceThroughSticker(hostGame, SpriteManager.explosionSequence,this.getPixelLocation());
        s.scaleTo(2);
    }
    
    /**
     * bullets just destroy when they go out of bounds
     */
    @Override
    public void constrainToWorld(){
         if(location.x < hostGame.worldBorder) destroy();
        if(location.y < hostGame.worldBorder) destroy();
        if(location.x > hostGame.worldWidth - hostGame.worldBorder) destroy();
        if(location.y > hostGame.worldHeight - hostGame.worldBorder) destroy();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TankDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GameObject2.MovementType;
import Framework.Sequence;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.Creature;
import java.awt.image.BufferedImage;

/**
 *
 * @author Joseph
 */
public class Tank extends Creature{
    public Turret turret;
    
    
    public Tank(Coordinate c) {
        super(c);
        Sequence sprites = new Sequence(new BufferedImage[]{SpriteManager.tankChasis});
        this.setAnimationTrue(sprites);
        this.movementType = MovementType.RotationBased;
        turret = new Turret(new Coordinate(0,0));
        this.addSubObject(turret);
        scale = .3;
        isSolid = true;
        preventOverlap = true;
        this.maxHealth = 200;//tanks can take 4 shots
        this.currentHealth=maxHealth;
    }
 
    
    public void fire(Coordinate target){
        if(turret.firing || target.distanceFrom(location) < getHeight()*3/5)return;
        turret.onFire(target);
    }
    
    
    @Override
    public void onCollide(GameObject2 other){
        System.out.println("tank collision");
    }
    
    
    
    public class Turret extends SubObject{
        Sequence idleAnimation =  new Sequence(new BufferedImage[]{SpriteManager.tankTurret});
        Sequence fireAnimation = new Sequence(SpriteManager.tankFireAnimation);
        public boolean firing = false;
        
        public Turret(Coordinate offset) {
            super(offset);
            this.setAnimationTrue(idleAnimation);
            scale = .3;
            idleAnimation.scaleTo(scale);
            fireAnimation.scaleTo(scale);
        }
        
        public void onFire(Coordinate target){
            setSequence(fireAnimation);
            firing = true;
            Coordinate muzzelLocation = this.offset.copy();
            muzzelLocation.y-=fireAnimation.frames[0].getHeight()*2/5;
            muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, rotation);
            muzzelLocation.add(getPixelLocation());
            OnceThroughSticker muzzelFlash = new OnceThroughSticker(getHostGame(),SpriteManager.explosionSequence,muzzelLocation);
            muzzelFlash.scaleTo(.75);
            getHostGame().addObject(new TankBullet(muzzelLocation.toDCoordinate(),target.toDCoordinate()));
        }
        
        
        @Override
        public void onAnimationCycle(){
            if(sequence == fireAnimation){
                firing = false;
                setSequence(idleAnimation);
            }
        }
    }
    
    
    
}



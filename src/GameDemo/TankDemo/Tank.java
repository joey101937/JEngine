/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TankDemo;

import Framework.Coordinate;
import Framework.GameObject2.MovementType;
import Framework.Sequence;
import Framework.Sprite;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.Creature;
import java.awt.image.BufferedImage;

/**
 * This is a gank gameobject. Tank class is the chasis
 * @author Joseph
 */
public class Tank extends Creature{
    public Turret turret;
    
    /*
    sets up the tank values
    */
    public Tank(Coordinate c) {
        super(c);
        Sprite chassSprite = new Sprite(SpriteManager.tankChasis);
        this.setAnimationFalse(chassSprite);
        this.movementType = MovementType.RotationBased;
        turret = new Turret(new Coordinate(0,0));
        this.addSubObject(turret);
        scale = .3;
        isSolid = true;
        preventOverlap = true;
        this.maxHealth = 200;//tanks can take 4 shots
        this.currentHealth=maxHealth;
    }
 
    //when a tank tries to fire, it first checks if its turret is still firing. 
    //if not, tell the turret to fire at target location
    public void fire(Coordinate target){
        if(turret.firing || target.distanceFrom(location) < getHeight()*3/5)return;
        turret.onFire(target);
    }

    
    
    public class Turret extends SubObject{
        Sequence fireAnimation = new Sequence(SpriteManager.tankFireAnimation);    //simple recoil animation
        Sprite turretSprite = new Sprite(SpriteManager.tankTurret);                 //simple turret sprite
        
        /*
        this firing boolean is linked to the animation  with the onAnimationCycle
        method below. This means the tank will not fire until the fire animation is
        done playing. 
        */
        public boolean firing = false;
        
        public Turret(Coordinate offset) {
            super(offset);
            this.setAnimationFalse(turretSprite);
            scale = .3;
            turretSprite.scaleTo(scale);
            fireAnimation.scaleTo(scale);
        }
        /*
        fires the gun at the location.
        first, play the firing animation on the gun, then create a small explosion
        effect for the muzzleflash, then create the bullet object and spawn it
        into the game world
        */
        public void onFire(Coordinate target){
            setAnimationTrue(fireAnimation);
            firing = true;
            Coordinate muzzelLocation = this.offset.copy();
            muzzelLocation.y-=fireAnimation.frames[0].getHeight()*2/5;
            muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, rotation);
            muzzelLocation.add(getPixelLocation());
            OnceThroughSticker muzzelFlash = new OnceThroughSticker(getHostGame(),SpriteManager.explosionSequence,muzzelLocation);
            muzzelFlash.scaleTo(.75);
            TankBullet bullet = new TankBullet(muzzelLocation.toDCoordinate(),target.toDCoordinate());
            bullet.shooter=this.getHost();
            getHostGame().addObject(bullet);
        }
        
        /*
        this runs whenever an animation cycle ends.
        here we use it to tell the gank when its ready to fire again and
        also to reset the object back to using the regular turret sprite
        */
        @Override
        public void onAnimationCycle(){
            System.out.println("animation cycle " + name);
            if(sequence == fireAnimation){
                firing = false;
                setAnimationFalse(turretSprite);
            }
        }
    }
    
    
    
}



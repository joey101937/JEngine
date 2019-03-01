/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GameObject2.MovementType;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSUnit;
import java.util.ArrayList;

/**
 * This is a gank gameobject. Tank class is the chasis
 * @author Joseph
 */
public class TankUnit extends RTSUnit{
    public Turret turret;
    private final static double VISUAL_SCALE = .2;
    private Long lastFiredTime = 0L;
    /*
    sets up the tank values
     */
    public TankUnit(Coordinate c) {
        super(c);
        init();
    }

    public TankUnit(int x, int y) {
        super(x, y);
        init();
    }

    private void init() {
        Sprite chassSprite = new Sprite(SpriteManager.tankChasis);
        this.setGraphic(chassSprite);
        this.movementType = MovementType.RotationBased;
        turret = new Turret(new Coordinate(0, 0));
        this.addSubObject(turret);
        setScale(VISUAL_SCALE);
        isSolid = true;
        preventOverlap = true;
        this.maxHealth = 200;//tanks can take 4 shots
        this.currentHealth = maxHealth;
    }

    //when a tank tries to fire, it first checks if its turret is still firing. 
    //if not, tell the turret to fire at target location
    public void fire(Coordinate target) {
        if (turret.firing || target.distanceFrom(location) < getHeight() * 3 / 5 || tickNumber-lastFiredTime < 60L) { //limited to one shot per 60 ticks
            return;
        }else{
            System.out.println("tickNumber " + tickNumber);
        }
        lastFiredTime = this.tickNumber;
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
            this.setGraphic(turretSprite);
            setScale(VISUAL_SCALE);
            turretSprite.scaleTo(getScale());
            fireAnimation.scaleTo(getScale());
        }
        /*
        fires the gun at the location.
        first, play the firing animation on the gun, then create a small explosion
        effect for the muzzleflash, then create the bullet object and spawn it
        into the game world
        */
        public void onFire(Coordinate target){
            setGraphic(fireAnimation); 
            firing = true;
            Coordinate muzzelLocation = new Coordinate(0,0);
            muzzelLocation.y-=fireAnimation.frames[0].getHeight()*2/5;
            muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, getRotation());
            muzzelLocation.add(getPixelLocation());
            //OnceThroughSticker muzzelFlash = new OnceThroughSticker(getHostGame(),SpriteManager.explosionSequence,muzzelLocation);
            //muzzelFlash.scaleTo(.5);
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
            if(getGraphic() == fireAnimation){
                firing = false;
                setGraphic(turretSprite);
            }
        }

        public RTSUnit nearestInRange() {
            if(getHostGame()==null){
                System.out.println("null host game");
                return null;
            }
            double range = 500;
            ArrayList<GameObject2> nearby = getHostGame().getObjectsNearPoint(getPixelLocation(), range);
            double closestDistance = range + 1;
            GameObject2 closest = null;
            if (!nearby.isEmpty()) {
                for (GameObject2 go : nearby) {
                    if (!(go instanceof RTSUnit) || go==this.getHost()) {
                        continue;
                    }
                    if (location.distanceFrom(go.location) < closestDistance) {
                        closestDistance = location.distanceFrom(go.location);
                        closest = go;
                    }
                }
            }
            return (RTSUnit) closest;
        }
        
        @Override
        public void tick(){
            super.tick();
            RTSUnit enemy = nearestInRange();
            if(enemy==null){
                rotateTo(this.getHost().getRotation());
            }else{
                lookAt(enemy);
                Coordinate offset = new Coordinate(Main.generateRandom(-enemy.getWidth()/4, enemy.getWidth()/4),Main.generateRandom(-enemy.getHeight()/4, enemy.getHeight()/4));
                Coordinate targetPoint = enemy.getPixelLocation();
                targetPoint.add(offset);
                ((TankUnit)getHost()).fire(targetPoint);
            }
        }

    }

    @Override
    public void onCollide(GameObject2 other){
        
    }
    
   @Override
   public void onDestroy(){
       OnceThroughSticker deathAni = new OnceThroughSticker(hostGame,SpriteManager.explosionSequence,getPixelLocation());
       deathAni.scale(1.5);
   }
}



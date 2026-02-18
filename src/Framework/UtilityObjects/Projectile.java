/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UtilityObjects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.PathingLayer;


/**
 * gameobject that travels along a trajectory
 * @author Joseph
 */
public class Projectile extends GameObject2{
    public int lifeTime = -1; //how mant ticks before this projectile is destroyed. -1 for infinite
    public double maxRange = -1; //how far this may travel from start before destroyed. -1 for infinite
    private final DCoordinate start; //initial spawn point
    
    /**
     * when overrideing tick method of projectile, always start with super.tick();
     */
    @Override
    public void tick(){
        super.tick();
        if((lifeTime>1 && tickNumber > lifeTime) || (maxRange>1 && getLocationAsOfLastTick().distanceFrom(start) > maxRange)){
            onTimeOut();
            destroy();
        }
    }

    /**
     * runs whenever the projectile dies due to maxRange or lifeTime restrictions
     */
    public void onTimeOut(){
    }
    
    
    private void projectileInit(){
        isSolid = true;
        this.preventOverlap=false;  //by default projectiles do not stop when they hit something
        baseSpeed = 10;
        this.movementType = MovementType.SpeedRatio;
        this.setHitbox(new Hitbox(this,0));//by default, projectiles use circle hitbox
        //projectiles not effected by pathing types
        for(PathingLayer.Type type : this.pathingModifiers.keySet()){
            pathingModifiers.put(type, 1.0);
        }
    }
    
    
    public void launch(DCoordinate destination){
        DCoordinate vel = destination.copy();
        vel.subtract(getLocation());
        this.velocity = vel;
        this.lookAt(destination);
        System.out.println(this.ID + " launching with destination " + destination);
    }
    
    public Projectile(Coordinate start){
        super(start);
        projectileInit();
        this.start = new DCoordinate(start);
    }
    
    public Projectile(DCoordinate start){
        super(start);
        projectileInit();
        this.start = start.copy();
    }
    
    public Projectile(Coordinate start, Coordinate destination) {
        super(start);
        projectileInit();
        launch(new DCoordinate(destination));
        this.start = new DCoordinate(start);
    }
    
    public Projectile(DCoordinate start, DCoordinate destination) {
        super(start);
        projectileInit();
        launch(destination);
        this.start = start.copy();
    }
    
        /**
     * projectiles by default just destroy when they go out of bounds
     */
    @Override
    public void onCollideWorldBorder(DCoordinate l) {
        destroy();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller.Actors;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Hitbox;
import Framework.PathingLayer.Type;
import SampleGames.SideScroller.DamageNumber;
import SampleGames.SideScroller.MinotaurGame;
import SampleGames.SideScroller.Terrain.Terrain;

/**
 *
 * @author Joseph
 */
public abstract class SSActor extends GameObject2 {
    private Action currentAction = Action.Idle;
    public static enum Action {Idle,Walk,PreAttack1,PreAttack2,PostAttack,TakingDamage,Dying};
    public Long jumpTick = 0L; //last tick the character started jumpin at
    
    private int currentHealth = 100,maxHealth =100;
    public boolean isInvuln = false;
    
    public void takeDamage(int i){
        if(getCurrentHealth()>0)getHostGame().addObject(new DamageNumber(i,getPixelLocation()));
        if(i>currentHealth){
            currentHealth = 0;
        }else{
            currentHealth-=i;
        }
        if(currentHealth==0){
           if(getCurrentAction()!=Action.Dying)startDying();
        }
    }
    
    public void startDying(){
        setCurrentAction(Action.Dying);
    }
    
    public void heal(int i ){
        currentHealth+=i;
        if(currentHealth>maxHealth){
            currentHealth=maxHealth;
        }
    }
    
    public double getPercentHealthMissing() {
        return 1 - (double)getCurrentHealth() / getMaxHealth();
    }
    
    
    public int getCurrentHealth(){
        return currentHealth;
    }
    public int getMaxHealth(){
        return maxHealth;
    }
    public void setMaxHealth(int i){
        maxHealth = i;
    }
    public void setCurrentHealth(int i ){
        currentHealth = i;
    }
    
    
    public Action getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
    }
    
    /**
     * checks if terrain under this object is pathable.
     *
     * @return if terrain under this object is pathable
     */
    public boolean isOnGround() {
        for (GameObject2 go : getHostGame().getAllObjects()) {
            if (getHitbox().intersectsIfMoved(go.getHitbox(), new Coordinate(0, (int) velocity.y))) {
                return true;
            }

        }
        if(getHostGame().getPathingLayer()!=null){
            Coordinate c = getPixelLocation();
            c.y+=getHeight()/2;
            if(getHostGame().getPathingLayer().getTypeAt(c) == Type.impass){
                return true;
            }
        }
        return false;
    }

    public boolean freeToAct(){
        return getCurrentAction()==Action.Idle || getCurrentAction() == Action.Walk;
    }
    public SSActor(Coordinate c) {
        super(c);
        init();
    }

    public SSActor(DCoordinate dc) {
        super(dc);
        init();
    }

    public SSActor(int x, int y) {
        super(x, y);
        init();

    }

    private void init() {
        isSolid = true;
        collisionSliding = true;
        this.movementType = GameObject2.MovementType.RawVelocity;
    }

    public void jump(){
        if(isJumping()) return;
        boolean canJump = false;
        Coordinate[] jumpHitboxDims = {        
            new Coordinate(getPixelLocation().x - getWidth()/2, getPixelLocation().y + getHeight()/2), // bot left of sprite
            new Coordinate(getPixelLocation().x + getWidth()/2, getPixelLocation().y + getHeight()/2), // bot right of sprite     
            new Coordinate(getPixelLocation().x - getWidth()/2, getPixelLocation().y + getHeight()/2 + 10), // extend 10px under sprite
            new Coordinate(getPixelLocation().x + getWidth()/2, getPixelLocation().y + getHeight()/2 + 10),
        };
        Hitbox jumpHitbox = new Hitbox(jumpHitboxDims);
        for(GameObject2 other : getHostGame().getObjectsIntersecting(jumpHitbox)) {
            if(other != this && other.isSolid) {
                canJump = true;
                break;
            }
        }
        if(canJump) {
            jumpTick=tickNumber;
        }
    }
    
    
    public boolean isJumping(){
    return 100 - (tickNumber - jumpTick) > 0;
    }
    
    public void adjustVelocityForGravityAndJump() {
        if (isJumping()) {
            velocity.y = -3.5 / 2;
            return;
        } else {
                velocity.y = 2.5 / 2;
        }

    }
    
    /**
     * runs attack action either right or left
     * @param right if the attack is facing right or not.
     */
    public abstract void attack(boolean right);
}

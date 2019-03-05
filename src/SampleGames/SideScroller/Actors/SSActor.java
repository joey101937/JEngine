/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller.Actors;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import SampleGames.SideScroller.DamageNumber;
import SampleGames.SideScroller.SSGame;

/**
 *
 * @author Joseph
 */
public abstract class SSActor extends GameObject2 {
    private Action currentAction = Action.Idle;
    public static enum Action {Idle,Walk,PreAttack,PostAttack,TakingDamage,Dying};
    
    private int currentHealth = 100,maxHealth =100;
    public boolean isInvuln = false;
    
    public void takeDamage(int i){
        getHostGame().addObject(new DamageNumber(i,getPixelLocation()));
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

    @Override
    public void onCollide(GameObject2 other) {
        if (other == SSGame.floor) {
            location.y-=3;
        }
    }
    
    /**
     * runs attack action either right or left
     * @param right if the attack is facing right or not.
     */
    public abstract void attack(boolean right);
}

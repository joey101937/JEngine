/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Minotaur.Actors;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Main;
import GameDemo.Minotaur.MinotaurGame;
import java.util.Collection;

/**
 *
 * @author joey101937
 */
public class NPCMinotaur extends Minotaur{
    
    public int attackInterval = 100; //attack max once per 100 ticks
    
    public int desiredXCoordinate = 0;
    
    public Long lastAttackedTick = 0L;
    
    public NPCMinotaur(Coordinate c) {
        super(c);
        desiredXCoordinate = c.x;
    }
    
    @Override
    public void tick() {
        super.tick();
        Collection<GameObject2> nearbyObjects = this.getHostGame().getObjectsNearPoint(getPixelLocation(), 400);
        nearbyObjects.remove(this);
        if(nearbyObjects.isEmpty() || getCurrentHealth() <= 0) {
            this.velocity.x = 0;
            return;
        }
        for (GameObject2 nearbyObject : nearbyObjects) {
            if(nearbyObject == MinotaurGame.playerMinotaur) {
                // the nearby object is the player's minotaur
                desiredXCoordinate = nearbyObject.getPixelLocation().x; // set our desired location to be where the player is
                if (Coordinate.distanceBetween(this.getPixelLocation(), nearbyObject.getPixelLocation()) < 100) {
                    // if player is right next to this npc, stop moving and attack
                    this.velocity.x = 0;
                    if(freeToAct() && lastAttackedTick + attackInterval < tickNumber) {
                       lastAttackedTick = this.tickNumber;
                       this.attack((nearbyObject.location.x - this.location.x) > 0); // right or left attack based on if their x is farther to right than ours
                    }
                    return;
                }
            }
        }
        this.velocity.x = Main.clamp(desiredXCoordinate - getPixelLocation().x, getBaseSpeed() , -getBaseSpeed() ); // cap speed to 3 left or right
    }
    
}

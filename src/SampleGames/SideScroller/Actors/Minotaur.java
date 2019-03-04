/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller.Actors;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.PathingLayer;
import Framework.SpriteManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Joseph
 */
public class Minotaur extends GameObject2{
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public Long jumpTick = 0L; //last tick the character started jumpin at
    public boolean facingRight = true;
    
    public Minotaur(DCoordinate c) {
        super(c);
        characterSetup();
    }
        
    public Minotaur(Coordinate c){
        super(c);
        characterSetup();
    }
    
    /**
     * set up basics of character
     */
    private void characterSetup(){
        name = "Minotaur " + ID;
        baseSpeed = 3.5;
        setScale(1.5);
        isSolid=true;
        this.movementType = GameObject2.MovementType.RawVelocity;
        //initial animation
        Sequence idleSequenceL = new Sequence(SpriteManager.minotaurIdle_Left);
        Sequence idleSequenceR = new Sequence(SpriteManager.minotaurIdle_Right);
        this.setGraphic(idleSequenceR);
        //add animation sequences
        this.animations.put("IdleL", idleSequenceL);
        this.animations.put("IdleR", idleSequenceR);
        this.animations.put("walkRight", new Sequence(SpriteManager.minotaurRun_Right));
        this.animations.put("walkLeft", new Sequence(SpriteManager.minotaurRun_Left));    
    }
    
    /**
     * updates to correct animation depending on what direction the character is
     * going
     */
    private void updateAnimation() {
        if (velocity.x > 0) {
            this.facingRight=true;
            this.setGraphic(animations.get("walkRight"));
        } else if (velocity.x < 0) {
            this.facingRight=false;
            this.setGraphic(animations.get("walkLeft"));
        } else {
            if(facingRight){
                this.setGraphic(animations.get("IdleR"));
            }else{
                this.setGraphic(animations.get("IdleL"));
            }
            
        }
    }

    public boolean isJumping(){
    return 25 - (tickNumber - jumpTick) > 0;
    }
    
    private void adjustVelocityForGravityAndJump() {
        if(velocity.y>0)velocity.y=0;
        if (isJumping()) {
            velocity.y = -7;
            return;
        }
        if (!isOnGround()) {
            velocity.y = 5;        
            //System.out.println(velocity);
        } else if (velocity.y > 0) {
            velocity.y = 0;
        }
    }

    /**
     * this runs every 'tick' (think update in unity) sets appropriate animation
     * based on velocity
     */
    @Override
    public void tick() {
        adjustVelocityForGravityAndJump();
        if (getCurrentTerrain() == PathingLayer.Type.hostile) {  
            velocity.y -= 3;
        }
        super.tick();
        updateAnimation();
    }

    /**
     * checks if terrain under this object is pathable.
     * @return if terrain under this object is pathable
     */
    public boolean isOnGround() {
        Coordinate c = getPixelLocation().copy();
        c.y+=(getHeight()/2)-5;
        PathingLayer.Type type = getHostGame().getPathingLayer().getTypeAt(c);
        return pathingModifiers.get(type) < .05;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_PathingLayer;

import Framework.GameObject2;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.PathingLayer;
import Framework.GraphicalAssets.Sequence;
import Framework.Hitbox;
import Framework.DemoSpriteManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Example of a game character using GO2 setup
 * @author Joseph
 */
public class SideScrollCharacter extends GameObject2{
    
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public Long jumpTick = -1000L; //last tick the character started jumpin at
    public boolean isOnGround = false;
    
    public SideScrollCharacter(DCoordinate c) {
        super(c);
        characterSetup();
    }
        
    public SideScrollCharacter(Coordinate c){
        super(c);
        characterSetup();
    }
    
    /**
     * set up basics of character
     */
    private void characterSetup(){
        setName("Sample Character");
        baseSpeed = 3.5;
        setScale(.5);
        this.collisionSliding = true;
        isSolid=true;
        this.movementType = GameObject2.MovementType.RawVelocity;
        //initial animation
        Sequence idleSequence = new Sequence(DemoSpriteManager.sampleChar_idle);
        this.setGraphic(idleSequence);
        //add animation sequences
        this.animations.put("Idle", idleSequence);
        this.animations.put("walkUp", new Sequence(DemoSpriteManager.sampleChar_walkUp));
        this.animations.put("walkDown", new Sequence(DemoSpriteManager.sampleChar_walkDown));
        this.animations.put("walkRight", new Sequence(DemoSpriteManager.sampleChar_walkRight));
        this.animations.put("walkLeft", new Sequence(DemoSpriteManager.sampleChar_walkLeft));
        for(String s : animations.keySet()){
            animations.get(s).setFrameDelay(animations.get(s).getFrameDelay()*3);  //slow animation speed by 3x
        }
        this.setHitbox(new Hitbox(this, getWidth()/2));
    }
    
    /**
     * updates to correct animation depending on what direction the character is
     * going
     */
    private void updateAnimation() {
        if (velocity.x > 0) {
            this.setGraphic(animations.get("walkRight"));
        } else if (velocity.x < 0) {
            this.setGraphic(animations.get("walkLeft"));
        } else {
            this.setGraphic(animations.get("Idle"));
        }
    }

    public boolean isJumping(){
        return 50 - (tickNumber - jumpTick) > 0;
    }
    
    private void adjustVelocityForGravityAndJump() {
        if(velocity.y>0)velocity.y=0;
        if (isJumping()) {
            velocity.y = -3.5;
            return;
        } else {
            velocity.y = 2.5;
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
    protected boolean isOnGround() {
        return !this.isNewLocationClearForPathing(getPixelLocation().add(0, 25));
    }
    
   
}

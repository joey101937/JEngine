/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_TERRAIN;

import Framework.GameObject2;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.PathingLayer;
import Framework.GraphicalAssets.Sequence;
import Framework.SpriteManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Example of a game character using GO2 setup
 * @author Joseph
 */
public class SideScrollCharacter extends GameObject2{
    
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public Long jumpTick = 0L; //last tick the character started jumpin at
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
        name = "Sample Character";
        baseSpeed = 3.5;
        scale = .5;
        isSolid=true;
        this.movementType = GameObject2.MovementType.RawVelocity;
        //initial animation
        Sequence idleSequence = new Sequence(SpriteManager.sampleChar_idle);
        this.setAnimationTrue(idleSequence);
        //add animation sequences
        this.animations.put("Idle", idleSequence);
        this.animations.put("walkUp", new Sequence(SpriteManager.sampleChar_walkUp));
        this.animations.put("walkDown", new Sequence(SpriteManager.sampleChar_walkDown));
        this.animations.put("walkRight", new Sequence(SpriteManager.sampleChar_walkRight));
        this.animations.put("walkLeft", new Sequence(SpriteManager.sampleChar_walkLeft));
        for(String s : animations.keySet()){
            animations.get(s).frameDelay*=3;  //slow animation speed by 3x
        }        
    }
    
    /**
     * updates to correct animation depending on what direction the character is
     * going
     */
    private void updateAnimation() {
        if (velocity.x > 0) {
            this.sequence = animations.get("walkRight");
        } else if (velocity.x < 0) {
            this.sequence = animations.get("walkLeft");
        } else {
            this.sequence = animations.get("Idle");
        }
    }

    public boolean isJumping(){
    return 15 - (tickNumber - jumpTick) > 0;
    }
    
    private void adjustVelocityForGravityAndJump() {
        if(velocity.y>0)velocity.y=0;
        if (isJumping()) {
            velocity.y = -11;
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
    protected boolean isOnGround() {
        Coordinate c = getPixelLocation().copy();
        c.y+=(getHeight()/2)-5;
        PathingLayer.Type type = hostGame.getPathingLayer().getTypeAt(c);
        return pathingModifiers.get(type) < .05;
    }
    
   
}

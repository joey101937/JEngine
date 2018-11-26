/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Sequence;
import Framework.SpriteManager;

/**
 * Example of a game character using GO2 setup
 * @author Joseph
 */
public class SampleCharacter extends GameObject2{
    
    public SampleCharacter(DCoordinate c) {
        super(c);
        characterSetup();
    }
    
    public SampleCharacter(Coordinate c){
        super(c);
        characterSetup();
    }
    
    /**
     * set up basics of character
     */
    private void characterSetup(){
        name = "Sample Character";
        baseSpeed = 3.5;
        scale = 2;
        isSolid=true;
        this.movementType = GameObject2.MovementType.SpeedRatio;
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
            animations.get(s).frameDelay*=3;
        }        
    }
    
    /**
     * this runs every 'tick' (think update in unity)
     */
    @Override
    public void tick() {
        tickNumber++;
        updateLocation();
        if (velocity.x > 0) {
            this.sequence = animations.get("walkRight");
        } else if (velocity.x < 0) {
            this.sequence = animations.get("walkLeft");
        } else {
            if(velocity.y > 0){
                this.sequence = animations.get("walkDown");
            }else if(velocity.y < 0){
                this.sequence = animations.get("walkUp");
            }else{
                 this.sequence = animations.get("Idle");
            }
        }
    }

}

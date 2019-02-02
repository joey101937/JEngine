/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SandboxDemo;

import Framework.GameObject2;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.SpriteManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Example of a game character using GO2 setup
 * @author Joseph
 */
public class SampleCharacter extends GameObject2{
    
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    
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
        scale = 1;
        isSolid=true;
        this.movementType = GameObject2.MovementType.SpeedRatio;
        //initial animation
        Sequence idleSequence = new Sequence(SpriteManager.sampleChar_idle);
        this.setGraphic(idleSequence);
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
     * this runs every 'tick' (think update in unity)
     * sets appropriate animation based on velocity
     */
    @Override
    public void tick() {
        super.tick();
        if (velocity.x > 0) {
            this.setGraphic(animations.get("walkRight"));
        } else if (velocity.x < 0) {
            this.setGraphic(animations.get("walkLeft"));
        } else {
            if(velocity.y > 0){
                this.setGraphic(animations.get("walkDown"));
            }else if(velocity.y < 0){
                this.setGraphic(animations.get("walkUp"));
            }else{
                 this.setGraphic(animations.get("Idle"));
            }
        }
    }

}

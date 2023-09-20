/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TownDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.Hitbox;
import Framework.SpriteManager;
import java.util.HashMap;

/**
 *
 * @author guydu
 */
public class TownCharacter extends GameObject2 {
     public HashMap<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    
    public TownCharacter(int x, int y) {
        super(x, y);
        setup();
    }
    
    private void setup() {
        setName("Town Character");
        baseSpeed = 3.5;
        isSolid=true;
        this.setHitbox(new Hitbox(this, 0));
        this.generateDefaultPathingOffsets();
        this.movementType = GameObject2.MovementType.SpeedRatio;
        this.setScale(.25);
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
            animations.get(s).frameDelay=150;  //slow animation speed to 150ms
        }        
    }
    
    @Override
    public void onGameEnter () {
        this.velocity = new DCoordinate(0,0);
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

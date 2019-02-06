/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SandboxDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Hitbox;
import Framework.GraphicalAssets.Sequence;
import Framework.SpriteManager;

/**
 * Example of simple creature implementation
 * @author joey
 */
public class SampleBird extends Creature{
    public static int numBirds = 0;
    
    public SampleBird(Coordinate c) {
        super(c);
        setup();
    }
    public SampleBird(DCoordinate dc) {
        super(dc);
        setup();
    }
    
    private void setup(){
        setGraphic(new Sequence(SpriteManager.birdySequence));
        isSolid=true;
        name = "Bird " + numBirds++;
        //set scale randomly from .1 - 2.1
        this.scale=Math.random()*2 + .1;
        //set this gameobject to use a circular hitbox with a radius of width/2, to increase performance over the default polygonal hitbox
        this.setHitbox(new Hitbox(this,getWidth()/2));
    }
}

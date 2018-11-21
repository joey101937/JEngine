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
 *
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
        setAnimationTrue(new Sequence(SpriteManager.birdySequence));
        setAnimationTrue(new Sequence(SpriteManager.birdySequence));
        isSolid=true;
        name = "Bird " + numBirds++;
        innateRotation = 90;
    }
}

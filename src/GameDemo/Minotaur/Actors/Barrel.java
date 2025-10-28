/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Minotaur.Actors;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.DemoSpriteManager;

/**
 *
 * @author Joseph
 */
public class Barrel extends SSActor{

    public Barrel(Coordinate c) {
        super(c);
        init();
    }

    public Barrel(DCoordinate dc) {
        super(dc);
         init();
    }

    public Barrel(int x, int y) {
        super(x, y);
         init();
    }
    
    @Override
    public void tick(){
        adjustVelocityForGravityAndJump();
        super.tick();
    }
    
    private void init(){
        this.setGraphic(new Sprite(DemoSpriteManager.barrel));
        setScale(2);
        setMaxHealth(1);
        setCurrentHealth(1);
    }
    
    @Override
    public void onAnimationCycle(){
        if(getCurrentAction()==Action.Dying){
            destroy();
        }
    }
    
    @Override
    public void attack(boolean right) {}
    
    @Override
    public void startDying(){
        setCurrentAction(Action.Dying);
        Sequence deathSeq = new Sequence(DemoSpriteManager.barrelDeath);
        deathSeq.setFrameDelay(150);
        this.setGraphic(deathSeq);
    }
}

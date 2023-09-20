/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TownDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.Hitbox;
import Framework.SpriteManager;

/**
 *
 * @author guydu
 */
public class TownBird extends GameObject2{
    
    public TownBird(int x, int y) {
        super(x, y);
        this.setGraphic(new Sequence(SpriteManager.birdySequence));
        this.setScale(.25);
        this.setBaseSpeed(2);
        this.isSolid = true; // needs to be solid to collide with portal
        this.hitbox = new Hitbox(this, 0);
    }
    
}

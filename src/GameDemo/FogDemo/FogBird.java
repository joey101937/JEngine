/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.FogDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.Hitbox;
import Framework.SpriteManager;

/**
 *
 * @author guydu
 */
public class FogBird extends GameObject2{
    
    public FogBird(int x, int y) {
        super(x, y);
        this.setGraphic(new Sequence(SpriteManager.birdySequence));
        this.setScale(1);
        this.setBaseSpeed(2);
        this.isSolid = true; // needs to be solid to collide with portal
        this.hitbox = new Hitbox(this, 0);
        this.velocity.x = 1;
    }
    
}

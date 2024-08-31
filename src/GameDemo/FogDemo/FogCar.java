
package GameDemo.FogDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;

/**
 *
 * @author guydu
 */
public class FogCar extends GameObject2 {
    
    public FogCar(int x, int y) {
        super(x, y);
        this.setGraphic(new Sprite(SpriteManager.car));
        this.setScale(1);
        this.setRotation(90);
        this.setBaseSpeed(2);
        this.velocity.x = 1;
    }
    
}

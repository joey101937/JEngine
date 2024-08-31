
package GameDemo.FogDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;

/**
 *
 * @author guydu
 */
public class FogCar extends GameObject2 {
    // num ticks before getting to top speed
    private double accelleration = Main.ticksPerSecond * 2;
    private double accellerationStage = 0;
    private double maxSpeed = 3;
    
    @Override
    public void tick(){
        double accellarationPercent = accellerationStage / accelleration;
        this.setBaseSpeed(maxSpeed * accellarationPercent);
        if(this.velocity.x != 0 && accellerationStage < accelleration) {
            accellerationStage++;
        } else if (this.velocity.x == 0) {
            // lose accumulated accelleration when stopped
            accellerationStage = 0;
        }
        // fade out audio after the car leaves the screen
        if(!this.isOnScreen() && tickNumber > 1 && tickNumber % 60 == 0) {
            var sound = FogDemo.engineSound;
            if(sound.getVolume() < .5f) {
                sound.setLooping(false);
            } else {
                sound.setVolume(sound.getVolume() - .01f);
            }
        }
    }
    
    public FogCar(int x, int y) {
        super(x, y);
        this.setGraphic(new Sprite(SpriteManager.car));
        this.setScale(1);
        this.setRotation(90);
        this.setBaseSpeed(2);
        this.velocity.x = 1;
    }
    
}


package GameDemo.FogDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.DemoSpriteManager;

/**
 *
 * @author guydu
 */
public class FogCar extends GameObject2 {
    // num ticks before getting to top speed
    private double accelleration = Main.ticksPerSecond * 4;
    private double accellerationStage = 0;
    private double maxSpeed = 12;
    
    @Override
    public void tick(){
        double accellarationPercent = accellerationStage / accelleration;
        this.setBaseSpeed(maxSpeed * accellarationPercent);
        this.movementType = MovementType.RotationBased;
        if(this.velocity.y != 0 && accellerationStage < accelleration) {
            if(accellarationPercent > 40) {
                accellerationStage++;
            }
            if(accellarationPercent > 60) {
                accellerationStage+=5;
            }
            accellerationStage++;
        } else if (this.velocity.y == 0) {
            // lose accumulated accelleration when stopped
            accellerationStage = 0;
        }
        // fade out audio after the car leaves the screen
        if(!this.isOnScreen() && tickNumber > 1 && tickNumber % 30 == 0) {
            var sound = FogDemo.engineSound;
            if(sound.getVolume() < .2f) {
                sound.setLooping(false);
            } else {
                sound.setVolume(sound.getVolume() - .01f);
            }
        }
        
        this.setRotation(getRotation()+.03);
    }
    
    public FogCar(int x, int y) {
        super(x, y);
        this.setGraphic(new Sprite(DemoSpriteManager.car));
        this.setScale(1);
        this.setRotation(90);
        this.setBaseSpeed(2);
        this.velocity.y = -1;
    }
    
}

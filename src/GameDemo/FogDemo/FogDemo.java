
package GameDemo.FogDemo;

import Framework.Audio.SoundEffect;
import Framework.Game;
import Framework.Main;
import Framework.DemoSpriteManager;
import Framework.Window;
import java.io.File;

/**
 *
 * @author guydu
 */
public class FogDemo {
    public static SoundEffect engineSound = new SoundEffect(new File("Assets/Sounds/engineIdle.au"));
    
    public static void main(String[] args) {
        DemoSpriteManager.initialize();
        Game game = new Game(DemoSpriteManager.dirtBGNight);
        if(Game.runningOnSmallerScreen()) {
            Game.scaleForResolutionAspectRatio();
        }
        Main.ticksPerSecond = 120;
        Window.initialize(game);
        
        game.addObject(new FogObject(500, 500));
        game.addObject(new FogObject(000, 500));
        game.addObject(new FogObject(800, 800));
        game.addObject(new FogObject(1000, 500));
        game.addObject(new FogCar(500,500));
        
        
        engineSound.setLooping(true);
        engineSound.setVolume(.85f);
        engineSound.start();
        
    }
}

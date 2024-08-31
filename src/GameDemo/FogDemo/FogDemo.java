
package GameDemo.FogDemo;

import Framework.Audio.SoundEffect;
import Framework.Game;
import Framework.GraphicalAssets.Sequence;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.AnimatedSticker;
import Framework.Window;
import java.io.File;

/**
 *
 * @author guydu
 */
public class FogDemo {
    public static SoundEffect engineSound = new SoundEffect(new File("Assets/Sounds/engineIdle.au"));
    
    public static void main(String[] args) {
        Game game = new Game(SpriteManager.dirtBGNight);
        if(Game.runningOnSmallerScreen()) {
            Game.scaleForResolutionAspectRatio();
        }
        Main.ticksPerSecond = 120;
        Window.initialize(game);
        AnimatedSticker.defaultFrameDelay = -1;
        game.setZoom(1);
        
        
        Sequence fogSequence = new Sequence(SpriteManager.fogSequence);
        
        fogSequence.setFrameDelay(120 + (int)(Math.random() * 60));
        
        System.out.println("got delay of " + fogSequence.getFrameDelay());
        
        game.addObject(new FogObject(500, 500));
        game.addObject(new FogObject(000, 500));
        game.addObject(new FogObject(800, 800));
        game.addObject(new FogObject(1000, 500));
        game.addObject(new FogCar(500,500));
        
        
        engineSound.setLooping(true);
        engineSound.setVolume(.9f);
        engineSound.start();
        
        // Sticker lightSticker = new Sticker(game, SpriteManager.localizedLight, new Coordinate(500, 500), 5000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(500, 500), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(800, 500), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(500, 800), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(000, 000), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(1000, 1000), 50000);
        
    }
}

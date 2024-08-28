/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.FogDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GraphicalAssets.Sequence;
import Framework.SpriteManager;
import Framework.Stickers.AnimatedSticker;
import Framework.Stickers.Sticker;
import Framework.Window;

/**
 *
 * @author guydu
 */
public class FogDemo {
    public static void main(String[] args) {
        Game game = new Game(SpriteManager.dirtBGNight);
        Window.initialize(game);
        AnimatedSticker.defaultFrameDelay = -1;
        
        
        Sequence fogSequence = new Sequence(SpriteManager.fogSequence);
        
        fogSequence.setFrameDelay(120 + (int)(Math.random() * 60));
        
        System.out.println("got delay of " + fogSequence.getFrameDelay());
        
        game.addObject(new FogObject(500, 500));
        game.addObject(new FogObject(000, 500));
        game.addObject(new FogObject(800, 800));
        game.addObject(new FogObject(1000, 500));
        game.addObject(new FogBird(500,500));
        
        
        // Sticker lightSticker = new Sticker(game, SpriteManager.localizedLight, new Coordinate(500, 500), 5000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(500, 500), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(800, 500), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(500, 800), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(000, 000), 50000);
//        new AnimatedSticker(game, fogSequence, new Coordinate(1000, 1000), 50000);
        
    }
}

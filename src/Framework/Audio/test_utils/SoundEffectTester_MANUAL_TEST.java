/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio.test_utils;

import Framework.Audio.SoundEffect;
import Framework.Game;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.DemoSpriteManager;
import java.io.File;

/**
 *
 * @author Joseph
 */
public class SoundEffectTester_MANUAL_TEST {
    /**
     * used for testing purposes
     * @param args 
     */
    public static void main(String[] args) {
        SoundEffect effect = new SoundEffect(new File(Main.assets+"/Sounds/A-few-jumps-away-by-Arthur-Vyncke.au"));
        effect.start();
        effect.setLooping(true);
        Main.wait(2000);
        System.out.println("pausing");
        effect.pause();
        Main.wait(2000);
        System.out.println("resuming");
        effect.resume();
        Main.wait(2000);
        System.out.println("pausing");
        effect.pause();
        Main.wait(2000);
        System.out.println("resuming");
        effect.resume();
        Main.wait(2000);
        Game g = new Game(new Sprite(DemoSpriteManager.spaceBG));
        effect.linkToGame(g);
        System.out.println("game pausing");
        g.setPaused(true);
        //effect.onGamePause(true);
        Main.wait(2000);
        System.out.println("game resuming");
        g.setPaused(false);
        //effect.onGamePause(false);
        Main.wait(2000);
        System.out.println("game and direct pausing");
        g.setPaused(true);
        effect.onGamePaused(true);
        effect.pause();
        Main.wait(2000);
        System.out.println("ungamepausing");
        g.setPaused(false);
        Main.wait(2000);
        System.out.println("direct unpausing");
        effect.resume();
        Main.display("press ok to restart song");
        effect.restart();
    }
}

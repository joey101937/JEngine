/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.AudioDemo;

import Framework.Audio.SoundEffect;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Window;
import java.io.File;

/**
 * Example of global audio(music) and local audio (blasts)
 * @author Joseph
 */
public class AudioDemo {
   protected static Game game1;
   protected static Game game2;
    
    public static void main(String[] args) {
        //set up the games 
        game1 = new Game(SpriteManager.spaceBG);
        game1.setInputHandler(new AudioDemoInputHandler());
        game2 = new Game(SpriteManager.dirtBG);
        game2.setInputHandler(new AudioDemoInputHandler());
        //start up the window
        Window.initialize(game1);
        //get file we want to use for global music
        File musicFile = new File(Main.assets+"/Sounds/Music.au");
        //create soundeffect with our music file
        SoundEffect globalSound = new SoundEffect(musicFile);
        /*
        This sound effect is going to be global, across both games so we will
        opt to not link it to either game. Additionally, we will have this sound
        loop and lower the volume of it
        */
        globalSound.setLooping(true); //looping
        globalSound.setVolume(.7f);   //70% volume
        globalSound.start(); //starts the sound
    }
}

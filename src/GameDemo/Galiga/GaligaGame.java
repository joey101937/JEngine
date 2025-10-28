/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga;

import Framework.Audio.SoundEffect;
import Framework.DCoordinate;
import Framework.Game;
import Framework.Main;
import Framework.DemoSpriteManager;
import Framework.Window;
import java.io.File;

/**
 *
 * @author Joseph
 */
public class GaligaGame {
    
    public static PlayerShip player;
    public static Game mainGame; 
    public static SoundEffect deathSound = new SoundEffect(new File("Assets/Sounds/blast1.au"));
    public static SoundEffect pewSound = new SoundEffect(new File("Assets/Sounds/pew.au"));
    public static SoundEffect bgMusic = new SoundEffect(new File("Assets/Sounds/A-few-jumps-away-by-Arthur-Vyncke.au"));
    public static GaligaUI UI = new GaligaUI();
    
    public static void main(String[] args) {
        DemoSpriteManager.initialize();
        Main.ticksPerSecond = 60;
        Main.enableLerping = true;
        mainGame = new Game(DemoSpriteManager.spaceBG);
        mainGame.setName("Galiga Game");
        Window.initialize(mainGame);
        DCoordinate spawnPoint = new DCoordinate(mainGame.getWorldWidth()/2,mainGame.getWorldHeight()-150);
        player = new PlayerShip(spawnPoint);
        mainGame.addObject(player);
        mainGame.setInputHandler(new GaligaInput());
        mainGame.addIndependentEffect(UI);
        mainGame.addIndependentEffect(new GameDriver());
        bgMusic.setVolume(.75f);
        bgMusic.setLooping(true);
        bgMusic.start();
    }
   
}

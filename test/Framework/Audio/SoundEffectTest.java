/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import java.io.File;
import java.util.Objects;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joseph
 */
public class SoundEffectTest {
    Game game;
    SoundEffect effect;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        game = new Game(SpriteManager.spaceBG);
        effect = new SoundEffect(new File(Main.assets+"/Sounds/Music.au"));
    }
    
    @After
    public void tearDown() {
        game = null;
        effect = null;
    }

    @Test
    public void nullSourceTest(){
        boolean caught = false;
        try{
           SoundEffect sound = new SoundEffect(null); 
        }catch(RuntimeException e){
            caught = true;
        }
        if(!caught)fail();
    }
    
    @Test
    public void directPause(){
        effect.start();
        assert effect.isPaused()==false;
        effect.pause();
        assert effect.isPaused()==true;
        effect.resume();
        assert effect.isPaused()==false;
        effect.pause();
        assert effect.isPaused()==true;
        effect.resume();
    }

    @Test
    public void linkToGame(){
        effect.start();
        assert effect.getHostGame()==null;
        effect.linkToGame(game);
        assert effect.getHostGame()==game;
        effect.linkToGame(null);
        assert effect.getHostGame() == null;
        effect.linkToGame(game);
        assert effect.getHostGame()==game;
        assert game.audioManager.getAllSounds().size()==1;
    }
    
    @Test
    public void pauseViaGame(){
        effect.start();
        effect.linkToGame(game);
        game.setPaused(true);
        assert game.isPaused();
        assert effect.isPaused();
        game.setPaused(false);
        assert game.isPaused()==false;
        assert effect.isPaused()==false;
        game.setPaused(true);
        assert game.isPaused();
        assert effect.isPaused();
        game.setPaused(false);
        assert game.isPaused()==false;
        assert effect.isPaused()==false;      
    }
    
    @Test
    public void releaseLockOnLinkToNull() {
        effect.start();
        effect.linkToGame(game);
        game.setPaused(true);
        assert game.isPaused();
        effect.linkToGame(null);
        assert effect.isPaused() == false;
    }
    
    @Test
    public void removeOnDisable(){
        effect.start();
        effect.linkToGame(game);
        effect.disable();
        assert effect.isDisabled();
        assert effect.getHostGame() == null;
        assert game.audioManager.getAllSounds().size()==0;
    }
    
    @Test
    public void testUtilities(){
        assert effect.getPercentDone() == 0;
        effect.setVolume(0);
        assert effect.getVolume()==0;
        effect.setVolume(1);
        assert effect.getVolume()==1;
        effect.setVolume(.5f);
        assert effect.getVolume()==.5;
        assert Objects.equals(effect.createCopy().getSoundLength(), effect.getSoundLength());
        assert effect.isLooping()==false;
        assert effect.getMicroPosition() == 0;
    }
}

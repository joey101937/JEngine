/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

import Framework.Main;
import java.io.File;
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
    
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void nullSourceError(){
        boolean caught = false;
        try{
           SoundEffect sound = new SoundEffect(null); 
        }catch(RuntimeException e){
            caught = true;
        }
        if(!caught)fail();
    }
    
    @Test
    public void playTest(){
        SoundEffect effect = new SoundEffect(new File(Main.assets+"/Sounds/machinegun.au"));
        effect.start();
        effect.setLooping(true);
        Main.wait(3000);
        effect.pause();
        Main.wait(3000);
        effect.resume();
        while(effect.getThread().isAlive()){
            Main.wait(10);
        }
        
    }
    
}

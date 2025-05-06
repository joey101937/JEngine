/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

import Framework.Game;
import Framework.Main;
import Framework.Window;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Joseph
 */
public class SoundEffectTest2 {
    Game game;
    SoundEffect effect;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        game = new Game(ImageIO.read(Window.class.getResource("/Resources/JEngineIcon.png")));
       // effect = new SoundEffect(new File(Main.assets+"/Sounds/A-few-jumps-away-by-Arthur-Vyncke.au"));
       effect = new SoundEffect(new File(Main.assets+"/Sounds/pew.au"));
    }
    
    @After
    public void tearDown() {
        game = null;
        effect = null;
    }
    
    @Test
    public void testUtilities(){
        System.out.println("starting");
        effect.createCopy().start();
        Main.wait(200);
        effect.restart();
        Main.wait(3000);
//       AsyncPlayer p1 = effect.playCopyAsync(.9f);
//        System.out.println("p1 created" + (p1 != null));
//       Main.wait(1000);
//        AsyncPlayer p2 = effect.playCopyAsync(.9f);
//         System.out.println("p2 created" + (p2 != null));
//       Main.wait(1000);
//        AsyncPlayer p3 = effect.playCopyAsync(.9f);
//         System.out.println("p3 created" + (p3 != null));
//       Main.wait(1000);
//        AsyncPlayer p4 = effect.playCopyAsync(.9f);
//         System.out.println("p4 created" + (p4 != null));
//       Main.wait(8000);
//        System.out.println(p1 != null && p1.mySound.running);
//        System.out.println(p2 != null && p2.mySound.running);
//        System.out.println(p3 != null && p3.mySound.running);
//        System.out.println(p4 != null && p4.mySound.running);
//        System.out.println(p1 != null ? p1.mySound.getMicroPosition(): "null");
//        System.out.println(p2 != null ? p2.mySound.getMicroPosition() : "null");

    }
}

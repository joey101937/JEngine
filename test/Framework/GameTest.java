/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import org.junit.Test;

/**
 *
 * @author Joseph
 */
public class GameTest {  
    
    @Test
    public void game_window_test(){
        Game g = new Game(SpriteManager.spaceBG);
        assert g.backgroundImage == SpriteManager.spaceBG;
        Window.initialize(g);
        assert Window.currentGame == g;
        assert Window.frame.getWidth() == g.windowWidth;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author Joseph
 */
public class RTSGame {
    public static Game game = new Game(SpriteManager.dirtBG);
    
    public static void main(String[] args) {
        Window.initialize(game);
        game.setInputHandler(new RTSInput());
        game.getCamera().camSpeed=20;
        game.addIndependentEffect(new SelectionBoxEffect());
        
        for (int i = 0; i < 10; i++) {
            game.addObject(new TankUnit(100 + (i * 200), 200, 0));
        }
        for (int i = 0; i < 10; i++) {
            game.addObject(new TankUnit(100 + (i * 200), 800, 1));
        }
    }
}

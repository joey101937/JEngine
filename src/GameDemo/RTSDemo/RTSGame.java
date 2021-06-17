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
//        TankUnit testUnit = new TankUnit(200,200);
//        TankUnit testUnit2 = new TankUnit(800,200);
//        TankUnit testUnit3 = new TankUnit(800,800);
//        game.addObject(testUnit);
//        game.addObject(testUnit2);
//        game.addObject(testUnit3);

        for (int i = 0; i < 10; i ++) {
            TankUnit testTeam2 = new TankUnit(1000 + (i * 150), 400, 2);
            game.addObject(testTeam2);
        }
        
         for (int i = 0; i < 10; i ++) {
            TankUnit testTeam1 = new TankUnit(1000 + (i * 150), 1000, 1);
            game.addObject(testTeam1);
        }
       
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import Framework.DCoordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Window;

/**
 *
 * @author Joseph
 */
public class GaligaGame {
    
    public static void main(String[] args) {
        Game mainGame = new Game(SpriteManager.spaceBG);
        Window.initialize(mainGame);
        Main.debugMode=true;
        DCoordinate spawnPoint = new DCoordinate(mainGame.getWorldWidth()/2,mainGame.getWorldHeight()-150);
        PlayerShip player = new PlayerShip(spawnPoint);
        mainGame.addObject(player);
    }
   
}

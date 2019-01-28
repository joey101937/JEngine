/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.PlatformerDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Window;

/**
 *
 * @author Joseph
 */
public class PlatformerGame {
    
    protected static PlatformCharacter playerCharacter = new PlatformCharacter(new Coordinate(100,100));
 
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.platformBG);
        g.setPathingLayer(SpriteManager.platformPathing);
        g.start();
        Window.initialize(g);
        g.addObject(playerCharacter);
        g.setInputHandler(new PlatformerInput());
        g.requestFocus();
        g.worldBorder=0;
        Main.debugMode=true;
    }
}

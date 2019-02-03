/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_TERRAIN;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Window;

/**
 *
 * @author Joseph
 */
public class SideScrollGame {
    
    protected static SideScrollCharacter playerCharacter = new SideScrollCharacter(new Coordinate(100,100));
 
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.platformBG);
        g.setPathingLayer(SpriteManager.platformPathing);
        g.start();
        Window.initialize(g);
        g.addObject(playerCharacter);
        g.setInputHandler(new SideScollInput());
        g.requestFocus();
        g.worldBorder=0;
        //Main.debugMode=true;
    }
}

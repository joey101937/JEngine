/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_PathingLayer;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.UtilityObjects.TextObject;
import Framework.Window;

/**
 *
 * @author Joseph
 */
public class SideScrollDemo {
    
    protected static SideScrollCharacter playerCharacter;
 
    public static void main(String[] args) {
        SpriteManager.initialize();
        playerCharacter = new SideScrollCharacter(new Coordinate(100,200));
        Main.ticksPerSecond = 120;
        Game g = new Game(SpriteManager.platformBG);
        g.setPathingLayer(SpriteManager.platformPathing);
        Window.initialize(g);
        g.addObject(playerCharacter);
        g.setInputHandler(new SideScollInput());
        g.requestFocus();
        g.worldBorder=20;
        g.getCamera().setTarget(playerCharacter);
        g.addObject(new SidescrollerBird(g.getWorldWidth() - g.worldBorder, 100));
        g.addObject(new TextObject(0, 0, "Press x to toggle debug view. A/D/Space to move" ));
        playerCharacter.generateDefaultPathingOffsets();
    }
}

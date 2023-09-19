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
import java.awt.Dimension;

/**
 *
 * @author Joseph
 */
public class SideScrollDemo {
    
    protected static SideScrollCharacter playerCharacter = new SideScrollCharacter(new Coordinate(100,200));
 
    public static void main(String[] args) {
        Main.ticksPerSecond = 120;
        Game g = new Game(SpriteManager.platformBG);
        g.setPathingLayer(SpriteManager.platformPathing);
        g.start();
        Window.initialize(g);
        g.addObject(playerCharacter);
        g.setInputHandler(new SideScollInput());
        g.requestFocus();
        g.worldBorder=20;
        g.getCamera().setTarget(playerCharacter);
        g.addObject(new SidescrollerBird(g.getWorldWidth() - g.worldBorder, 100));
        Game.NATIVE_RESOLUTION = new Dimension(1920, 1080);
        Game.scaleForResolutionAspectRatio();
        playerCharacter.generateDefaultPathingOffsets();
    }
}

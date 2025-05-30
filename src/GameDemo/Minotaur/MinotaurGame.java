/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Minotaur;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.UtilityObjects.BlockObject;
import Framework.Window;
import GameDemo.Minotaur.Actors.Minotaur;
import GameDemo.Minotaur.Levels.SSLevel1;

/**
 * Sidescroll Game main class
 * @author Joseph
 */
public class MinotaurGame {
    public static Game scene1;
    public static BlockObject floor;
    public static Minotaur playerMinotaur;
    
    
    
    public static void main(String[] args) {
        Main.enableLerping = false;
        SpriteManager.initialize();
        scene1 = new SSLevel1();
        playerMinotaur = new Minotaur(new Coordinate(100, 0));
        Main.ticksPerSecond = 240; // high ticks per second for more responsiveness
        scene1.worldBorder = 30;
        scene1.addObject(playerMinotaur);
        playerMinotaur.setMaxHealth(300);
        playerMinotaur.setCurrentHealth(300);
        scene1.getCamera().setTarget(playerMinotaur);
        scene1.setInputHandler(new MinotaurInput());
        Window.initialize(scene1);
        // Game.scaleForResolutionAspectRatio();
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.Coordinate;
import Framework.Game;
import Framework.UtilityObjects.BlockObject;
import Framework.Window;
import SampleGames.SideScroller.Actors.Minotaur;
import SampleGames.SideScroller.Levels.SSLevel1;

/**
 * Sidescroll Game main class
 * @author Joseph
 */
public class MinotaurGame {
    public static Game scene1 = new SSLevel1();
    public static BlockObject floor;
    public static Minotaur playerMinotaur = new Minotaur(new Coordinate(100,700));
    
    
    
    public static void main(String[] args) {
        Window.initialize(scene1);
        scene1.worldBorder = 30;
        scene1.addObject(playerMinotaur);
        playerMinotaur.setMaxHealth(300);
        playerMinotaur.setCurrentHealth(300);
        scene1.getCamera().setTarget(playerMinotaur);
        scene1.setInputHandler(new MinotaurInput());
        Game.scaleForResolutionAspectRatio();
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.UtilityObjects.BlockObject;
import Framework.Window;
import SampleGames.SideScroller.Actors.Minotaur;
import SampleGames.SideScroller.Levels.SSLevel1;

/**
 * Sidescroll Game main class
 * @author Joseph
 */
public class SSGame {
    public static Game scene1 = new SSLevel1();
    public static BlockObject floor;
    public static Minotaur minotaur = new Minotaur(new Coordinate(100,100));
    
    
    
    public static void main(String[] args) {
        Window.initialize(scene1);
        //scene1.setZoom(1.15);
        scene1.worldBorder = 30;
        scene1.addObject(minotaur);
        scene1.getCamera().setTarget(minotaur);
        scene1.setInputHandler(new SSInput());
    }
}

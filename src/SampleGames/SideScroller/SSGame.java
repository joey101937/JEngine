/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Window;
import SampleGames.SideScroller.Actors.Minotaur;

/**
 * Sidescroll Game main class
 * @author Joseph
 */
public class SSGame {
    public static Game scene1 =new Game(SpriteManager.platformBG);
    public static Minotaur minotaur = new Minotaur(new Coordinate(100,100));
    public static void main(String[] args) {
        Window.initialize(scene1);
        //scene1.setZoom(1.15);
        scene1.worldBorder = 30;
        scene1.setPathingLayer(SpriteManager.platformPathing);
        scene1.addObject(minotaur);
        scene1.getCamera().setTarget(minotaur);
        scene1.setInputHandler(new SSInput());
        //Main.debugMode=true;
    }
}

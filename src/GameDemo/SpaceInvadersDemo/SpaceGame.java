/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.*;

/**
 * This example game demonstrates moving objects between scenes, and using mouse
 * input to make objects move around the scene.
 * 
 * This game creates a Spaceship object that will follow the mouse around on
 * on screen using rotation based movement. Pressing G will swap between a small
 * space themed world and a large dirt-terrain themed world.
 * 
 * Clicking the mouse will create an explosion effect at the point of click
 * 
 * Pressing G will swap between scenes
 * Pressing X will bring up options menu
 * @author Joseph
 */
public class SpaceGame {

    public static Spaceship ship;
    protected static Game spaceGame = new Game(SpriteManager.spaceBG);
    protected static Game dirtGame = new Game(SpriteManager.dirtBG);

    
    public static void main(String[] args) {
        spaceGame.name = "space game";
        dirtGame.name = "dirt game";
        Game g = spaceGame;
        Window.initialize(g);
        g.setInputHandler(new SpaceInputHandler());
        ship = new Spaceship(new Coordinate(100,100));
        g.addObject(ship);
    }
}

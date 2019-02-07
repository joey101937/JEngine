/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceDemo;

import Framework.*;
import Framework.UI_Elements.Examples.Button;
import Framework.UtilityObjects.BlockObject;
import Framework.UtilityObjects.TextObject;
import java.awt.Color;

/**
 * This example game demonstrates moving objects between scenes, and using mouse
 * input to make objects move around the scene.
 * 
 * This game creates a Spaceship object that will follow the mouse around on
 * on screen using rotation based movement. Pressing G will swap between the two
 * worlds, firstGame and secondGame.
 * 
 * Clicking the mouse will create an explosion effect at the point of click
 * 
 * Pressing G will swap between scenes
 * Pressing X will bring up options menu
 * @author Joseph
 */
public class SpaceGame {

    public static Spaceship ship;
    protected static Game firstGame = new Game(SpriteManager.spaceBG);
    protected static Game secondGame = new Game(SpriteManager.spaceBG2);

    
    public static void main(String[] args) {
        Game.scaleForResolutionAspectRatio();
        firstGame.name = "First game";
        secondGame.name = "Second game";
        Window.initialize(firstGame);
        firstGame.setInputHandler(new SpaceInputHandler());
        secondGame.setInputHandler(new SpaceInputHandler());
        ship = new Spaceship(new Coordinate(100,100));
        firstGame.addObject(ship);
        //creates and adds text object
        TextObject text = new TextObject(50,300,"Sample Text Sample Text\nSample Text Sample Text\nSample Text Sample Text");
        text.setColor(Color.green);
        text.setZLayer(2); //text is on higher z-layer (2>1) so it renders on top
        firstGame.addObject(text);
        //creates and adds block object
        BlockObject block = new BlockObject(new Coordinate(500,400),100,200);
        firstGame.addObject(block);
        Main.debugMode = true;
        Button b = new Button(firstGame,new Coordinate(500,100));
        Window.addUIElement(b);
    }
}

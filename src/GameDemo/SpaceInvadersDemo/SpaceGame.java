/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.UI_Elements.OptionsMenu;
import Framework.*;

/**
 *
 * @author Joseph
 */
public class SpaceGame {
    public static Spaceship ship;
    
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.spaceBG);
        Window.initialize(g);
        g.start();
        g.setInputHandler(new SpaceInputHandler());
        OptionsMenu.display();
        
        
        ship = new Spaceship(new Coordinate(100,100));
        g.addObject(ship);
    }
}

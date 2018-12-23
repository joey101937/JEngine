/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TankDemo;

import Framework.Game;
import Framework.Window;
import GameDemo.DemoInputHandler;

/**
 *
 * @author Joseph
 */
public class TankGame {
    public static void main(String[] args) {
        Game g = new Game();
        Window window = new Window(g);
        g.setInputHandler(new TankInputHandler());
        g.start();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.PlatformerDemo;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;

/**
 *
 * @author Joseph
 */
public class PlatformerGame {
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.platformBG);
        g.setPathingLayer(SpriteManager.platformPathing);
        g.start();
        Window.initialize(g);
    }
}

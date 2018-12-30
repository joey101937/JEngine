/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.*;
import GameDemo.SandboxDemo.DemoInputHandler;

/**
 *
 * @author Joseph
 */
public class SpaceGame {
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.spaceBG);
        Window window = new Window(g);
        for(GameObject2 go : g.getAllObjects()){
            g.removeObject(go);
        }
        g.start();
        g.setInputHandler(new SpaceInputHandler());
    }
}

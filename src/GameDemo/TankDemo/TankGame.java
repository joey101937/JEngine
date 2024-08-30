/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TankDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Window;

/**
 *
 * @author Joseph
 */
public class TankGame {
    
    public static Tank player = new Tank(new Coordinate(1000,500));
    public static Tank otherTank = new Tank(new Coordinate(2000,1000));
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.dirtBG);
        g.name = "Tank Game";
        Window.initialize(g);
        g.setInputHandler(new TankInputHandler());
        g.addObject(player);
        g.addObject(otherTank);
        g.getCamera().setTarget(player);
        Game.scaleForResolution();
        g.start();
        Main.display("Press X for options menu");
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.Handler;
import Framework.Main;
import Framework.SpriteManager;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 *
 * @author Joseph
 */
public class RTSGame {
    public static Game game = new Game(SpriteManager.dirtBG);
    public static Minimap minimap = new Minimap(game, new Coordinate(0,0));
    public static MinimapButton button = new MinimapButton(game, new Coordinate(0, minimap.getHeight()));
    
    public static void main(String[] args) {
        Main.tickType = Handler.TickType.unified;
        Window.initialize(game);
        game.setInputHandler(new RTSInput());
        game.getCamera().camSpeed=20;
        game.addIndependentEffect(new SelectionBoxEffect());
        Window.addUIElement(minimap);
        Window.addUIElement(button);
        
        for (int i = 0; i < 30; i++) {
            game.addObject(new TankUnit(100 + (i * 120), 800, 0));
        }
        for (int i = 0; i < 30; i++) {
            game.addObject(new TankUnit(100 + (i * 120), 1500, 1));
        }
    }
}

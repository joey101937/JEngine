/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;

/**
 *
 * @author Joseph
 */
public class PlayerShip extends GameObject2{

    public PlayerShip(Coordinate c) {
        super(c);
        
    }

    public PlayerShip(DCoordinate dc) {
        super(dc);
        initialize();
    }

    public PlayerShip(int x, int y) {
        super(x, y);
        initialize();        
    }
    
    private void initialize(){
        Sprite s = new Sprite(SpriteManager.spaceship);
        setGraphic(s);
        scale(.333);
        isSolid=true;
        name = "Player";
    }
}

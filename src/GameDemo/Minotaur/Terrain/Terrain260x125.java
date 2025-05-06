/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Minotaur.Terrain;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;

/**
 *
 * @author Joseph
 */
public class Terrain260x125 extends GameObject2 implements Terrain{

    public Terrain260x125(Coordinate c) {
        super(c);
        init();
    }

    public Terrain260x125(DCoordinate dc) {
        super(dc);
        init();
    }

    public Terrain260x125(int x, int y) {
        super(x, y);
        init();
    }
    
    private void init(){
        this.setGraphic(new Sprite(SpriteManager.terrain260x125));
        isSolid = true;
        preventOverlap=true;
        setName("Terrain260x125");
    }
}

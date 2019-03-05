/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller.Levels;

import Framework.Coordinate;
import Framework.Game;
import Framework.SpriteManager;
import Framework.UtilityObjects.BlockObject;
import SampleGames.SideScroller.Actors.Minotaur;
import SampleGames.SideScroller.SSGame;
import SampleGames.SideScroller.Terrain.Terrain260x125;

/**
 *
 * @author Joseph
 */
public class SSLevel1 extends Game{
    
    public SSLevel1() {
        super(SpriteManager.SSBackground);
        setup();
    }

    private void setup() {
        setupFloor();
        Terrain260x125 block1 = new Terrain260x125(1000, (690 - 125 / 2));
        addObject(block1);
        addObject(new Minotaur(new Coordinate(800, 100)));
        addObject(new Minotaur(new Coordinate(1200, 100)));
        addObject(new Minotaur(new Coordinate(1400, 100)));

    }

    private void setupFloor() {
        SSGame.floor = new BlockObject(new Coordinate(0, 690), getWorldWidth(), getWorldHeight() - 690);
        SSGame.floor.setCentered(false);
        SSGame.floor.isInvisible = true;
        addObject(SSGame.floor);
    }

}

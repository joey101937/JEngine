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
import Framework.UtilityObjects.TextObject;
import SampleGames.SideScroller.Actors.Barrel;
import SampleGames.SideScroller.Actors.Minotaur;
import SampleGames.SideScroller.SSGame;
import SampleGames.SideScroller.Terrain.Terrain260x125;
import java.awt.Color;

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
        TextObject ADText = new TextObject(new Coordinate(200,600),"A and D to Move");
        ADText.setColor(Color.black);
        addObject(ADText);
        TextObject jumpText = new TextObject(new Coordinate(900, 500), "Spacebar to Jump");
        jumpText.setColor(Color.black);
        addObject(jumpText);
        TextObject attackText = new TextObject(new Coordinate(1500, 300), "Click to Attack");
        attackText.setColor(Color.black);
        addObject(attackText);
        addObject(new Barrel(new Coordinate(1600, 100)));
        addObject(new Barrel(new Coordinate(1600, 200)));
        addObject(new Barrel(new Coordinate(1600, 300)));
        addObject(new Barrel(new Coordinate(1600, 400)));
        addObject(new Barrel(new Coordinate(1600, 500)));
        addObject(new Barrel(new Coordinate(1660, 500)));
        
        addObject(new Minotaur(new Coordinate(2000, 100)));
        addObject(new Minotaur(new Coordinate(2200, 100)));
        addObject(new Minotaur(new Coordinate(2400, 100)));
    }

    private void setupFloor() {
        SSGame.floor = new BlockObject(new Coordinate(0, 690), getWorldWidth(), getWorldHeight() - 690);
        SSGame.floor.setCentered(false);
        SSGame.floor.isInvisible = true;
        addObject(SSGame.floor);
    }

}

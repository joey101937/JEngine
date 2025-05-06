/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Minotaur.Levels;

import Framework.Coordinate;
import Framework.Game;
import Framework.SpriteManager;
import Framework.UtilityObjects.BlockObject;
import Framework.UtilityObjects.TextObject;
import GameDemo.Minotaur.Actors.Barrel;
import GameDemo.Minotaur.Actors.NPCMinotaur;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurHealthBarEffect;
import GameDemo.Minotaur.Terrain.Terrain260x125;
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
        addIndependentEffect(new MinotaurHealthBarEffect(this));
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
        
        for(int i = 0; i < 10; i++) {
            addObject(new NPCMinotaur(new Coordinate(2000 + (450 * i), 200)));
        }
    }

    private void setupFloor() {
        MinotaurGame.floor = new BlockObject(new Coordinate(0, 690), getWorldWidth(), getWorldHeight() - 690);
        MinotaurGame.floor.setCentered(false);
        MinotaurGame.floor.isInvisible = true;
        addObject(MinotaurGame.floor);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects;

import Framework.Main;
import java.awt.image.BufferedImage;

/**
 *
 * @author Joseph
 */
public class Player extends GameObject{

    public Player(int x, int y) {
        super(x, y);
        name = "Player";
        speed = 4;
        spritesR = new BufferedImage[9];
        spritesL = new BufferedImage[9];
        spritesIdle = new BufferedImage[9];
        for (int i = 0; i < 9; i++) {
            spritesR[i] = Main.loadSprite("birdies" + i + ".png"); //populates the right direction sprites with bird flying sprite
            spritesIdle[i] = Main.loadSprite("birdies" + i + ".png"); //populates idle sprite list
            spritesL[i] = Main.loadSprite("birdies" + i + ".png"); //populates left sprite list
        }
    }

    @Override
    public void collide(GameObject go) {
        
    }
    
}

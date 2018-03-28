/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Template;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import GameObjects.*;

/**
 *
 * @author Joseph
 */
public class Input implements KeyListener{
    //FIELDS
    public Game hostGame;
    public Player player;
    public Input(Game x){
        hostGame = x;
        player = x.player;
    }
    
    
    @Override
    public void keyTyped(KeyEvent e) {
        
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case 'W':
                player.velY = -player.speed;
                break;
            case 'S': 
                player.velY = player.speed;
                break;
            case 'A':
                player.velX = player.speed*-1;
                break;
            case 'D':
                player .velX = player.speed;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
         switch (e.getKeyCode()) {
            case 'W':
                player.velY = 0;
                break;
            case 'S':
                player.velY = 0;
                break;
            case 'A':
                if (player.velX < 0) {
                    player.velX = 0;
                }
                break;
            case 'D':
                if (player.velX > 0) {
                    player.velX = 0;
                }
                break;
        }
    }

}

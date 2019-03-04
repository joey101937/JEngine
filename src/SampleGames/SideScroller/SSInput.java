/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.InputHandler;
import SampleGames.SideScroller.Actors.Minotaur;
import java.awt.event.KeyEvent;

/**
 *
 * @author Joseph
 */
public class SSInput extends InputHandler{
        @Override
    public void keyPressed(KeyEvent e){
        switch(e.getKeyChar()){
            case 'a':
                SSGame.minotaur.velocity.x = -3;
                break;
            case 'd':
                SSGame.minotaur.velocity.x = 3;
                break;
            case ' ':
                if(SSGame.minotaur.isOnGround()){
                    SSGame.minotaur.jumpTick = SSGame.minotaur.tickNumber;
                }           
                break;
            default:
                break;
        }
    }
    
        @Override
    public void keyReleased(KeyEvent e){
        Minotaur player = SSGame.minotaur;
        switch(e.getKeyChar()){
            case 'a':
                if(player.velocity.x<0)player.velocity.x = 0;
                break;
            case 'd':
                if(player.velocity.x>0)player.velocity.x = 0;
                break;
            default: 
                break;
        }
    }
}

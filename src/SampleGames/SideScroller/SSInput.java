/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.InputHandler;
import Framework.UI_Elements.OptionsMenu;
import SampleGames.SideScroller.Actors.Minotaur;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Joseph
 */
public class SSInput extends InputHandler{
        @Override
    public void keyPressed(KeyEvent e){
        switch(e.getKeyChar()){
            case 'a':
                if(!SSGame.minotaur.freeToAct())break;
                SSGame.minotaur.velocity.x = -3;
                break;
            case 'd':
                 if(!SSGame.minotaur.freeToAct())break;
                SSGame.minotaur.velocity.x = 3;
                break;
            case ' ':
                if(SSGame.minotaur.isOnGround()){
                    SSGame.minotaur.jumpTick = SSGame.minotaur.tickNumber;
                }           
                break;
            case 'x':
                OptionsMenu.display();
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
    
    @Override
    public void mousePressed(MouseEvent e){
        if(e.getButton() == 1){
            if(locationOfMouseEvent(e).x > SSGame.minotaur.getPixelLocation().x){
                SSGame.minotaur.attack(true);//right
            }else{
                SSGame.minotaur.attack(false);//left
            }
            
        }
    }
}

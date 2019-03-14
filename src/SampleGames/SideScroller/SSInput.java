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
                if(!SSGame.playerMinotaur.freeToAct())break;
                SSGame.playerMinotaur.velocity.x = -3;
                break;
            case 'd':
                 if(!SSGame.playerMinotaur.freeToAct())break;
                SSGame.playerMinotaur.velocity.x = 3;
                break;
            case ' ':
                if(SSGame.playerMinotaur.isOnGround()){
                    SSGame.playerMinotaur.jump();
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
        Minotaur player = SSGame.playerMinotaur;
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
            if(locationOfMouseEvent(e).x > SSGame.playerMinotaur.getPixelLocation().x){
                SSGame.playerMinotaur.attack(true);//right
            }else{
                SSGame.playerMinotaur.attack(false);//left
            }
            
        }
    }
}

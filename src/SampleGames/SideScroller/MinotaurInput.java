/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.AsyncInputHandler;
import Framework.UI_Elements.OptionsMenu;
import SampleGames.SideScroller.Actors.Minotaur;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Joseph
 */
public class MinotaurInput extends AsyncInputHandler{
    @Override
    public void onKeyPressed(KeyEvent e){
        switch(e.getKeyChar()){
            case 'a':
                MinotaurGame.playerMinotaur.velocity.x = -MinotaurGame.playerMinotaur.getBaseSpeed();
                break;
            case 'd':
                MinotaurGame.playerMinotaur.velocity.x = MinotaurGame.playerMinotaur.getBaseSpeed();
                break;
            case ' ':
                if(MinotaurGame.playerMinotaur.isOnGround()){
                    MinotaurGame.playerMinotaur.jump();
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
    public void onKeyReleased(KeyEvent e){
        Minotaur player = MinotaurGame.playerMinotaur;
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
    public void onMousePressed(MouseEvent e){
        if(e.getButton() == 1){
            if(locationOfMouseEvent(e).x > MinotaurGame.playerMinotaur.getPixelLocation().x){
                MinotaurGame.playerMinotaur.attack(true);//right
            }else{
                MinotaurGame.playerMinotaur.attack(false);//left
            }
            
        }
    }
}

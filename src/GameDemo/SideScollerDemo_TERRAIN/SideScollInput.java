/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_TERRAIN;

import Framework.InputHandler;
import java.awt.event.KeyEvent;
/**
 *
 * @author Joseph
 */
public class SideScollInput extends InputHandler{
    @Override
    public void keyPressed(KeyEvent e){
        switch(e.getKeyChar()){
            case 'a':
                SideScrollDemo.playerCharacter.velocity.x = -1.5;
                break;
            case 'd':
                SideScrollDemo.playerCharacter.velocity.x = 1.5;
                break;
            case ' ':
                if(SideScrollDemo.playerCharacter.isOnGround()){
                    SideScrollDemo.playerCharacter.jumpTick = SideScrollDemo.playerCharacter.tickNumber;
                }           
                break;
            default:
                break;
        }
    }
    
        @Override
    public void keyReleased(KeyEvent e){
        SideScrollCharacter player = SideScrollDemo.playerCharacter;
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

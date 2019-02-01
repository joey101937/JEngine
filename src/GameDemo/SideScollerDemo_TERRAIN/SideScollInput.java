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
                SideScrollGame.playerCharacter.velocity.x = -3;
                break;
            case 'd':
                SideScrollGame.playerCharacter.velocity.x = 3;
                break;
            case ' ':
                if(SideScrollGame.playerCharacter.isOnGround()){
                    SideScrollGame.playerCharacter.jumpTick = SideScrollGame.playerCharacter.tickNumber;
                }           
                break;
            default:
                break;
        }
    }
    
        @Override
    public void keyReleased(KeyEvent e){
        SideScrollCharacter player = SideScrollGame.playerCharacter;
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

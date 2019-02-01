/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.PlatformerDemo;

import Framework.InputHandler;
import java.awt.event.KeyEvent;
/**
 *
 * @author Joseph
 */
public class PlatformerInput extends InputHandler{
    @Override
    public void keyPressed(KeyEvent e){
        switch(e.getKeyChar()){
            case 'a':
                PlatformerGame.playerCharacter.velocity.x = -3;
                break;
            case 'd':
                PlatformerGame.playerCharacter.velocity.x = 3;
                break;
            case ' ':
                if(PlatformerGame.playerCharacter.isOnGround()){
                    PlatformerGame.playerCharacter.jumpTick = PlatformerGame.playerCharacter.tickNumber;
                }           
                break;
            default:
                break;
        }
    }
    
        @Override
    public void keyReleased(KeyEvent e){
        PlatformCharacter player = PlatformerGame.playerCharacter;
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

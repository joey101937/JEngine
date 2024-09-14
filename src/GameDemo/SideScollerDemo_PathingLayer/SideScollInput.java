/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SideScollerDemo_PathingLayer;

import Framework.InputHandler;
import Framework.Main;
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
            case 'A':
                SideScrollDemo.playerCharacter.velocity.x = -1.5;
                break;
            case 'd':
            case 'D':
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
            case 'A':
                if(player.velocity.x<0)player.velocity.x = 0;
                break;
            case 'd':
            case 'D':
                if(player.velocity.x>0)player.velocity.x = 0;
                break;
            case 'x':
                Main.debugMode = !Main.debugMode;
                break;
            default: 
                break;
        }
    }
}

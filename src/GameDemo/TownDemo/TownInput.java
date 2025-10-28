/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TownDemo;

import Framework.AsyncInputHandler;
import Framework.Main;
import java.awt.event.KeyEvent;

/**
 *
 * @author guydu
 */
public class TownInput extends AsyncInputHandler{
    @Override
    public void onKeyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case 'W':
                TownDemo.playerCharacter.velocity.y = -1;
                break;
            case 'S':
                TownDemo.playerCharacter.velocity.y = 1;
                break;
            case 'A':
                TownDemo.playerCharacter.velocity.x = -1;
                break;
            case 'D':
                TownDemo.playerCharacter.velocity.x = 1;
                break;
            case 'X':
                Main.debugMode = !Main.debugMode;
                break;
            case 'E':
                if(TownDemo.playerCharacter.distanceFrom(TownDemo.portalToInside2) < 70) {
                    TownDemo.portalToInside2.trigger(TownDemo.playerCharacter);
                }
                break;
            case 'B':
                TownDemo.townBird.velocity.y = -1;
                break;
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
         switch(e.getKeyCode()) {
            case 'W':
                if(TownDemo.playerCharacter.velocity.y < 0){
                    TownDemo.playerCharacter.velocity.y = 0;
                }
                break;
            case 'S':
                if(TownDemo.playerCharacter.velocity.y > 0){
                    TownDemo.playerCharacter.velocity.y = 0;
                }
                break;
            case 'A':
                if(TownDemo.playerCharacter.velocity.x < 0){
                    TownDemo.playerCharacter.velocity.x = 0;
                }
                break;
            case 'D':
                if(TownDemo.playerCharacter.velocity.x > 0){
                    TownDemo.playerCharacter.velocity.x = 0;
                }
                break;
        }
    }
}

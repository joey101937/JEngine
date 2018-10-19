/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import static Framework.Game.visHandler;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import GameObjects.*;
import javafx.scene.input.KeyCode;

/**
 *
 * @author Joseph
 */
public class Input implements KeyListener{
    //FIELDS
    public Game hostGame;
    private double shiftSpeedMultiplier = 3.0;
    private boolean isShiftDown = false;
    public Input(Game x){
        hostGame = x;
        
    }
    
    
    @Override
    public void keyTyped(KeyEvent e) {
        
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
          switch(e.getKeyCode()){
            case KeyEvent.VK_SHIFT:
                if(isShiftDown)break;
                Camera.xVel*=shiftSpeedMultiplier;
                Camera.yVel*=shiftSpeedMultiplier;
                isShiftDown = true;
                System.out.println("shift down");
                break;
        }
        switch (e.getKeyCode()) {
            case 'W':
                Camera.yVel = Camera.camSpeed;
                Game.testObject.velocity.y = -4;
                break;
            case 'D':
                Camera.xVel = -Camera.camSpeed;
                Game.testObject.velocity.x = 4;
                break;
            case 'S':
                Camera.yVel = -Camera.camSpeed;
                Game.testObject.velocity.y = 4;
                break;
            case 'A':
                Camera.xVel = Camera.camSpeed;
                Game.testObject.velocity.x = -4;
                break;
        }
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 'W':
                if(Camera.yVel>0)Camera.yVel = 0;
                  Game.testObject.velocity.y = 0;
                break;
            case 'S':
               if(Camera.yVel<0)Camera.yVel = 0;
               Game.testObject.velocity.y = 0;
                break;
            case 'A':
                if(Camera.xVel>0)Camera.xVel = 0;
                Game.testObject.velocity.x = 0;
                break;
            case 'D':
                if(Camera.xVel<0)Camera.xVel = 0;
                Game.testObject.velocity.x = 0;
                break;
        }
           switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                Camera.xVel/=shiftSpeedMultiplier;
                 Camera.yVel/=shiftSpeedMultiplier;
                 isShiftDown = false;
                break;
        }

    }

}

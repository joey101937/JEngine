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

/**
 *
 * @author Joseph
 */
public class Input implements KeyListener{
    //FIELDS
    public Game hostGame;
    public Input(Game x){
        hostGame = x;
        
    }
    
    
    @Override
    public void keyTyped(KeyEvent e) {
        
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
      
        switch (e.getKeyCode()) {
            case 'W':
                Camera.yVel = Camera.camSpeed;
                Game.testObject.velocity.y = -Game.testObject.speed;
                break;
            case 'D':
                Camera.xVel = -Camera.camSpeed;
                Game.testObject.velocity.x = Game.testObject.speed;
                break;
            case 'S':
                Camera.yVel = -Camera.camSpeed;
                Game.testObject.velocity.y = Game.testObject.speed;
                break;
            case 'A':
                Camera.xVel = Camera.camSpeed;
                Game.testObject.velocity.x = -Game.testObject.speed;
                break;
            case 'Q':
                for(GameObject2 go : Game.handler.storage){
                    System.out.println(go.tickNumber + " " + go.name);
                    System.out.println(go.renderNumber + " " + go.name + " render.");
                }
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
        

    }

}

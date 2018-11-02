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
                Game.testObject.velocity.y = -Game.testObject.speed;
                break;
            case 'D':
                Game.testObject.velocity.x = Game.testObject.speed;
                break;
            case 'S':
                Game.testObject.velocity.y = Game.testObject.speed;
                break;
            case 'A':
                Game.testObject.velocity.x = -Game.testObject.speed;
                break;
            case 'Q':
                for(GameObject2 go : Game.handler.getAllObjects()){
                    System.out.println(go.tickNumber + " " + go.name);
                    System.out.println(go.renderNumber + " " + go.name + " render.");               
                }
                 System.out.println("Camera Tick: " + Camera.tickNumber);
                break;
            case 'Z':
                int prev = Game.handler.size();
                int i = (int)Math.random()*Game.handler.getAllObjects().size();
                Game.handler.removeObject(Game.handler.getAllObjects().get(i));
                System.out.println(Game.handler.size() + " -> " + prev);
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

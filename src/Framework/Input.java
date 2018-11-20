/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameObjects.GameObject2;
import Framework.Stickers.OnceThroughSticker;
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
                Game.testObject.velocity.y = -Game.testObject.getSpeed();
                break;
            case 'D':
                Game.testObject.velocity.x = Game.testObject.getSpeed();
                break;
            case 'S':
                Game.testObject.velocity.y = Game.testObject.getSpeed();
                break;
            case 'A':
                Game.testObject.velocity.x = -Game.testObject.getSpeed();
                break;
            case 'Q':
                //debug used to check tick numbers
                for(GameObject2 go : hostGame.handler.getAllObjects()){
                    System.out.println(go.tickNumber + " " + go.name);
                    System.out.println(go.renderNumber + " " + go.name + " render.");               
                }
                 System.out.println("Camera Tick: " + Camera.tickNumber);
                 System.out.println(Game.testObject.location);
                break;
            case 'Z':
                //destroy random object and play explosion sticker there for science
                int prev = hostGame.handler.size();
                int i = (int)Math.random()*hostGame.handler.getAllObjects().size();
                GameObject2 victim = hostGame.handler.getAllObjects().get(i);
                hostGame.handler.removeObject(victim);
                new OnceThroughSticker(SpriteManager.explosionSequence,victim.getPixelLocation(),999);
                System.out.println(hostGame.handler.size() + " -> " + prev);
                SampleBird bird = new SampleBird(new DCoordinate(Game.worldWidth*Math.random(),Game.worldHeight*Math.random()));
                
                bird.velocity = new DCoordinate(-.5,-.5);
                hostGame.addObject(bird);
                break;   
            case 'P':
                System.out.println(hostGame.pathingLayer.getTypeAt(Game.testObject.getPixelLocation()));
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SandboxDemo;

import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.InputHandler;
import Framework.UtilityObjects.Projectile;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.UI_Elements.OptionsMenu;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Joseph
 */
public class DemoInputHandler extends InputHandler{

    /**
     * Constructor, sets hostgame
     * @param host host game
     */
    public DemoInputHandler(Game host){
        super(host);
    }
    /**
     * creates without hostgame set
     */
    public DemoInputHandler(){
        super();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
      
        switch (e.getKeyCode()) {
            case 'W':
                hostGame.testObject.velocity.y = -hostGame.testObject.getSpeed();
                break;
            case 'D':
                hostGame.testObject.velocity.x = hostGame.testObject.getSpeed();   
                break;
            case 'S':
                hostGame.testObject.velocity.y = hostGame.testObject.getSpeed();
                break;
            case 'A':
                hostGame.testObject.velocity.x = -hostGame.testObject.getSpeed();
                break;
            case 'Q':
                //debug used to check tick numbers
                for(GameObject2 go : hostGame.handler.getAllObjects()){
                    System.out.println(go.tickNumber + " " + go.getName());
                    System.out.println(go.renderNumber + " " + go.getName() + " render.");               
                }
                 System.out.println("Camera Tick: " + hostGame.getCamera().tickNumber);
                 System.out.println(hostGame.testObject.location);
                break;
            case 'Z':
                //destroy random object and play explosion sticker there for science
                int prev = hostGame.handler.size();
                int i = (int)Math.random()*hostGame.handler.getAllObjects().size();
                GameObject2 victim = hostGame.handler.getAllObjects().get(i);
                hostGame.handler.removeObject(victim);
                new OnceThroughSticker(hostGame, new Sequence(SpriteManager.explosionSequence), victim.getPixelLocation(),999);
                System.out.println(hostGame.handler.size() + " -> " + prev);
                SampleBird bird = new SampleBird(new DCoordinate(hostGame.getWorldWidth()*Math.random(),hostGame.getWorldHeight()*Math.random()));
                
                bird.velocity = new DCoordinate(-.5,-.5);
                hostGame.addObject(bird);
                break;   
            case 'P':
                System.out.println(hostGame.getPathingLayer().getTypeAt(hostGame.testObject.getPixelLocation()));
                break;
            case 'G':{
                LaunchMenu.changeGame();
                break;
            }
            case 'X':{
                OptionsMenu.display();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 'W':
                if(hostGame.getCamera().yVel>0)hostGame.getCamera().yVel = 0;
                  hostGame.testObject.velocity.y = 0;
                break;
            case 'S':
               if(hostGame.getCamera().yVel<0)hostGame.getCamera().yVel = 0;
               hostGame.testObject.velocity.y = 0;
                break;
            case 'A':
                if(hostGame.getCamera().xVel>0)hostGame.getCamera().xVel = 0;
                hostGame.testObject.velocity.x = 0;
                break;
            case 'D':
                if(hostGame.getCamera().xVel<0)hostGame.getCamera().xVel = 0;
                hostGame.testObject.velocity.x = 0;
                break;
        }
        

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
      //Sticker s = new OnceThroughSticker(hostGame,SpriteManager.explosionSequence,locationOfMouse(e),9999);
     // s.scaleTo(2.0);
      //GameObject2 bird = new SampleBird(this.locationOfMouse(e));
     // bird.velocity= new DCoordinate(.5,.5);
     Projectile p = new Bullet(new DCoordinate(hostGame.testObject.location),new DCoordinate(locationOfMouseEvent(e)));
      hostGame.addObject(p);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    
}

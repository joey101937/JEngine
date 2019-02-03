/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.InputHandler;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.UI_Elements.OptionsMenu;
import Framework.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * This inputhandler handles all user input for the space game.
 * @author Joseph
 */
public class SpaceInputHandler extends InputHandler{
    protected static Coordinate waypoint = new Coordinate(0,0);

    
    /*
    When the mose is pressed, create an explosion effect at the location of the mouse 
    */
    @Override
    public void mousePressed(MouseEvent e){
        OnceThroughSticker effect = new OnceThroughSticker(hostGame,SpriteManager.explosionSequence,locationOfMouseEvent(e));
    }
    
    /*
    when the 'G' key is pressed, swap between two scenes. First scene is space,
    the other scene is a large dirt scene. The ship is added to the new scene
    and the input handler is swapped over. Note the input handler is removed
    from the previous game and added to the new game when swapping.
    
    Note that when the ship is out in the 
    dirt scene farther than the bounds of the space world and you try swapping
    to the space world, the ship will be instantly constrained to the size of
    the space scene and appear at the edge of the sceen closest to where teh ship
    was in the larger scene.
    
    */
    @Override
    public void keyPressed(KeyEvent e){
        //pressing G swaps between scenes.
        if(e.getKeyChar()=='g'){
            if(hostGame != SpaceGame.firstGame){
             //if the game is on dirt, go to space
            Game g = SpaceGame.firstGame;
            Window.setCurrentGame(g);
            g.setInputHandler(this);
            g.addObject(SpaceGame.ship);
            g.camera.setTarget(SpaceGame.ship);
            }else{
             //if the game is in space, go to dirt
            Game g = SpaceGame.secondGame;
            Window.setCurrentGame(g);
            g.setInputHandler(this);
            g.addObject(SpaceGame.ship);
            g.camera.setTarget(SpaceGame.ship);
            }
        }
        //pressing X brings up options menu
        if(e.getKeyChar()=='x'){
            OptionsMenu.display();
        }
    }
    
    /*
    move moved and mouse dragged listen to when the user moves the mouse and
    tracks the location of the mouse in waypoint.
    */
    @Override
    public void mouseMoved(MouseEvent e){
        waypoint = locationOfMouseEvent(e);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        waypoint = locationOfMouseEvent(e);
    }
}

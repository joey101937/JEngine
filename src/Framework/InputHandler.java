/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Manages all user input to a game. Acts as key, Mouse, and MouseMotion Listener.
 * Should be added to a game with game.setInputHandler method
 * @author Joseph
 */
public abstract class InputHandler implements KeyListener, MouseListener, MouseMotionListener{
    //FIELDS
    protected Game hostGame = null;
    public InputHandler(Game x){
        hostGame = x;  
    }
    /**
     * creates handler without host game
     */
    public InputHandler(){}

    /**
     * sets host game. Prints warning if the host game has already been set
     * NOTE DOES NOT SET THE GIVEN GAME'S INPUT TO THIS OBJECT, TO DO
     * THAT, USE GAME CLASS setInputHandler METHOD
     * @param g game to be next host
     */
    protected void setHostGame(Game g){
       if(hostGame==null){
            hostGame = g;
            return;
       }else{
           System.out.println("WARNING YOU ARE REMOVING AN INPUT HANDLER FROM GAME " + hostGame.name + " TO ADD TO GAME " + g.name);
           hostGame = g;
       }
    }
    
    /**
     * returns this hostgame
     * @return 
     */
    public Game getHostGame(){
        return hostGame;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {      
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
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
    
    /**
     * returns the pixel coordinate in the world cooresponding with a mouse event
     */
    public Coordinate locationOfMouse(MouseEvent e){
        return new Coordinate(e.getX() + (int)-hostGame.camera.location.x,e.getY()+(int)-hostGame.camera.location.y );
    }

}

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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Manages all user input to a game. Acts as key, Mouse, and MouseMotion Listener.
 * Should be added to a game with game.setInputHandler method
 * @author Joseph
 */
public abstract class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
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
           if(Main.debugMode)System.out.println("NOTICE YOU ARE REMOVING AN INPUT HANDLER FROM GAME " + hostGame.name + " TO ADD TO GAME " + g.name);
           hostGame = g;
       }
    }
    
    /**
     * returns this hostgame
     * @return  hostGame of this input handler
     */
    public Game getHostGame(){
        return hostGame;
    }
    
    public void tick() {
        
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    /**
     * returns the pixel coordinate in the world cooresponding with a mouse event
     * @param e the mouse event that generated this event
     * @return pixel location of mouse in world
     */
    public Coordinate locationOfMouseEvent(MouseEvent e) {
        DCoordinate loc;
        if(Main.overviewMode()){
             loc = new DCoordinate(e.getX()/Game.OVERVIEW_MODE_ZOOM/Game.resolutionScaleX/hostGame.getZoom() + -hostGame.getCamera().location.x, e.getY()/Game.OVERVIEW_MODE_ZOOM/Game.resolutionScaleY/hostGame.getZoom() - hostGame.getCamera().location.y);
        }else{
             loc = new DCoordinate(e.getX()/Game.resolutionScaleX/hostGame.getZoom() + -hostGame.getCamera().location.x, e.getY()/Game.resolutionScaleY/hostGame.getZoom() - hostGame.getCamera().location.y);
        }
        return loc.toCoordinate();
    }
    
    public static Coordinate locationOfMouseEvent(MouseEvent e, Game g) {
        DCoordinate loc;
        if(Main.overviewMode()){
             loc = new DCoordinate(e.getX()/Game.OVERVIEW_MODE_ZOOM/Game.resolutionScaleX/g.getZoom() + -g.getCamera().location.x, e.getY()/Game.OVERVIEW_MODE_ZOOM/Game.resolutionScaleY/g.getZoom() - g.getCamera().location.y);
        }else{
             loc = new DCoordinate(e.getX()/Game.resolutionScaleX/g.getZoom() + -g.getCamera().location.x, e.getY()/Game.resolutionScaleY/g.getZoom() - g.getCamera().location.y);
        }
        return loc.toCoordinate();
    }
    
    /**
     * returns the pixel coordinate in the world cooresponding with a mouse event
     * @param e the mouse event that generated this event
     * @return pixel location of mouse in world
     */
    public Coordinate getLocationOfMouseEvent(MouseEvent e) {
        return locationOfMouseEvent(e);
    }

}

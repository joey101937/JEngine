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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages all user input to a game. Acts as key, Mouse, and MouseMotion Listener.
 * Should be added to a game with game.setInputHandler method
 * @author Joseph
 */
public abstract class AsyncInputHandler extends InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
    
    private ExecutorService asyncService = Executors.newCachedThreadPool();
    
    public AsyncInputHandler(Game x, boolean isAsync){
        hostGame = x;
    }
    
    public AsyncInputHandler(Game x){
        hostGame = x;  
    }
    /**
     * creates handler without host game
     */
    public AsyncInputHandler(){}

    
    /**
     * returns this hostgame
     * @return  hostGame of this input handler
     */
    public Game getHostGame(){
        return hostGame;
    }
    
    public void onKeyTyped(KeyEvent e) {  
    };
    
    @Override
    public final void keyTyped(KeyEvent e) {
        asyncService.submit(new Task(this, e, "keyTyped"));
    }
    
    public void onKeyPressed(KeyEvent e){
    };

    @Override
    public final void keyPressed(KeyEvent e) {      
        asyncService.submit(new Task(this, e, "keyPressed"));
    }
    
    public void onKeyReleased(KeyEvent e){
    };

    @Override
    public final void keyReleased(KeyEvent e) {
       asyncService.submit(new Task(this, e, "keyReleased"));
    }
    
    public void onMouseClicked(MouseEvent e) {  
    };

    @Override
    public final void mouseClicked(MouseEvent e) {
        asyncService.submit(new Task(this, e, "mouseClicked"));
    }
    
    public void onMousePressed(MouseEvent e) {  
    };

    @Override
    public final void mousePressed(MouseEvent e) {
       asyncService.submit(new Task(this, e, "mousePressed"));
    }
    
    public void onMouseReleased(MouseEvent e){  
    };

    @Override
    public final void mouseReleased(MouseEvent e) {
       asyncService.submit(new Task(this, e, "mouseReleased"));
    }
    
    public void onMouseEntered(MouseEvent e) {  
    };

    @Override
    public final void mouseEntered(MouseEvent e) {
        asyncService.submit(new Task(this, e, "mouseEntered"));
    }
    
    public void onMouseExited(MouseEvent e){  
    };

    @Override
    public final void mouseExited(MouseEvent e) {
        asyncService.submit(new Task(this, e, "mouseExited"));
    }
    
    public void onMouseDragged(MouseEvent e) {  
    };

    @Override
    public final void mouseDragged(MouseEvent e) {
        asyncService.submit(new Task(this, e, "mouseDragged"));
    }
    
    public void onMouseMoved(MouseEvent e) {  
    };

    @Override
    public final void mouseMoved(MouseEvent e) {
        asyncService.submit(new Task(this, e, "mouseMoved"));
    }
    
    public void onMouseWheelMoved(MouseWheelEvent e){  
    };

    @Override
    public final void mouseWheelMoved(MouseWheelEvent e) {
        asyncService.submit(new Task(this, e, "mouseWheelMoved"));
    }
    
    
    private static class Task implements Runnable {
        protected KeyEvent ke;
        protected MouseEvent me;
        protected MouseWheelEvent mwe;
        protected AsyncInputHandler ih;
        protected String methodName;
        
        public Task(AsyncInputHandler ih, KeyEvent e, String methodName) {
            this.ke = e;
            this.ih = ih;
            this.methodName = methodName;
        }
        
        public Task(AsyncInputHandler ih, MouseEvent e, String methodName) {
            this.me = e;
            this.ih = ih;
            this.methodName = methodName;
        }
        
        public Task(AsyncInputHandler ih, MouseWheelEvent e, String methodName) {
            this.mwe = e;
            this.ih = ih;
            this.methodName = methodName;
        }

        @Override
        public void run() {
            switch(methodName) {
                case "keyTyped":
                    ih.onKeyTyped(ke);
                    break;
                case "keyPressed":
                    ih.onKeyPressed(ke);
                    break;
                case "keyReleased":
                    ih.onKeyReleased(ke);
                    break;
                case "mouseClicked":
                    ih.onMouseClicked(me);
                    break;
                case "mousePressed":
                    ih.onMousePressed(me);
                    break;
                case "mouseReleased":
                    ih.onMouseReleased(me);
                    break;
                case "mouseEntered":
                    ih.onMouseEntered(me);
                    break;
                case "mouseExited":
                    ih.onMouseExited(me);
                    break;
                case "mouseDragged":
                    ih.onMouseDragged(me);
                    break;
                case "mouseMoved":
                    ih.onMouseMoved(me);
                    break;
                case "mouseWheelMoved":
                    ih.onMouseWheelMoved(mwe);
                    break;
                default:
                    System.out.println("Unknown async input handler method: " + methodName);
                    return;
            }
        }
        
    }

}

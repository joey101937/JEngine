/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.Game;
import Framework.InputHandler;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Joseph
 */
public class SpaceInputHandler extends InputHandler{
    @Override
    public void mousePressed(MouseEvent e){
        OnceThroughSticker effect = new OnceThroughSticker(hostGame,SpriteManager.explosionSequence,locationOfMouse(e));
    }
    
    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyChar()=='g'){
            if(hostGame.getBackgroundImage()==SpriteManager.spaceBG){
            System.out.println("g");
            Game g = new Game(SpriteManager.dirtBG);
            Window.mainWindow.setCurrentGame(g);
            g.setInputHandler(this);
            }else{
            System.out.println("g");
            Game g = new Game(SpriteManager.spaceBG);
            Window.mainWindow.setCurrentGame(g);
            g.setInputHandler(this);
            }
        }
    }
}

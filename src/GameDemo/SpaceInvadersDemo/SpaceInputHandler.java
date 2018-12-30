/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.InputHandler;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
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
}

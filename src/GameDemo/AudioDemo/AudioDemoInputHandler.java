/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.AudioDemo;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.InputHandler;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 *
 * @author Joseph
 */
public class AudioDemoInputHandler extends InputHandler {

    /**
    if user presses G, swap between scenes
    */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'g') {
            if (Window.currentGame == AudioDemo.game1) {
                Window.setCurrentGame(AudioDemo.game2);
            } else {
                Window.setCurrentGame(AudioDemo.game1);
            }
        }
    }
    
    /**
     * when the user clicks the mouse, create an explosion at that location and
     * play a blast sound effect. this sound effect is linked to the current game
     * so you will only hear it when the game is active, try clicking then immediately
     * swapping over to the other game and see the effect
     * @param e 
     */
    @Override
    public void mousePressed(MouseEvent e){
        //store location of mouse press and create an explosion effec there
        Coordinate c = locationOfMouseEvent(e);
        OnceThroughSticker s = new OnceThroughSticker(hostGame, new Sequence(SpriteManager.explosionSequence),c);
        //create a blast sound effect
        File blastFile = new File(Main.assets+"/Sounds/blast1.au");
        SoundEffect sound = new SoundEffect(blastFile);
        //link the blast sound to the current game
        sound.linkToGame(hostGame);
        //now start the sound
        sound.start();
    }
}

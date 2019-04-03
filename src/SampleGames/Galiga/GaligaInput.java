/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.InputHandler;
import java.awt.event.KeyEvent;

/**
 *
 * @author Joseph
 */
public class GaligaInput extends InputHandler{
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyChar()){
            case 'a':
                GaligaGame.player.velocity.x = -1;
                break;
            case 'd':
                GaligaGame.player.velocity.x = 1;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'a':
                if (GaligaGame.player.velocity.x < 0) {
                    GaligaGame.player.velocity.x = 0;
                }
                break;
            case 'd':
                if (GaligaGame.player.velocity.x > 0) {
                    GaligaGame.player.velocity.x = 0;
                }
                break;
            case ' ':
                Coordinate spawnPoint = GaligaGame.player.getPixelLocation();
                spawnPoint.y -= GaligaGame.player.getHeight() / 2;
                Coordinate dest = new Coordinate(spawnPoint);
                dest.y--;
                Bolt b = new Bolt(spawnPoint, dest);
                b.isFriendly = true;
                GaligaGame.mainGame.addObject(b);
                SoundEffect se = GaligaGame.pewSound.createCopy();
                se.setVolume(.7f);
                se.start();
                break;
        }
    }

}

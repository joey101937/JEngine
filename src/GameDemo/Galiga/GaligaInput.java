/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga;

import Framework.AsyncInputHandler;
import Framework.Coordinate;
import Framework.UI_Elements.OptionsMenu;
import java.awt.event.KeyEvent;

/**
 *
 * @author Joseph
 */
public class GaligaInput extends AsyncInputHandler {

    @Override
    public void onKeyPressed(KeyEvent e) {
        PlayerShip player = GaligaGame.player;
        switch (e.getKeyChar()) {
            case 'a':
            case 'A':
                player.velocity.x = -1;
                break;
            case 'd':
            case 'D':
                player.velocity.x = 1;
                break;
            case 'w':
            case 'W':
                player.velocity.y = -1;
                break;
            case 's':
            case 'S':
                player.velocity.y = 1;
                break;
            case 'x':
            case 'X':
                OptionsMenu.display();
                break;
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'a':
            case 'A':
                if (GaligaGame.player.velocity.x < 0) {
                    GaligaGame.player.velocity.x = 0;
                }
                break;
            case 'd':
            case 'D':
                if (GaligaGame.player.velocity.x > 0) {
                    GaligaGame.player.velocity.x = 0;
                }
                break;
            case 'w':
            case 'W':
                if (GaligaGame.player.velocity.y < 0) {
                    GaligaGame.player.velocity.y = 0;
                }
                break;
            case 's':
            case 'S':
                if (GaligaGame.player.velocity.y > 0) {
                    GaligaGame.player.velocity.y = 0;
                }
                break;
            case ' ':
                if (!GaligaGame.player.isAlive()) {
                    break;
                }
                Coordinate spawnPoint = GaligaGame.player.getPixelLocation();
                spawnPoint.y -= GaligaGame.player.getHeight() / 2;
                Coordinate dest = new Coordinate(spawnPoint);
                dest.y--;
                Bolt b = new Bolt(spawnPoint, dest);
                b.isFriendly = true;
                GaligaGame.mainGame.addObject(b);
                GaligaGame.pewSound.playCopy(.7f);
                break;
        }
    }

}

package GameDemo.SpaceInvadersDemo;

import Framework.AsyncInputHandler;
import Framework.UI_Elements.OptionsMenu;
import java.awt.event.KeyEvent;

/**
 * Keyboard control: WASD to fly (with momentum), SPACE to fire, E to unleash a
 * stocked Nova Bomb, R to restart after a game over, X for the options menu.
 *
 * @author Joseph
 */
public class SpaceInvadersInput extends AsyncInputHandler {

    private PlayerShip player() { return SpaceInvadersGame.player; }

    @Override
    public void onKeyPressed(KeyEvent e) {
        PlayerShip p = player();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  if (p != null) p.left = true; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: if (p != null) p.right = true; break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    if (p != null) p.up = true; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  if (p != null) p.down = true; break;
            case KeyEvent.VK_SPACE: if (p != null) p.firing = true; break;
            case KeyEvent.VK_E:     if (p != null) p.novaHeld = true; break;
            case KeyEvent.VK_R:
                if (SpaceInvadersGame.driver.isGameOver()) SpaceInvadersGame.driver.restart();
                break;
            case KeyEvent.VK_X:
                OptionsMenu.display();
                break;
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        PlayerShip p = player();
        if (p == null) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  p.left = false; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: p.right = false; break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    p.up = false; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  p.down = false; break;
            case KeyEvent.VK_SPACE: p.firing = false; break;
        }
    }
}

package GameDemo.Minotaur;

import Framework.InputHandler;
import Framework.Main;
import Framework.UI_Elements.OptionsMenu;
import GameDemo.Minotaur.Actors.PlayerMinotaur;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Keyboard and mouse for the Minotaur game.
 *
 * Deliberately synchronous rather than using the engine's async handler: every callback here
 * does nothing but set a flag, so there is no work to move off the event thread, and running in
 * order guarantees a press can never be delivered after its own release. A key held down
 * repeats at the OS level, so presses are edge-triggered — otherwise holding the jump key would
 * spend the mid-air jump the instant the character left the ground.
 *
 * @author Joseph
 */
public class MinotaurInput extends InputHandler {

    private boolean leftDown, rightDown, downDown;
    private boolean jumpDown, dashDown, roarDown, lightDown, heavyDown;

    private PlayerMinotaur player() {
        return MinotaurGame.player;
    }

    private void updateMove() {
        PlayerMinotaur p = player();
        if (p == null) return;
        p.moveInput = (rightDown ? 1 : 0) - (leftDown ? 1 : 0);
        p.crouchInput = downDown;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        PlayerMinotaur p = player();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                leftDown = true; updateMove(); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                rightDown = true; updateMove(); break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
                downDown = true; updateMove(); break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP: case KeyEvent.VK_SPACE:
                if (!jumpDown) {
                    jumpDown = true;
                    if (p != null) p.pressJump();
                }
                break;
            case KeyEvent.VK_SHIFT: case KeyEvent.VK_L:
                if (!dashDown) {
                    dashDown = true;
                    if (p != null) p.pressDash();
                }
                break;
            case KeyEvent.VK_J:
                if (!lightDown) {
                    lightDown = true;
                    if (p != null) { p.facingOverride = null; p.pressLight(); }
                }
                break;
            case KeyEvent.VK_K:
                if (!heavyDown) {
                    heavyDown = true;
                    if (p != null) { p.facingOverride = null; p.pressHeavy(); }
                }
                break;
            case KeyEvent.VK_E:
                if (!roarDown) {
                    roarDown = true;
                    if (p != null) p.pressRoar();
                }
                break;
            case KeyEvent.VK_R:
                MinotaurGame.requestRestart();
                break;
            case KeyEvent.VK_M:
                MinotaurSfx.setEnabled(!MinotaurSfx.isEnabled());
                break;
            case KeyEvent.VK_F1:
                Main.debugMode = !Main.debugMode;
                break;
            case KeyEvent.VK_X:
                OptionsMenu.display();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        PlayerMinotaur p = player();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                leftDown = false; updateMove(); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                rightDown = false; updateMove(); break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
                downDown = false; updateMove(); break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP: case KeyEvent.VK_SPACE:
                jumpDown = false;
                if (p != null) p.releaseJump();
                break;
            case KeyEvent.VK_SHIFT: case KeyEvent.VK_L:
                dashDown = false; break;
            case KeyEvent.VK_J:
                lightDown = false; break;
            case KeyEvent.VK_K:
                heavyDown = false; break;
            case KeyEvent.VK_E:
                roarDown = false; break;
            default:
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        PlayerMinotaur p = player();
        if (p == null) return;
        // clicking to one side swings that way, so the mouse can turn the character
        p.facingOverride = locationOfMouseEvent(e).x >= p.getPixelLocation().x;
        if (e.getButton() == MouseEvent.BUTTON1) p.pressLight();
        else if (e.getButton() == MouseEvent.BUTTON3) p.pressHeavy();
        else if (e.getButton() == MouseEvent.BUTTON2) p.pressDash();
    }

    /** Clears held state, so a key held across a level change does not stay stuck down. */
    public void reset() {
        leftDown = rightDown = downDown = false;
        jumpDown = dashDown = roarDown = lightDown = heavyDown = false;
    }
}

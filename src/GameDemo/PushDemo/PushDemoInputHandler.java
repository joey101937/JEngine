package GameDemo.PushDemo;

import Framework.Game;
import Framework.InputHandler;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Input handler for the Push Demo. WASD moves the red PushBlock.
 */
public class PushDemoInputHandler extends InputHandler {

    public PushDemoInputHandler(Game host) {
        super(host);
    }

    public PushDemoInputHandler() {
        super();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (hostGame.testObject == null) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                hostGame.testObject.velocity.y = -1;
                break;
            case KeyEvent.VK_S:
                hostGame.testObject.velocity.y = 1;
                break;
            case KeyEvent.VK_A:
                hostGame.testObject.velocity.x = -1;
                break;
            case KeyEvent.VK_D:
                hostGame.testObject.velocity.x = 1;
                break;
            case KeyEvent.VK_C:
                // Clear all pushes in the game
                for (var obj : hostGame.handler.getAllObjects()) {
                    obj.clearPushes();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (hostGame.testObject == null) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                if (hostGame.testObject.velocity.y < 0)
                    hostGame.testObject.velocity.y = 0;
                break;
            case KeyEvent.VK_S:
                if (hostGame.testObject.velocity.y > 0)
                    hostGame.testObject.velocity.y = 0;
                break;
            case KeyEvent.VK_A:
                if (hostGame.testObject.velocity.x < 0)
                    hostGame.testObject.velocity.x = 0;
                break;
            case KeyEvent.VK_D:
                if (hostGame.testObject.velocity.x > 0)
                    hostGame.testObject.velocity.x = 0;
                break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}

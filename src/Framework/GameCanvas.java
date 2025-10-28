/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

/**
 * A single Canvas that can render any Game instance.
 * This eliminates the need for Game to extend Canvas and allows efficient game switching.
 *
 * @author Joseph
 */
public class GameCanvas extends Canvas {

    private volatile Game currentGame;
    private volatile boolean transitionInProgress = false;

    public GameCanvas() {
        setIgnoreRepaint(true);
    }

    /**
     * Sets which game this canvas should render
     * @param game The game to render
     */
    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }

    /**
     * Marks that a transition is in progress. Prevents rendering during transition.
     * @param inProgress true if transition is happening
     */
    public void setTransitionInProgress(boolean inProgress) {
        this.transitionInProgress = inProgress;
    }

    /**
     * Checks if a transition is currently in progress
     * @return true if transitioning
     */
    public boolean isTransitionInProgress() {
        return transitionInProgress;
    }

    /**
     * Gets the currently rendered game
     * @return The current game
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Renders the current game using this canvas's buffer strategy.
     * Called by the Game's render loop.
     */
    public void renderCurrentGame() {
        // Don't render during transitions
        if (transitionInProgress || currentGame == null) {
            return;
        }

        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            int numBuffer = 2;
            if (Main.tripleBuffer) {
                numBuffer = 3;
            }
            createBufferStrategy(numBuffer);
            System.out.println("generating buffer");
            return;
        }

        // Check if buffer was lost (can happen during transitions)
        if (bs.contentsLost()) {
            System.out.println("Buffer contents lost, skipping frame");
            return;
        }

        currentGame.renderToBufferStrategy(bs);
    }

    /**
     * Applies the input handler from the current game to this canvas
     */
    public void applyCurrentGameInputHandler(boolean applying) {
        if (currentGame == null || currentGame.getInputHandler() == null) {
            return;
        }

        InputHandler inputHandler = currentGame.getInputHandler();

        if (applying) {
            addMouseListener(inputHandler);
            addMouseMotionListener(inputHandler);
            addKeyListener(inputHandler);
            addMouseWheelListener(inputHandler);
        } else {
            removeMouseListener(inputHandler);
            removeMouseMotionListener(inputHandler);
            removeKeyListener(inputHandler);
            removeMouseWheelListener(inputHandler);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Sprite;
import Framework.SpriteManager;
import Framework.SubObject;
import Framework.Window;
import GameDemo.SandboxDemo.SampleCharacter;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Example UI element, a minimap. UI elements are attached to the window.
 *
 * @author Joseph
 */
public final class Minimap extends Panel implements UIElement {

    public final MinimapInterior interior;
    public static final double screenPortion = .1; //how much of the screen to take up
    public final Game hostGame;

    /**
     * creates a minimap object based on given game. Automatically attaches
     * itself to the main window, which must not be null
     *
     * @param g Game to create a minimap of
     */
    public Minimap(Game g) {
        hostGame = g;
        //this determines where it is on screen reletive to top left
        setLocation(new Coordinate(10, 10));
        //create a minimap interior and add it to this panel
        interior = new MinimapInterior();
        Dimension size = new Dimension((int) (hostGame.getWorldWidth() * screenPortion * Game.getResolutionScaleX()), (int) (hostGame.getWorldHeight() * screenPortion * Game.getResolutionScaleY()));
        interior.setSize(size);
        //TODO make this border stuff work
        int border = (int)(20*Game.getResolutionScaleX());
        size.width+=border;
        size.height+=border;
        this.setSize(size);
        this.setLayout(null);
        interior.setLocation(100, border);
        this.setBackground(Color.black);
        this.add(interior);
        //add UI elements to both the mainWindow.panel AND mainWindow.UIElements
        Window.mainWindow.UIElements.add(this);
        Window.mainWindow.panel.add(this);
        //setting z order to make this render above the game
        Window.mainWindow.panel.setComponentZOrder(this, 0);
        Window.mainWindow.panel.setComponentZOrder(hostGame, 1);
    }

    /*
    TODO
    objects not on-camera dont render
    flickering
    size of the thing
    input handler
    possibly incorporate this into UIElement interface for ease of use in future
     */
    /**
     * sets the minimap to be at a certain location relative to the top left of
     * the screen. This is the topleft point of the minimap
     *
     * @param c new location
     */
    public void setLocation(Coordinate c) {
        this.setLocation((int) (c.x * Game.getResolutionScaleX()), (int) (c.y * Game.getResolutionScaleY()));
    }

    /**
     * for testing
     */
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.dirtBG);
        Window w = new Window(g);
        g.start();
        Minimap m = new Minimap(g);
        SampleCharacter character = new SampleCharacter(new Coordinate(200, 700));
        character.velocity.x = 2;
        g.addObject(character);
        g.camera.setTarget(character);
    }
    /**
     * the minimap render method first checks to make sure its game in active, and
     * then if it is, show and update
     */
    @Override
    public void render() {
        if (Window.mainWindow.currentGame != hostGame) {
            this.setVisible(false);
            return;
        } else {
            this.setVisible(true);
        }

        interior.paint(interior.getGraphics());
        // interior.repaint();
    }

    /**
     * This class actully does the drawing
     */
    private class MinimapInterior extends Canvas {

        private final BufferedImage background;

        public MinimapInterior() {
            background = scaleImage(hostGame.getBackgroundImage(), screenPortion * Game.getResolutionScaleX(), screenPortion * Game.getResolutionScaleY());
        }

        @Override
        public void paint(Graphics g) {
            this.setLocation(0, 0);
            Graphics2D g2d = (Graphics2D) g;
            if (g2d == null) {
                return;
            }
            g2d.drawImage(background, 0, 0, null);
            g2d.scale(screenPortion * Game.getResolutionScaleX(), screenPortion * Game.getResolutionScaleY());
            for (GameObject2 go : hostGame.getAllObjects()) {
                go.render(g2d);
                for (SubObject sub : go.subObjects) {
                    sub.render(g2d);
                }
            }
            g2d.setColor(Color.green);
            g2d.draw(hostGame.camera.getFieldOfView());
           // g2d.setColor(Color.black);
            //g2d.drawRect(-(int)hostGame.camera.location.x, -(int)hostGame.camera.location.y, (int)(hostGame.camera.getFieldOfView().width*screenPortion*Game.getResolutionScaleX()), (int)(hostGame.camera.getFieldOfView().height*screenPortion*Game.getResolutionScaleY()));
          //  g2d.setStroke(new BasicStroke(50));
            //g2d.drawRect(0, 0, (int) (this.getWidth() / screenPortion / Game.getResolutionScaleX()), (int) (this.getHeight() / screenPortion / Game.getResolutionScaleY()));
            g2d.dispose();
        }

        private BufferedImage scaleImage(BufferedImage before, double xScale, double yScale) {

            int w = before.getWidth();
            int h = before.getHeight();
            BufferedImage after = new BufferedImage((int) (w * xScale), (int) (h * yScale), BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(xScale, yScale);
            AffineTransformOp scaleOp
                    = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(before, after);
            return after;
        }

    }

}

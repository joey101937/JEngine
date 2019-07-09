/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements.Examples;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;
import Framework.SubObject;
import Framework.UI_Elements.UIElement;
import Framework.Window;
import GameDemo.SandboxDemo.SampleCharacter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Example UI element, a minimap. UI elements are attached to the window.
 *
 * @author Joseph
 */
public final class Minimap extends UIElement {

    public final MinimapInterior interior;
    private double screenPortion = .1; //how much of the screen to take up
    public Game hostGame;

    /**
     * creates a minimap object based on given game. Automatically attaches
     * itself to the main window, which must not be null
     *
     * @param g Game to create a minimap of
     */
    public Minimap(Game g, Coordinate loc) {
        hostGame = g;
        //location determines where it is on screen reletive to top left
        setLocation(loc);
        //create a minimap interior and add it to this panel
         Dimension size = new Dimension((int) (hostGame.getWorldWidth() / Game.getResolutionScaleX() * screenPortion), (int) (hostGame.getWorldHeight() / Game.getResolutionScaleY() * screenPortion));
        interior = new MinimapInterior(size);
        MinimapMouseListener mmm = new MinimapMouseListener(hostGame, this);
        interior.addMouseListener(mmm);
        interior.addMouseMotionListener(mmm);
        interior.setSize(size);
        this.setSize(size);
        this.setLayout(null);
        interior.setLocation(0, 0);
        this.setBackground(Color.black);
        this.add(interior);
    }

    /*
    TODO
    flickering
    size of the thing
    input handler
     */
    /**
     * sets the minimap to be at a certain location relative to the top left of
     * the screen. This is the topleft point of the minimap
     *
     * @param c new location
     */
    public void setLocation(Coordinate c) {
        this.setLocation((int) (c.x / Game.getResolutionScaleX()), (int) (c.y / Game.getResolutionScaleY()));
    }

    /**
     * for testing
     *
     * @param args commandline args
     */
    public static void main(String[] args) {
        Game.scaleForResolution();
        Game g = new Game(new Sprite(SpriteManager.dirtBG));
        Window.initialize(g);
        g.start();
        Minimap m = new Minimap(g, new Coordinate(10, 10));
        Window.addUIElement(m);
        SampleCharacter character = new SampleCharacter(new Coordinate(200, 700));
        character.velocity.x = 2;
        g.addObject(character);
        g.getCamera().setTarget(character);
    }

    /**
     * the minimap render method first checks to make sure its game in active,
     * and then if it is, show and update
     */
    @Override
    public void render() {
        if (Window.currentGame != hostGame) {
            this.setVisible(false);
            return;
        } else {
            this.setVisible(true);
        }

        //interior.paint(interior.getGraphics());
         interior.repaint();
    }

    @Override
    public void tick() {
    }
    
    private static class MinimapMouseListener implements MouseListener, MouseMotionListener {

        Game hostGame;
        Minimap map;
            
        public MinimapMouseListener(Game hostGame, Minimap m) {
            this.hostGame = hostGame;
            this.map = m;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            panTo(e);
            DCoordinate relativePoint = new DCoordinate(0, 0);
            relativePoint.x = (double) e.getX() / (double) map.getWidth();
            relativePoint.x *= hostGame.getWorldWidth();
            relativePoint.y = (double) e.getY() / (double) map.getHeight();
            relativePoint.y *= hostGame.getWorldHeight();
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseDragged(MouseEvent e) {
        Coordinate relativePoint = new Coordinate(0, 0);
            panTo(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {}
        
        private void panTo(MouseEvent e) {
            DCoordinate relativePoint = new DCoordinate(0, 0);
            relativePoint.x = (double) e.getX() / (double) map.getWidth();
            relativePoint.x *= hostGame.getWorldWidth();
            relativePoint.y = (double) e.getY() / (double) map.getHeight();
            relativePoint.y *= hostGame.getWorldHeight();
            hostGame.getCamera().centerOn(relativePoint.toCoordinate());
        }
        
    }

    public double getScreenPortion() {
        return screenPortion;
    }

    public void setScreenPortion(double screenPortion) {
        this.screenPortion = screenPortion;
        Dimension size =new Dimension((int) (hostGame.getWorldWidth() / Game.getResolutionScaleX() * screenPortion), (int) (hostGame.getWorldHeight() / Game.getResolutionScaleY() * screenPortion));
        interior.setSize(size);
        this.setSize(size);
    }
    

    
    
    /**
     * This class actully does the drawing
     */
    private class MinimapInterior extends JPanel {

        private BufferedImage background;
        private double xScale = 1, yScale = 1;
        public MinimapInterior(Dimension d) {
            try{
                background = scaleImage(hostGame.getBackgroundImage().getCurrentImage(),
                    (xScale=(double)d.width / hostGame.getWorldWidth()),
                    (yScale=(double)d.height / hostGame.getWorldHeight()));
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }
        
        @Override
        public void setSize(Dimension d) {
            try {
                background = scaleImage(hostGame.getBackgroundImage().getCurrentImage(),
                        (xScale=(double)d.width / hostGame.getWorldWidth()),
                        (yScale=(double)d.height / hostGame.getWorldHeight()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.setSize(d);
        }

        @Override
        public void paintComponent(Graphics g) {
            this.setLocation(0, 0);
            Graphics2D g2d = (Graphics2D) g;
            if (g2d == null) {
                return;
            }
            g2d.drawImage(background, 0, 0, null);
            g2d.scale(xScale, yScale);
            for (GameObject2 go : hostGame.getAllObjects()) {
                go.render(g2d);
                for (SubObject sub : go.subObjects) {
                    sub.render(g2d);
                }
            }
            g2d.setColor(Color.black);
            g2d.draw(hostGame.getCamera().getFieldOfView());
            g2d.setStroke(new BasicStroke(70));
            g2d.drawRect(0, 0, (int) (this.getWidth() / screenPortion / Game.getResolutionScaleX()), (int) (this.getHeight() / screenPortion / Game.getResolutionScaleY()));
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

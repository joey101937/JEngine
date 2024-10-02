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
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.SandboxDemo.SampleCharacter;
import java.awt.*;
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
    private double screenPortion = .12; //how much of the screen to take up
    public Game hostGame;
    public boolean useSimpleRender = true;
    private double widthOfFrame = 1;
    private double heightOfFrame = 1;
    private SimpleRenderHelper simpleRenderHelper = null;
    private MinimapMouseListener listener;

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
        widthOfFrame = (hostGame.getWindowWidth() * Game.getResolutionScaleX() * screenPortion * ((double)hostGame.getWorldWidth()/hostGame.getWorldHeight()));
        heightOfFrame = (widthOfFrame * ((double)hostGame.getWorldHeight()/hostGame.getWorldWidth()));
        Dimension size = new Dimension((int)widthOfFrame, (int) heightOfFrame);
        interior = new MinimapInterior(size);
        listener = new MinimapMouseListener(hostGame, this);
        interior.addMouseListener(listener);
        interior.addMouseMotionListener(listener);
        interior.setSize(size);
        this.setSize(size);
        this.setLayout(null);
        interior.setLocation(0, 0);
        this.setBackground(Color.black);
        this.add(interior);
    }
    
    public void setSimpleRenderHelper (SimpleRenderHelper srh) {
        this.simpleRenderHelper = srh;
    }
    
    public void setMinimapMouseListener(MinimapMouseListener listener) {
        interior.removeMouseListener(this.listener);
        interior.removeMouseMotionListener(this.listener);
        this.listener = listener;
        interior.addMouseListener(listener);
        interior.addMouseMotionListener(listener);
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
        Game g = new Game(new Sprite(RTSAssetManager.dirtBG));
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
    
    public static class MinimapMouseListener implements MouseListener, MouseMotionListener {

        public Game hostGame;
        public Minimap map;
            
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
        
        public void panTo(MouseEvent e) {
            DCoordinate relativePoint = new DCoordinate(0, 0);
            relativePoint.x = (double) e.getX() / (double) map.getWidth();
            relativePoint.x *= hostGame.getWorldWidth();
            relativePoint.y = (double) e.getY() / (double) map.getHeight();
            relativePoint.y *= hostGame.getWorldHeight();
            hostGame.addTickDelayedEffect(1, x -> {
                 hostGame.getCamera().centerOn(relativePoint.toCoordinate());
            });
           
        }
        
    }

    public double getScreenPortion() {
        return screenPortion;
    }

    public void setScreenPortion(double screenPortion) {
        this.screenPortion = screenPortion;
        widthOfFrame = (Game.getNATIVE_RESOLUTION().width * Game.getResolutionScaleX() * screenPortion * ((double)hostGame.getWorldWidth()/hostGame.getWorldHeight()));
        heightOfFrame = (widthOfFrame * ((double)hostGame.getWorldHeight()/hostGame.getWorldWidth()));
        Dimension size = new Dimension((int)widthOfFrame, (int) heightOfFrame);
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
                if (useSimpleRender) {
                    if(simpleRenderHelper != null) {
                        simpleRenderHelper.simpleRender(go, g2d);
                    } else {
                        g.fillOval(go.getPixelLocation().x - go.getWidth()/2, go.getPixelLocation().y - go.getHeight()/2, go.getWidth(), go.getHeight());
                    }
                } else {
                    go.render(g2d, true);
                    for (SubObject sub : go.getAllSubObjects()) {
                        sub.render(g2d, true);
                    }
                }
            }
            g2d.setColor(Color.black);
            g2d.draw(hostGame.getCamera().getFieldOfView());
            g2d.setStroke(new BasicStroke(5));
            g2d.scale(1/xScale, 1/yScale); // scale back to normal
            drawGradientBorder(g2d, 0, 0, (int)(widthOfFrame), (int) (heightOfFrame));
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

        private void drawGradientBorder(Graphics2D g, int x, int y, int width, int height) {
            int borderWidth = 5;
            
            // Top gradient
            GradientPaint topGradient = new GradientPaint(x, y, borderLight, x, y + borderWidth, borderDark);
            g.setPaint(topGradient);
            g.fillRect(x, y, width, borderWidth);

            // Bottom gradient
            GradientPaint bottomGradient = new GradientPaint(x, y + height - borderWidth, borderDark, x, y + height, borderLight);
            g.setPaint(bottomGradient);
            g.fillRect(x, y + height - borderWidth, width, borderWidth);

            // Left gradient
            GradientPaint leftGradient = new GradientPaint(x, y, borderLight, x + borderWidth, y, borderDark);
            g.setPaint(leftGradient);
            g.fillRect(x, y, borderWidth, height);

            // Right gradient
            GradientPaint rightGradient = new GradientPaint(x + width - borderWidth, y, borderDark, x + width, y, borderLight);
            g.setPaint(rightGradient);
            g.fillRect(x + width - borderWidth, y, borderWidth, height);
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.SpriteManager;
import Framework.Window;
import GameDemo.SandboxDemo.SampleCharacter;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;

/**
 * Example UI element, a minimap.
 * UI elements are attached to the window. 
 * @author Joseph
 */
public final class Minimap extends Panel implements UIElement{
    public final MinimapInterior interior;
    public static final double screenPortion = .2; //how much of the screen to take up
    public final Game hostGame;
    /**
     * creates a minimap object based on given game.
     * Automatically attaches itself to the main window, which must not be null
     * @param g Game to create a minimap of
     */
    public Minimap(Game g){
        hostGame = g;
        //this determines where it is on screen reletive to top left
        this.setLocation((int)(100*Game.getResolutionScaleX()),(int)(100*Game.getResolutionScaleY()));
        //create a minimap interior and add it to this panel
        interior = new MinimapInterior();
        this.setSize(new Dimension((int)(hostGame.worldWidth*screenPortion*Game.getResolutionScaleX()),(int)(hostGame.worldHeight*screenPortion*Game.getResolutionScaleY())));     
        interior.setSize(this.getSize());
        this.add(interior);
        //add UI elements to both the mainWindow.panel AND mainWindow.UIElements
        Window.mainWindow.UIElements.add(this);
        Window.mainWindow.panel.add(this);
        //setting z order to make this render above the game
        Window.mainWindow.panel.setComponentZOrder(this, 0);
        Window.mainWindow.panel.setComponentZOrder(hostGame, 1);
    }
    
    /**
     * sets the minimap to be at a certain location relative to the top left
     * of the screen. This is the topleft point of the minimap
     * @param c new location
     */
    public void setLocation(Coordinate c){
        this.setLocation((int)(c.x*Game.getResolutionScaleX()), (int)(c.y*Game.getResolutionScaleY()));
    }
    
    
    /**
     * for testing
     */
    public static void main(String[] args) {
        Game g = new Game(SpriteManager.dirtBG);
        Window w = new Window(g);
        g.start();
        Minimap m = new Minimap(g);
        SampleCharacter character = new SampleCharacter(new Coordinate(200,200));
        character.velocity.x=2;
        g.addObject(character);
        g.camera.setTarget(character);
    }

    @Override
    public void update() {
        if (Window.mainWindow.currentGame != hostGame) {
            this.setVisible(false);
            return;
        } else {
            this.setVisible(true);
        }
    
        interior.paint(interior.getGraphics());
    }
    


    /**
     * This class actully does the drawing
     */
    private class MinimapInterior extends Canvas {

        @Override
        public void paint(Graphics g) {
            this.setLocation(0, 0);
            Graphics2D g2d = (Graphics2D) g;
            if(g2d==null)return;
            g2d.scale(screenPortion * Game.getResolutionScaleX(), screenPortion * Game.getResolutionScaleY());
            g2d.drawImage(hostGame.getBackgroundImage(), 0, 0, null);
            for (GameObject2 go : hostGame.getAllObjects()) {
               // g.drawOval(go.getPixelLocation().x, go.getPixelLocation().y, 25, 25);
                go.render(g2d);
            }
            g2d.setColor(Color.green);
            g2d.draw(hostGame.camera.getFieldOfView());
            g2d.setColor(Color.black);
           //g2d.drawRect(-(int)hostGame.camera.location.x, -(int)hostGame.camera.location.y, (int)(hostGame.camera.getFieldOfView().width*screenPortion*Game.getResolutionScaleX()), (int)(hostGame.camera.getFieldOfView().height*screenPortion*Game.getResolutionScaleY()));
            g2d.setStroke(new BasicStroke(50));
            g2d.drawRect(0, 0, (int) (this.getWidth() / screenPortion / Game.getResolutionScaleX()), (int) (this.getHeight() / screenPortion / Game.getResolutionScaleY()));
            g2d.dispose();
        }

    }

}

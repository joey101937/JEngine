/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.UI_Elements.UIElement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This contains the code that actually displays the game in a window and starts the game
 * @author Joseph
 */
public class Window {
    /*  FIELDS  */
    public static JPanel panel = new JPanel();
    public static JFrame frame;
    public static String title = "Window Name";
    public static Window mainWindow;
    public static Game currentGame;
    private static ArrayList<UIElement> UIElements = new ArrayList<>();
    public static final Coordinate screenSize = new Coordinate(Toolkit.getDefaultToolkit().getScreenSize().width,Toolkit.getDefaultToolkit().getScreenSize().height);

    
    
    /**
     * Must be the first method called, creates the window with the given default game
     * @param g Initial game to be loaded on window creation
     */
    public static void initialize(Game g){
        if(mainWindow == null){
             mainWindow = new Window(g);
             if(g.hasStarted==false){
                 g.start();
             }
        }else{
            System.out.println("WARNING, TRYING TO INITIALIZE WINDOW WHEN ALREADY INITIALIZED");
        }
    }
    
    public static Window getMainWindow(){
        if(mainWindow == null){
            System.out.println("Main Winow has not been initialized. Call 'Window.initialize(Game);'");
            return null;
        }
        return mainWindow;
    }
    
    
    private Window(Game g) {
        panel.setLayout(null);
        frame = new JFrame(title);
        frame.setIconImage(SpriteManager.programIcon);
        Dimension d = new Dimension(g.windowWidth,g.windowHeight);
        g.setBounds(0, 0, g.windowWidth, g.windowHeight);
        panel.setSize(d);
        panel.add(g);
        panel.setBackground(new Color(85, 85, 115)); //blue background
        frame.add(panel);
        frame.setMinimumSize(d);
        frame.setMaximumSize(d);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.requestFocus();
        g.window=this;
        mainWindow = this;
        currentGame = g;
        frame.setResizable(false);
    }
    
    /**
     * pauses the current game, removes it, then addes the new game and starts it or unpauses it as appropriate
     * @param g new game
     */
    public synchronized static void setCurrentGame(Game g) {
        currentGame.setPaused(true);
        while(!currentGame.pausedSafely){
            Main.wait(2);
        }
        //panel.remove(currentGame);
        currentGame.setVisible(false);
        
        Dimension d = new Dimension(g.windowWidth, g.windowHeight);
        g.setBounds(0, 0, g.windowWidth, g.windowHeight);
        frame.setSize(d);
        panel.setSize(d);
        boolean alreadyContained = false;
        for (Component c : panel.getComponents()) {
            if (c == g) {
                g.setVisible(true);
                panel.setComponentZOrder(g, 0);
                alreadyContained = true;
            }
        }
        if (!alreadyContained) {
            panel.add(g);
        }
        //panel.setComponentZOrder(currentGame, panel.getComponentCount()-1);
        if (g.hasStarted) {
            g.setPaused(false);
        }else{
            g.start();
        }
        currentGame = g;
        currentGame.requestFocus();
        setZOrders();
    }
    
    protected static void UIElementsOnRender(){
        for(UIElement ele: UIElements){
            ele.render();
        }
    }
    
    protected static void TickUIElements(){
        for(UIElement ele: UIElements){
            ele.tick();
        }
    }
    
    
    public static ArrayList<UIElement> getUIElements() {
        return UIElements;
    }

    public static void addUIElement(UIElement uie){
        getUIElements().add(uie);
        Window.panel.add(uie);
        setZOrders();
    }
    public static boolean removeUIElement(UIElement uie){
        if(getUIElements().remove(uie)){
            Window.panel.remove(uie);
            setZOrders();
            return true;
        }
        return false;
    }
    
    private static void setZOrders(){
        for(UIElement ele : Window.UIElements){
            Window.panel.setComponentZOrder(ele, Window.UIElements.indexOf(ele));
            Window.panel.setComponentZOrder(currentGame, Window.UIElements.size());
        }
    }

    protected static void updateFrameSize() {
        if (frame != null && panel != null && currentGame != null) {
            Coordinate worldSize = new Coordinate(currentGame.getWorldWidth(), currentGame.getWorldHeight());
           // System.out.println("current world size is " + worldSize);

            if (worldSize.x < Window.screenSize.x && Game.resolutionScaleX <= 1) {
                worldSize.x *= Game.resolutionScaleX;
            }
            if (worldSize.y < Window.screenSize.y && Game.resolutionScaleY <= 1) {
                worldSize.y *= Game.resolutionScaleY;
            }
            //worldSize.x /= Game.resolutionScaleX;
            //worldSize.y /= Game.resolutionScaleY;
            //System.out.println("after scaling its " + worldSize);
            Dimension d = new Dimension(0, 0);
            if (worldSize.x > Window.screenSize.x) {
                d.width = Window.screenSize.x;
            } else {
                d.width = worldSize.x;
            }
            if (worldSize.y > Window.screenSize.y) {
                d.height = Window.screenSize.y;
            } else {
                d.height = worldSize.y;
            }
           // System.out.println("d is " + d);
            frame.setMinimumSize(d);
            frame.setSize(d);
        }
    }
}

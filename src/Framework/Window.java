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
import java.util.concurrent.CopyOnWriteArrayList;
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
    public static String title = "JEngine Window";
    public static Window mainWindow;
    public volatile static Game currentGame;
    private static CopyOnWriteArrayList<UIElement> UIElements = new CopyOnWriteArrayList<>();
    public static final Coordinate screenSize = new Coordinate(Toolkit.getDefaultToolkit().getScreenSize().width,Toolkit.getDefaultToolkit().getScreenSize().height);

    
    
    /**
     * Must be the first method called, creates the window with the given default game
     * @param g Initial game to be loaded on window creation
     */
    public static void initialize(Game g){
        if(mainWindow == null){
             mainWindow = new Window(g, false);
             if(g.hasStarted==false){
                 g.start();
             }
        }else{
            System.out.println("WARNING, TRYING TO INITIALIZE WINDOW WHEN ALREADY INITIALIZED");
        }
    }
    
    public static void initializeFullScreen(Game g) {
          if(mainWindow == null){
             mainWindow = new Window(g, true);
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
    
    public static void updateTitlePerGame(Game g) {
        if(g.name != null && !g.name.equals("Untitled Game")) {
            title = g.name;
        }
    }
    
    private Window(Game g, boolean fullscreen) {
        currentGame = g;
        g.window=this;
        panel.setLayout(null);
        frame = new JFrame(title);
        if(fullscreen) setFullscreenWindowed(true);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Window.class.getResource("/Resources/JEngineIcon.png")));
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
        mainWindow = this;
        frame.setResizable(false);
        updateTitlePerGame(g);
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
        updateTitlePerGame(g);
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
    
    
    public static CopyOnWriteArrayList<UIElement> getUIElements() {
        return UIElements;
    }

    // note for joey- this has potential for concurrency issues, need single add/remove synchronized method
    public synchronized static void addUIElement(UIElement uie){
        if(getUIElements().contains(uie))return;
        getUIElements().add(uie);
        Window.panel.add(uie);
        setZOrders();
    }
    public synchronized static boolean removeUIElement(UIElement uie){
        if(getUIElements().remove(uie)){
            Window.panel.remove(uie);
            setZOrders();
            return true;
        }
        return false;
    }
    
    private static void setZOrders(){
        System.out.println(Window.UIElements.size() + " ui elements");
        for(UIElement ele : Window.UIElements){
            Window.panel.setComponentZOrder(ele, Window.UIElements.indexOf(ele));
            Window.panel.setComponentZOrder(currentGame, Window.UIElements.size());
        }
    }

    protected static void updateFrameSize() {
        if (frame != null && panel != null && currentGame != null) {
            Coordinate worldSize = new Coordinate(currentGame.getWorldWidth(), currentGame.getWorldHeight());
            if (worldSize.x < Window.screenSize.x && Game.resolutionScaleX <= 1) {
                worldSize.x *= Game.resolutionScaleX;
            }
            if (worldSize.y < Window.screenSize.y && Game.resolutionScaleY <= 1) {
                worldSize.y *= Game.resolutionScaleY;
            }
            
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
            frame.setMinimumSize(d);
            frame.setSize(d);
        }
    }
    
    
    public static void setFullscreenWindowed(boolean x) {
        if(x) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(false);
            frame.dispose();
            frame.setUndecorated(true);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } else {
            frame.setLocationRelativeTo(null);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(false);
            frame.dispose();
            frame.setUndecorated(false);
            frame.pack();
            frame.setVisible(true);
        }
    }

}

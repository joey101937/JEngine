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
    private GameCanvas gameCanvas;

    
    
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
             g.waitForTick(1);
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
        if(g.getName() != null && !g.getName().equals("Untitled Game")) {
            title = g.getName();
        }
    }
    
    private Window(Game g, boolean fullscreen) {
        currentGame = g;
        g.window = this;

        // Create a single GameCanvas for all games
        gameCanvas = new GameCanvas();
        gameCanvas.setCurrentGame(g);

        panel.setLayout(null);
        frame = new JFrame(title);
        if(fullscreen) setFullscreenWindowed(true);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Window.class.getResource("/Resources/JEngineIcon.png")));
        Dimension d = new Dimension(g.windowWidth,g.windowHeight);
        gameCanvas.setBounds(0, 0, g.windowWidth, g.windowHeight);
        panel.setSize(d);
        panel.add(gameCanvas);
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
        // Pause old game and wait for it to safely pause
        if (currentGame != null) {
            currentGame.setPaused(true);
            // Wait for the game to reach a safe paused state
            int waitCount = 0;
            while (!currentGame.pausedSafely && waitCount < 100) {
                Main.wait(1);
                waitCount++;
            }
        }

        // Mark transition as in progress - this blocks rendering
        mainWindow.gameCanvas.setTransitionInProgress(true);

        // Give any in-flight render call time to complete
        Main.wait(5);

        // Now safe to swap everything
        g.window = mainWindow;

        // Update window dimensions if needed
        Dimension d = new Dimension(g.windowWidth, g.windowHeight);
        mainWindow.gameCanvas.setBounds(0, 0, g.windowWidth, g.windowHeight);
        frame.setSize(d);
        panel.setSize(d);

        // Switch the game reference atomically
        currentGame = g;
        mainWindow.gameCanvas.setCurrentGame(g);

        // Start or unpause the new game
        if (g.hasStarted) {
            g.setPaused(false);
        } else {
            g.start();
        }

        // Wait for new game to have completed at least one full tick
        // This ensures it has stable state to render
        long startTick = g.getGameTickNumber();
        int waitCount = 0;
        while (g.getGameTickNumber() <= startTick && waitCount < 100) {
            Main.wait(2);
            waitCount++;
        }

        // One more brief wait to ensure first render is ready
        Main.wait(5);

        // Now re-enable rendering - next frame will be the new game
        mainWindow.gameCanvas.setTransitionInProgress(false);

        mainWindow.gameCanvas.requestFocus();
        setZOrders();
        updateTitlePerGame(g);
        mainWindow.gameCanvas.validate();
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
        uie.revalidate();
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
            if (mainWindow != null && mainWindow.gameCanvas != null) {
                Window.panel.setComponentZOrder(mainWindow.gameCanvas, Window.UIElements.size());
            }
        }
    }

    /**
     * Gets the single GameCanvas instance used by this window
     * @return The GameCanvas
     */
    public GameCanvas getGameCanvas() {
        return gameCanvas;
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

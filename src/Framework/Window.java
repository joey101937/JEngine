/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.UI_Elements.UIElement;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.security.InvalidParameterException;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * This contains the code that actually displays the game in a window and starts the game
 * @author Joseph
 */
public class Window extends Application{
    /*  FIELDS  */
    public static StackPane layout = new StackPane();
    public static Stage stage;
    public static String title = "JEngine Window"; //name of window
    public static Window mainWindow;
    public static Game currentGame;
    private static CopyOnWriteArrayList<UIElement> UIElements = new CopyOnWriteArrayList<>();
    public static final Coordinate screenSize = new Coordinate(Toolkit.getDefaultToolkit().getScreenSize().width,Toolkit.getDefaultToolkit().getScreenSize().height);

    
    
    /**
     * Must be the first method called, creates the window with the given default game
     * @param g Initial game to be loaded on window creation
     */
    public static void initialize(Game g){
        if(g == null) {
            throw new InvalidParameterException("Game must not be null");
        }
        if(mainWindow == null){
             currentGame = g; 
             launch();
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
    
    
    // this is called within the launch 
    private Window() {
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
        
        // Dimension d = new Dimension(g.windowWidth, g.windowHeight);
        // g.setBounds(0, 0, g.windowWidth, g.windowHeight);
        // frame.setSize(d);
        // layout.setSize(d);
 
        
        // coming back to this
//        boolean alreadyContained = false;
//        for (Component c : layout.getComponents()) {
//            if (c == g) {
//                g.setVisible(true);
//                layout.setComponentZOrder(g, 0);
//                alreadyContained = true;
//            }
//        }
//        if (!alreadyContained) {
//            layout.add(g);
//        }
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
    
    
    public static CopyOnWriteArrayList<UIElement> getUIElements() {
        return UIElements;
    }

    public synchronized static void addUIElement(UIElement uie){
//        if(getUIElements().contains(uie))return;
//        getUIElements().add(uie);
//        Window.layout.add(uie);
//        setZOrders();
    }
    public synchronized static boolean removeUIElement(UIElement uie){
//        if(getUIElements().remove(uie)){
//            Window.layout.remove(uie);
//            setZOrders();
//            return true;
//        }
//        return false;
    return true;
    }
    
    private static void setZOrders(){
//        System.out.println(Window.UIElements.size() + " ui elements");
//        for(UIElement ele : Window.UIElements){
//            Window.layout.setComponentZOrder(ele, Window.UIElements.indexOf(ele));
//            Window.layout.setComponentZOrder(currentGame, Window.UIElements.size());
//        }
    }

    protected static void updateFrameSize() {
        if (stage != null && layout != null && currentGame != null) {
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
            stage.setMinWidth(d.width);
            stage.setMinHeight(d.height);
            stage.setWidth(d.width);
            stage.setHeight(d.height);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if(currentGame == null) throw new Exception("Trying to start window application with null game");
        stage = primaryStage;
        mainWindow = this;
        currentGame.window=this;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
          @Override
          public void handle(WindowEvent t) {
              System.exit(0);
          }
        });
        stage.setScene(currentGame.scene);
        stage.show();
        stage.requestFocus();
        mainWindow = this;
        stage.setResizable(false);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.UI_Elements.UIElement;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This contains the code that actually displays the game in a window and starts the game
 * @author Joseph
 */
public class Window {
    /*  FIELDS  */
    public JPanel panel = new JPanel();
    public JFrame frame;
    public String title = "Window Name";
    public static Window mainWindow;
    public Game currentGame;
    public ArrayList<UIElement> UIElements = new ArrayList<>();
    
    public Window(Game g) {
        panel.setLayout(null);
        frame = new JFrame(title);
        Dimension d = new Dimension(Game.windowWidth,Game.windowHeight);
        g.setBounds(0, 0, Game.windowWidth, Game.windowHeight);
        panel.setSize(d);
        panel.add(g);
        frame.add(panel);
        frame.setMinimumSize(d);
        frame.setMaximumSize(d);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.requestFocus();
        g.window=this;
        mainWindow = this;
        currentGame = g;
    }
    
    /**
     * pauses the current game, removes it, then addes the new game and starts it or unpauses it as appropriate
     * @param g new game
     */
    public void setCurrentGame(Game g) {
        currentGame.setPaused(true);
        while(!currentGame.pausedSafely){
            Main.wait(2);
        }
        panel.remove(currentGame);
        Dimension d = new Dimension(Game.windowWidth, Game.windowHeight);
        g.setBounds(0, 0, Game.windowWidth, Game.windowHeight);
        panel.setSize(d);
        panel.add(g);
        if (g.hasStarted) {
            g.setPaused(false);
            System.out.println("unpause");
        }else{
            g.start();
        }
        currentGame = g;
        currentGame.requestFocus();
    }
    
    protected void updateUIElements(){
        for(UIElement ele: UIElements){
            ele.update();
        }
    }
    
    
}

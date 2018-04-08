/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * this contains the code that actually displays the game in a window and starts the game
 * @author Joseph
 */
public class Window {
    /*  FIELDS  */
    public static JPanel panel = new JPanel();
    public JFrame frame;
    public String title = "Basic Platformer";
    
    public Window(Game g){
        frame = new JFrame(title);
        Dimension d = new Dimension(700,700);
        g.setBounds(0, 0, g.width, g.height);
        panel.setSize(d);
        panel.add(g);
        frame.add(panel);
        frame.setMinimumSize(d);
        frame.setMaximumSize(d);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        g.start();
    }
}

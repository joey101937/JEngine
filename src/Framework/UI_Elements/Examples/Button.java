/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UI_Elements.Examples;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.UIElement;
import Framework.Window;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * Example of how to implement a button UI Element
 * extend this class and override onPress method to make a quick button that does what you want
 * @author Joseph
 */
public class Button extends UIElement{
    private Game hostGame = null;
    private JButton button = null;

    public Button(Game g, Coordinate location) {
        hostGame = g;                                   //stores the game this belongs to
        setLocation(location.x, location.y);            //set location on screen
        super.setSize(150, 50);                               //sizing
        button = new JButton("Press Me");               //creats a new JButton
        button.setSize(super.getSize());                 //button fills size of panel
        button.addActionListener(new ActionListener() { //adds an action listener
            @Override
            public void actionPerformed(ActionEvent e) {
                onPress();                             //calls onPress function when pressed
            }
        });
        this.add(button);                               //adds JButton to this panel
    }
    /**
     * this is called whenever the button is pressed
     */
    public void onPress(){
        System.out.println("button pressed");         
        hostGame.requestFocus();                      //returns focus to game
    }
    
    @Override
    public void render() {
        if (Window.currentGame != hostGame) {
            this.setVisible(false);
            return;
        } else {
            this.setVisible(true);
        }
    }
    
    public Game getHostGame() {
        return hostGame;
    }

    public void setHostGame(Game hostGame) {
        this.hostGame = hostGame;
    }  
    
    @Override
    public void setSize(int x, int y){
        super.setSize(x, y);
        button.setSize(x, y);
    }
    
}

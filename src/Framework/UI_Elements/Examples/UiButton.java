package Framework.UI_Elements.Examples;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.UIElement;
import Framework.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * Example of how to implement a button UI Element
 * extend this class and override onPress method to make a quick button that does what you want
 * @author Joseph
 */
public class UiButton extends UIElement{ // ui element extends awt panel and adds tick and render methods to interact with Game objects
    private Game hostGame = null;
    public JButton buttonInterior = null;

    public UiButton(Game g, Coordinate location) {
        hostGame = g;                                   //stores the game this belongs to
        setLocation(location.x, location.y);            //set location on screen
        super.setSize(150, 50);                               //sizing
        buttonInterior = new JButton("Press Me");               //creats a new JButton
        buttonInterior.setSize(super.getSize());                 //button fills size of panel
        buttonInterior.addActionListener(new ActionListener() { //adds an action listener
            @Override
            public void actionPerformed(ActionEvent e) {
                onPress();                             //calls onPress function when pressed
            }
        });
        this.add(buttonInterior);                               //adds JButton to this panel
    }
    /**
     * this is called whenever the button is pressed
     */
    public void onPress(){
        System.out.println("button pressed");         
        hostGame.requestFocus();  //returns focus to game
    }
    
    @Override // runs when the hostGame renders
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
        if(Game.getResolutionScaleX()!=1){
            System.out.println("using no res scaling!");
        }
        super.setSize((int)(x * Game.getResolutionScaleX()), (int)(y*Game.getResolutionScaleY()));
        buttonInterior.setSize(getSize());
    }
    @Override // runs when hostgame ticks
    public void tick(){
    };
}

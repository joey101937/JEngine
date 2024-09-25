package GameDemo.RTSDemo;

import Framework.Game;
import Framework.UI_Elements.UIElement;
import static GameDemo.RTSDemo.SelectionBoxEffect.selectedUnits;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author guydu
 */
public class InfoPanel extends UIElement {

    Game hostGame;
    InfoPanelInterior interior;

    public InfoPanel(Game game, int x, int y, int width) {
        super();
        this.hostGame = game;
        this.setBounds(x, y, width, 200);
        this.setLayout(null);
        this.setVisible(true);
        interior = new InfoPanelInterior(width, 200);
        this.add(interior);
    }

    @Override
    public void render() {
        try {
            interior.repaint();
        } catch (Exception e) {
            System.out.println("exceptino on render");
        }
    }

    @Override
    public void tick() {
    }

    private class InfoPanelInterior extends JPanel {
        
        public Color lightGray = new Color(150,150,150);

        public InfoPanelInterior(int width, int height) {
            this.setLocation(0, 0);
            this.setBounds(0, 0, width, height);
            this.setBackground(lightGray);
            this.setVisible(true);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            this.setLocation(0, 0);
            Graphics2D g2d = (Graphics2D) g;
            if (g2d == null) {
                return;
            }
            //border
            g2d.setColor(Color.black);
            g2d.setStroke(new BasicStroke(5));
            g2d.drawRect(0, 0, (int) (getWidth()), (int) (getHeight()));
            if (!selectedUnits.isEmpty()) {
                ArrayList<RTSUnit> selectedUnits = SelectionBoxEffect.selectedUnits;
                g2d.drawImage(selectedUnits.get(0).getSelectionImage(), 5, 15, null);
                int imageWidth = selectedUnits.get(0).getSelectionImage().getWidth();
                g2d.drawString(selectedUnits.get(0).getClass().getName(), imageWidth + 5, 40);
            }
            g2d.dispose();
        }
    }
}

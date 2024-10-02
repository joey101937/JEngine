package GameDemo.RTSDemo;

import Framework.Game;
import Framework.UI_Elements.UIElement;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author guydu
 */
public class InfoPanel extends UIElement {

    private static final Font titleFont = new Font("TimesRoman", Font.BOLD, 18);
    private static final Font healthFont = new Font("TimesRoman", Font.BOLD, 16);
    private static final Font otherCountFont = new Font("TimesRoman", Font.BOLD, 14);
    private static final Font infoLinesFont = new Font("TimesRoman", Font.BOLD, 12);

    private static final Color healthColor = Color.BLACK;
    private static HashMap<String, BufferedImage> unitNameImageMap = new HashMap<>();

    private static void populateUnitNameImageMap() {
        if (unitNameImageMap.isEmpty()) {
            unitNameImageMap.put("TankUnit", RTSAssetManager.tankSelectionImage);
            unitNameImageMap.put("LightTank", RTSAssetManager.lightTankSelectionImage);
            unitNameImageMap.put("Bazookaman", RTSAssetManager.bazookamanSelectionImage);
            unitNameImageMap.put("Rifleman", RTSAssetManager.riflemanSelectionImage);
            unitNameImageMap.put("Hellicopter", RTSAssetManager.hellicopterSelectionImage);
            unitNameImageMap.put("Landmine", RTSAssetManager.landmineSelectionImage);
        }
    }

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
        populateUnitNameImageMap();
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

        public Color lightGray = new Color(150, 150, 150);
        public RTSUnit mainUnit = null;

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
            ArrayList<RTSUnit> selectedUnits = new ArrayList(SelectionBoxEffect.selectedUnits.stream().filter(
                    x -> !x.isRubble && x.isAlive() && (!ExternalCommunicator.isMultiplayer || x.team == ExternalCommunicator.localTeam)).toList()
            );
            HashMap<String, Integer> unitCountMap = new HashMap<>();
            selectedUnits.forEach(unit -> unitCountMap.put(unit.getName(), unitCountMap.getOrDefault(unit.getName(), 0) + 1));
            //todo account for team of local player
            mainUnit = null;
            if (!selectedUnits.isEmpty()) {
                mainUnit = selectedUnits.get(0);
                g2d.drawImage(unitNameImageMap.get(mainUnit.getName()), 5, 15, null);
                int imageWidth = unitNameImageMap.get(mainUnit.getName()).getWidth();
                g2d.setFont(titleFont);
                g2d.drawString(mainUnit.getName(), imageWidth + 15, 40);
                g2d.setColor(healthColor);
                g2d.setFont(healthFont);
                g2d.drawString("" + mainUnit.currentHealth + " / " + mainUnit.maxHealth, imageWidth + 15, 65);
                drawOtherSelected(g2d, unitCountMap);
                drawInfoLines(g2d, mainUnit);
                drawCommandButtons(g2d, mainUnit);
            }
            g2d.dispose();
        }

        private void drawInfoLines(Graphics2D g, RTSUnit unit) {
            if (unit == null) {
                return;
            }
            g.setFont(infoLinesFont);
            int gradualHeight = 0;
            for (String s : unit.getInfoLines()) {
                g.drawString(s,unitNameImageMap.get(unit.getName()).getWidth() + 15, 90 + gradualHeight);
                gradualHeight += 20;
            }
        }

        private void drawOtherSelected(Graphics2D g, HashMap<String, Integer> nameCountMap) {
            int gradualWidth = 0;
            g.setFont(otherCountFont);
            for (String unitName : nameCountMap.keySet()) {
                var image = unitNameImageMap.get(unitName);
                int imageWidth = 60;
                int imageHeight = 60;
                g.drawImage(image, gradualWidth, getHeight() - imageHeight, imageWidth, imageHeight, null);
                gradualWidth += imageWidth + 10;
                g.drawString("x" + nameCountMap.get(unitName), gradualWidth - imageWidth / 2, getHeight() - 10);
            }
        }
        
        
        private void drawCommandButtons(Graphics2D g, RTSUnit unit) {
            // start at top right and progress down and over
            int currentX = this.getWidth();
            int currentY = 10;
            int buttonRenderWidth = (this.getHeight()-10)/2;
            int buttonRenderHeight = (this.getHeight()-10)/2;
            for( int i = 0; i < unit.getButtons().size(); i++) {
                CommandButton cb = unit.getButtons().get(i);
                g.drawImage(cb.iconImage, currentX - buttonRenderWidth, currentY, buttonRenderWidth, buttonRenderHeight, null);
                if(!cb.isPassive && cb.numUsesRemaining >= 0) {
                    g.setColor(Color.WHITE);
                    g.drawString("x"+cb.numUsesRemaining, currentX - buttonRenderWidth + 10, currentY + 10);
                }
                currentY += buttonRenderHeight;
                if((i+1) % 2 == 0) {
                    // columns of two
                    currentX -= buttonRenderWidth;
                    currentY = 10;
                }
            }
        }
        
        private CommandButton getButtonAtLocation(int x, int y) {
            if (mainUnit == null) return null;
            
            int buttonRenderWidth = (this.getHeight() - 10) / 2;
            int buttonRenderHeight = (this.getHeight() - 10) / 2;
            int currentX = this.getWidth();
            int currentY = 10;
            
            for (int i = 0; i < mainUnit.getButtons().size(); i++) {
                CommandButton cb = mainUnit.getButtons().get(i);
                
                if (x >= currentX - buttonRenderWidth && x < currentX &&
                    y >= currentY && y < currentY + buttonRenderHeight) {
                    return cb;
                }
                
                currentY += buttonRenderHeight;
                if ((i + 1) % 2 == 0) {
                    // Move to the next column
                    currentX -= buttonRenderWidth;
                    currentY = 10;
                }
            }
            
            return null;
        }
    }
}

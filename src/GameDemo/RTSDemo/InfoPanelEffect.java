package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class InfoPanelEffect extends IndependentEffect {
    public CommandButton hoveredButton = null;

    private static final Font titleFont = new Font("TimesRoman", Font.BOLD, 18);
    private static final Font healthFont = new Font("TimesRoman", Font.BOLD, 16);
    private static final Font otherCountFont = new Font("TimesRoman", Font.BOLD, 14);
    private static final Font infoLinesFont = new Font("TimesRoman", Font.BOLD, 12);

    private static final Color healthColor = Color.BLACK;
    private static final Color lightGray = new Color(150, 150, 150);
    private static HashMap<String, BufferedImage> unitNameImageMap = new HashMap<>();

    private Game hostGame;
    private int x, y, width, height;
    private RTSUnit mainUnit = null;

    public InfoPanelEffect(Game game, int x, int y, int width, int height) {
        this.hostGame = game;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        populateUnitNameImageMap();
    }

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

    @Override
    public void render(Graphics2D g) {
        g.setColor(lightGray);
        g.fillRect(x, y, width, height);

        g.setColor(Color.black);
        g.setStroke(new BasicStroke(5));
        g.drawRect(x, y, width, height);

        ArrayList<RTSUnit> selectedUnits = new ArrayList<>(SelectionBoxEffect.selectedUnits.stream().filter(
                x -> !x.isRubble && x.isAlive() && (!ExternalCommunicator.isMultiplayer || x.team == ExternalCommunicator.localTeam)).toList()
        );
        HashMap<String, Integer> unitCountMap = new HashMap<>();
        selectedUnits.forEach(unit -> unitCountMap.put(unit.getName(), unitCountMap.getOrDefault(unit.getName(), 0) + 1));

        mainUnit = null;
        if (!selectedUnits.isEmpty()) {
            mainUnit = selectedUnits.get(0);
            g.drawImage(unitNameImageMap.get(mainUnit.getName()), x + 5, y + 15, null);
            int imageWidth = unitNameImageMap.get(mainUnit.getName()).getWidth();
            g.setFont(titleFont);
            g.drawString(mainUnit.getName(), x + imageWidth + 15, y + 40);
            g.setColor(healthColor);
            g.setFont(healthFont);
            g.drawString("" + mainUnit.currentHealth + " / " + mainUnit.maxHealth, x + imageWidth + 15, y + 65);
            drawOtherSelected(g, unitCountMap);
            drawInfoLines(g, mainUnit);
            drawCommandButtons(g, mainUnit);
        }
    }

    private void drawInfoLines(Graphics2D g, RTSUnit unit) {
        if (unit == null) {
            return;
        }
        g.setFont(infoLinesFont);
        int gradualHeight = 0;
        for (String s : unit.getInfoLines()) {
            g.drawString(s, x + unitNameImageMap.get(unit.getName()).getWidth() + 15, y + 90 + gradualHeight);
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
            g.drawImage(image, x + gradualWidth, y + height - imageHeight, imageWidth, imageHeight, null);
            gradualWidth += imageWidth + 10;
            g.drawString("x" + nameCountMap.get(unitName), x + gradualWidth - imageWidth / 2, y + height - 10);
        }
    }

    private void drawCommandButtons(Graphics2D g, RTSUnit unit) {
        int currentX = x + width - 10;
        int currentY = y + 10;
        int buttonRenderWidth = (height - 20) / 2;
        int buttonRenderHeight = (height - 20) / 2;
        for (int i = 0; i < unit.getButtons().size(); i++) {
            CommandButton cb = unit.getButtons().get(i);
            g.drawImage(cb == hoveredButton ? cb.hoveredImage : cb.iconImage, currentX - buttonRenderWidth, currentY, buttonRenderWidth, buttonRenderHeight, null);
            if (!cb.isPassive && cb.numUsesRemaining >= 0) {
                g.setColor(Color.WHITE);
                g.drawString("x" + cb.numUsesRemaining, currentX - buttonRenderWidth + 10, currentY + 10);
            }
            currentY += buttonRenderHeight;
            if ((i + 1) % 2 == 0) {
                currentX -= buttonRenderWidth;
                currentY = y + 10;
            }
        }
    }

    public CommandButton getButtonAtLocation(int mouseX, int mouseY) {
        if (mainUnit == null) return null;

        int buttonRenderWidth = (height - 20) / 2;
        int buttonRenderHeight = (height - 20) / 2;
        int currentX = x + width - 10;
        int currentY = y + 10;

        for (int i = 0; i < mainUnit.getButtons().size(); i++) {
            CommandButton cb = mainUnit.getButtons().get(i);

            if (mouseX >= currentX - buttonRenderWidth && mouseX < currentX &&
                mouseY >= currentY && mouseY < currentY + buttonRenderHeight) {
                return cb;
            }

            currentY += buttonRenderHeight;
            if ((i + 1) % 2 == 0) {
                currentX -= buttonRenderWidth;
                currentY = y + 10;
            }
        }

        return null;
    }

    @Override
    public void tick() {
        // Update hover state
        Coordinate mousePos = hostGame.getInputHandler().getMousePosition();
        hoveredButton = getButtonAtLocation(mousePos.x, mousePos.y);

        // Handle click
        if (hostGame.getInputHandler().isMousePressed()) {
            CommandButton clickedButton = getButtonAtLocation(mousePos.x, mousePos.y);
            if (clickedButton != null) {
                int buttonIndex = mainUnit.getButtons().indexOf(clickedButton);
                for (RTSUnit u : SelectionBoxEffect.selectedUnits) {
                    if (u.getClass() == mainUnit.getClass()) {
                        u.getButtons().get(buttonIndex).onTrigger.accept(null);
                    }
                }
            }
        }
    }
}

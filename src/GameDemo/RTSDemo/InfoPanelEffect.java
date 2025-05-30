package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
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
    private static final Color borderDark = new Color(100, 100, 100);
    private static final Color borderLight = new Color(200, 200, 200);
    private static final Color cooldownColor = new Color(0, 0, 0, 128); // Semi-transparent black
    private static final ColorSpace GRAYSCALE_COLORSPACE = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    private static HashMap<String, BufferedImage> unitNameImageMap = new HashMap<>();
    private static HashMap<Class<? extends CommandButton>, BufferedImage> brightenedButtonCache = new HashMap<>();
    private static HashMap<Class<? extends CommandButton>, BufferedImage> grayscaleButtonCache = new HashMap<>();

    private Game hostGame;
    public int baseX, baseY, width, height;
    public int x, y;
    private RTSUnit mainUnit = null;

    public InfoPanelEffect(Game game, int x, int y, int width, int height) {
        this.hostGame = game;
        this.baseX = x;
        this.baseY = y;
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
        double scaleAmount = 1/RTSGame.game.getZoom();
        g.scale(scaleAmount, scaleAmount);
        g.setColor(lightGray);
        Coordinate cameraOffset = RTSGame.game.getCamera().getWorldRenderLocation().toCoordinate();
        
        cameraOffset.scale(1/scaleAmount);
        x = baseX + cameraOffset.x;
        y = baseY + cameraOffset.y;
        
        g.fillRect(x, y, width, height);

        drawGradientBorder(g, x, y, width, height);

        ArrayList<RTSUnit> selectedUnits = new ArrayList<>(SelectionBoxEffect.selectedUnits.stream().filter(
                u -> !u.isRubble && u.isAlive() && (!ExternalCommunicator.isMultiplayer || u.team == ExternalCommunicator.localTeam)).toList()
        );
        HashMap<String, Integer> unitCountMap = new HashMap<>();
        selectedUnits.forEach(unit -> unitCountMap.put(unit.getName(), unitCountMap.getOrDefault(unit.getName(), 0) + 1));

        mainUnit = null;
        if (!selectedUnits.isEmpty()) {
            mainUnit = selectedUnits.get(0);
            g.drawImage(unitNameImageMap.get(mainUnit.getName()), x + 5, y + 15, null);
            int imageWidth = unitNameImageMap.get(mainUnit.getName()).getWidth();
            g.setFont(titleFont);
            g.setColor(Color.BLACK);
            g.drawString(mainUnit.getName(), x + imageWidth + 15, y + 40);
            g.setColor(healthColor);
            g.setFont(healthFont);
            g.drawString("" + mainUnit.currentHealth + " / " + mainUnit.maxHealth, x + imageWidth + 15, y + 65);
            drawOtherSelected(g, unitCountMap);
            drawInfoLines(g, mainUnit);
            drawCommandButtons(g, mainUnit);
        }
        g.scale(1/scaleAmount, 1/scaleAmount);
    }

    private void drawGradientBorder(Graphics2D g, int x, int y, int width, int height) {
        int borderWidth = 2;
        
        // Top gradient
        GradientPaint topGradient = new GradientPaint(x, y, borderLight, x, y + borderWidth, borderDark);
        g.setPaint(topGradient);
        g.fillRect(x, y, width, borderWidth);

        // Bottom gradient
        GradientPaint bottomGradient = new GradientPaint(x, y + height - borderWidth, borderDark, x, y + height, borderLight);
        g.setPaint(bottomGradient);
        g.fillRect(x, y + height - borderWidth, width, borderWidth);

        // Left gradient
        GradientPaint leftGradient = new GradientPaint(x, y, borderLight, x + borderWidth, y, borderDark);
        g.setPaint(leftGradient);
        g.fillRect(x, y, borderWidth, height);

        // Right gradient
        GradientPaint rightGradient = new GradientPaint(x + width - borderWidth, y, borderDark, x + width, y, borderLight);
        g.setPaint(rightGradient);
        g.fillRect(x + width - borderWidth, y, borderWidth, height);
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
            BufferedImage toDraw = cb.iconImage;
            
            if (cb == hoveredButton) {
                if (cb.hoveredImage != null) {
                    toDraw = cb.hoveredImage;
                } else {
                    toDraw = getBrightenedImage(cb);
                }
            }
            
            if (cb.isDisabled) {
                if (cb.disabledImage != null) {
                    toDraw = cb.disabledImage;
                } else {
                    toDraw = getGrayscaleImage(cb);
                }
            }
            g.drawImage(toDraw, currentX - buttonRenderWidth, currentY, buttonRenderWidth, buttonRenderHeight, null);
            if (!cb.isPassive && cb.numUsesRemaining >= 0) {
                g.setColor(Color.WHITE);
                g.drawString("x" + cb.numUsesRemaining, currentX - buttonRenderWidth + 10, currentY + 25);
            }
            renderCooldownCircle(g, cb, currentX - buttonRenderWidth, currentY, buttonRenderWidth, buttonRenderHeight);
            currentY += buttonRenderHeight + 4; // Add 4px vertical padding
            if ((i + 1) % 2 == 0) {
                currentX -= (buttonRenderWidth + 4); // Add 4px horizontal padding
                currentY = y + 10;
            }
        }
    }

    public CommandButton getButtonAtLocation(int mouseX, int mouseY) {
        if (mainUnit == null) return null;
        
        double scaleAmount = RTSGame.game.getZoom();
        mouseX*=scaleAmount;
        mouseY*=scaleAmount;
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
    
    public void triggerButtonAt(int mouseX, int mouseY) {
        CommandButton cb = getButtonAtLocation(mouseX, mouseY);
        if(cb == null) return;
        int buttonIndex = mainUnit.getButtons().indexOf(cb);
        for(RTSUnit unit: SelectionBoxEffect.selectedUnits) {
            if(!unit.isRubble && unit.team == mainUnit.team && unit.getClass() == mainUnit.getClass()) {
                CommandButton button = unit.getButtons().get(buttonIndex);
                button.onTrigger.accept(null);
            }
        }
    }

    private void renderCooldownCircle(Graphics2D g, CommandButton button, int x, int y, int width, int height) {
        if (button.cooldownPercent <= 0) return;
        
        // Save the original color
        Color originalColor = g.getColor();
        
        // Set the color for the cooldown overlay
        g.setColor(cooldownColor);
        
        // Calculate the angle based on cooldown percentage (0-100)
        double angle = (360.0 * button.cooldownPercent) / 100.0;
        
        // Draw the arc centered on the button
        g.fillArc(x, y, width, height, 90, -(int)angle);
        
        // Restore the original color
        g.setColor(originalColor);
    }

    private BufferedImage getGrayscaleImage(CommandButton button) {
        BufferedImage out = grayscaleButtonCache.computeIfAbsent(button.getClass(), k -> {
            ColorConvertOp op = new ColorConvertOp(GRAYSCALE_COLORSPACE, null);
            BufferedImage grayImage = new BufferedImage(
                button.iconImage.getWidth(), 
                button.iconImage.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            op.filter(button.iconImage, grayImage);
            return grayImage;
        });
        grayscaleButtonCache.put(button.getClass(), out);
        return out;
    }

    private BufferedImage getBrightenedImage(CommandButton button) {
        BufferedImage out = brightenedButtonCache.computeIfAbsent(button.getClass(), k -> {
            BufferedImage brightened = new BufferedImage(
                button.iconImage.getWidth(),
                button.iconImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            
            // Manual pixel-by-pixel brightening
            for (int x = 0; x < button.iconImage.getWidth(); x++) {
                for (int y = 0; y < button.iconImage.getHeight(); y++) {
                    int rgb = button.iconImage.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xff;
                    int red = Math.min(255, (int)((rgb >> 16 & 0xff) * 1.2));
                    int green = Math.min(255, (int)((rgb >> 8 & 0xff) * 1.2));
                    int blue = Math.min(255, (int)((rgb & 0xff) * 1.2));
                    
                    rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    brightened.setRGB(x, y, rgb);
                }
            }
            return brightened;
        });
        return out;
    }

    @Override
    public void tick() {
        
    }
    
    @Override
    public int getZLayer() {
        return Integer.MAX_VALUE;
    }
}

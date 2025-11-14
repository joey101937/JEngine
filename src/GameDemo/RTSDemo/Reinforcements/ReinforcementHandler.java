package GameDemo.RTSDemo.Reinforcements;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.Hitbox;
import Framework.Window;
import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class ReinforcementHandler extends IndependentEffect {
    private static SoundEffect successSound = new SoundEffect(new File("Assets/Sounds/reinforcement-success.au"));
    private final Font headerFont = new Font("timesRoman", Font.BOLD, 16);
    private final Color backgroundColor = new Color(150, 150, 150);
    private final Color barColor = new Color(0, 255, 0);
    private final Color lightGreen = new Color(100, 255, 100);
    private static final Color borderDark = new Color(100, 100, 100);
    private static final Color borderLight = new Color(200, 200, 200);
    private boolean wasAvailableLastTick = false;
    public int reserveCount = 0;
    public double rechargeInterval = Main.ticksPerSecond * 1; // num ticks between reinforcement charges
    public long lastUsedTick = 0;
    public boolean isMenuOpen = false; // when this is true, make a gray
    public Coordinate locationOnScreen;
    public int width = 250;
    public int height = 30;
    public int menuHeight = 180;
    public ArrayList<ReinforcementType> reinforcementTypes = new ArrayList<>();
    public ReinforcementType hoveredReinforcementType = null;
    public ReinforcementType selectedReinforcementType = null;
    

    public ReinforcementHandler(Coordinate location, int startingNumber) {
        reserveCount = startingNumber;
        locationOnScreen = location;
        reinforcementTypes.add(ReinforcementType.mediumTanks);
        reinforcementTypes.add(ReinforcementType.lightTanks);
        reinforcementTypes.add(ReinforcementType.hellicopters);
        reinforcementTypes.add(ReinforcementType.infantry);
        // todo
        reinforcementTypes.add(new ReinforcementTypeMediumTanks());
        reinforcementTypes.add(new ReinforcementTypeMediumTanks());

    }

    @Override
    public void render(Graphics2D g) {
        double scaleAmount = 1/Window.currentGame.getZoom();
        g.scale(scaleAmount, scaleAmount);
        Coordinate toRender = new Coordinate(locationOnScreen).add(Window.currentGame.getCamera().getWorldRenderLocation().scale(1/scaleAmount));
        double percentReady = (double)(Window.currentGame.getGameTickNumber() - lastUsedTick) / rechargeInterval;
        
        // Draw background
        g.setColor(backgroundColor);
        g.fillRect(toRender.x, toRender.y, width, height);
        
        // Draw progress bar
        if (percentReady <= 1) {
            g.setColor(barColor);
            g.fillRect(toRender.x, toRender.y, (int)(width * percentReady), height);
        } else {
            drawBreathingBar(g, toRender.x, toRender.y, width, height, percentReady);
        }
        
        // Draw border
        drawGradientBorder(g, toRender.x, toRender.y, width, height);
        
        // Draw text
        g.setColor(Color.BLACK);
        g.setFont(headerFont);
        g.drawString("Reinforcements ("+ reserveCount +")", toRender.x + 30, toRender.y + 20);
        g.scale(1/scaleAmount, 1/scaleAmount);
        if(isMenuOpen) {
            renderMenu(g);
        }
    }
    
    public void renderMenu(Graphics2D g) {
        double scaleAmount = 1/Window.currentGame.getZoom();
        g.scale(scaleAmount, scaleAmount);
        Coordinate menuRenderLoc = new Coordinate(locationOnScreen).add(Window.currentGame.getCamera().getWorldRenderLocation().scale(1/scaleAmount));
        menuRenderLoc.y -= menuHeight;
        g.setColor(backgroundColor);
        g.fillRect(menuRenderLoc.x, menuRenderLoc.y, width, menuHeight);
        
        int ongoingX = menuRenderLoc.x;
        int ongoingY = menuRenderLoc.y;
        int index = 0;
        // render in two rows of three
        for(ReinforcementType type : reinforcementTypes) {
            index++;
            BufferedImage image = type.icon;
            if(type == selectedReinforcementType || type == hoveredReinforcementType) {
                image = type.hoverIcon;
            }
            g.drawImage(image, ongoingX, ongoingY, width/3, menuHeight/2, null);
            if(index % 3 != 0) {
                ongoingX += width/3;
            } else {
                ongoingX = menuRenderLoc.x;
                ongoingY += menuHeight/2;
            }
        }
                
        drawGradientBorder(g, menuRenderLoc.x, menuRenderLoc.y, width, menuHeight);
        g.scale(1/scaleAmount, 1/scaleAmount);
    }

    @Override
    public void tick() {
        if(!wasAvailableLastTick && isAvailable()) {
            toggleMenuOpen();
        }
        
        wasAvailableLastTick = isAvailable();
    }

    @Override
    public int getZLayer() {
        return 99999999;
    }
    
    public boolean intersectsMainBar(Coordinate mouseLocation) {
        double scaleAmount = Window.currentGame.getZoom();
        int scaledMouseX = (int)(mouseLocation.x * scaleAmount);
        int scaledMouseY = (int)(mouseLocation.y * scaleAmount);
        
        Coordinate renderLocation = new Coordinate(locationOnScreen).add(Window.currentGame.getCamera().getWorldRenderLocation().scale(scaleAmount)); 
        return scaledMouseX >= renderLocation.x && scaledMouseX < renderLocation.x + width &&
               scaledMouseY >= renderLocation.y && scaledMouseY < renderLocation.y + height;
    }
    
    public ReinforcementType getReinforcementAtLocation(Coordinate mouseLocation) {
        if (!isMenuOpen) {
            return null;
        }
        
        double scaleAmount = Window.currentGame.getZoom();
        int scaledMouseX = (int)(mouseLocation.x * scaleAmount);
        int scaledMouseY = (int)(mouseLocation.y * scaleAmount);
        
        Coordinate menuRenderLoc = new Coordinate(locationOnScreen).add(Window.currentGame.getCamera().getWorldRenderLocation().scale(scaleAmount));
        menuRenderLoc.y -= menuHeight;
        
        int buttonWidth = width / 3;
        int buttonHeight = menuHeight / 2;
        
        for (int i = 0; i < reinforcementTypes.size(); i++) {
            int row = i / 3;
            int col = i % 3;
            
            int buttonX = menuRenderLoc.x + col * buttonWidth;
            int buttonY = menuRenderLoc.y + row * buttonHeight;
            
            if (scaledMouseX >= buttonX && scaledMouseX < buttonX + buttonWidth &&
                scaledMouseY >= buttonY && scaledMouseY < buttonY + buttonHeight) {
                return reinforcementTypes.get(i);
            }
        }
        
        return null;
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

    private void drawBreathingBar(Graphics2D g, int x, int y, int width, int height, double percentReady) {
        double breathingCycle = (Math.sin(Window.currentGame.getGameTickNumber() * 0.1) + 1) / 2; // 0 to 1
        Color startColor = barColor;
        Color endColor = lightGreen;
        
        Color currentColor = new Color(
            (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * breathingCycle),
            (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * breathingCycle),
            (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * breathingCycle)
        );
        
        LinearGradientPaint gradient = new LinearGradientPaint(
            x, y, x + width, y,
            new float[]{0f, 1f},
            new Color[]{currentColor, startColor}
        );
        
        g.setPaint(gradient);
        g.fillRect(x, y, width, height);
    }

    /**
     * returns the location closest to desiredLocation that the given object can
     * exist at without colliding with another RTSUnit returns the
     * desiredLocation if the object can exist at that location without
     * colliding. Only considers GameObject2s that extends the RTSUnit class
     *
     * @param desiredLocation
     * @param object
     * @return
     */
    public static Coordinate getClosestOpenLocation(Coordinate desiredLocation, GameObject2 object) {
        Game currentGame = Window.currentGame;
        if (currentGame == null) {
            System.out.println("Error: Current game is null");
            return desiredLocation;
        }

        // Check if the desired location is already open
        if (isLocationOpen(desiredLocation, object)) {
            return desiredLocation;
        }

        // If not, search for the closest open location
        int maxSearchRadius = 600; // Adjust this value as needed
        int searchGranularity = 20;
        for (int radius = 1; radius <= maxSearchRadius; radius++) {
            for (int x = -radius; x <= radius; x += searchGranularity) {
                for (int y = -radius; y <= radius; y += searchGranularity) {
                    if (Math.abs(x) == radius || Math.abs(y) == radius) {
                        Coordinate testLocation = new Coordinate(desiredLocation.x + x, desiredLocation.y + y);
                        if (isLocationOpen(testLocation, object)) {
                            return testLocation;
                        }
                    }
                }
            }
        }

        // If no open location found within the search radius, return the original desired location
        System.out.println("Warning: No open location found within search radius");
        return desiredLocation;
    }

    private static boolean isLocationOpen(Coordinate location, GameObject2 object) {
        Game currentGame = Window.currentGame;
        if (currentGame == null) {
            return false;
        }

        // Check if the location is within the game world bounds
        if (location.x < currentGame.worldBorder || location.y < currentGame.worldBorder
                || location.x > currentGame.getWorldWidth() - currentGame.worldBorder
                || location.y > currentGame.getWorldHeight() - currentGame.worldBorder) {
            return false;
        }

        // Create a temporary hitbox for the object at the test location
        Hitbox tempHitbox;
        if (object.getHitbox() != null) {
            if (object.getHitbox().type == Hitbox.Type.box) {
                Coordinate[] tempVerts = new Coordinate[4];
                for (int i = 0; i < 4; i++) {
                    tempVerts[i] = object.getHitbox().vertices[i].copy().add(location);
                }
                tempHitbox = new Hitbox(tempVerts);
            } else {
                tempHitbox = new Hitbox(location.toDCoordinate(), object.getHitbox().radius);
            }
        } else {
            // If the object doesn't have a hitbox, create a small circular hitbox
            tempHitbox = new Hitbox(location.toDCoordinate(), 5);
        }

        // Check for collisions with other RTSUnits
        for (GameObject2 go : currentGame.getAllObjects()) {
            if ((go instanceof RTSUnit || go instanceof KeyBuilding) && go != object && go.getHitbox() != null && go.isSolid) {
                if (tempHitbox.intersects(go.getHitbox())) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public synchronized void toggleMenuOpen() {
        if(!isMenuOpen && !isAvailable()) return;
        isMenuOpen = !isMenuOpen;
        if(!isMenuOpen) {
            selectedReinforcementType = null;
            hoveredReinforcementType = null;
        }
    }
    
    public boolean isAvailable() {
        if(reserveCount < 1) return false;
        if(KeyBuilding.getClosest(new Coordinate(0,0), ExternalCommunicator.localTeam) == null) return false;
        return 1 < (double)(Window.currentGame.getGameTickNumber() - lastUsedTick) / rechargeInterval;
    }
    
    public void setSelectedReinforcementType(ReinforcementType type) {
        selectedReinforcementType = type;
    }
    
    public void callReinforcement(ReinforcementType type, Coordinate targetLocation) {
        if(!isAvailable()) return;
        successSound.playCopy(.7);
        type.onTrigger(targetLocation, ExternalCommunicator.localTeam);
        reserveCount--;
        lastUsedTick = Window.currentGame.getGameTickNumber();
        isMenuOpen = false;
        selectedReinforcementType = null;
        hoveredReinforcementType = null;
    }
}

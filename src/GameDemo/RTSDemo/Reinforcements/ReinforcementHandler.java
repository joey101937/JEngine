package GameDemo.RTSDemo.Reinforcements;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.Hitbox;
import Framework.Window;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.Commands.CallReinforcementCommand;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSInput;
import GameDemo.RTSDemo.RTSUIStyle;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class ReinforcementHandler extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private static SoundEffect successSound = new SoundEffect(new File("Assets/Sounds/reinforcement-success.au"));
    private boolean wasAvailableLastTick = false;
    public int reserveCount = 0;
    public double rechargeInterval = RTSGame.desiredTPS * 1; // num ticks between reinforcement charges
    public long lastUsedTick = 0;
    public boolean isMenuOpen = false; // when true, the option stack is expanded above the header
    public Coordinate locationOnScreen;
    public int width = 232;
    public int height = 38; // header height
    private static final int ROW_HEIGHT = 52;
    private static final int ROW_GAP = 5;
    private static final int GAP_ABOVE_HEADER = 6;
    private static final int HEADER_ARC = 10;
    private static final int ROW_ARC = 8;
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
    }

    @Override
    public void render(Graphics2D g) {
        double scaleAmount = 1/Window.currentGame.getZoom();
        g.scale(scaleAmount, scaleAmount);
        RTSUIStyle.enableAA(g);
        Coordinate header = new Coordinate(locationOnScreen).add(Window.currentGame.getCamera().getWorldRenderLocation().scale(1/scaleAmount));

        if (isMenuOpen) {
            renderStack(g, header.x, header.y);
        }
        renderHeader(g, header.x, header.y);

        g.scale(1/scaleAmount, 1/scaleAmount);
    }

    private void renderHeader(Graphics2D g, int hx, int hy) {
        double percentReady = (double)(Window.currentGame.getGameTickNumber() - lastUsedTick) / rechargeInterval;
        boolean ready = percentReady >= 1 && reserveCount > 0;

        RTSUIStyle.drawGlassPanel(g, hx, hy, width, height, HEADER_ARC);

        // Recharge fill: accent while charging, breathing green when a call-in is ready.
        Shape oldClip = g.getClip();
        g.setClip(new java.awt.geom.RoundRectangle2D.Float(hx, hy, width, height, HEADER_ARC, HEADER_ARC));
        if (!ready) {
            g.setColor(RTSUIStyle.ACCENT_DIM);
            g.fillRect(hx, hy, (int)(width * Math.min(percentReady, 1)), height);
        } else {
            double breathe = (Math.sin(Window.currentGame.getGameTickNumber() * 0.12) + 1) / 2;
            g.setColor(new Color(118, 146, 62, (int)(70 + breathe * 90)));
            g.fillRect(hx, hy, width, height);
        }
        g.setClip(oldClip);
        RTSUIStyle.strokePanelFrame(g, hx, hy, width, height, HEADER_ARC); // re-stroke frame over the fill

        // Expand/collapse chevron.
        int chevX = hx + 16;
        int chevMidY = hy + height / 2;
        g.setColor(ready ? RTSUIStyle.TEXT : RTSUIStyle.TEXT_MUTED);
        g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        if (isMenuOpen) {
            g.drawLine(chevX, chevMidY + 3, chevX + 5, chevMidY - 3);
            g.drawLine(chevX + 5, chevMidY - 3, chevX + 10, chevMidY + 3);
        } else {
            g.drawLine(chevX, chevMidY - 3, chevX + 5, chevMidY + 3);
            g.drawLine(chevX + 5, chevMidY + 3, chevX + 10, chevMidY - 3);
        }

        g.setFont(RTSUIStyle.LABEL_FONT);
        RTSUIStyle.drawShadowedVCentered(g, "REINFORCEMENTS", chevX + 24, hy + height / 2, RTSUIStyle.TEXT);
        // Reserve-count badge on the right.
        String count = String.valueOf(reserveCount);
        int badgeH = height - 12;
        int badgeW = Math.max(g.getFontMetrics().stringWidth(count) + 14, 24);
        int badgeX = hx + width - badgeW - 8;
        int badgeY = hy + (height - badgeH) / 2;
        g.setColor(ready ? RTSUIStyle.READY : RTSUIStyle.BADGE_BG);
        g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 8, 8);
        RTSUIStyle.drawShadowedCentered(g, count, badgeX + badgeW / 2, badgeY + badgeH / 2, RTSUIStyle.CREAM);
    }

    /** Geometry of stack row {@code i} (list order, top-down); shared by draw and hit-test. */
    private int[] rowRect(int i, int hx, int hy) {
        int n = reinforcementTypes.size();
        int stackHeight = n * ROW_HEIGHT + (n - 1) * ROW_GAP;
        int stackTop = hy - GAP_ABOVE_HEADER - stackHeight;
        int rowY = stackTop + i * (ROW_HEIGHT + ROW_GAP);
        return new int[]{hx, rowY, width, ROW_HEIGHT};
    }

    private void renderStack(Graphics2D g, int hx, int hy) {
        boolean available = isAvailable();
        for (int i = 0; i < reinforcementTypes.size(); i++) {
            ReinforcementType type = reinforcementTypes.get(i);
            int[] r = rowRect(i, hx, hy);
            int rx = r[0], ry = r[1];
            boolean active = type == selectedReinforcementType || type == hoveredReinforcementType;

            RTSUIStyle.drawCard(g, rx, ry, width, ROW_HEIGHT, ROW_ARC, active);
            if (type == selectedReinforcementType) {
                // Emphasize the armed choice with a filled olive wash.
                g.setColor(RTSUIStyle.ACCENT_GLOW);
                g.fillRoundRect(rx + 1, ry + 1, width - 2, ROW_HEIGHT - 2, ROW_ARC, ROW_ARC);
            }

            // Thumbnail on the left.
            int icon = ROW_HEIGHT - 12;
            int iconX = rx + 6;
            int iconY = ry + 6;
            RTSUIStyle.drawSlot(g, iconX, iconY, icon, icon, 6, false);
            BufferedImage image = active && type.hoverIcon != null ? type.hoverIcon : type.icon;
            RTSUIStyle.drawRoundedImage(g, image, iconX + 2, iconY + 2, icon - 4, icon - 4, 5);

            // Name + unit-count subtitle to the right of the thumbnail.
            int textX = iconX + icon + 12;
            int textW = width - (textX - rx) - 12;
            g.setFont(RTSUIStyle.TITLE_FONT);
            RTSUIStyle.drawShadowedString(g, RTSUIStyle.fitString(g, type.name, textW), textX, ry + 24,
                    active ? RTSUIStyle.ACCENT : RTSUIStyle.TEXT);
            g.setFont(RTSUIStyle.BODY_FONT);
            RTSUIStyle.drawShadowedString(g, RTSUIStyle.fitString(g, unitCountSummary(type), textW),
                    textX, ry + 42, RTSUIStyle.TEXT_MUTED);

            // Dim rows while a call-in is on cooldown / unavailable.
            if (!available) {
                g.setColor(new Color(28, 20, 12, 110));
                g.fillRoundRect(rx, ry, width, ROW_HEIGHT, ROW_ARC, ROW_ARC);
            }
        }

        // Tooltip for the hovered option, to the right of its row.
        if (hoveredReinforcementType != null) {
            int idx = reinforcementTypes.indexOf(hoveredReinforcementType);
            if (idx >= 0) {
                int[] r = rowRect(idx, hx, hy);
                renderTypeTooltip(g, hoveredReinforcementType, r[0] + width + 8, r[1]);
            }
        }
    }

    /** Short "N units" summary derived from the type's contents map. */
    private String unitCountSummary(ReinforcementType type) {
        int total = 0;
        for (int c : type.contents.values()) {
            total += c;
        }
        return total > 0 ? total + " units" : "";
    }

    private void renderTypeTooltip(Graphics2D g, ReinforcementType type, int tx, int ty) {
        ArrayList<String> rawLines = new ArrayList<>(type.infoLines);
        if (!type.contents.isEmpty()) {
            rawLines.add("");
            rawLines.add("Contains:");
            for (var entry : type.contents.entrySet()) {
                rawLines.add("  " + entry.getValue() + "× " + prettyUnitName(entry.getKey()));
            }
        }

        int tw = 250;
        int pad = 14;
        int lineH = 17;
        // Wrap each line to the panel width so nothing spills past the edge.
        g.setFont(RTSUIStyle.BODY_FONT);
        ArrayList<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            lines.addAll(RTSUIStyle.wrapLines(g, line, tw - pad * 2));
        }

        int th = 44 + lines.size() * lineH + 8;
        ty = Math.max(ty, 4); // keep the top on-screen

        RTSUIStyle.drawGlassPanel(g, tx, ty, tw, th, 12);
        g.setFont(RTSUIStyle.TITLE_FONT);
        RTSUIStyle.drawShadowedString(g, type.name, tx + pad, ty + 26, RTSUIStyle.TEXT);
        g.setColor(RTSUIStyle.ACCENT_DIM);
        g.fillRect(tx + pad, ty + 34, tw - pad * 2, 1);

        g.setFont(RTSUIStyle.BODY_FONT);
        int ly = ty + 54;
        for (String line : lines) {
            RTSUIStyle.drawShadowedString(g, line, tx + pad, ly, RTSUIStyle.TEXT);
            ly += lineH;
        }
    }

    private static String prettyUnitName(Class<?> unitClass) {
        String s = unitClass.getSimpleName();
        // Insert spaces before internal capitals: "TransportHelicopter" -> "Transport Helicopter".
        return s.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }

    @Override
    public void onPostDeserialization(Framework.Game game) {
        // Restore transient fields in all reinforcement types
        for (ReinforcementType type : reinforcementTypes) {
            if (type != null) {
                type.restoreTransientFields();
            }
        }

        // Update static reference so other code uses the new deserialized instance
        RTSGame.reinforcementHandler = this;
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

        Coordinate header = new Coordinate(locationOnScreen).add(Window.currentGame.getCamera().getWorldRenderLocation().scale(scaleAmount));

        for (int i = 0; i < reinforcementTypes.size(); i++) {
            int[] r = rowRect(i, header.x, header.y);
            if (scaledMouseX >= r[0] && scaledMouseX < r[0] + r[2] &&
                scaledMouseY >= r[1] && scaledMouseY < r[1] + r[3]) {
                return reinforcementTypes.get(i);
            }
        }

        return null;
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
            if (go.isSolid && go.preventOverlap && go != object && go.getHitbox() != null) {
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
        if(!ReinforcementPoint.anyOwnedBy(ExternalCommunicator.localTeam)) return false;
        return 1 < (double)(Window.currentGame.getGameTickNumber() - lastUsedTick) / rechargeInterval;
    }
    
    public void setSelectedReinforcementType(ReinforcementType type) {
        selectedReinforcementType = type;
    }
    
    public void callReinforcement(ReinforcementType type, Coordinate targetLocation) {
        if(!isAvailable()) return;
        int reinforcementIndex = reinforcementTypes.indexOf(type);
        if(reinforcementIndex < 0) return;
        // The spawn itself runs through a command so both simulations recreate it
        // deterministically on the same tick. The command group is generated here
        // (once) and travels over the wire so unit grouping matches on both sides.
        String commandGroup = RTSInput.generateRandomCommandGroup();
        long spawnTick = Window.currentGame.getGameTickNumber() + RTSInput.getInputDelay();
        RTSGame.commandHandler.addCommand(new CallReinforcementCommand(
                spawnTick,
                ExternalCommunicator.localTeam,
                reinforcementIndex,
                targetLocation,
                commandGroup
        ), true);
        successSound.playCopy(.7);
        reserveCount--;
        lastUsedTick = Window.currentGame.getGameTickNumber();
        isMenuOpen = false;
        selectedReinforcementType = null;
        hoveredReinforcementType = null;
    }
}

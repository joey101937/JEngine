package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Window;
import Framework.SerializationManager;
import GameDemo.RTSDemo.MapEditor.MapData;
import GameDemo.RTSDemo.MapEditor.MapLoader;
import GameDemo.RTSDemo.MapEditor.MapSerializer;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;

public class GameMenuEffect extends IndependentEffect {

    private transient Game game;
    private boolean isOpen = false;

    private static final int PANEL_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 44;
    private static final int BUTTON_SPACING = 10;
    private static final int PADDING = 20;
    private static final int TITLE_HEIGHT = 40;

    private static final Color OVERLAY_COLOR    = new Color(0, 0, 0, 140);
    private static final Color PANEL_BG         = new Color(20, 22, 28, 230);
    private static final Color BUTTON_NORMAL     = new Color(45, 50, 62, 220);
    private static final Color BUTTON_HOVER      = new Color(70, 78, 100, 240);
    private static final Color BUTTON_TEXT       = new Color(220, 225, 235);
    private static final Color TITLE_COLOR       = new Color(180, 190, 210);
    private static final Color BORDER_LIGHT      = new Color(100, 110, 140, 200);
    private static final Color BORDER_DARK       = new Color(30, 35, 45, 200);

    private static final Font TITLE_FONT  = new Font("SansSerif", Font.BOLD, 15);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private transient int hoveredButtonIndex = -1;

    private static final List<String> BUTTON_LABELS = new ArrayList<>(List.of(
        "Toggle Pause",
        "Toggle Fullscreen",
        "Quick Save",
        "Quick Load",
        "Load Map",
        "Quit"
    ));

    public GameMenuEffect(Game game) {
        this.game = game;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        isOpen = true;
    }

    public void close() {
        isOpen = false;
        hoveredButtonIndex = -1;
    }

    public void toggle() {
        if (isOpen) close(); else open();
    }

    @Override
    public int getZLayer() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean shouldSerialize() {
        return false;
    }

    @Override
    public void onPostDeserialization(Game game) {
        this.game = game;
        RTSGame.gameMenuEffect = this;
    }

    @Override
    public void tick() {}

    @Override
    public void render(Graphics2D g) {
        if (!isOpen) return;

        double scaleAmount = 1.0 / game.getZoom();
        g.scale(scaleAmount, scaleAmount);
        Coordinate cameraOffset = game.getCamera().getWorldRenderLocation().toCoordinate();
        cameraOffset.scale(1.0 / scaleAmount);

        int screenW = (int)(game.getWindowWidth() / game.getZoom());
        int screenH = (int)(game.getWindowHeight() / game.getZoom());

        int numButtons = BUTTON_LABELS.size();
        int panelHeight = TITLE_HEIGHT + PADDING + numButtons * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING + PADDING;

        int panelX = cameraOffset.x + (screenW - PANEL_WIDTH) / 2;
        int panelY = cameraOffset.y + (screenH - panelHeight) / 2;

        // Full-screen dimming overlay
        g.setColor(OVERLAY_COLOR);
        g.fillRect(cameraOffset.x, cameraOffset.y, screenW, screenH);

        // Panel background
        g.setColor(PANEL_BG);
        g.fillRoundRect(panelX, panelY, PANEL_WIDTH, panelHeight, 10, 10);

        drawGradientBorder(g, panelX, panelY, PANEL_WIDTH, panelHeight);

        // Title
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(TITLE_FONT);
        g.setColor(TITLE_COLOR);
        FontMetrics tfm = g.getFontMetrics();
        String title = "Menu";
        int titleX = panelX + (PANEL_WIDTH - tfm.stringWidth(title)) / 2;
        int titleY = panelY + TITLE_HEIGHT / 2 + tfm.getAscent() / 2 - 2;
        g.drawString(title, titleX, titleY);

        // Divider under title
        g.setColor(BORDER_LIGHT);
        g.drawLine(panelX + 10, panelY + TITLE_HEIGHT, panelX + PANEL_WIDTH - 10, panelY + TITLE_HEIGHT);

        // Buttons
        g.setFont(BUTTON_FONT);
        FontMetrics bfm = g.getFontMetrics();
        for (int i = 0; i < numButtons; i++) {
            int bx = panelX + PADDING / 2;
            int by = panelY + TITLE_HEIGHT + PADDING / 2 + i * (BUTTON_HEIGHT + BUTTON_SPACING);
            int bw = PANEL_WIDTH - PADDING;

            g.setColor(hoveredButtonIndex == i ? BUTTON_HOVER : BUTTON_NORMAL);
            g.fillRoundRect(bx, by, bw, BUTTON_HEIGHT, 8, 8);

            g.setColor(BORDER_LIGHT);
            g.drawRoundRect(bx, by, bw, BUTTON_HEIGHT, 8, 8);

            g.setColor(BUTTON_TEXT);
            String label = BUTTON_LABELS.get(i);
            int lx = bx + (bw - bfm.stringWidth(label)) / 2;
            int ly = by + (BUTTON_HEIGHT + bfm.getAscent() - bfm.getDescent()) / 2 - 1;
            g.drawString(label, lx, ly);
        }

        g.scale(1 / scaleAmount, 1 / scaleAmount);
    }

    private void drawGradientBorder(Graphics2D g, int x, int y, int width, int height) {
        int bw = 2;
        g.setPaint(new GradientPaint(x, y, BORDER_LIGHT, x, y + bw, BORDER_DARK));
        g.fillRect(x, y, width, bw);
        g.setPaint(new GradientPaint(x, y + height - bw, BORDER_DARK, x, y + height, BORDER_LIGHT));
        g.fillRect(x, y + height - bw, width, bw);
        g.setPaint(new GradientPaint(x, y, BORDER_LIGHT, x + bw, y, BORDER_DARK));
        g.fillRect(x, y, bw, height);
        g.setPaint(new GradientPaint(x + width - bw, y, BORDER_DARK, x + width, y, BORDER_LIGHT));
        g.fillRect(x + width - bw, y, bw, height);
    }

    // Returns screen pixel rect [x, y, w, h] for button i.
    // Computed in raw screen pixels (no camera offset) to match mouse event coordinates.
    private int[] getButtonScreenRect(int i) {
        int screenW = game.getWindowWidth();
        int screenH = game.getWindowHeight();
        int numButtons = BUTTON_LABELS.size();
        int panelHeight = TITLE_HEIGHT + PADDING + numButtons * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING + PADDING;
        int panelX = (screenW - PANEL_WIDTH) / 2;
        int panelY = (screenH - panelHeight) / 2;

        int bx = panelX + PADDING / 2;
        int by = panelY + TITLE_HEIGHT + PADDING / 2 + i * (BUTTON_HEIGHT + BUTTON_SPACING);
        int bw = PANEL_WIDTH - PADDING;
        return new int[]{bx, by, bw, BUTTON_HEIGHT};
    }

    public void updateHover(int screenX, int screenY) {
        if (!isOpen) return;
        hoveredButtonIndex = -1;
        for (int i = 0; i < BUTTON_LABELS.size(); i++) {
            int[] r = getButtonScreenRect(i);
            if (screenX >= r[0] && screenX <= r[0] + r[2] && screenY >= r[1] && screenY <= r[1] + r[3]) {
                hoveredButtonIndex = i;
                return;
            }
        }
    }

    // Returns the index of the button at the given screen coords, or -1
    public int getButtonIndexAt(int screenX, int screenY) {
        if (!isOpen) return -1;
        for (int i = 0; i < BUTTON_LABELS.size(); i++) {
            int[] r = getButtonScreenRect(i);
            if (screenX >= r[0] && screenX <= r[0] + r[2] && screenY >= r[1] && screenY <= r[1] + r[3]) {
                return i;
            }
        }
        return -1;
    }

    public void triggerButton(int index) {
        switch (index) {
            case 0 -> { if (!ExternalCommunicator.isMultiplayer) game.setPaused(!game.isPaused()); }
            case 1 -> Window.setFullscreenWindowed(!Window.frame.isUndecorated());
            case 2 -> SerializationManager.quickSave(game);
            case 3 -> SerializationManager.quickLoad(game);
            case 4 -> loadMap();
            case 5 -> System.exit(0);
        }
    }

    private void loadMap() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Map");
        if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            MapData data = MapSerializer.load(f);
            for (GameObject2 go : new ArrayList<>(game.handler.getAllObjects())) {
                game.handler.removeObject(go);
            }
            MapLoader.loadIntoGame(data, game);
            close();
        } catch (Exception ex) {
            System.err.println("Load map failed: " + ex.getMessage());
        }
    }
}

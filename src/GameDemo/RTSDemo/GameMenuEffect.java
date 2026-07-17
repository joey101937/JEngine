package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.Window;
import Framework.SerializationManager;
import GameDemo.RTSDemo.MapEditor.MapData;
import GameDemo.RTSDemo.MapEditor.MapLoader;
import GameDemo.RTSDemo.MapEditor.MapSerializer;
import GameDemo.RTSDemo.FogOfWar.FogOfWarEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.Pathfinding.NavigationManager;
import GameDemo.RTSDemo.Replay.ReplayManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;

public class GameMenuEffect extends IndependentEffect {

    private transient Game game;
    private boolean isOpen = false;

    private static final int PANEL_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 44;
    private static final int BUTTON_SPACING = 10;
    private static final int PADDING = 20;
    private static final int TITLE_HEIGHT = 40;

    // Warm scrim dimming the battlefield behind the menu.
    private static final Color OVERLAY_COLOR = new Color(18, 12, 6, 150);

    private transient int hoveredButtonIndex = -1;

    private boolean showingSettings = false;

    private static final List<String> MAIN_LABELS = List.of(
        "Toggle Pause",
        "Settings",
        "Quick Save",
        "Quick Load",
        "Load Map",
        "Save Replay",
        "Load Replay",
        "Quit"
    );
    // Indices that begin a two-button row; the following index shares its row as the right half.
    private static final Set<Integer> MAIN_PAIRS = Set.of(2, 5);

    private static final List<String> SETTINGS_LABELS = List.of(
        "Toggle Fullscreen",
        "Toggle Fog of War",
        "Toggle Pathing Grid",
        "Back"
    );
    private static final Set<Integer> SETTINGS_PAIRS = Set.of();

    private List<String> activeLabels() {
        return showingSettings ? SETTINGS_LABELS : MAIN_LABELS;
    }

    private Set<Integer> activePairs() {
        return showingSettings ? SETTINGS_PAIRS : MAIN_PAIRS;
    }

    private boolean isPairLeft(int i) {
        return activePairs().contains(i);
    }

    private boolean isPairRight(int i) {
        return activePairs().contains(i - 1);
    }

    // Row that button i renders on. Paired buttons share a row, so rows < button count.
    private int rowOf(int i) {
        int row = -1;
        for (int j = 0; j <= i; j++) {
            if (!isPairRight(j)) row++;
        }
        return row;
    }

    private int numRows() {
        return rowOf(activeLabels().size() - 1) + 1;
    }

    // Rect [x, y, w, h] for button i, laid out from the given panel origin.
    private int[] buttonRect(int i, int originX, int originY) {
        int by = originY + TITLE_HEIGHT + PADDING / 2 + rowOf(i) * (BUTTON_HEIGHT + BUTTON_SPACING);
        int fullBx = originX + PADDING / 2;
        int fullBw = PANEL_WIDTH - PADDING;
        int halfW = (fullBw - BUTTON_SPACING) / 2;
        if (isPairLeft(i)) {
            return new int[]{fullBx, by, halfW, BUTTON_HEIGHT};
        } else if (isPairRight(i)) {
            int rightX = fullBx + halfW + BUTTON_SPACING;
            return new int[]{rightX, by, fullBw - halfW - BUTTON_SPACING, BUTTON_HEIGHT};
        }
        return new int[]{fullBx, by, fullBw, BUTTON_HEIGHT};
    }

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
        showingSettings = false;
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
        RTSUIStyle.enableAA(g);
        Coordinate cameraOffset = game.getCamera().getWorldRenderLocation().toCoordinate();
        cameraOffset.scale(1.0 / scaleAmount);

        int screenW = (int)(game.getWindowWidth() / game.getZoom());
        int screenH = (int)(game.getWindowHeight() / game.getZoom());

        int numButtons = activeLabels().size();
        int panelHeight = TITLE_HEIGHT + PADDING + numRows() * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING + PADDING;

        int panelX = cameraOffset.x + (screenW - PANEL_WIDTH) / 2;
        int panelY = cameraOffset.y + (screenH - panelHeight) / 2;

        // Full-screen dimming scrim.
        g.setColor(OVERLAY_COLOR);
        g.fillRect(cameraOffset.x, cameraOffset.y, screenW, screenH);

        // Aged-canvas panel with ink frame + rivets.
        RTSUIStyle.drawGlassPanel(g, panelX, panelY, PANEL_WIDTH, panelHeight, 14);

        // Title, stencil-style uppercase.
        g.setFont(RTSUIStyle.TITLE_FONT);
        String title = showingSettings ? "SETTINGS" : "MENU";
        RTSUIStyle.drawShadowedCentered(g, title, panelX + PANEL_WIDTH / 2, panelY + TITLE_HEIGHT / 2, RTSUIStyle.TEXT);

        // Divider under title.
        g.setColor(RTSUIStyle.ACCENT_DIM);
        g.fillRect(panelX + 14, panelY + TITLE_HEIGHT, PANEL_WIDTH - 28, 1);

        // Buttons as light canvas cards; hovered gains the olive frame + glow.
        g.setFont(RTSUIStyle.LABEL_FONT);
        for (int i = 0; i < numButtons; i++) {
            int[] r = buttonRect(i, panelX, panelY);
            int bx = r[0];
            int by = r[1];
            int bw = r[2];
            boolean hovered = hoveredButtonIndex == i;

            RTSUIStyle.drawCard(g, bx, by, bw, BUTTON_HEIGHT, 8, hovered);
            RTSUIStyle.drawShadowedCentered(g, activeLabels().get(i), bx + bw / 2, by + BUTTON_HEIGHT / 2,
                    hovered ? RTSUIStyle.ACCENT : RTSUIStyle.TEXT);
        }

        g.scale(1 / scaleAmount, 1 / scaleAmount);
    }

    // Returns screen pixel rect [x, y, w, h] for button i.
    // Computed in raw screen pixels (no camera offset) to match mouse event coordinates.
    private int[] getButtonScreenRect(int i) {
        int screenW = game.getWindowWidth();
        int screenH = game.getWindowHeight();
        int panelHeight = TITLE_HEIGHT + PADDING + numRows() * (BUTTON_HEIGHT + BUTTON_SPACING) - BUTTON_SPACING + PADDING;
        int panelX = (screenW - PANEL_WIDTH) / 2;
        int panelY = (screenH - panelHeight) / 2;
        return buttonRect(i, panelX, panelY);
    }

    public void updateHover(int screenX, int screenY) {
        if (!isOpen) return;
        hoveredButtonIndex = -1;
        for (int i = 0; i < activeLabels().size(); i++) {
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
        for (int i = 0; i < activeLabels().size(); i++) {
            int[] r = getButtonScreenRect(i);
            if (screenX >= r[0] && screenX <= r[0] + r[2] && screenY >= r[1] && screenY <= r[1] + r[3]) {
                return i;
            }
        }
        return -1;
    }

    public void triggerButton(int index) {
        if (showingSettings) {
            switch (index) {
                case 0 -> Window.setFullscreenWindowed(!Window.frame.isUndecorated());
                case 1 -> FogOfWarEffect.enabled = !FogOfWarEffect.enabled;
                case 2 -> NavigationManager.displayPathingDebugGrid = !NavigationManager.displayPathingDebugGrid;
                case 3 -> { showingSettings = false; hoveredButtonIndex = -1; }
            }
            return;
        }
        switch (index) {
            case 0 -> { if (!ExternalCommunicator.isMultiplayer) game.setPaused(!game.isPaused()); }
            case 1 -> { showingSettings = true; hoveredButtonIndex = -1; }
            case 2 -> SerializationManager.quickSave(game);
            case 3 -> SerializationManager.quickLoad(game);
            case 4 -> loadMap();
            case 5 -> saveReplay();
            case 6 -> loadReplay();
            case 7 -> System.exit(0);
        }
    }

    private void saveReplay() {
        close();
        File saved = ReplayManager.saveReplay();
        if (saved != null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Replay saved to:\n" + saved.getAbsolutePath(),
                    "Replay Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadReplay() {
        close();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Replay");
        File replayDir = new File("replays");
        if (replayDir.exists()) fc.setCurrentDirectory(replayDir);
        if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            ReplayManager.ParsedReplay replay = ReplayManager.parseReplayFile(f);

            // Local deterministic playback: not multiplayer, replay control mode on.
            ExternalCommunicator.isMultiplayer = false;
            RTSGame.desiredTPS = replay.tps;
            Main.ticksPerSecond = replay.tps;
            Main.setRandomSeed(replay.seed);
            ReplayManager.isReplayMode = true;

            BufferedImage bg = MapLoader.loadBackground(replay.mapData.background);
            if (bg == null) bg = RTSAssetManager.grassBG;

            Game newGame = new Game(bg);

            // Remove old minimap from the window overlay before the new one is added
            if (RTSGame.minimap != null) {
                Window.removeUIElement(RTSGame.minimap);
                RTSGame.minimap = null;
            }

            RTSGame.applyLoadingScreen(newGame);
            RTSGame.setup(newGame);
            // Regenerate unit IDs from _1 so they match the IDs referenced by the recorded commands.
            RTSUnitIdHelper.reset();
            MapLoader.loadIntoGame(replay.mapData, newGame);
            ReplayManager.setCurrentMap(replay.mapData, replay.mapName);

            // Inject the recorded command stream. Game tick is still ~0, so every recorded
            // execute tick is in the future and accepted; commands fire on their recorded ticks
            // as the deterministic sim advances. shouldCommunicate=false bypasses the replay gate.
            for (String mp : replay.commandStrings) {
                var cmd = ReplayManager.parseCommand(mp);
                if (cmd != null) RTSGame.commandHandler.addCommand(cmd, false);
            }

            newGame.setOnGameStabilized(x -> {
                RTSGame.setupUI(newGame);
                newGame.setLoadingScreenActive(false);
            });

            Game oldGame = RTSGame.game;
            RTSGame.game = newGame;
            Window.setCurrentGame(newGame);

            if (oldGame != null && oldGame != newGame) {
                Window.panel.remove(oldGame.getCanvas());
                KeyBuilding.removeForGame(oldGame);
                SelectionBoxEffect.selectedUnits.clear();
                ControlGroupHelper.clearAll();
                GameDemo.RTSDemo.SceneryObjects.SceneryObject.clearForGame(oldGame);
                oldGame.dispose();
            }

        } catch (Exception ex) {
            System.err.println("Load replay failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadMap() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Map");
        if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            MapData data = MapSerializer.load(f);

            // Loading a fresh map exits replay playback and restores normal player control.
            ReplayManager.isReplayMode = false;

            BufferedImage bg = MapLoader.loadBackground(data.background);
            if (bg == null) bg = RTSAssetManager.grassBG;

            Game newGame = new Game(bg);

            // Remove old minimap from the window overlay before the new one is added
            if (RTSGame.minimap != null) {
                Window.removeUIElement(RTSGame.minimap);
                RTSGame.minimap = null;
            }

            RTSGame.applyLoadingScreen(newGame);
            RTSGame.setup(newGame);
            // Reset the deterministic unit-ID counter so this game's units get IDs from _1,
            // making the game reproducible if a replay of it is recorded and later played back.
            RTSUnitIdHelper.reset();
            MapLoader.loadIntoGame(data, newGame);
            ReplayManager.setCurrentMap(data, f.getName());

            newGame.setOnGameStabilized(x -> {
                RTSGame.setupUI(newGame);
                newGame.setLoadingScreenActive(false);
            });

            Game oldGame = RTSGame.game;
            RTSGame.game = newGame;
            Window.setCurrentGame(newGame);

            if (oldGame != null && oldGame != newGame) {
                // Detach the retired canvas so panel -> canvas -> listeners no longer
                // pins the old game; without this the old game graph cannot be collected.
                Window.panel.remove(oldGame.getCanvas());
                // Drop static registries that hold the retired game's units/buildings;
                // their hostGame back-references would otherwise pin the whole old game.
                KeyBuilding.removeForGame(oldGame);
                SelectionBoxEffect.selectedUnits.clear();
                ControlGroupHelper.clearAll();
                // Drop the scenery quadtree for this game. It is keyed weakly by game,
                // but its value holds scenery whose hostGame points back to the key,
                // so the entry never self-evicts and pins the whole retired game.
                GameDemo.RTSDemo.SceneryObjects.SceneryObject.clearForGame(oldGame);
                oldGame.dispose();
            }

        } catch (Exception ex) {
            System.err.println("Load map failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

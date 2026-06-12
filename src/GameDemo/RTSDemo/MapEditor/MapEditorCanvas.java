package GameDemo.RTSDemo.MapEditor;

import Framework.Main;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MapEditorCanvas extends JPanel {

    private MapData mapData = new MapData();
    private BufferedImage backgroundImage;

    // Camera
    private double camX = 0, camY = 0, zoom = 0.1;
    private boolean fittedOnce = false;

    // Editor state
    EditorObjectType paletteSelection = null;
    private PlacedObject selectedObject = null;

    // Drag tracking
    private int pressScreenX, pressScreenY;
    private PlacedObject dragTarget = null;
    private int dragStartWorldX, dragStartWorldY;
    private boolean isDragging = false;

    // Mouse position on screen
    private int mouseX, mouseY;
    private boolean mouseOnCanvas = false;

    // Callbacks
    private Runnable onSelectionChanged;
    private Consumer<String> statusUpdater;

    // ── Public API ────────────────────────────────────────────────────────────

    public MapData getMapData() { return mapData; }

    public void setMapData(MapData d) {
        mapData = d;
        selectedObject = null;
        backgroundImage = loadBg(d.background);
        fittedOnce = false;
        notifySelection();
        repaint();
    }

    public PlacedObject getSelectedObject() { return selectedObject; }

    public void deleteSelectedObject() {
        if (selectedObject != null) {
            mapData.objects.remove(selectedObject);
            selectedObject = null;
            notifySelection();
            repaint();
        }
    }

    public void setPaletteSelection(EditorObjectType type) {
        paletteSelection = type;
        selectedObject = null;
        notifySelection();
        repaint();
    }

    public void setOnSelectionChanged(Runnable r) { onSelectionChanged = r; }
    public void setStatusUpdater(Consumer<String> c) { statusUpdater = c; }

    public void changeBackground(String filename) {
        mapData.background = filename;
        backgroundImage = loadBg(filename);
        fittedOnce = false;
        repaint();
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public MapEditorCanvas() {
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        backgroundImage = RTSAssetManager.grassBG;

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                pressScreenX = e.getX();
                pressScreenY = e.getY();
                isDragging = false;
                dragTarget = null;

                if (SwingUtilities.isRightMouseButton(e)) {
                    paletteSelection = null;
                    selectedObject = null;
                    notifySelection();
                    repaint();
                    return;
                }

                if (paletteSelection == null) {
                    PlacedObject hit = hitTest(e.getX(), e.getY());
                    selectedObject = hit;
                    dragTarget = hit;
                    if (hit != null) {
                        dragStartWorldX = hit.x;
                        dragStartWorldY = hit.y;
                    }
                    notifySelection();
                    repaint();
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                int dx = e.getX() - pressScreenX;
                int dy = e.getY() - pressScreenY;
                boolean wasClick = Math.abs(dx) < 5 && Math.abs(dy) < 5;
                if (SwingUtilities.isLeftMouseButton(e) && paletteSelection != null && wasClick) {
                    placeAt(e.getX(), e.getY());
                }
                isDragging = false;
                dragTarget = null;
                repaint();
            }

            @Override public void mouseEntered(MouseEvent e) { mouseOnCanvas = true; }
            @Override public void mouseExited(MouseEvent e)  { mouseOnCanvas = false; repaint(); }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - pressScreenX;
                int dy = e.getY() - pressScreenY;
                if (Math.abs(dx) > 4 || Math.abs(dy) > 4) isDragging = true;

                if (dragTarget != null && paletteSelection == null && isDragging) {
                    dragTarget.x = dragStartWorldX + (int)(dx / zoom);
                    dragTarget.y = dragStartWorldY + (int)(dy / zoom);
                    updateStatus();
                } else if (dragTarget == null && paletteSelection == null && isDragging) {
                    camX -= dx / zoom;
                    camY -= dy / zoom;
                    pressScreenX = e.getX();
                    pressScreenY = e.getY();
                }
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }

            @Override public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                updateStatus();
                if (paletteSelection != null) repaint();
            }
        });

        addMouseWheelListener(e -> {
            if (selectedObject != null && paletteSelection == null) {
                double step = e.isShiftDown() ? 1 : 15;
                selectedObject.rotation = (selectedObject.rotation + e.getWheelRotation() * step % 360 + 360) % 360;
                notifySelection();
            } else {
                double factor = e.getWheelRotation() < 0 ? 1.15 : 1.0 / 1.15;
                double wx = toWorldX(e.getX());
                double wy = toWorldY(e.getY());
                zoom = Math.max(0.02, Math.min(zoom * factor, 4.0));
                camX = wx - e.getX() / zoom;
                camY = wy - e.getY() / zoom;
            }
            repaint();
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    deleteSelectedObject();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    paletteSelection = null;
                    selectedObject = null;
                    notifySelection();
                    repaint();
                }
                if (e.getKeyCode() == KeyEvent.VK_F) {
                    fittedOnce = false;
                    fitView();
                    repaint();
                }
            }
        });
    }

    // ── Placement ─────────────────────────────────────────────────────────────

    private void placeAt(int screenX, int screenY) {
        if (paletteSelection == null) return;
        PlacedObject obj = new PlacedObject(paletteSelection.className, (int) toWorldX(screenX), (int) toWorldY(screenY));
        if (!paletteSelection.hasTeam()) obj.team = 0;
        if (!paletteSelection.hasHp())  obj.hpPercent = 0;
        mapData.objects.add(obj);
        selectedObject = obj;
        paletteSelection = null;
        notifySelection();
        repaint();
    }

    // ── Hit testing ───────────────────────────────────────────────────────────

    private PlacedObject hitTest(int sx, int sy) {
        for (int i = mapData.objects.size() - 1; i >= 0; i--) {
            PlacedObject obj = mapData.objects.get(i);
            EditorObjectType t = EditorObjectType.fromClassName(obj.type);
            if (t == null) continue;
            BufferedImage img = t.getScaledImage(t.hasTeam() ? obj.team : 0);
            if (img == null) continue;
            int hw = Math.max((int)(img.getWidth()  * zoom / 2), 6);
            int hh = Math.max((int)(img.getHeight() * zoom / 2), 6);
            int ox = toScreenX(obj.x);
            int oy = toScreenY(obj.y);
            if (Math.abs(sx - ox) <= hw && Math.abs(sy - oy) <= hh) return obj;
        }
        return null;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!fittedOnce) fitView();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        if (backgroundImage != null) {
            int bw = (int)(backgroundImage.getWidth()  * zoom);
            int bh = (int)(backgroundImage.getHeight() * zoom);
            g2.drawImage(backgroundImage, toScreenX(0), toScreenY(0), bw, bh, null);
        } else {
            g2.setColor(new Color(60, 100, 60));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Placed objects
        for (PlacedObject obj : mapData.objects) {
            drawObject(g2, obj, obj == selectedObject);
        }

        // Ghost
        if (paletteSelection != null && mouseOnCanvas) {
            drawGhost(g2);
        }

        // Hint bar
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, getHeight() - 20, getWidth(), 20);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        String hint = paletteSelection != null
                ? "CLICK to place " + paletteSelection.displayName + "  |  ESC / Right-click to cancel"
                : selectedObject != null
                    ? "DRAG to move  |  SCROLL to rotate (Shift=1 deg)  |  DEL to delete  |  ESC to deselect"
                    : "Click object to select  |  Scroll to zoom  |  Drag background to pan  |  F to fit view";
        g2.drawString(hint, 8, getHeight() - 5);
    }

    private void drawObject(Graphics2D g2, PlacedObject obj, boolean selected) {
        EditorObjectType type = EditorObjectType.fromClassName(obj.type);
        if (type == null) return;
        int team = type.hasTeam() ? obj.team : 0;

        int sx = toScreenX(obj.x);
        int sy = toScreenY(obj.y);

        // Body dimensions used for the selection rectangle
        BufferedImage bodyImg = type.getScaledImage(team);
        int selHw = bodyImg != null ? Math.max((int)(bodyImg.getWidth()  * zoom / 2) + 3, 6) : 8;
        int selHh = bodyImg != null ? Math.max((int)(bodyImg.getHeight() * zoom / 2) + 3, 8) : 8;

        AffineTransform old   = g2.getTransform();
        Stroke         oldStr = g2.getStroke();

        g2.translate(sx, sy);
        g2.rotate(Math.toRadians(obj.rotation));

        if (selected) {
            g2.setColor(new Color(80, 200, 255, 60));
            g2.fillRect(-selHw, -selHh, selHw * 2, selHh * 2);
        }

        drawComposite(g2, type, team);

        if (selected) {
            g2.setColor(new Color(80, 200, 255));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(-selHw, -selHh, selHw * 2, selHh * 2);
        }

        // Team dot below the object
        if (type.hasTeam()) {
            g2.setColor(teamColor(obj.team));
            g2.fillOval(-5, selHh - 3, 10, 10);
        }

        g2.setStroke(oldStr);
        g2.setTransform(old);
    }

    private void drawGhost(Graphics2D g2) {
        Composite      oldComp = g2.getComposite();
        AffineTransform old    = g2.getTransform();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));
        g2.translate(mouseX, mouseY);
        drawComposite(g2, paletteSelection, 0);
        g2.setTransform(old);
        g2.setComposite(oldComp);
    }

    /**
     * Draws the full multi-layer visual for an object type.
     * The Graphics2D transform must already be translated to the object's screen
     * centre and rotated. Each layer is drawn centred at (0,0).
     */
    private void drawComposite(Graphics2D g2, EditorObjectType type, int team) {
        switch (type) {
            case TANK -> {
                drawRaw(g2, RTSAssetManager.getTankChasis(team), type.visualScale);
                drawRaw(g2, RTSAssetManager.getTankTurret(team), type.visualScale);
            }
            case LIGHT_TANK -> {
                drawRaw(g2, RTSAssetManager.getLightTankHull(team),   type.visualScale);
                drawRaw(g2, RTSAssetManager.getLightTankTurret(team), type.visualScale);
            }
            case RIFLEMAN -> {
                drawRaw(g2, RTSAssetManager.infantryLegs, type.visualScale);
                BufferedImage[] f = RTSAssetManager.getRifleIdle(team);
                if (f != null && f.length > 0) drawRaw(g2, f[0], type.visualScale);
            }
            case BAZOOKAMAN -> {
                drawRaw(g2, RTSAssetManager.infantryLegs, type.visualScale);
                BufferedImage[] f = RTSAssetManager.getBazookaIdle(team);
                if (f != null && f.length > 0) drawRaw(g2, f[0], type.visualScale);
            }
            case HELICOPTER -> {
                drawRaw(g2, RTSAssetManager.getHellicopterBody(team),  type.visualScale);
                drawRaw(g2, RTSAssetManager.getHellicopterBlades(team), type.visualScale);
            }
            case APACHE -> {
                drawRaw(g2, RTSAssetManager.getApacheBody(team),  type.visualScale);
                drawRaw(g2, RTSAssetManager.getApacheBlades(team), type.visualScale);
            }
            case TRANSPORT_HELI -> {
                drawRaw(g2, RTSAssetManager.getTransportHeliBody(team), type.visualScale);
                drawRaw(g2, RTSAssetManager.getTransportHeliRoof(team), type.visualScale);
            }
            default -> drawRaw(g2, type.getRawImage(team), type.visualScale);
        }
    }

    /** Draws a raw (unscaled) image centred at (0,0) in current transform space. */
    private void drawRaw(Graphics2D g2, BufferedImage img, double scale) {
        if (img == null) return;
        int dw = Math.max(1, (int)(img.getWidth()  * scale * zoom));
        int dh = Math.max(1, (int)(img.getHeight() * scale * zoom));
        g2.drawImage(img, -dw / 2, -dh / 2, dw, dh, null);
    }

    // ── Camera helpers ────────────────────────────────────────────────────────

    private int    toScreenX(double wx) { return (int)((wx - camX) * zoom); }
    private int    toScreenY(double wy) { return (int)((wy - camY) * zoom); }
    private double toWorldX(int sx)     { return sx / zoom + camX; }
    private double toWorldY(int sy)     { return sy / zoom + camY; }

    private void fitView() {
        if (backgroundImage == null || getWidth() == 0 || getHeight() == 0) return;
        zoom = Math.min((double)(getWidth()  - 10) / backgroundImage.getWidth(),
                        (double)(getHeight() - 30) / backgroundImage.getHeight());
        camX = -((getWidth()  - backgroundImage.getWidth()  * zoom) / 2.0 / zoom);
        camY = -((getHeight() - backgroundImage.getHeight() * zoom) / 2.0 / zoom);
        fittedOnce = true;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private BufferedImage loadBg(String filename) {
        if (filename.equals("terrainPlaygroundHighground130.png")) return RTSAssetManager.grassBG;
        try {
            return ImageIO.read(new File(Main.getAssets() + "DemoAssets/TankGame/" + filename));
        } catch (IOException e) {
            return null;
        }
    }

    private void notifySelection() {
        if (onSelectionChanged != null) onSelectionChanged.run();
        updateStatus();
    }

    private void updateStatus() {
        if (statusUpdater == null) return;
        double wx = toWorldX(mouseX);
        double wy = toWorldY(mouseY);
        String sel = selectedObject != null ? selectedObject.type
                   : paletteSelection != null ? "[placing " + paletteSelection.displayName + "]"
                   : "none";
        statusUpdater.accept(String.format("World: (%.0f, %.0f)  |  Zoom: %.2fx  |  Selected: %s", wx, wy, zoom, sel));
    }

    private static Color teamColor(int team) {
        return switch (team) {
            case -1 -> Color.LIGHT_GRAY;
            case 0  -> new Color(0, 200, 0);
            case 1  -> Color.RED;
            case 2  -> Color.DARK_GRAY;
            case 3  -> new Color(200, 160, 80);
            case 4  -> new Color(180, 220, 255);
            default -> Color.WHITE;
        };
    }
}

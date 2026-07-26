/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * draws the green rectangular selection box
 *
 * @author Joseph
 */
public class SelectionBoxEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private transient Game game;

    public static Set<RTSUnit> selectedUnits = new CopyOnWriteArraySet<>();
    public static final Color uncontrollableColor = new Color(.5f, .5f, .5f, .8f);
    public static final Color selectionColor = new Color(0f, 1f, 0f, .8f);
    private static Rectangle selectionZone = null;
    private transient Stroke stroke3 = new BasicStroke(3);
    private transient Stroke stroke1 = new BasicStroke(1);

    // For serialization: store selected unit IDs
    private HashSet<String> selectedUnitIDsForSerialization = new HashSet<>();

    // Cached selection-ring bitmaps keyed by (diameter, color). Drawing a baked sprite avoids
    // re-tessellating an antialiased stroked oval per selected unit every frame. The ring is
    // held as a VolatileImage - the same accelerated path the engine's sprites and background
    // use - because this is blitted once per selected unit per frame, and a large selection
    // turns that into more pixels than the rest of the frame combined. The BufferedImage is
    // kept alongside it to rebuild from whenever the volatile surface is lost.
    private static final int RING_STROKE = 3;
    private static final int RING_PAD = 4;
    private static final HashMap<Long, BufferedImage> ringSourceCache = new HashMap<>();
    private static final HashMap<Long, VolatileImage> ringCache = new HashMap<>();

    public static VolatileImage getRingSprite(int diameter, Color color) {
        long key = ((long) diameter << 32) ^ (color.getRGB() & 0xffffffffL);
        BufferedImage source = ringSourceCache.get(key);
        if (source == null) {
            int size = diameter + RING_PAD * 2;
            source = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = source.createGraphics();
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ig.setStroke(new BasicStroke(RING_STROKE));
            ig.setColor(color);
            ig.drawOval(RING_PAD, RING_PAD, diameter, diameter);
            ig.dispose();
            ringSourceCache.put(key, source);
        }
        VolatileImage cached = ringCache.get(key);
        VolatileImage valid = cached == null
                ? Graphic.getVolatileFromBuffered(source)
                : Graphic.getValidatedVolatileImage(cached, source);
        if (valid != cached) {
            ringCache.put(key, valid);
        }
        return valid;
    }

    public SelectionBoxEffect(Game g) {
        game = g;
    }

    /**
     * Called before serialization to save selected unit IDs
     */
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        // Save selected unit IDs before serialization
        selectedUnitIDsForSerialization.clear();
        for (RTSUnit unit : selectedUnits) {
            if (unit.ID != null) {
                selectedUnitIDsForSerialization.add(unit.ID);
            }
        }
        out.defaultWriteObject();
    }

    /**
     * Called after deserialization to restore transient fields and selections
     */
    @Override
    public void onPostDeserialization(Game g) {
        // Restore game reference
        this.game = g;

        // Restore transient fields
        stroke3 = new BasicStroke(3);
        stroke1 = new BasicStroke(1);

        // Restore selections by finding units with matching IDs
        selectedUnits.clear();
        if (game != null && selectedUnitIDsForSerialization != null) {
            for (GameObject2 obj : game.getAllObjects()) {
                if (obj instanceof RTSUnit unit && obj.ID != null) {
                    if (selectedUnitIDsForSerialization.contains(obj.ID)) {
                        unit.setSelected(true);
                        selectedUnits.add(unit);
                    }
                }
            }
        }
    }

    @Override
    public int getZLayer() {
        // under ground units
        return 0;
    }

    @Override
    public void render(Graphics2D g) {
        drawSelectionBox(g);
        drawSelectionCirclesGround(g);
    }

    @Override
    public void tick() {
        Coordinate downLoc = RTSInput.getMouseDownLocation();
        Coordinate dragLoc = RTSInput.getMouseDraggedLocation();
        if (downLoc != null && dragLoc != null && selectionZone != null) {
            selectedUnits.forEach(x -> x.setSelected(false));
            selectedUnits.clear();
        }
        if (selectionZone != null) {
            for (GameObject2 go : game.getObjectsIntersectingArea(selectionZone)) {
                if (go instanceof RTSUnit) {
                    ((RTSUnit) go).setSelected(true);
                    selectedUnits.add((RTSUnit) go);
                }
            }
        }
    }

    private void drawSelectionBox(Graphics2D g) {
        g.setStroke(stroke1);
        Color originalColor = g.getColor();
        g.setColor(Color.green);
        Coordinate downLoc = RTSInput.getMouseDownLocation();
        Coordinate dragLoc = RTSInput.getMouseDraggedLocation();
        if (downLoc != null && dragLoc != null) {
            int mlX = dragLoc.x;
            int mlY = dragLoc.y;
            int width = Math.abs(mlX - downLoc.x);
            int height = Math.abs(mlY - downLoc.y);
            boolean down = false;
            boolean up = false;
            boolean right = false;
            boolean left = false;
            int buffer = 2;     //minimum distance the cursor must move before the selection box is drawn
            if (downLoc.x > mlX + buffer) {
                left = true;
            }
            if (downLoc.x < mlX - buffer) {
                right = true;
            }
            if (downLoc.y < mlY - buffer) {
                down = true;
            }
            if (downLoc.y > mlY + buffer) {
                up = true;
            }
            if (left && up) {
                selectionZone = new Rectangle(mlX, mlY, width, height);
            }
            if (left && down) {
                selectionZone = new Rectangle(mlX, mlY - height, width, height);
            }
            if (right && down) {
                selectionZone = new Rectangle(downLoc.x, downLoc.y, width, height);
            }
            if (right && up) {
                selectionZone = new Rectangle(downLoc.x, downLoc.y - height, width, height);
            }
            if (selectionZone != null) {
                g.drawRect(selectionZone.x, selectionZone.y, selectionZone.width, selectionZone.height);
            }
        } else {
            selectionZone = null;
        }
    }

    private void drawSelectionCirclesGround(Graphics2D g) {
        g.setStroke(stroke3);
        for (RTSUnit unit : selectedUnits) {
            if (unit.plane > 1) {
                continue;
            }
            if (unit.isSelected() && !unit.isRubble) {
                Color ringColor = selectionColor;
                if (ExternalCommunicator.isMultiplayer && ExternalCommunicator.localTeam != unit.team) {
                    ringColor = uncontrollableColor;
                }
                Coordinate coord = unit.getRenderLocation().toCoordinate();
                int sideLength = Math.max(unit.getWidth(), unit.getHeight());
                VolatileImage ring = getRingSprite(sideLength, ringColor);
                g.drawImage(ring, coord.x - ring.getWidth() / 2, coord.y - ring.getHeight() / 2, null);
                var desiredLoc = unit.getDesiredLocation();
                if (!unit.isCloseEnoughToDesired()) {
                    g.setColor(ringColor);
                    Coordinate lineStart = Coordinate.nearestPointOnCircle(coord, desiredLoc, sideLength / 2);
                    g.drawLine(lineStart.x, lineStart.y, desiredLoc.x, desiredLoc.y);
                }
            }
        }
    }

    public static Rectangle getSelectionZone() {
        if (selectionZone == null) {
            return null;
        }
        return new Rectangle(selectionZone.x, selectionZone.y, selectionZone.width, selectionZone.height);
    }
}

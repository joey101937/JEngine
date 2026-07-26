package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyBuildingRingEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private static final BasicStroke ringStroke = new BasicStroke(12);
    private static final AlphaComposite ringComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
    private static final AlphaComposite progressComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);
    /** Stroked ring outlines centered on the origin, keyed by capture radius. */
    private static final Map<Integer, Shape> ringOutlines = new ConcurrentHashMap<>();
    private static final int POINT_LIST_REFRESH_TICKS = 30;

    private transient Game game;
    /** Reinforcement points found in the world, so rendering does not scan every object each frame. */
    private transient volatile List<GameObject2> points;
    private transient int ticksSinceRefresh;

    @Override
    public int getZLayer() {
        return -200;
    }

    public KeyBuildingRingEffect (Game g) {
        game = g;
    }

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
        this.points = null;
    }

    @Override
    public void render(Graphics2D g) {
        List<GameObject2> current = points;
        if (current == null) {
            current = refreshPoints();
        }
        Rectangle view = game.getCamera().getFieldOfView();
        for (GameObject2 obj : current) {
            if (obj instanceof ReinforcementPoint rp) {
                renderCaptureRing(g, obj, rp, view);
            }
        }
    }

    @Override
    public void tick() {
        if (points == null || ++ticksSinceRefresh >= POINT_LIST_REFRESH_TICKS) {
            ticksSinceRefresh = 0;
            refreshPoints();
        }
    }

    private List<GameObject2> refreshPoints() {
        List<GameObject2> found = new ArrayList<>();
        for (GameObject2 obj : game.getAllObjects()) {
            if (obj instanceof ReinforcementPoint) {
                found.add(obj);
            }
        }
        points = found;
        return found;
    }

    private void renderCaptureRing(Graphics2D g, GameObject2 gameObject, ReinforcementPoint point, Rectangle view) {
        if(!point.isCapturable()) return;
        Coordinate pixelLocation = gameObject.getPixelLocation();
        int radius = (int) point.getCaptureRadius();
        // The ring reaches a full capture radius past the point itself, so it is culled against
        // its own bounds rather than the object's. Rasterizing an antialiased oval this large is
        // costly enough to be worth skipping whenever none of it can be seen.
        if (!view.intersects(pixelLocation.x - radius, pixelLocation.y - radius, radius * 2, radius * 2)) {
            return;
        }
        Color ringColor = point.getOwningTeam() == -1 ? Color.GRAY : RTSUnit.getColorFromTeam(point.getOwningTeam());

        Composite originalComposite = g.getComposite();
        Object originalAntialiasing = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

        // Fighting over a point usually puts the camera inside the ring, where the whole outline
        // sits offscreen and only the interior is in view.
        if (!isViewInsideCircle(view, pixelLocation, radius - ringStroke.getLineWidth() / 2)) {
            g.setComposite(ringComposite);
            g.setColor(ringColor);
            g.translate(pixelLocation.x, pixelLocation.y);
            g.fill(getRingOutline(radius));
            g.translate(-pixelLocation.x, -pixelLocation.y);
        }

        // Capture progress fills the contesting team's color outward from the center.
        double progress = point.getCaptureProgress();
        int capturingTeam = point.getCapturingTeam();
        if (progress > 0 && capturingTeam != -1) {
            int fillRadius = (int) (radius * Math.min(1.0, progress));
            g.setComposite(progressComposite);
            g.setColor(RTSUnit.getColorFromTeam(capturingTeam));
            // A 10% tint spanning thousands of pixels reads the same without an antialiased edge,
            // and once it covers the viewport a rect fill replaces rasterizing the circle at all.
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            if (isViewInsideCircle(view, pixelLocation, fillRadius)) {
                g.fillRect(view.x, view.y, view.width, view.height);
            } else {
                g.fillOval(pixelLocation.x - fillRadius, pixelLocation.y - fillRadius, fillRadius * 2, fillRadius * 2);
            }
        }

        if (originalAntialiasing != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalAntialiasing);
        }
        g.setComposite(originalComposite);
    }

    /** True when every corner of the view lies within radius of center. */
    private static boolean isViewInsideCircle(Rectangle view, Coordinate center, double radius) {
        if (radius <= 0) {
            return false;
        }
        double farthestX = Math.max(center.x - view.x, view.x + view.width - center.x);
        double farthestY = Math.max(center.y - view.y, view.y + view.height - center.y);
        return farthestX * farthestX + farthestY * farthestY <= radius * radius;
    }

    /**
     * The ring outline is stroked once per radius so each frame only fills a cached path
     * instead of re-running the stroker over a several-thousand-pixel circle.
     */
    private static Shape getRingOutline(int radius) {
        Shape outline = ringOutlines.get(radius);
        if (outline == null) {
            outline = ringStroke.createStrokedShape(new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2));
            ringOutlines.put(radius, outline);
        }
        return outline;
    }
}

package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Purely visual indicator shown client-side when the local player sets a preferred target.
 * Draws a dashed red square that expands and fades once over the target unit.
 * Never serialized — only triggered from input, never from network commands.
 */
public class PreferredTargetIndicator extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private static final long DURATION_MS = 500;
    private static final float STROKE_WIDTH = 1.6f;
    private static final int EXPAND_PX = 8;
    private static final float[] DASH = {4.5f, 3.5f};

    private static final Set<String> activeTargetIds = ConcurrentHashMap.newKeySet();

    public static boolean hasActiveIndicator(RTSUnit target) {
        return activeTargetIds.contains(target.ID);
    }

    private final transient RTSUnit target;
    private final transient Game game;
    private final long createdAt = System.currentTimeMillis();

    public PreferredTargetIndicator(Game game, RTSUnit target) {
        this.game = game;
        this.target = target;
        activeTargetIds.add(target.ID);
        game.addIndependentEffect(this);
    }

    private void remove() {
        if (target != null) activeTargetIds.remove(target.ID);
        game.removeIndependentEffect(this);
    }

    @Override
    public boolean shouldSerialize() {
        return false;
    }

    @Override
    public void onPostDeserialization(Game g) { }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - createdAt > DURATION_MS) {
            remove();
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (target == null || !target.isAlive() || target.isRubble) {
            remove();
            return;
        }

        long elapsed = System.currentTimeMillis() - createdAt;
        if (elapsed >= DURATION_MS) return;

        double progress = (double) elapsed / DURATION_MS;

        float alpha = (float) (1.0 - progress);
        int half = (int) (target.getSideLength() * 0.30 + progress * EXPAND_PX);

        Coordinate loc = target.getRenderLocation();

        Composite oldComposite = g.getComposite();
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, DASH, 0f));
        g.setColor(Color.RED);
        g.drawRect(loc.x - half, loc.y - half, half * 2, half * 2);

        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    @Override
    public int getZLayer() {
        return 200;
    }
}

package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class KeyBuildingRingEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private static final BasicStroke ringStroke = new BasicStroke(12);

    private transient Game game;

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
    }

    @Override
    public void render(Graphics2D g) {
        Rectangle view = game.getCamera().getFieldOfView();
        for (GameObject2 obj : game.getAllObjects()) {
            if (obj instanceof ReinforcementPoint rp) {
                renderCaptureRing(g, obj, rp, view);
            }
        }
    }

    @Override
    public void tick() {
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

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(ringColor);
        g.setStroke(ringStroke);
        g.drawOval(pixelLocation.x - radius, pixelLocation.y - radius, radius * 2, radius * 2);

        // Capture progress fills the contesting team's color outward from the center.
        double progress = point.getCaptureProgress();
        int capturingTeam = point.getCapturingTeam();
        if (progress > 0 && capturingTeam != -1) {
            int fillRadius = (int) (radius * Math.min(1.0, progress));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g.setColor(RTSUnit.getColorFromTeam(capturingTeam));
            g.fillOval(pixelLocation.x - fillRadius, pixelLocation.y - fillRadius, fillRadius * 2, fillRadius * 2);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}

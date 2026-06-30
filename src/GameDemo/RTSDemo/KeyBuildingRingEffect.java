package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class KeyBuildingRingEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

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
        for (GameObject2 obj : game.getAllObjects()) {
            if (obj instanceof ReinforcementPoint rp) {
                renderCaptureRing(g, obj, rp);
            }
        }
    }

    @Override
    public void tick() {
    }

    private void renderCaptureRing(Graphics2D g, GameObject2 gameObject, ReinforcementPoint point) {
        if(!point.isCapturable()) return;
        Coordinate pixelLocation = gameObject.getPixelLocation();
        Color ringColor = point.getOwningTeam() == -1 ? Color.GRAY : RTSUnit.getColorFromTeam(point.getOwningTeam());
        int radius = (int) point.getCaptureRadius();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(ringColor);
        g.setStroke(new BasicStroke(12));
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

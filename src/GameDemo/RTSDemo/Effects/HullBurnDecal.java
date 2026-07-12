package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.RenderHook;
import GameDemo.RTSDemo.RTSGame;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * A {@link RenderHook} that renders a scorched burn mark directly on a vehicle's hull.
 * <p>
 * Unlike {@link BurnMarkEffect} (which is a world-space {@code IndependentEffect}),
 * this decal is attached to the target unit and rendered between the hull sprite and
 * its turret SubObject, so it is always correctly layered without a z-layer number.
 * The mark follows the vehicle's position and rotation by storing the impact offset
 * in vehicle-local polar coordinates and recomputing the world position each frame
 * using {@code host.getRotationRealTime()}, giving lerp-smooth tracking.
 * <p>
 * Fades out over the specified duration using wall-clock time so the fade is smooth
 * regardless of game tick rate. Geometry is pre-computed once in the constructor and
 * seeded from the world position so the shape is stable across frames.
 * </p>
 */
public class HullBurnDecal extends RenderHook {

    private final double localOffsetDist;
    private final double localOffsetAngle;    // angle in radians, vehicle-body space
    private final double initialVehicleRotation; // degrees, at spawn time

    private final long startTimeMs;
    private final long durationMs;

    // Pre-computed geometry relative to (0,0)
    private final Path2D.Float mainShape;
    private final Path2D.Float outerScorch;
    private final int[] scatterX, scatterY, scatterRX, scatterRY;
    private final int[] streakEX, streakEY;
    private final float[] streakWidths;
    private final RadialGradientPaint gradient;

    public HullBurnDecal(Coordinate worldBurnPos, GameObject2 host, int radius, int durationTicks) {
        this.durationMs = (long)(durationTicks * 1000.0 / RTSGame.desiredTPS);
        this.startTimeMs = System.currentTimeMillis();

        Coordinate hostCenter = host.getPixelLocation();
        double dx = worldBurnPos.x - hostCenter.x;
        double dy = worldBurnPos.y - hostCenter.y;
        localOffsetDist = Math.sqrt(dx * dx + dy * dy);
        initialVehicleRotation = host.getRotation();
        localOffsetAngle = Math.atan2(dy, dx) - Math.toRadians(initialVehicleRotation);

        Random rand = new Random(worldBurnPos.x * 7919L + worldBurnPos.y * 6271L);
        double baseRotation = rand.nextDouble() * Math.PI * 2;

        int numSpikes = 13;
        mainShape = new Path2D.Float();
        for (int i = 0; i < numSpikes * 2; i++) {
            double angle = baseRotation + i * Math.PI / numSpikes;
            double r = (i % 2 == 0)
                ? radius * (0.72 + rand.nextDouble() * 0.32)
                : radius * (0.38 + rand.nextDouble() * 0.22);
            float px = (float)(Math.cos(angle) * r);
            float py = (float)(Math.sin(angle) * r);
            if (i == 0) mainShape.moveTo(px, py);
            else        mainShape.lineTo(px, py);
        }
        mainShape.closePath();

        int numOuter = 10;
        outerScorch = new Path2D.Float();
        for (int i = 0; i < numOuter * 2; i++) {
            double angle = baseRotation + i * Math.PI / numOuter + 0.15;
            double r = (i % 2 == 0)
                ? radius * (0.90 + rand.nextDouble() * 0.50)
                : radius * (0.60 + rand.nextDouble() * 0.25);
            float px = (float)(Math.cos(angle) * r);
            float py = (float)(Math.sin(angle) * r);
            if (i == 0) outerScorch.moveTo(px, py);
            else        outerScorch.lineTo(px, py);
        }
        outerScorch.closePath();

        int numScatter = 18;
        scatterX = new int[numScatter]; scatterY = new int[numScatter];
        scatterRX = new int[numScatter]; scatterRY = new int[numScatter];
        for (int i = 0; i < numScatter; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist  = radius * (0.55 + rand.nextDouble() * 0.80);
            scatterX[i]  = (int)(Math.cos(angle) * dist);
            scatterY[i]  = (int)(Math.sin(angle) * dist);
            scatterRX[i] = 2 + rand.nextInt(7);
            scatterRY[i] = 1 + rand.nextInt(4);
        }

        int numStreaks = 10;
        streakEX = new int[numStreaks]; streakEY = new int[numStreaks];
        streakWidths = new float[numStreaks];
        for (int i = 0; i < numStreaks; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            int len = (int)(radius * (0.6 + rand.nextDouble() * 1.0));
            streakEX[i] = (int)(Math.cos(angle) * len);
            streakEY[i] = (int)(Math.sin(angle) * len);
            streakWidths[i] = 0.6f + rand.nextFloat() * 1.8f;
        }

        float[] fractions = {0.0f, 0.18f, 0.52f, 1.0f};
        Color[] colors = {
            new Color(5,  3,  1,  230),
            new Color(16, 10, 4,  215),
            new Color(30, 20, 10, 150),
            new Color(36, 24, 12, 0)
        };
        gradient = new RadialGradientPaint(new Point2D.Float(0, 0), radius, fractions, colors);
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() >= startTimeMs + durationMs;
    }

    @Override
    public void render(Graphics2D g, GameObject2 host) {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        float alpha = 1.0f - (float) elapsed / durationMs;
        if (alpha <= 0) return;

        double currentRot = host.getRotationRealTime();
        double worldAngle = localOffsetAngle + Math.toRadians(currentRot);
        Coordinate renderCenter = host.getRenderLocation().toCoordinate();
        int cx = renderCenter.x + (int)(Math.cos(worldAngle) * localOffsetDist);
        int cy = renderCenter.y + (int)(Math.sin(worldAngle) * localOffsetDist);

        Composite     oldComposite = g.getComposite();
        Color         oldColor     = g.getColor();
        Paint         oldPaint     = g.getPaint();
        Stroke        oldStroke    = g.getStroke();
        AffineTransform oldTransform = g.getTransform();

        g.translate(cx, cy);
        g.rotate(Math.toRadians(currentRot - initialVehicleRotation));

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.38f));
        g.setColor(new Color(26, 17, 9));
        g.fill(outerScorch);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setPaint(gradient);
        g.fill(mainShape);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.52f));
        g.setColor(new Color(18, 11, 6));
        for (int i = 0; i < scatterX.length; i++) {
            g.fillOval(scatterX[i] - scatterRX[i], scatterY[i] - scatterRY[i],
                       scatterRX[i] * 2, scatterRY[i] * 2);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.25f));
        g.setColor(new Color(14, 9, 4));
        for (int i = 0; i < streakEX.length; i++) {
            g.setStroke(new BasicStroke(streakWidths[i]));
            g.drawLine(0, 0, streakEX[i], streakEY[i]);
        }

        g.setTransform(oldTransform);
        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setColor(oldColor);
        g.setComposite(oldComposite);
    }
}

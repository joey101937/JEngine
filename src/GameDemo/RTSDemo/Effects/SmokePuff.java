package GameDemo.RTSDemo.Effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * A single Java2D smoke particle: a soft radial-gradient puff that swells,
 * drifts, and fades over a fixed lifetime measured in game ticks. Shared by the
 * smoke effects in this package (one-shot poofs and continuous exhaust trails).
 * <p>
 * A puff is world-anchored at birth: it stores the world position it was emitted
 * from and evolves purely from the current game tick, so it stays put and drifts
 * on its own once released (it does not chase whatever spawned it). Purely
 * cosmetic — it carries no game state and is never serialized.
 */
class SmokePuff {
    private final double baseX, baseY;      // world position at birth
    private final long   birthTick;
    private final int    lifeTicks;
    private final double startRadius, endRadius;
    private final double driftX, driftY;    // world drift per tick
    private final double maxAlpha;
    private final int    baseGray;          // 0..255 core grey while young

    SmokePuff(double baseX, double baseY, long birthTick, int lifeTicks,
              double startRadius, double endRadius,
              double driftX, double driftY, double maxAlpha, int baseGray) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.birthTick = birthTick;
        this.lifeTicks = lifeTicks;
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.driftX = driftX;
        this.driftY = driftY;
        this.maxAlpha = maxAlpha;
        this.baseGray = baseGray;
    }

    boolean isDead(long currentTick) {
        return currentTick - birthTick >= lifeTicks;
    }

    /** ease-out (fast then settling): 1-(1-x)^2 */
    private static double easeOut(double x) {
        x = x < 0 ? 0 : (x > 1 ? 1 : x);
        return 1.0 - (1.0 - x) * (1.0 - x);
    }

    private static int clampByte(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    /**
     * Renders this puff for the given tick. Assumes {@code g} is already in world
     * space and antialiasing is on; leaves paint/composite as the caller set them
     * aside from restoring the composite it changed.
     */
    void render(Graphics2D g, long currentTick) {
        double age = currentTick - birthTick;
        if (age < 0 || age >= lifeTicks) return;
        double t = age / lifeTicks;                       // 0..1 across life
        double r = startRadius + (endRadius - startRadius) * easeOut(t);
        if (r <= 0.5) return;
        double cx = baseX + driftX * age;
        double cy = baseY + driftY * age;
        // fade in quickly, then ease back out over the remainder of the life
        double a = (t < 0.2) ? t / 0.2 : 1.0 - (t - 0.2) / 0.8;
        a = (a < 0 ? 0 : (a > 1 ? 1 : a)) * maxAlpha;
        if (a <= 0.01) return;
        // young puffs sit a touch lighter, cooling toward mid grey as they thin out
        int gray = clampByte(baseGray + (int) (25 * (1 - t)));

        Composite oldComposite = g.getComposite();
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(cx, cy), (float) r,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(gray, gray, clampByte(gray - 4), 230),
                            new Color(clampByte(gray - 18), clampByte(gray - 18), clampByte(gray - 22), 140),
                            new Color(clampByte(gray - 28), clampByte(gray - 28), clampByte(gray - 32), 0)}));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.setComposite(oldComposite);
    }
}

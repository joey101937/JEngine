package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.RTSGame;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * A brief, fully Java2D-drawn burst of mud and dirt kicked up when a shell strikes
 * soft ground or infantry. Implemented as an {@link IndependentEffect} so it has no
 * backing game object and is managed directly by the {@link Game}.
 * <p>
 * Drawn for a near-straight-down (nearly top-down) camera, so ejected soil is not
 * lofted along screen {@code -Y}; instead it sprays <em>radially outward across the
 * ground</em> in every direction from the impact point. The animation is fast and
 * front-loaded — grit flies out hard in the first fraction of the life, then slows
 * and fades — so it snaps rather than drifting in slow motion. It is composited
 * from four passes evolving over a normalized life {@code t} (0 to 1 across
 * {@code totalDurationTicks}):
 * <ul>
 *   <li>a spatter of dark, wet-soil blotches left on the ground at the impact
 *       point, which appear instantly and slowly soak away,</li>
 *   <li>an expanding, ground-hugging ring of brown dust that blooms outward and
 *       thins as it grows,</li>
 *   <li>heavier soil clods thrown a short way out from the centre, and</li>
 *   <li>a dense radial spray of fine grit, each grain drawn as a short streak that
 *       smears outward while it is moving fast and shrinks to a speck as it lands.</li>
 * </ul>
 * All geometry is seeded deterministically from the spawn position and tick, and
 * the animation is driven purely by the game tick (never wall-clock), so the shape
 * is stable frame-to-frame and matches across machines. Being cosmetic, it never
 * serializes and removes itself once the burst has settled.
 */
public class MudSplashEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private final transient Game game;
    private final Coordinate worldPos;
    private final double size;
    private final int zLayer;
    private final long startTick;
    private final int totalDurationTicks;

    // Ground soil blotches left at the impact point.
    private final double[] blotchX, blotchY, blotchRX, blotchRY, blotchShade;

    // Radial ejecta grains: angle, outward reach, thickness, stagger, brightness.
    // isCore marks heavier, shorter-thrown soil clods concentrated near the centre.
    private final double[] grainAngle, grainReach, grainSize, grainDelay, grainShade;
    private final boolean[] grainCore;

    private final int NUM_BLOTCH, NUM_GRAIN;

    // Muted, desaturated earthy palette.
    private static final Color SOIL_DARK = new Color(44, 33, 22);   // wet soil
    private static final Color SOIL_MID  = new Color(70, 55, 38);
    private static final Color DIRT_DUST = new Color(102, 85, 61);  // dry dirt/dust

    /**
     * @param game     the game to attach to
     * @param worldPos world-space center of the impact
     * @param size     rough radius of the splash in world pixels (a tank round reads well around 16-22)
     * @param zLayer   render layer (draw just above the ground/units so ejecta sits on top)
     */
    public MudSplashEffect(Game game, Coordinate worldPos, double size, int zLayer) {
        this.game = game;
        this.worldPos = new Coordinate(worldPos);
        this.size = size;
        this.zLayer = zLayer;
        this.startTick = game.getGameTickNumber();

        Random rand = new Random(worldPos.x * 7919L + worldPos.y * 6271L + startTick * 31L + 41L);

        NUM_BLOTCH = 6 + rand.nextInt(3);
        NUM_GRAIN  = 40 + rand.nextInt(16);

        // Ground blotches: small, low-lying, clustered near the impact.
        blotchX = new double[NUM_BLOTCH];
        blotchY = new double[NUM_BLOTCH];
        blotchRX = new double[NUM_BLOTCH];
        blotchRY = new double[NUM_BLOTCH];
        blotchShade = new double[NUM_BLOTCH];
        for (int i = 0; i < NUM_BLOTCH; i++) {
            double a = rand.nextDouble() * Math.PI * 2;
            double d = size * (0.06 + rand.nextDouble() * 0.5);
            blotchX[i] = Math.cos(a) * d;
            blotchY[i] = Math.sin(a) * d;
            blotchRX[i] = size * (0.09 + rand.nextDouble() * 0.15);
            blotchRY[i] = blotchRX[i] * (0.6 + rand.nextDouble() * 0.35);
            blotchShade[i] = 0.7 + rand.nextDouble() * 0.5;
        }

        // Radial ejecta: full-circle spray. Core clods are heavier and thrown less far;
        // the rest is fine grit flung further out.
        grainAngle = new double[NUM_GRAIN];
        grainReach = new double[NUM_GRAIN];
        grainSize = new double[NUM_GRAIN];
        grainDelay = new double[NUM_GRAIN];
        grainShade = new double[NUM_GRAIN];
        grainCore = new boolean[NUM_GRAIN];
        int coreCount = 7 + rand.nextInt(4);
        for (int i = 0; i < NUM_GRAIN; i++) {
            boolean core = i < coreCount;
            grainCore[i] = core;
            // spread around the circle but jittered so it never looks regular
            grainAngle[i] = (i / (double) NUM_GRAIN) * Math.PI * 2 + (rand.nextDouble() - 0.5) * 0.9;
            if (core) {
                grainReach[i] = size * (0.6 + rand.nextDouble() * 0.9);
                grainSize[i] = size * (0.08 + rand.nextDouble() * 0.08);
                grainShade[i] = 0.55 + rand.nextDouble() * 0.35; // darker wet soil
            } else {
                grainReach[i] = size * (1.2 + rand.nextDouble() * 1.7);
                grainSize[i] = size * (0.028 + rand.nextDouble() * 0.06);
                grainShade[i] = 0.6 + rand.nextDouble() * 0.7;
            }
            grainDelay[i] = rand.nextDouble() * 0.04;
        }

        // Short and snappy.
        this.totalDurationTicks = Math.max(4, (int) (RTSGame.desiredTPS * 0.4));
    }

    @Override public int     getZLayer()       { return zLayer; }
    @Override public boolean shouldSerialize() { return false; }

    @Override
    public void tick() {
        if (game.getGameTickNumber() - startTick >= totalDurationTicks) {
            game.removeIndependentEffect(this);
        }
    }

    /** ease-out (fast then settling): 1-(1-x)^2 */
    private static double easeOut(double x) {
        x = x < 0 ? 0 : (x > 1 ? 1 : x);
        return 1.0 - (1.0 - x) * (1.0 - x);
    }

    private static double clamp01(double x) {
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }

    /** normalized progress of a sub-phase running from lo to hi within [0,1] life */
    private static double phase(double t, double lo, double hi) {
        return clamp01((t - lo) / (hi - lo));
    }

    private static Color withAlpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * clamp01(a)));
    }

    /** scales a colour's brightness by f, preserving alpha (clamped) */
    private static Color shade(Color c, double f) {
        int r = (int) Math.max(0, Math.min(255, c.getRed()   * f));
        int g = (int) Math.max(0, Math.min(255, c.getGreen() * f));
        int b = (int) Math.max(0, Math.min(255, c.getBlue()  * f));
        return new Color(r, g, b);
    }

    @Override
    public void render(Graphics2D g) {
        long now = game.getGameTickNumber();
        double age = now - startTick;
        if (age < 0 || age >= totalDurationTicks) return;
        double t = age / totalDurationTicks;

        Composite       oldComposite = g.getComposite();
        Color           oldColor     = g.getColor();
        Paint           oldPaint     = g.getPaint();
        Stroke          oldStroke    = g.getStroke();
        AffineTransform oldTransform = g.getTransform();
        Object          oldAA        = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.translate(worldPos.x, worldPos.y);

        // --- Ground soil blotches: appear instantly, hold, then slowly soak away ---
        double blotchAlpha = 1.0 - phase(t, 0.45, 1.0);
        if (blotchAlpha > 0.01) {
            for (int i = 0; i < NUM_BLOTCH; i++) {
                g.setColor(withAlpha(SOIL_DARK, 0.5 * blotchShade[i]));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) blotchAlpha));
                g.fill(new Ellipse2D.Double(
                        blotchX[i] - blotchRX[i], blotchY[i] - blotchRY[i],
                        2 * blotchRX[i], 2 * blotchRY[i]));
            }
        }

        // --- Expanding dust ring: a soft brown annulus that grows outward and thins ---
        double ringT = phase(t, 0.0, 0.75);
        double ringAlpha = (1.0 - ringT) * 0.35;
        if (ringAlpha > 0.01) {
            double rOuter = size * (0.4 + 2.4 * easeOut(ringT));
            double rInner = rOuter * (0.35 + 0.4 * ringT);
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(0, 0), (float) rOuter,
                    new float[]{0f, (float) clamp01(rInner / rOuter), 0.85f, 1f},
                    new Color[]{withAlpha(SOIL_MID, 0.0),
                                withAlpha(SOIL_MID, 0.0),
                                withAlpha(DIRT_DUST, 0.6),
                                withAlpha(DIRT_DUST, 0.0)}));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) ringAlpha));
            g.fill(new Ellipse2D.Double(-rOuter, -rOuter, 2 * rOuter, 2 * rOuter));
        }

        // --- Radial ejecta grit: streaks that smear outward fast, then land as specks ---
        // outward = easeOut(p) * reach  → fast off the mark, decelerating as it lands.
        // The streak trails back toward the centre, long while moving fast, gone at rest.
        for (int i = 0; i < NUM_GRAIN; i++) {
            double p = phase(t, grainDelay[i], 1.0);
            if (p <= 0 || p >= 1) continue;
            double out = easeOut(p) * grainReach[i];
            double cos = Math.cos(grainAngle[i]);
            double sin = Math.sin(grainAngle[i]);
            double x = cos * out;
            double y = sin * out;

            double a = 1.0 - phase(t, 0.5, 1.0);
            if (a <= 0.01) continue;

            Color body = shade(grainCore[i] ? SOIL_DARK : DIRT_DUST, grainShade[i]);
            g.setColor(body);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));

            // streak length scales with remaining outward speed (proportional to 1-p)
            double streak = grainReach[i] * (1.0 - p) * (grainCore[i] ? 0.28 : 0.45);
            if (streak > grainSize[i]) {
                g.setStroke(new BasicStroke((float) (grainSize[i] * 1.7),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(x, y, x - cos * streak, y - sin * streak));
            } else {
                g.fill(new Ellipse2D.Double(x - grainSize[i], y - grainSize[i],
                        2 * grainSize[i], 2 * grainSize[i]));
            }
        }

        g.setTransform(oldTransform);
        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setColor(oldColor);
        g.setComposite(oldComposite);
        if (oldAA != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }
}

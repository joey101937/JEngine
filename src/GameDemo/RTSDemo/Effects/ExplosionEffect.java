package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
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
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * A short-lived, fully Java2D-drawn orange fireball explosion, rendered in world
 * space where a projectile detonated. Implemented as an {@link IndependentEffect}
 * so it has no backing game object and is managed directly by the {@link Game}.
 * <p>
 * The explosion is composited from several layered passes that each evolve over a
 * normalized life {@code t} (0 to 1 across {@code durationTicks}):
 * <ul>
 *   <li>an initial white/yellow muzzle flash that blooms and vanishes fast,</li>
 *   <li>an expanding radial-gradient fireball whose colour cools from white-hot to
 *       deep ember-red as it grows and rises slightly,</li>
 *   <li>a thin shockwave ring that races out ahead of the fireball,</li>
 *   <li>bright ember sparks flung outward under a little gravity, and</li>
 *   <li>dark smoke puffs that swell, drift upward, and linger as everything fades.</li>
 * </ul>
 * All geometry is seeded from the spawn position so the shape is deterministic and
 * stable frame-to-frame, and the animation is driven purely by tick count (never
 * wall-clock), so it stays in sync across machines. Being cosmetic, it does not
 * serialize.
 */
public class ExplosionEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private final transient Game game;
    private final Coordinate worldPos;
    private final int radius;
    private final int durationTicks;
    private final int zLayer;
    private int ticksElapsed = 0;

    // Deterministic fireball-lobe, ember, and smoke geometry, precomputed from the spawn position.
    private final double[] lobeAngle, lobeDist, lobeRad, lobePhase, lobeFlick, lobeShade;
    private final double[] emberAngle, emberDist, emberSize, emberDelay;
    private final boolean[] emberDebris; // dark-grey charred chunk instead of a glowing spark
    private final double[] smokeOffX, smokeOffY, smokeRad, smokeDrift, smokeDelay;

    private static final int NUM_LOBES = 8;
    private static final int NUM_EMBERS = 16;
    private static final int NUM_SMOKE = 16;

    /**
     * @param radius        approximate radius of the fireball at full bloom, in world pixels
     * @param durationTicks total lifetime before the effect removes itself
     * @param zLayer        render layer (explosions read best drawn on top of units)
     */
    public ExplosionEffect(Game game, Coordinate worldPos, int radius, int durationTicks, int zLayer) {
        this.game = game;
        this.worldPos = new Coordinate(worldPos);
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.zLayer = zLayer;

        Random rand = new Random(worldPos.x * 7919L + worldPos.y * 6271L + 31L);

        // Fire lobes: overlapping billows that break the fireball's silhouette off a clean disc.
        lobeAngle = new double[NUM_LOBES];
        lobeDist = new double[NUM_LOBES];
        lobeRad = new double[NUM_LOBES];
        lobePhase = new double[NUM_LOBES];
        lobeFlick = new double[NUM_LOBES];
        lobeShade = new double[NUM_LOBES];
        for (int i = 0; i < NUM_LOBES; i++) {
            // spread roughly evenly around the ring but jittered so it never looks regular
            lobeAngle[i] = (i / (double) NUM_LOBES) * Math.PI * 2 + (rand.nextDouble() - 0.5) * 0.9;
            lobeDist[i] = radius * (0.28 + rand.nextDouble() * 0.42);
            lobeRad[i] = radius * (0.42 + rand.nextDouble() * 0.34);
            lobePhase[i] = rand.nextDouble() * Math.PI * 2;
            lobeFlick[i] = 0.7 + rand.nextDouble() * 0.9;
            // per-lobe brightness: some billows burn bright, others are sooty/dark
            lobeShade[i] = 0.45 + rand.nextDouble() * 0.75;
        }

        emberAngle = new double[NUM_EMBERS];
        emberDist = new double[NUM_EMBERS];
        emberSize = new double[NUM_EMBERS];
        emberDelay = new double[NUM_EMBERS];
        emberDebris = new boolean[NUM_EMBERS];
        for (int i = 0; i < NUM_EMBERS; i++) {
            emberAngle[i] = rand.nextDouble() * Math.PI * 2;
            emberDist[i] = radius * (0.85 + rand.nextDouble() * 0.85);
            emberSize[i] = 0.9 + rand.nextDouble() * 1.7;
            emberDelay[i] = rand.nextDouble() * 0.08;
            emberDebris[i] = rand.nextDouble() < 0.45; // ~45% fall as dark charred chunks
        }

        smokeOffX = new double[NUM_SMOKE];
        smokeOffY = new double[NUM_SMOKE];
        smokeRad = new double[NUM_SMOKE];
        smokeDrift = new double[NUM_SMOKE];
        smokeDelay = new double[NUM_SMOKE];
        for (int i = 0; i < NUM_SMOKE; i++) {
            double a = rand.nextDouble() * Math.PI * 2;
            double d = radius * (0.10 + rand.nextDouble() * 0.50);
            smokeOffX[i] = Math.cos(a) * d;
            smokeOffY[i] = Math.sin(a) * d;
            smokeRad[i] = radius * (0.34 + rand.nextDouble() * 0.40);
            smokeDrift[i] = radius * (0.30 + rand.nextDouble() * 0.45);
            smokeDelay[i] = 0.12 + rand.nextDouble() * 0.30;
        }
    }

    @Override public int     getZLayer()       { return zLayer; }
    @Override public boolean shouldSerialize() { return false; }

    @Override
    public void tick() {
        ticksElapsed++;
        if (ticksElapsed >= durationTicks) {
            game.removeIndependentEffect(this);
        }
    }

    /** ease-out (fast then settling): 1-(1-x)^2 */
    private static double easeOut(double x) {
        x = Math.max(0.0, Math.min(1.0, x));
        return 1.0 - (1.0 - x) * (1.0 - x);
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }

    /** normalized progress of a sub-phase running from lo to hi within [0,1] life */
    private static double phase(double t, double lo, double hi) {
        return clamp01((t - lo) / (hi - lo));
    }

    /** darkens a colour by a multiplier (clamped), preserving alpha */
    private static Color scaleColor(Color c, double f) {
        int r = (int) Math.max(0, Math.min(255, c.getRed()   * f));
        int g = (int) Math.max(0, Math.min(255, c.getGreen() * f));
        int b = (int) Math.max(0, Math.min(255, c.getBlue()  * f));
        return new Color(r, g, b, c.getAlpha());
    }

    private static Color lerpColor(Color a, Color b, double f) {
        f = clamp01(f);
        return new Color(
                (int) (a.getRed()   + (b.getRed()   - a.getRed())   * f),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                (int) (a.getBlue()  + (b.getBlue()  - a.getBlue())  * f));
    }

    @Override
    public void render(Graphics2D g) {
        if (ticksElapsed >= durationTicks) return;
        double t = (double) ticksElapsed / durationTicks;

        Composite       oldComposite = g.getComposite();
        Color           oldColor     = g.getColor();
        Paint           oldPaint     = g.getPaint();
        Stroke          oldStroke    = g.getStroke();
        AffineTransform oldTransform = g.getTransform();
        Object          oldAA        = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.translate(worldPos.x, worldPos.y);

        // Fireball rises a touch as it burns.
        double rise = -radius * 0.18 * easeOut(t);

        // --- Smoke puffs (drawn first, underneath the fire): swell, rise, linger ---
        for (int i = 0; i < NUM_SMOKE; i++) {
            double st = phase(t, smokeDelay[i], 1.0);
            if (st <= 0) continue;
            double grow = 0.35 + 0.65 * easeOut(st);
            double r = smokeRad[i] * grow;
            double cx = smokeOffX[i];
            double cy = smokeOffY[i] + rise - smokeDrift[i] * easeOut(st);
            // fade in, hold, then fade out over the puff's own life
            double a = (st < 0.25) ? st / 0.25 : 1.0 - (st - 0.25) / 0.75;
            a = clamp01(a) * 0.62;
            if (a <= 0.01) continue;
            // young puffs billow up near-white, cooling to mid grey as they dissipate
            int gray = 150 + (int) (95 * (1 - st));
            if (gray > 245) gray = 245;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(cx, cy), (float) r,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{new Color(gray, gray, gray - 6, 235),
                                new Color(gray - 20, gray - 20, gray - 24, 150),
                                new Color(gray - 30, gray - 30, gray - 34, 0)}));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));
            g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        }

        // --- Muzzle flash: a big soft near-white bloom that snaps in and out early ---
        double flash = 1.0 - phase(t, 0.0, 0.16);
        if (flash > 0.01) {
            double fr = radius * (0.9 + 0.7 * phase(t, 0.0, 0.16));
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(0, rise), (float) fr,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{new Color(255, 250, 225, 255), new Color(255, 210, 120, 180), new Color(255, 170, 60, 0)}));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (flash * 0.9)));
            g.fill(new Ellipse2D.Double(-fr, rise - fr, 2 * fr, 2 * fr));
        }

        // --- Fireball: a cluster of overlapping billowing lobes so the silhouette is
        //     turbulent rather than a clean disc, with a brighter hot core on top ---
        double bloom = easeOut(phase(t, 0.0, 0.45));
        double fireAlpha = 1.0 - phase(t, 0.35, 0.95);
        if (fireAlpha > 0.01) {
            // cool from white-hot toward deep ember over the fireball's life
            double cool = phase(t, 0.0, 0.85);
            Color core = lerpColor(new Color(255, 248, 220), new Color(255, 150, 45), cool);
            Color mid  = lerpColor(new Color(255, 190, 70),  new Color(210, 70, 20),  cool);
            Color rim  = lerpColor(new Color(220, 90, 25),   new Color(110, 30, 12),  cool);
            Color rimClear = new Color(rim.getRed(), rim.getGreen(), rim.getBlue(), 0);

            // Outer billows: each lobe blooms and flickers on its own phase, and the lobes
            // push outward as the fireball grows, giving a lumpy, evolving outline. Per-lobe
            // shade makes some billows burn bright while others go sooty and dark, so the
            // orange breaks up instead of reading as one clean gradient.
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (fireAlpha * 0.9)));
            Color soot = lerpColor(new Color(80, 40, 20), new Color(35, 28, 26), cool);
            for (int i = 0; i < NUM_LOBES; i++) {
                double flick = 0.82 + 0.18 * Math.sin(ticksElapsed * 0.55 * lobeFlick[i] + lobePhase[i]);
                double lr = lobeRad[i] * (0.35 + 0.75 * bloom) * flick;
                if (lr <= 0.5) continue;
                double push = lobeDist[i] * (0.4 + 0.6 * bloom);
                double lx = Math.cos(lobeAngle[i]) * push;
                double ly = Math.sin(lobeAngle[i]) * push + rise;
                double shade = lobeShade[i];
                Color lmid = scaleColor(mid, shade);
                Color lrim = scaleColor(rim, shade);
                Color lclear = new Color(lrim.getRed(), lrim.getGreen(), lrim.getBlue(), 0);
                g.setPaint(new RadialGradientPaint(
                        new Point2D.Double(lx, ly), (float) lr,
                        new float[]{0f, 0.55f, 1f},
                        new Color[]{lmid, lrim, lclear}));
                g.fill(new Ellipse2D.Double(lx - lr, ly - lr, 2 * lr, 2 * lr));

                // dark soot pocket riding the darker billows, punching turbulence into the fire
                if (shade < 0.85) {
                    double sr = lr * 0.5;
                    double sx = lx + Math.cos(lobePhase[i]) * lr * 0.25;
                    double sy = ly + Math.sin(lobePhase[i]) * lr * 0.25;
                    g.setPaint(new RadialGradientPaint(
                            new Point2D.Double(sx, sy), (float) sr,
                            new float[]{0f, 1f},
                            new Color[]{soot, new Color(soot.getRed(), soot.getGreen(), soot.getBlue(), 0)}));
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (fireAlpha * 0.55)));
                    g.fill(new Ellipse2D.Double(sx - sr, sy - sr, 2 * sr, 2 * sr));
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (fireAlpha * 0.9)));
                }
            }

            // Hot core sits over the billows, keeping a bright centre.
            double coreR = radius * (0.22 + 0.55 * bloom);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fireAlpha));
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(0, rise), (float) coreR,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{core, mid, new Color(mid.getRed(), mid.getGreen(), mid.getBlue(), 0)}));
            g.fill(new Ellipse2D.Double(-coreR, rise - coreR, 2 * coreR, 2 * coreR));
        }

        // --- Shockwave ring: thin, races out ahead of the fireball, thins and fades ---
        double ringT = phase(t, 0.0, 0.5);
        double ringAlpha = 1.0 - ringT;
        if (ringAlpha > 0.01) {
            double ringR = radius * (0.35 + 1.35 * easeOut(ringT));
            float ringW = (float) (radius * 0.10 * (1.0 - ringT));
            if (ringW > 0.4f) {
                g.setStroke(new BasicStroke(ringW));
                g.setColor(new Color(255, 225, 170));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (ringAlpha * 0.6)));
                g.draw(new Ellipse2D.Double(-ringR, -ringR, 2 * ringR, 2 * ringR));
            }
        }

        // --- Ember sparks: bright points flung outward, arcing down under gravity ---
        double emberAlphaBase = 1.0 - phase(t, 0.25, 0.9);
        if (emberAlphaBase > 0.01) {
            for (int i = 0; i < NUM_EMBERS; i++) {
                double et = phase(t, emberDelay[i], 1.0);
                double travel = easeOut(et);
                double ex = Math.cos(emberAngle[i]) * emberDist[i] * travel;
                double ey = Math.sin(emberAngle[i]) * emberDist[i] * travel
                        + radius * 0.6 * et * et; // gravity pulls the arc down
                double sz = emberSize[i] * (1.0 - 0.5 * et);
                if (sz <= 0.3) continue;
                Color col;
                if (emberDebris[i]) {
                    // charred chunk: starts warm-dark and settles to dark grey as it falls
                    int gshade = 40 + (int) (25 * (1 - et));
                    col = lerpColor(new Color(70, 45, 30), new Color(gshade, gshade, gshade), phase(t, 0.0, 0.5));
                } else {
                    double cool = phase(t, 0.0, 0.7);
                    col = lerpColor(new Color(255, 240, 180), new Color(255, 120, 30), cool);
                }
                g.setColor(col);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) emberAlphaBase));
                g.fill(new Ellipse2D.Double(ex - sz, ey - sz, 2 * sz, 2 * sz));
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

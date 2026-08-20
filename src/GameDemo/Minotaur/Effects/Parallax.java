package GameDemo.Minotaur.Effects;

import Framework.DCoordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.Minotaur.MinotaurArt;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Depth behind the playfield. Three bands of silhouettes — a distant ridge, a middle field of
 * ruined columns, and a close band of rubble — each scrolling at its own fraction of the
 * camera's speed.
 *
 * Nothing here is an image. Each band is a handful of seeded polygons in "band space" that
 * tile every {@code period} pixels, so a six-thousand-pixel level costs a few hundred points
 * instead of a few hundred megabytes of backdrop.
 *
 * @author Joseph
 */
public class Parallax extends IndependentEffect {

    private static class Band {
        double factor;          // 0 = pinned to the camera, 1 = moves with the world
        int period;             // horizontal repeat length in band space
        Color color;
        List<Polygon> shapes = new ArrayList<>();
    }

    private final Game hostGame;
    private final int horizonY;
    private final List<Band> bands = new ArrayList<>();
    private final Color hazeColor;
    private final int worldHeight;

    /**
     * @param game     level this draws into
     * @param p        palette supplying the silhouette colours
     * @param horizonY world Y that the bands stand on
     * @param seed     shape seed; different per level so no two backdrops repeat
     */
    public Parallax(Game game, MinotaurArt.Palette p, int horizonY, long seed) {
        this.hostGame = game;
        this.horizonY = horizonY;
        this.worldHeight = game.getWorldHeight();
        this.hazeColor = new Color(p.haze.getRed(), p.haze.getGreen(), p.haze.getBlue(), 26);

        bands.add(ridge(0.16, 2600, p.farRock, 430, 210, seed));
        bands.add(ruins(0.36, 1750, p.midRock, seed + 31));
        bands.add(rubble(0.62, 1180, p.nearRock, seed + 97));
    }

    @Override
    public int getZLayer() { return -80; }

    /* ==================================================================
     *  band construction
     * ================================================================== */

    /** A continuous jagged skyline built as one polygon per period. */
    private Band ridge(double factor, int period, Color color, int height, int variance, long seed) {
        Band b = new Band();
        b.factor = factor; b.period = period; b.color = color;
        Random rng = new Random(seed);
        int steps = 26;
        int[] xs = new int[steps + 3];
        int[] ys = new int[steps + 3];
        // first and last sample share a height so consecutive copies join seamlessly
        int endHeight = height - variance / 2;
        for (int i = 0; i <= steps; i++) {
            xs[i] = (int) (i * (period / (double) steps));
            if (i == 0 || i == steps) {
                ys[i] = -endHeight;
            } else {
                double ramp = Math.sin(i / (double) steps * Math.PI);
                ys[i] = (int) -(height * 0.45 + ramp * height * 0.4 + rng.nextInt(variance) - variance * 0.35);
            }
        }
        xs[steps + 1] = period; ys[steps + 1] = worldHeight;
        xs[steps + 2] = 0;      ys[steps + 2] = worldHeight;
        b.shapes.add(new Polygon(xs, ys, xs.length));
        return b;
    }

    /** Broken columns and arch fragments standing on the horizon. */
    private Band ruins(double factor, int period, Color color, long seed) {
        Band b = new Band();
        b.factor = factor; b.period = period; b.color = color;
        Random rng = new Random(seed);
        int x = 40;
        while (x < period - 120) {
            int w = 46 + rng.nextInt(60);
            int h = 150 + rng.nextInt(300);
            if (rng.nextInt(4) == 0) {
                // an arch: two legs joined by a lintel, cut from one polygon
                int gap = w;
                int legW = 34;
                int total = legW * 2 + gap;
                b.shapes.add(new Polygon(
                        new int[]{x, x + legW, x + legW, x + legW + gap, x + legW + gap, x + total, x + total, x},
                        new int[]{0, 0, -h + 46, -h + 46, 0, 0, -h, -h}, 8));
                x += total + 60 + rng.nextInt(140);
            } else {
                int lean = rng.nextInt(3) - 1;
                b.shapes.add(new Polygon(
                        new int[]{x, x + w, x + w + lean * 10, x + lean * 10},
                        new int[]{0, 0, -h, -h}, 4));
                // broken capital
                b.shapes.add(new Polygon(
                        new int[]{x - 8 + lean * 10, x + w + 8 + lean * 10, x + w + 8 + lean * 10, x - 8 + lean * 10},
                        new int[]{-h, -h, -h - 22, -h - 22}, 4));
                x += w + 70 + rng.nextInt(190);
            }
        }
        return b;
    }

    /** Close boulders and slabs that sit just behind the play surface. */
    private Band rubble(double factor, int period, Color color, long seed) {
        Band b = new Band();
        b.factor = factor; b.period = period; b.color = color;
        Random rng = new Random(seed);
        int x = 0;
        while (x < period - 60) {
            int w = 70 + rng.nextInt(150);
            int h = 40 + rng.nextInt(120);
            b.shapes.add(new Polygon(
                    new int[]{x, x + w / 5, x + w - w / 6, x + w},
                    new int[]{0, -h, -h + rng.nextInt(24), 0}, 4));
            x += w + 50 + rng.nextInt(220);
        }
        return b;
    }

    /* ==================================================================
     *  drawing
     * ================================================================== */

    @Override
    public void tick() {
        // purely decorative; the camera drives everything
    }

    @Override
    public void render(Graphics2D g) {
        Rectangle view = hostGame.getCamera().getFieldOfView();
        DCoordinate cam = hostGame.getCamera().getWorldRenderLocation();
        Graphics2D g2 = (Graphics2D) g.create();
        // hard silhouette edges keep the backdrop in the same visual language as the pixel art
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        for (Band band : bands) {
            // a feature authored at band coordinate L lands at world X = camX*(1-f) + L,
            // which makes it drift across the screen at f times the world's rate
            double base = cam.x * (1.0 - band.factor);
            int first = (int) Math.floor((cam.x - base - band.period) / band.period);
            int last = (int) Math.ceil((cam.x + view.width - base) / band.period);
            for (int k = first; k <= last; k++) {
                int ox = (int) (base + (long) k * band.period);
                g2.translate(ox, horizonY);
                g2.setColor(band.color);
                for (Polygon poly : band.shapes) {
                    g2.fillPolygon(poly);
                }
                g2.translate(-ox, -horizonY);
            }
            // each band further away sits behind a little more atmosphere
            g2.setColor(hazeColor);
            g2.fillRect((int) cam.x, 0, view.width, worldHeight);
        }
        g2.dispose();
    }

    @Override
    public boolean shouldSerialize() { return false; }
}

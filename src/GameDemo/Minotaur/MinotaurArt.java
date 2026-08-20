package GameDemo.Minotaur;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Every image in the Minotaur game is produced here. Nothing in this class touches the
 * engine, so it can be rendered to disk by a scratch harness and eyeballed before it ever
 * runs in game.
 *
 * The art follows one rule that keeps the hand-drawn work and the original pixel-art
 * minotaur sheets looking like they belong together: <b>creatures and terrain are drawn at
 * one third scale with anti-aliasing off, then upscaled {@link #U}x with nearest-neighbour
 * sampling</b>, so every drawn shape lands on the same chunky pixel grid as the source
 * sprites. Light — slashes, fire, portals, orbs — is the deliberate exception and is drawn
 * smooth at full resolution, because glow reads as glow.
 *
 * @author Joseph
 */
public final class MinotaurArt {

    /** Pixel upscale factor. All low-resolution art is authored at 1/U of its final size. */
    public static final int U = 3;
    /** World tile size in final pixels. One tile is 16 authored pixels. */
    public static final int TILE = 16 * U;

    private MinotaurArt() {}

    /* ==================================================================
     *  palettes
     * ================================================================== */

    /** Colour scheme for one level: sky, silhouettes, stone and the accent light. */
    public static class Palette {
        public final String name;
        public final Color skyTop, skyBottom, haze;
        public final Color farRock, midRock, nearRock;
        public final Color stoneLight, stoneMid, stoneDark, stoneEdge;
        public final Color accent;
        public final boolean stars;

        public Palette(String name, Color skyTop, Color skyBottom, Color haze,
                       Color farRock, Color midRock, Color nearRock,
                       Color stoneLight, Color stoneMid, Color stoneDark, Color stoneEdge,
                       Color accent, boolean stars) {
            this.name = name;
            this.skyTop = skyTop; this.skyBottom = skyBottom; this.haze = haze;
            this.farRock = farRock; this.midRock = midRock; this.nearRock = nearRock;
            this.stoneLight = stoneLight; this.stoneMid = stoneMid;
            this.stoneDark = stoneDark; this.stoneEdge = stoneEdge;
            this.accent = accent; this.stars = stars;
        }
    }

    public static final Palette DESERT = new Palette("Sunscorched Ruins",
            new Color(58, 34, 74), new Color(232, 150, 92), new Color(255, 190, 130),
            new Color(120, 82, 104), new Color(150, 96, 84), new Color(96, 60, 52),
            new Color(198, 150, 96), new Color(146, 100, 60), new Color(84, 55, 36),
            new Color(52, 33, 24), new Color(255, 186, 92), false);

    public static final Palette CAVERN = new Palette("The Hollow Deep",
            new Color(9, 13, 26), new Color(28, 42, 70), new Color(70, 120, 170),
            new Color(26, 38, 62), new Color(38, 54, 84), new Color(24, 34, 54),
            new Color(96, 122, 168), new Color(58, 78, 116), new Color(31, 44, 70),
            new Color(16, 23, 38), new Color(110, 214, 255), true);

    public static final Palette EMBER = new Palette("Ember Halls",
            new Color(26, 10, 12), new Color(104, 26, 16), new Color(255, 128, 56),
            new Color(58, 22, 20), new Color(88, 34, 26), new Color(52, 20, 16),
            new Color(150, 74, 52), new Color(104, 46, 34), new Color(58, 26, 20),
            new Color(32, 14, 12), new Color(255, 132, 48), false);

    public static final Palette LABYRINTH = new Palette("Heart of the Labyrinth",
            new Color(16, 8, 28), new Color(52, 22, 78), new Color(160, 110, 235),
            new Color(38, 20, 58), new Color(56, 30, 84), new Color(32, 17, 50),
            new Color(120, 88, 168), new Color(78, 52, 118), new Color(44, 27, 68),
            new Color(24, 14, 38), new Color(199, 125, 255), true);

    /* ==================================================================
     *  low level helpers
     * ================================================================== */

    private static BufferedImage img(int w, int h) {
        return new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
    }

    /** Graphics for chunky authored art: no anti-aliasing, so shapes snap to the pixel grid. */
    private static Graphics2D crisp(BufferedImage bi) {
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        return g;
    }

    /** Graphics for light and energy: smooth, because glow should not be blocky. */
    private static Graphics2D smooth(BufferedImage bi) {
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g;
    }

    /** Nearest-neighbour magnification. Keeps authored pixels as hard squares. */
    public static BufferedImage upscale(BufferedImage src, int factor) {
        if (factor <= 1) return src;
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w * factor, h * factor, BufferedImage.TYPE_INT_ARGB);
        int[] row = new int[w];
        int[] wide = new int[w * factor];
        for (int y = 0; y < h; y++) {
            src.getRGB(0, y, w, 1, row, 0, w);
            for (int x = 0; x < w; x++) {
                int argb = row[x];
                int base = x * factor;
                for (int i = 0; i < factor; i++) wide[base + i] = argb;
            }
            for (int i = 0; i < factor; i++) {
                out.setRGB(0, y * factor + i, w * factor, 1, wide, 0, w * factor);
            }
        }
        return out;
    }

    public static BufferedImage[] upscale(BufferedImage[] src, int factor) {
        BufferedImage[] out = new BufferedImage[src.length];
        for (int i = 0; i < src.length; i++) out[i] = upscale(src[i], factor);
        return out;
    }

    /** Mirrors an image horizontally. */
    public static BufferedImage mirror(BufferedImage src) {
        BufferedImage out = img(src.getWidth(), src.getHeight());
        Graphics2D g = crisp(out);
        g.drawImage(src, src.getWidth(), 0, -src.getWidth(), src.getHeight(), null);
        g.dispose();
        return out;
    }

    public static BufferedImage[] mirror(BufferedImage[] src) {
        BufferedImage[] out = new BufferedImage[src.length];
        for (int i = 0; i < src.length; i++) out[i] = mirror(src[i]);
        return out;
    }

    /** Blends every opaque pixel toward {@code tint} and scales its brightness. */
    public static BufferedImage recolor(BufferedImage src, Color tint, double blend, double brightness) {
        BufferedImage out = img(src.getWidth(), src.getHeight());
        int tr = tint.getRed(), tg = tint.getGreen(), tb = tint.getBlue();
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int p = src.getRGB(x, y);
                int a = p >>> 24;
                if (a == 0) { out.setRGB(x, y, 0); continue; }
                int r = (p >> 16) & 255, gg = (p >> 8) & 255, b = p & 255;
                r = (int) ((r * (1 - blend) + tr * blend) * brightness);
                gg = (int) ((gg * (1 - blend) + tg * blend) * brightness);
                b = (int) ((b * (1 - blend) + tb * blend) * brightness);
                out.setRGB(x, y, (a << 24) | (clamp(r) << 16) | (clamp(gg) << 8) | clamp(b));
            }
        }
        return out;
    }

    public static BufferedImage[] recolor(BufferedImage[] src, Color tint, double blend, double brightness) {
        BufferedImage[] out = new BufferedImage[src.length];
        for (int i = 0; i < src.length; i++) out[i] = recolor(src[i], tint, blend, brightness);
        return out;
    }

    /** Solid silhouette of an image in one colour — used for after-images and shadows. */
    public static BufferedImage silhouette(BufferedImage src, Color c) {
        BufferedImage out = img(src.getWidth(), src.getHeight());
        int rgb = (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
        double alphaScale = c.getAlpha() / 255.0;
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int a = src.getRGB(x, y) >>> 24;
                if (a == 0) { out.setRGB(x, y, 0); continue; }
                out.setRGB(x, y, (((int) (a * alphaScale)) << 24) | rgb);
            }
        }
        return out;
    }

    private static int clamp(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }

    private static Color shade(Color c, double f) {
        return new Color(clamp((int) (c.getRed() * f)), clamp((int) (c.getGreen() * f)),
                clamp((int) (c.getBlue() * f)), c.getAlpha());
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clamp(a));
    }

    /** Filled oval with a one-pixel darker rim, the workhorse of the creature art. */
    private static void blob(Graphics2D g, int x, int y, int w, int h, Color fill, Color rim) {
        g.setColor(rim);
        g.fillOval(x - 1, y - 1, w + 2, h + 2);
        g.setColor(fill);
        g.fillOval(x, y, w, h);
    }

    private static void box(Graphics2D g, int x, int y, int w, int h, Color fill, Color rim) {
        g.setColor(rim);
        g.fillRect(x - 1, y - 1, w + 2, h + 2);
        g.setColor(fill);
        g.fillRect(x, y, w, h);
    }

    private static Polygon poly(int[] xs, int[] ys) {
        return new Polygon(xs, ys, xs.length);
    }

    /** Soft radial glow, for the smooth light pass. */
    private static void glow(Graphics2D g, double cx, double cy, double radius, Color core) {
        if (radius <= 0.5) return;
        RadialGradientPaint p = new RadialGradientPaint(
                new Point2D.Double(cx, cy), (float) radius,
                new float[]{0f, 0.45f, 1f},
                new Color[]{core, alpha(core, core.getAlpha() / 3), alpha(core, 0)});
        g.setPaint(p);
        g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
    }

    /* ==================================================================
     *  minotaur sprite-sheet normalisation
     *
     *  The source sheets are tightly cropped per frame, so the body drifts
     *  around inside each canvas — the swing wind-up frame is 100px wide with
     *  the torso sitting 25px right of where it sits in every other frame.
     *  These tables re-seat every frame onto one canvas whose horizontal centre
     *  IS the torso and whose foot line is fixed, which is what makes turning,
     *  swinging and dying read as one continuous character.
     * ================================================================== */

    /** Normalised authored canvas. Odd width so a horizontal mirror maps the torso onto itself. */
    public static final int SHEET_W = 85, SHEET_H = 54;
    /** Torso centre column and the ground line, in authored pixels. */
    public static final int SHEET_BODY_X = 42, SHEET_FOOT_Y = 46;

    /** Distance from the sprite centre down to the character's feet, in final pixels. */
    public static final int FOOT_OFFSET = (SHEET_FOOT_Y - SHEET_H / 2) * U;

    private static BufferedImage[] reseat(BufferedImage[] frames, int[] dx, int dy) {
        BufferedImage[] out = new BufferedImage[frames.length];
        for (int i = 0; i < frames.length; i++) {
            BufferedImage dst = img(SHEET_W, SHEET_H);
            Graphics2D g = crisp(dst);
            g.drawImage(frames[i], dx[Math.min(i, dx.length - 1)], dy, null);
            g.dispose();
            out[i] = dst;
        }
        return out;
    }

    private static int[] flat(int value, int count) {
        int[] out = new int[count];
        java.util.Arrays.fill(out, value);
        return out;
    }

    public static BufferedImage[] normalizeIdle(BufferedImage[] src) {
        return upscale(reseat(src, flat(25, src.length), 5), U);
    }

    public static BufferedImage[] normalizeRun(BufferedImage[] src) {
        return upscale(reseat(src, flat(23, src.length), 5), U);
    }

    /** The wind-up frame is authored 25px to the right of the rest of the swing. */
    public static BufferedImage[] normalizeSwing(BufferedImage[] src) {
        int[] dx = flat(25, src.length);
        if (dx.length > 0) dx[0] = 0;
        return upscale(reseat(src, dx, 2), U);
    }

    public static BufferedImage[] normalizeDeath(BufferedImage[] src) {
        return upscale(reseat(src, flat(25, src.length), 6), U);
    }

    /* ==================================================================
     *  creatures  (authored small, upscaled hard)
     * ================================================================== */

    /**
     * Imp: a knee-high crimson gremlin that closes the distance in hops. Four frames of a
     * hop cycle — crouch, launch, tuck, land — so its silhouette pulses even at a distance.
     */
    public static BufferedImage[] impFrames() {
        Color skin = new Color(196, 58, 52), skinDark = new Color(126, 30, 32);
        Color rim = new Color(48, 12, 16), belly = new Color(230, 130, 96);
        Color horn = new Color(232, 214, 178), eye = new Color(255, 214, 92);
        int[] lift = {0, -2, -4, -2};
        boolean[] tuck = {false, false, true, false};

        BufferedImage[] out = new BufferedImage[4];
        for (int f = 0; f < 4; f++) {
            BufferedImage bi = img(26, 26);
            Graphics2D g = crisp(bi);
            int b = lift[f];

            // legs: planted while crouching, folded under the body mid-hop
            if (tuck[f]) {
                box(g, 8, 20 + b, 4, 3, skinDark, rim);
                box(g, 14, 20 + b, 4, 3, skinDark, rim);
            } else {
                box(g, 7, 19 + b, 4, 6, skinDark, rim);
                box(g, 15, 19 + b, 4, 6, skinDark, rim);
                g.setColor(rim);
                g.fillRect(6, 24 + b, 6, 2);
                g.fillRect(14, 24 + b, 6, 2);
            }

            // tail flicking behind
            g.setColor(skinDark);
            g.fillRect(3, 15 + b, 4, 2);
            g.fillRect(1, 13 + b - (f % 2), 3, 2);

            blob(g, 6, 11 + b, 14, 10, skin, rim);          // torso
            g.setColor(belly);
            g.fillOval(9, 14 + b, 8, 6);

            g.setColor(skinDark);                             // arms
            g.fillRect(18, 13 + b, 3, 7);
            g.fillRect(5, 13 + b, 3, 6);
            g.setColor(horn);
            g.fillRect(19, 19 + b, 3, 2);                     // claws

            blob(g, 6, 2 + b, 15, 11, skin, rim);             // head
            g.setColor(rim);                                  // horns
            g.fillPolygon(poly(new int[]{7, 5, 9}, new int[]{4 + b, -1 + b, 3 + b}));
            g.fillPolygon(poly(new int[]{17, 20, 19}, new int[]{4 + b, -1 + b, 4 + b}));
            g.setColor(horn);
            g.fillPolygon(poly(new int[]{7, 6, 9}, new int[]{4 + b, 0 + b, 3 + b}));
            g.fillPolygon(poly(new int[]{17, 19, 19}, new int[]{4 + b, 0 + b, 4 + b}));

            g.setColor(rim);                                  // snout
            g.fillRect(18, 8 + b, 5, 4);
            g.setColor(skin);
            g.fillRect(18, 8 + b, 4, 3);

            g.setColor(eye);                                  // eyes
            g.fillRect(12, 6 + b, 4, 3);
            g.fillRect(17, 6 + b, 3, 3);
            g.setColor(rim);
            g.fillRect(14, 7 + b, 2, 2);
            g.fillRect(18, 7 + b, 2, 2);

            g.setColor(horn);                                 // teeth
            g.fillRect(19, 11 + b, 1, 2);
            g.fillRect(21, 11 + b, 1, 2);

            g.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /**
     * Bat: a leathery flier. Wings are drawn as three-segment membranes whose angle drives
     * the flap, and the body barely moves, which is what sells "hovering".
     */
    public static BufferedImage[] batFrames() {
        Color body = new Color(92, 70, 128), bodyDark = new Color(56, 40, 82);
        Color rim = new Color(26, 16, 42), wing = new Color(74, 52, 106);
        Color eye = new Color(255, 122, 122);
        double[] flap = {-0.85, -0.15, 0.55, -0.15};

        BufferedImage[] out = new BufferedImage[4];
        for (int f = 0; f < 4; f++) {
            BufferedImage bi = img(30, 22);
            Graphics2D g = crisp(bi);
            double a = flap[f];
            int lift = (int) Math.round(a * 2);

            for (int side = -1; side <= 1; side += 2) {
                int rootX = 15 + side * 4;
                int tipX = rootX + side * 11;
                int tipY = (int) (11 + a * 7);
                int midY = (int) (10 + a * 4);
                Polygon p = poly(
                        new int[]{rootX, rootX + side * 5, tipX, tipX - side * 2, rootX + side * 3},
                        new int[]{8 + lift, midY - 3, tipY, tipY + 5, 14 + lift});
                g.setColor(rim);
                g.fillPolygon(p);
                g.translate(0, 1);
                g.setColor(side < 0 ? shade(wing, 0.85) : wing);
                g.fillPolygon(p);
                g.translate(0, -1);
            }

            blob(g, 11, 6 + lift, 9, 11, body, rim);          // body
            g.setColor(bodyDark);
            g.fillOval(13, 11 + lift, 5, 5);

            g.setColor(rim);                                   // ears
            g.fillPolygon(poly(new int[]{12, 11, 15}, new int[]{7 + lift, 1 + lift, 6 + lift}));
            g.fillPolygon(poly(new int[]{18, 20, 16}, new int[]{7 + lift, 1 + lift, 6 + lift}));

            g.setColor(eye);
            g.fillRect(12, 8 + lift, 2, 2);
            g.fillRect(17, 8 + lift, 2, 2);
            g.setColor(new Color(255, 235, 235));
            g.fillRect(14, 11 + lift, 1, 2);                   // fang
            g.fillRect(16, 11 + lift, 1, 2);

            g.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /**
     * Spitter: a hooded cultist that keeps its distance and lobs fire. {@code casting}
     * swaps the idle bob for a braced pose with the orb wound up.
     */
    public static BufferedImage[] spitterFrames(boolean casting) {
        Color robe = new Color(62, 78, 96), robeDark = new Color(38, 50, 66);
        Color rim = new Color(18, 24, 34), trim = new Color(120, 200, 168);
        Color eye = new Color(150, 255, 210);
        int frames = casting ? 3 : 4;
        int[] bob = casting ? new int[]{0, -1, -1} : new int[]{0, -1, 0, 1};
        int[] orbSize = casting ? new int[]{3, 5, 7} : new int[]{3, 3, 4, 3};

        BufferedImage[] out = new BufferedImage[frames];
        for (int f = 0; f < frames; f++) {
            BufferedImage bi = img(26, 34);
            Graphics2D g = crisp(bi);
            int b = bob[f];
            int lean = casting ? f : 0;

            g.setColor(rim);                                   // robe hem
            g.fillRect(4, 30, 17, 4);
            g.setColor(robeDark);
            g.fillRect(5, 30, 15, 3);

            Polygon body = poly(                               // robe
                    new int[]{9 - lean, 16 - lean, 21, 4},
                    new int[]{9 + b, 9 + b, 31, 31});
            g.setColor(rim);
            g.fillPolygon(poly(new int[]{8 - lean, 17 - lean, 22, 3},
                    new int[]{8 + b, 8 + b, 32, 32}));
            g.setColor(robe);
            g.fillPolygon(body);
            g.setColor(robeDark);                              // fold
            g.fillRect(11, 16 + b, 2, 14);
            g.setColor(trim);                                  // sash
            g.fillRect(6, 20 + b, 13, 2);

            g.setColor(rim);                                   // hood
            g.fillOval(6 - lean, 2 + b, 15, 14);
            g.setColor(robe);
            g.fillOval(7 - lean, 3 + b, 13, 12);
            g.setColor(new Color(10, 14, 20));                 // shadowed face
            g.fillOval(10 - lean, 7 + b, 9, 7);
            g.setColor(eye);
            g.fillRect(12 - lean, 9 + b, 2, 2);
            g.fillRect(16 - lean, 9 + b, 2, 2);

            g.setColor(robeDark);                              // casting arm
            g.fillRect(17 - lean, 15 + b, 5, 3);

            int os = orbSize[f];                               // channelled ember
            g.setColor(new Color(60, 255, 190));
            g.fillOval(22 - os / 2, 16 + b - os / 2, os, os);
            g.setColor(new Color(220, 255, 240));
            g.fillRect(22 - 1, 16 + b - 1, 2, 2);

            g.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /**
     * Stone Sentinel: a hovering slab of masonry with detached fists. Nothing about it walks,
     * which is why it can be animated convincingly in four frames. {@code shattered} strips
     * the armour plating for the second half of its life.
     */
    public static BufferedImage[] golemFrames(boolean shattered, boolean punching) {
        Color stone = shattered ? new Color(104, 96, 92) : new Color(128, 124, 122);
        Color stoneDark = shade(stone, 0.62), rim = new Color(28, 26, 30);
        Color core = shattered ? new Color(255, 118, 58) : new Color(120, 210, 255);
        int frames = punching ? 3 : 4;
        int[] bob = punching ? new int[]{1, -1, 0} : new int[]{0, -1, -2, -1};
        int[] reach = punching ? new int[]{-4, 6, 12} : new int[]{0, 1, 0, -1};

        // canvas is padded wide enough that the fully extended punch stays inside it while the
        // torso still sits on the canvas centre, so the object's position is its body
        final int ox = 17;
        BufferedImage[] out = new BufferedImage[frames];
        for (int f = 0; f < frames; f++) {
            BufferedImage bi = img(76, 48);
            Graphics2D g = crisp(bi);
            g.translate(0, 6);                                 // headroom for the crest
            int b = bob[f];
            int r = reach[f];

            // shoulder bar first, so the floating fists read as attached to the same creature
            g.setColor(rim);
            g.fillRect(ox - 4, 17 + b, 46, 5);
            g.setColor(stoneDark);
            g.fillRect(ox - 3, 18 + b, 44, 3);

            box(g, ox + 8, 11 + b, 26, 22, stone, rim);        // torso slab
            g.setColor(rim);
            g.drawRect(ox + 8, 11 + b, 25, 21);                // heavier outline
            g.setColor(stoneDark);
            g.fillRect(ox + 8, 11 + b, 26, 4);
            g.fillRect(ox + 8, 29 + b, 26, 4);
            if (!shattered) {                                  // armour plating
                g.setColor(shade(stone, 1.24));
                g.fillRect(ox + 10, 17 + b, 10, 10);
                g.fillRect(ox + 22, 17 + b, 10, 10);
                g.setColor(rim);
                g.drawRect(ox + 10, 17 + b, 10, 10);
                g.drawRect(ox + 22, 17 + b, 10, 10);
            } else {
                g.setColor(rim);                               // fracture lines
                g.fillPolygon(poly(new int[]{ox + 13, ox + 20, ox + 17, ox + 24},
                        new int[]{13 + b, 21 + b, 22 + b, 32 + b}));
                g.fillRect(ox + 26, 15 + b, 2, 14);
            }

            g.setColor(rim);                                   // core socket
            g.fillRect(ox + 18, 19 + b, 8, 8);
            g.setColor(core);                                  // power core
            g.fillRect(ox + 19, 20 + b, 6, 6);
            g.setColor(new Color(255, 255, 255, 210));
            g.fillRect(ox + 20, 21 + b, 3, 3);

            box(g, ox + 13, 1 + b, 16, 11, stone, rim);        // head
            g.setColor(rim);
            g.fillRect(ox + 11, -2 + b, 20, 3);                // crest, for a readable silhouette
            g.setColor(shade(stone, 1.2));
            g.fillRect(ox + 12, -1 + b, 18, 2);
            g.setColor(stoneDark);
            g.fillRect(ox + 13, 9 + b, 16, 3);
            g.setColor(rim);
            g.fillRect(ox + 15, 4 + b, 5, 5);
            g.fillRect(ox + 23, 4 + b, 5, 5);
            g.setColor(core);
            g.fillRect(ox + 16, 5 + b, 3, 3);
            g.fillRect(ox + 24, 5 + b, 3, 3);

            box(g, ox - 9, 22 + b - r / 2, 11, 11, stone, rim);     // trailing fist
            box(g, ox + 33 + r, 20 + b - r / 3, 12, 12, stone, rim);// striking fist
            g.setColor(stoneDark);
            g.fillRect(ox + 33 + r, 28 + b - r / 3, 12, 4);
            g.setColor(shade(stone, 1.2));
            g.fillRect(ox + 35 + r, 22 + b - r / 3, 4, 4);

            g.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /* ==================================================================
     *  effects  (smooth light)
     * ================================================================== */

    // There is deliberately no slash-arc generator here. The minotaur swing sheet already draws
    // its own crescent, so a generated arc laid on top only read as a doubled trail; the heavy
    // attacks mark themselves with embers and sparks instead.

    /** Glowing projectile with a hot core and a soft corona. */
    public static BufferedImage energyBall(int size, Color core, Color halo) {
        BufferedImage bi = img(size, size);
        Graphics2D g = smooth(bi);
        glow(g, size / 2.0, size / 2.0, size / 2.0, alpha(halo, 190));
        glow(g, size / 2.0, size / 2.0, size / 3.4, alpha(core, 235));
        g.setColor(new Color(255, 255, 255, 235));
        g.fill(new Ellipse2D.Double(size / 2.0 - size / 9.0, size / 2.0 - size / 9.0,
                size / 4.5, size / 4.5));
        g.dispose();
        return bi;
    }

    /** Collectible orb — a bright pearl inside a coloured shell. */
    public static BufferedImage pickupOrb(Color core) {
        int s = 40;
        BufferedImage bi = img(s, s);
        Graphics2D g = smooth(bi);
        glow(g, s / 2.0, s / 2.0, s / 2.0, alpha(core, 150));
        g.setColor(alpha(core, 245));
        g.fill(new Ellipse2D.Double(9, 9, 22, 22));
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new Ellipse2D.Double(13, 12, 8, 7));
        g.dispose();
        return bi;
    }

    /**
     * The level exit: a standing rune gate whose interior swirls. Sealed gates are drawn
     * cold and dim; the same frames get re-tinted once the floor is cleared.
     */
    public static BufferedImage[] portalFrames(Palette p, boolean open) {
        int w = 150, h = 230, frames = 8;
        Color light = open ? p.accent : new Color(90, 96, 110);
        BufferedImage[] out = new BufferedImage[frames];
        for (int f = 0; f < frames; f++) {
            BufferedImage bi = img(w, h);
            Graphics2D g = smooth(bi);
            double t = f / (double) frames;

            if (open) glow(g, w / 2.0, h / 2.0, 78, alpha(light, 90));

            g.setColor(new Color(24, 22, 30, open ? 210 : 235));  // gateway void
            g.fill(new Ellipse2D.Double(28, 24, w - 56, h - 48));

            if (open) {                                           // swirling interior
                for (int i = 0; i < 5; i++) {
                    double phase = t * Math.PI * 2 + i * 1.25;
                    double rx = 40 - i * 5, ry = 92 - i * 13;
                    g.setStroke(new BasicStroke(4.5f - i * 0.5f));
                    g.setColor(alpha(light, 190 - i * 26));
                    g.draw(new Arc2D.Double(w / 2.0 - rx, h / 2.0 - ry, rx * 2, ry * 2,
                            Math.toDegrees(phase), 150, Arc2D.OPEN));
                }
                glow(g, w / 2.0, h / 2.0, 38, alpha(light, 130));
            }

            g.setStroke(new BasicStroke(9f));                     // stone arch
            g.setColor(shade(p.stoneMid, 0.9));
            g.draw(new Ellipse2D.Double(24, 20, w - 48, h - 40));
            g.setStroke(new BasicStroke(3.5f));
            g.setColor(open ? alpha(light, 220) : shade(p.stoneLight, 0.7));
            g.draw(new Ellipse2D.Double(24, 20, w - 48, h - 40));

            for (int i = 0; i < 6; i++) {                         // runes around the arch
                double ang = -Math.PI / 2 + (i - 2.5) * 0.52;
                double rx = w / 2.0 + Math.cos(ang) * (w / 2.0 - 24);
                double ry = h / 2.0 + Math.sin(ang) * (h / 2.0 - 20);
                double pulse = open ? 0.6 + 0.4 * Math.sin(t * Math.PI * 2 + i) : 0.25;
                g.setColor(alpha(light, (int) (230 * pulse)));
                g.fill(new Ellipse2D.Double(rx - 5, ry - 5, 10, 10));
            }
            g.dispose();
            out[f] = bi;
        }
        return out;
    }

    /** Expanding shock ring used for slams and the rage roar. */
    public static BufferedImage shockRing(int size, Color c) {
        BufferedImage bi = img(size, size);
        Graphics2D g = smooth(bi);
        g.setStroke(new BasicStroke(Math.max(3f, size / 22f)));
        g.setColor(alpha(c, 200));
        g.draw(new Ellipse2D.Double(6, 6, size - 12, size - 12));
        g.setStroke(new BasicStroke(Math.max(1.5f, size / 55f)));
        g.setColor(new Color(255, 255, 255, 170));
        g.draw(new Ellipse2D.Double(10, 10, size - 20, size - 20));
        g.dispose();
        return bi;
    }

    /* ==================================================================
     *  terrain
     * ================================================================== */

    /**
     * A block of level geometry. Authored one third size so the brick courses, the lit cap
     * and the speckled wear all land on the same pixel grid as the characters.
     *
     * @param w      final width in pixels (a multiple of {@link #U})
     * @param h      final height in pixels
     * @param p      level palette
     * @param capped whether the top edge gets the bright lip that marks a standable surface
     */
    public static BufferedImage platform(int w, int h, Palette p, boolean capped) {
        int lw = Math.max(4, w / U), lh = Math.max(3, h / U);
        BufferedImage bi = img(lw, lh);
        Graphics2D g = crisp(bi);
        Random rng = new Random(lw * 7919L + lh * 104729L + p.name.hashCode());

        g.setColor(p.stoneMid);
        g.fillRect(0, 0, lw, lh);

        for (int by = capped ? 5 : 0; by < lh; by += 8) {          // brick courses
            int offset = ((by / 8) % 2 == 0) ? 0 : 8;
            g.setColor(shade(p.stoneMid, 0.86));
            g.fillRect(0, by, lw, 1);
            for (int bx = offset; bx < lw; bx += 16) {
                g.fillRect(bx, by, 1, Math.min(8, lh - by));
            }
        }

        for (int i = 0; i < lw * lh / 22; i++) {                   // wear speckle
            int x = rng.nextInt(lw), y = rng.nextInt(lh);
            g.setColor(rng.nextBoolean() ? shade(p.stoneMid, 0.78) : shade(p.stoneMid, 1.14));
            g.fillRect(x, y, 1 + rng.nextInt(2), 1);
        }

        if (capped) {
            g.setColor(p.stoneLight);                              // lit lip
            g.fillRect(0, 0, lw, 3);
            g.setColor(shade(p.stoneLight, 1.22));
            g.fillRect(0, 0, lw, 1);
            g.setColor(shade(p.stoneMid, 0.7));
            g.fillRect(0, 4, lw, 1);
            for (int x = 0; x < lw; x += 3) {                      // crumbled edge
                if (rng.nextInt(3) == 0) {
                    g.setColor(p.stoneLight);
                    g.fillRect(x, -1, 2, 2);
                }
            }
        }

        g.setColor(p.stoneDark);                                   // grounding shadow
        g.fillRect(0, lh - 2, lw, 2);
        g.setColor(p.stoneEdge);                                   // outline
        g.drawRect(0, 0, lw - 1, lh - 1);
        g.dispose();
        return upscale(bi, U);
    }

    /** A one-way ledge: thin, obviously suspended, and lit only on top. */
    public static BufferedImage ledge(int w, Palette p) {
        int lw = Math.max(6, w / U), lh = 8;
        BufferedImage bi = img(lw, lh);
        Graphics2D g = crisp(bi);
        g.setColor(p.stoneMid);
        g.fillRect(0, 2, lw, 5);
        g.setColor(p.stoneLight);
        g.fillRect(0, 0, lw, 3);
        g.setColor(shade(p.stoneLight, 1.25));
        g.fillRect(0, 0, lw, 1);
        g.setColor(p.stoneDark);
        g.fillRect(0, 6, lw, 2);
        for (int x = 2; x < lw - 2; x += 5) {                      // bracket studs
            g.setColor(p.stoneEdge);
            g.fillRect(x, 5, 2, 3);
        }
        g.setColor(p.stoneEdge);
        g.drawRect(0, 0, lw - 1, lh - 1);
        g.dispose();
        return upscale(bi, U);
    }

    /** Floor spikes. Alternating heights so the row does not look stamped. */
    public static BufferedImage spikes(int w, Palette p) {
        int lw = Math.max(8, w / U), lh = 14;
        BufferedImage bi = img(lw, lh);
        Graphics2D g = crisp(bi);
        Random rng = new Random(lw * 31L + 17);
        g.setColor(p.stoneDark);
        g.fillRect(0, lh - 4, lw, 4);
        for (int x = 0; x <= lw - 6; x += 6) {
            int tip = 1 + rng.nextInt(3);
            g.setColor(new Color(198, 202, 214));
            g.fillPolygon(poly(new int[]{x, x + 3, x + 6}, new int[]{lh - 3, tip, lh - 3}));
            g.setColor(new Color(120, 126, 142));
            g.fillPolygon(poly(new int[]{x + 3, x + 6, x + 3}, new int[]{tip, lh - 3, lh - 3}));
        }
        g.setColor(p.stoneEdge);
        g.fillRect(0, lh - 2, lw, 2);
        g.dispose();
        return upscale(bi, U);
    }

    /** Molten surface for pit hazards — animated by cycling the returned frames. */
    public static BufferedImage[] lava(int w, Palette p) {
        int lw = Math.max(8, w / U), lh = 12, frames = 4;
        BufferedImage[] out = new BufferedImage[frames];
        for (int f = 0; f < frames; f++) {
            BufferedImage bi = img(lw, lh);
            Graphics2D g = crisp(bi);
            g.setColor(new Color(122, 22, 8));
            g.fillRect(0, 0, lw, lh);
            g.setColor(new Color(220, 70, 18));
            g.fillRect(0, 0, lw, 5);
            for (int x = 0; x < lw; x++) {                          // rolling surface
                int wave = (int) (1.6 * Math.sin((x + f * 3) * 0.55));
                g.setColor(new Color(255, 168, 54));
                g.fillRect(x, 1 + wave, 1, 2);
                if (((x + f * 5) % 11) == 0) {
                    g.setColor(new Color(255, 232, 150));
                    g.fillRect(x, wave, 1, 2);
                }
            }
            g.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /**
     * The body of a lava pool below its animated surface. Without this the pit reads as an
     * empty hole with a strip of fire floating over it.
     */
    public static BufferedImage lavaBody(int w, int h) {
        int lw = Math.max(4, w / U), lh = Math.max(4, h / U);
        BufferedImage bi = img(lw, lh);
        Graphics2D g = crisp(bi);
        Random rng = new Random(lw * 61L + lh);
        g.setColor(new Color(104, 18, 8));
        g.fillRect(0, 0, lw, lh);
        g.setColor(new Color(140, 28, 10));
        g.fillRect(0, 0, lw, Math.max(2, lh / 3));
        for (int i = 0; i < lw * lh / 60; i++) {          // slow convection blotches
            int x = rng.nextInt(lw), y = rng.nextInt(lh);
            g.setColor(rng.nextBoolean() ? new Color(150, 40, 14) : new Color(78, 12, 6));
            g.fillRect(x, y, 2 + rng.nextInt(4), 1 + rng.nextInt(2));
        }
        g.dispose();
        return upscale(bi, U);
    }

    /** Breakable barrel. Drawn rather than loaded so it matches whichever level it stands in. */
    public static BufferedImage barrel() {
        Color wood = new Color(146, 96, 48), woodDark = new Color(96, 58, 28);
        Color rim = new Color(44, 26, 14), band = new Color(122, 122, 132);
        BufferedImage bi = img(20, 24);
        Graphics2D g = crisp(bi);
        g.setColor(rim);
        g.fillRoundRect(1, 1, 18, 22, 6, 6);
        g.setColor(wood);
        g.fillRoundRect(2, 2, 16, 20, 5, 5);
        g.setColor(woodDark);
        for (int x = 5; x < 18; x += 4) g.fillRect(x, 3, 1, 18);
        g.setColor(band);
        g.fillRect(2, 6, 16, 3);
        g.fillRect(2, 15, 16, 3);
        g.setColor(shade(band, 1.3));
        g.fillRect(2, 6, 16, 1);
        g.fillRect(2, 15, 16, 1);
        g.setColor(new Color(255, 220, 170, 70));
        g.fillRect(4, 3, 2, 18);
        g.dispose();
        return upscale(bi, U);
    }

    /** Barrel bursting into staves. */
    public static BufferedImage[] barrelBreak() {
        Color wood = new Color(146, 96, 48), rim = new Color(44, 26, 14);
        BufferedImage[] out = new BufferedImage[4];
        Random rng = new Random(4242);
        for (int f = 0; f < 4; f++) {
            BufferedImage bi = img(28, 28);
            Graphics2D g = crisp(bi);
            int spread = 1 + f * 4;
            for (int i = 0; i < 7; i++) {
                double ang = i * (Math.PI * 2 / 7) + f * 0.15;
                int x = (int) (13 + Math.cos(ang) * spread);
                int y = (int) (15 + Math.sin(ang) * spread * 0.7) - f * 2;
                int sw = 2 + rng.nextInt(3), sh = 3 + rng.nextInt(4);
                g.setColor(rim);
                g.fillRect(x - 1, y - 1, sw + 2, sh + 2);
                g.setColor(shade(wood, 0.8 + (i % 3) * 0.16));
                g.fillRect(x, y, sw, sh);
            }
            g.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /** Background scenery: broken pillars, braziers and bones that sell each level's theme. */
    public static BufferedImage pillar(int heightTiles, Palette p) {
        int lw = 18, lh = heightTiles * 16;
        BufferedImage bi = img(lw, lh);
        Graphics2D g = crisp(bi);
        Random rng = new Random(heightTiles * 977L + p.name.hashCode());
        g.setColor(p.stoneEdge);
        g.fillRect(2, 0, 14, lh);
        g.setColor(shade(p.stoneMid, 0.9));
        g.fillRect(3, 0, 12, lh);
        g.setColor(shade(p.stoneLight, 0.9));
        g.fillRect(3, 0, 4, lh);
        for (int y = 4; y < lh; y += 14) {                        // drum joints
            g.setColor(p.stoneEdge);
            g.fillRect(3, y, 12, 1);
        }
        g.setColor(p.stoneEdge);                                   // chipped top
        for (int i = 0; i < 5; i++) g.fillRect(3 + rng.nextInt(10), rng.nextInt(3), 2, 2);
        g.setColor(shade(p.stoneMid, 1.05));                       // capital and base
        g.fillRect(0, lh - 6, lw, 6);
        g.fillRect(0, 0, lw, 4);
        g.setColor(p.stoneEdge);
        g.drawRect(0, lh - 6, lw - 1, 5);
        g.drawRect(0, 0, lw - 1, 3);
        g.dispose();
        return upscale(bi, U);
    }

    /** Wall torch whose flame is animated by frame cycling. */
    public static BufferedImage[] brazier(Palette p) {
        BufferedImage[] out = new BufferedImage[4];
        for (int f = 0; f < 4; f++) {
            BufferedImage bi = img(16, 26);
            Graphics2D g = crisp(bi);
            g.setColor(p.stoneEdge);                               // bowl
            g.fillRect(4, 16, 8, 10);
            g.setColor(p.stoneMid);
            g.fillRect(5, 17, 6, 8);
            g.setColor(shade(p.stoneLight, 1.05));
            g.fillRect(3, 14, 10, 3);
            g.dispose();

            Graphics2D gs = smooth(bi);                            // flame is light, so smooth
            double t = f / 4.0;
            int lick = (int) (2 * Math.sin(t * Math.PI * 2));
            glow(gs, 8, 11 - lick, 9, new Color(255, 150, 40, 150));
            gs.setColor(new Color(255, 176, 60, 235));
            gs.fill(new Ellipse2D.Double(4.5, 5 - lick, 7, 12));
            gs.setColor(new Color(255, 236, 150, 245));
            gs.fill(new Ellipse2D.Double(6.2, 8 - lick, 3.6, 7));
            gs.dispose();
            out[f] = bi;
        }
        return upscale(out, U);
    }

    /* ==================================================================
     *  world backdrop
     * ================================================================== */

    /**
     * The level backdrop. Deliberately featureless beyond the sky wash and its light source:
     * every landmark the player can use to judge distance lives in the parallax layers, which
     * scroll at their own rates. Drawn opaque so the engine never composites it.
     */
    public static BufferedImage background(int w, int h, Palette p) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = smooth(bi);
        g.setPaint(new GradientPaint(0, 0, p.skyTop, 0, h * 0.86f, p.skyBottom));
        g.fillRect(0, 0, w, h);

        if (p.stars) {
            Random rng = new Random(p.name.hashCode());
            for (int i = 0; i < w / 9; i++) {
                int x = rng.nextInt(w), y = rng.nextInt((int) (h * 0.72));
                int b = 110 + rng.nextInt(130);
                g.setColor(new Color(b, b, Math.min(255, b + 34), 150 + rng.nextInt(100)));
                g.fillRect(x, y, 1 + rng.nextInt(2), 1 + rng.nextInt(2));
            }
        }

        // a single warm light source low on the horizon, giving the level a direction
        glow(g, w * 0.5, h * 0.80, h * 0.62, alpha(p.haze, 60));
        g.setPaint(new GradientPaint(0, h * 0.55f, new Color(0, 0, 0, 0), 0, h, alpha(shade(p.nearRock, 0.5), 190)));
        g.fillRect(0, (int) (h * 0.55), w, (int) (h * 0.45));
        g.dispose();
        return bi;
    }
}

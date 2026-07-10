package GameDemo.SpaceInvadersDemo;

import Framework.GraphicalAssets.Sprite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Every visual in the Space Invaders demo is drawn here with Java2D — no image
 * files are loaded. Sprites are built once at startup and shared: all grunts point
 * at the same {@link Sprite}, etc. (per the engine's "scale once, reuse" rule, we
 * bake the intended size into each image and never mutate the shared sprites).
 *
 * @author Joseph
 */
public final class SIAssets {

    // shared, immutable-once-built sprites
    public static Sprite player;
    public static Sprite grunt;
    public static Sprite diver;
    public static Sprite tank;
    public static Sprite boss;

    public static Sprite playerLaser;
    public static Sprite spreadLaser;
    public static Sprite enemyLaser;
    public static Sprite bossOrb;

    // one per PowerUp.Type ordinal
    public static Sprite[] powerups;

    public static BufferedImage background;

    private SIAssets() {}

    private static boolean built = false;

    public static void initialize(int worldWidth, int worldHeight) {
        if (built) return;
        built = true;

        player      = new Sprite(drawPlayer());
        grunt       = new Sprite(drawGrunt());
        diver       = new Sprite(drawDiver());
        tank        = new Sprite(drawTank());
        boss        = new Sprite(drawBoss());

        playerLaser = new Sprite(drawLaser(new Color(120, 255, 255), new Color(255, 255, 255), 9, 30));
        spreadLaser = new Sprite(drawLaser(new Color(255, 230, 120), new Color(255, 255, 255), 9, 26));
        enemyLaser  = new Sprite(drawLaser(new Color(255, 120, 150), new Color(255, 220, 230), 9, 22));
        bossOrb     = new Sprite(drawBossOrb());

        powerups = new Sprite[PowerUp.Type.values().length];
        for (PowerUp.Type t : PowerUp.Type.values()) {
            powerups[t.ordinal()] = new Sprite(drawPowerUp(t));
        }

        background = drawBackground(worldWidth, worldHeight);
    }

    /* ===================== helpers ===================== */

    private static BufferedImage img(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    private static Graphics2D prep(BufferedImage bi) {
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g;
    }

    /** Soft radial glow blob centered at (cx,cy). */
    private static void glow(Graphics2D g, double cx, double cy, double radius, Color core) {
        if (radius <= 0) return;
        Color edge = new Color(core.getRed(), core.getGreen(), core.getBlue(), 0);
        RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Double(cx, cy), (float) radius,
                new float[]{0f, 1f}, new Color[]{core, edge});
        g.setPaint(paint);
        g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
    }

    private static Polygon poly(int[] xs, int[] ys) {
        return new Polygon(xs, ys, xs.length);
    }

    /* ===================== player ===================== */

    private static BufferedImage drawPlayer() {
        int w = 58, h = 60;
        BufferedImage bi = img(w, h);
        Graphics2D g = prep(bi);
        int cx = w / 2;

        // engine glow at the rear
        glow(g, cx, h - 6, 16, new Color(120, 220, 255, 150));

        // main hull (arrow pointing up)
        Polygon hull = poly(
                new int[]{cx, cx + 20, cx + 13, cx, cx - 13, cx - 20},
                new int[]{2,  h - 16,  h - 8,   h - 14, h - 8, h - 16});
        g.setPaint(new GradientPaint(0, 0, new Color(90, 220, 255), 0, h, new Color(30, 90, 160)));
        g.fill(hull);
        g.setColor(new Color(210, 250, 255));
        g.setStroke(new BasicStroke(2f));
        g.draw(hull);

        // wing accents
        g.setColor(new Color(20, 60, 110));
        g.fill(poly(new int[]{cx - 20, cx - 8, cx - 8}, new int[]{h - 16, h - 26, h - 8}));
        g.fill(poly(new int[]{cx + 20, cx + 8, cx + 8}, new int[]{h - 16, h - 26, h - 8}));

        // cockpit
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, 22), 9f,
                new float[]{0f, 1f}, new Color[]{new Color(230, 255, 255), new Color(40, 120, 200)}));
        g.fill(new Ellipse2D.Double(cx - 7, 15, 14, 20));

        // nose highlight
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(poly(new int[]{cx, cx + 3, cx - 3}, new int[]{4, 16, 16}));

        g.dispose();
        return bi;
    }

    /* ===================== aliens ===================== */

    private static BufferedImage drawGrunt() {
        int w = 46, h = 40;
        BufferedImage bi = img(w, h);
        Graphics2D g = prep(bi);
        int cx = w / 2;
        glow(g, cx, h / 2.0, 20, new Color(200, 80, 255, 60));

        // body
        g.setPaint(new GradientPaint(0, 0, new Color(190, 90, 240), 0, h, new Color(90, 30, 130)));
        g.fill(new Ellipse2D.Double(6, 6, w - 12, h - 16));
        // dome
        g.setColor(new Color(230, 170, 255));
        g.fill(new Ellipse2D.Double(cx - 8, 3, 16, 16));
        // eyes
        g.setColor(new Color(20, 255, 180));
        g.fill(new Ellipse2D.Double(cx - 9, 14, 6, 6));
        g.fill(new Ellipse2D.Double(cx + 3, 14, 6, 6));
        // legs
        g.setColor(new Color(150, 60, 210));
        g.setStroke(new BasicStroke(3f));
        for (int i = -1; i <= 1; i++) {
            g.drawLine(cx + i * 10, h - 12, cx + i * 10 - 3, h - 2);
        }
        g.dispose();
        return bi;
    }

    private static BufferedImage drawDiver() {
        int w = 42, h = 46;
        BufferedImage bi = img(w, h);
        Graphics2D g = prep(bi);
        int cx = w / 2;
        glow(g, cx, h / 2.0, 20, new Color(80, 255, 140, 60));

        // sleek downward dart
        Polygon body = poly(
                new int[]{cx, cx + 16, cx + 6, cx, cx - 6, cx - 16},
                new int[]{h - 2, 12, 6, 14, 6, 12});
        g.setPaint(new GradientPaint(0, h, new Color(120, 255, 150), 0, 0, new Color(20, 140, 80)));
        g.fill(body);
        g.setColor(new Color(200, 255, 210));
        g.setStroke(new BasicStroke(2f));
        g.draw(body);
        // eye
        g.setColor(new Color(255, 60, 60));
        g.fill(new Ellipse2D.Double(cx - 5, 12, 10, 10));
        g.dispose();
        return bi;
    }

    private static BufferedImage drawTank() {
        int w = 60, h = 54;
        BufferedImage bi = img(w, h);
        Graphics2D g = prep(bi);
        int cx = w / 2;
        glow(g, cx, h / 2.0, 26, new Color(255, 140, 40, 60));

        // heavy hexagonal hull
        Polygon hull = poly(
                new int[]{cx - 14, cx + 14, cx + 26, cx + 14, cx - 14, cx - 26},
                new int[]{6, 6, h / 2, h - 8, h - 8, h / 2});
        g.setPaint(new GradientPaint(0, 0, new Color(255, 170, 70), 0, h, new Color(150, 60, 20)));
        g.fill(hull);
        g.setColor(new Color(70, 30, 10));
        g.setStroke(new BasicStroke(3f));
        g.draw(hull);
        // armored plating lines
        g.setColor(new Color(255, 210, 150, 180));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(cx - 12, 16, cx + 12, 16);
        g.drawLine(cx - 16, h - 18, cx + 16, h - 18);
        // core eye
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, h / 2.0), 11f,
                new float[]{0f, 1f}, new Color[]{new Color(255, 255, 200), new Color(220, 60, 20)}));
        g.fill(new Ellipse2D.Double(cx - 9, h / 2.0 - 9, 18, 18));
        g.dispose();
        return bi;
    }

    private static BufferedImage drawBoss() {
        int w = 260, h = 180;
        BufferedImage bi = img(w, h);
        Graphics2D g = prep(bi);
        int cx = w / 2;

        glow(g, cx, h / 2.0, 120, new Color(255, 40, 60, 45));

        // vast dreadnought hull
        Polygon hull = poly(
                new int[]{cx - 120, cx - 60, cx + 60, cx + 120, cx + 70, cx - 70},
                new int[]{40, 8, 8, 40, h - 20, h - 20});
        g.setPaint(new GradientPaint(0, 0, new Color(120, 130, 155), 0, h, new Color(40, 45, 65)));
        g.fill(hull);
        g.setColor(new Color(20, 22, 34));
        g.setStroke(new BasicStroke(4f));
        g.draw(hull);

        // side cannon pods
        g.setColor(new Color(70, 75, 95));
        g.fill(new Ellipse2D.Double(cx - 128, h / 2.0 - 20, 46, 46));
        g.fill(new Ellipse2D.Double(cx + 82, h / 2.0 - 20, 46, 46));
        g.setColor(new Color(255, 120, 60));
        g.fill(new Ellipse2D.Double(cx - 112, h / 2.0 - 6, 18, 18));
        g.fill(new Ellipse2D.Double(cx + 96, h / 2.0 - 6, 18, 18));

        // armored ridges
        g.setColor(new Color(160, 170, 200, 160));
        g.setStroke(new BasicStroke(3f));
        g.drawLine(cx - 60, 24, cx + 60, 24);
        g.drawLine(cx - 90, 52, cx + 90, 52);

        // glowing central core (the weak point)
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, h / 2.0 + 6), 44f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(255, 255, 220), new Color(255, 70, 60), new Color(120, 10, 20)}));
        g.fill(new Ellipse2D.Double(cx - 40, h / 2.0 - 34, 80, 80));
        g.setColor(new Color(255, 200, 180));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(cx - 40, h / 2.0 - 34, 80, 80));

        g.dispose();
        return bi;
    }

    /* ===================== projectiles ===================== */

    private static BufferedImage drawLaser(Color outer, Color core, int w, int h) {
        BufferedImage bi = img(w + 8, h + 8);
        Graphics2D g = prep(bi);
        double cx = bi.getWidth() / 2.0, cy = bi.getHeight() / 2.0;
        glow(g, cx, cy, (w + 8) / 2.0, new Color(outer.getRed(), outer.getGreen(), outer.getBlue(), 120));
        g.setColor(outer);
        g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - w / 2.0, cy - h / 2.0, w, h, w, w));
        g.setColor(core);
        g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - w / 4.0, cy - h / 2.0 + 2, w / 2.0, h - 4, w / 2.0, w / 2.0));
        g.dispose();
        return bi;
    }

    private static BufferedImage drawBossOrb() {
        int d = 26;
        BufferedImage bi = img(d + 8, d + 8);
        Graphics2D g = prep(bi);
        double c = bi.getWidth() / 2.0;
        glow(g, c, c, c, new Color(255, 90, 60, 150));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(c, c), (float) (d / 2.0),
                new float[]{0f, 1f}, new Color[]{new Color(255, 240, 180), new Color(230, 40, 30)}));
        g.fill(new Ellipse2D.Double(c - d / 2.0, c - d / 2.0, d, d));
        g.dispose();
        return bi;
    }

    /* ===================== power-ups ===================== */

    private static BufferedImage drawPowerUp(PowerUp.Type t) {
        int d = 36;
        BufferedImage bi = img(d, d);
        Graphics2D g = prep(bi);
        double c = d / 2.0;
        Color col = t.color;
        glow(g, c, c, c, new Color(col.getRed(), col.getGreen(), col.getBlue(), 120));
        // shell
        g.setPaint(new RadialGradientPaint(new Point2D.Double(c, c - 4), (float) (d / 2.0),
                new float[]{0f, 1f}, new Color[]{Color.WHITE, col}));
        g.fill(new Ellipse2D.Double(3, 3, d - 6, d - 6));
        g.setColor(new Color(255, 255, 255, 220));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(3, 3, d - 6, d - 6));

        // glyph
        g.setColor(new Color(25, 25, 35));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = d / 2, cy = d / 2;
        switch (t) {
            case RAPID: // lightning-ish bolt
                g.drawLine(cx + 4, 9, cx - 5, cy + 1);
                g.drawLine(cx - 5, cy + 1, cx + 2, cy + 1);
                g.drawLine(cx + 2, cy + 1, cx - 4, d - 8);
                break;
            case SPREAD: // three diverging lines
                g.drawLine(cx, cy + 6, cx, cy - 8);
                g.drawLine(cx, cy + 6, cx - 8, cy - 6);
                g.drawLine(cx, cy + 6, cx + 8, cy - 6);
                break;
            case SHIELD: // shield outline
                g.drawArc(cx - 8, cy - 9, 16, 18, 0, 360);
                g.drawLine(cx, cy - 8, cx, cy + 8);
                break;
            case NOVA: // starburst
                for (int a = 0; a < 360; a += 45) {
                    double r = Math.toRadians(a);
                    g.drawLine(cx, cy, cx + (int) (Math.cos(r) * 9), cy + (int) (Math.sin(r) * 9));
                }
                break;
        }
        g.dispose();
        return bi;
    }

    /* ===================== background ===================== */

    private static BufferedImage drawBackground(int w, int h) {
        BufferedImage bi = img(w, h);
        Graphics2D g = prep(bi);
        // deep-space vertical gradient
        g.setPaint(new GradientPaint(0, 0, new Color(6, 8, 20), 0, h, new Color(14, 10, 30)));
        g.fillRect(0, 0, w, h);

        // faint distant nebulae
        g.setColor(new Color(40, 20, 70, 40));
        glow(g, w * 0.25, h * 0.3, w * 0.35, new Color(60, 30, 110, 40));
        glow(g, w * 0.8, h * 0.7, w * 0.4, new Color(20, 60, 110, 36));

        // baked far stars (the moving stars are drawn live by Starfield)
        java.util.Random r = new java.util.Random(1234);
        for (int i = 0; i < w * h / 1400; i++) {
            int x = r.nextInt(w), y = r.nextInt(h);
            int b = 60 + r.nextInt(120);
            g.setColor(new Color(b, b, Math.min(255, b + 40), 180));
            int s = r.nextInt(2) + 1;
            g.fillRect(x, y, s, s);
        }
        g.dispose();
        return bi;
    }
}

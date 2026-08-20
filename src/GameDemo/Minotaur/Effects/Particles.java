package GameDemo.Minotaur.Effects;

import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Every non-sprite visual the combat throws off: sparks, blood, dust, embers, splinters,
 * damage numbers, shock rings and after-images.
 *
 * Backed by a fixed pool that is allocated once and recycled with a rolling cursor, so a
 * heavy fight never allocates and never has to compact a list. The pool is read by the render
 * threads while the game thread writes it; the only shared state is plain doubles on
 * already-allocated objects, so the worst a race can do is draw one particle a frame stale.
 *
 * @author Joseph
 */
public class Particles extends IndependentEffect {

    private enum Kind { SPARK, BLOOD, DUST, EMBER, SHARD, TEXT, RING, GHOST }

    private static final int POOL = 900;

    private static final class P {
        boolean alive;
        Kind kind;
        double x, y, vx, vy, gravity, drag;
        double size, sizeGrow, rot, rotV;
        int life, maxLife;
        Color color;
        String text;
        Font font;
        BufferedImage image;
        boolean above;      // drawn over the actors rather than under them
    }

    private final P[] pool = new P[POOL];
    private int cursor = 0;
    private final Random rng = new Random(0xB0BB1E);

    /** Set true for the pass that draws over actors; one instance is added per layer. */
    private final boolean overlay;

    public Particles(boolean overlay) {
        this.overlay = overlay;
        for (int i = 0; i < POOL; i++) pool[i] = new P();
    }

    @Override
    public int getZLayer() { return overlay ? 60 : -5; }

    private P claim() {
        for (int i = 0; i < POOL; i++) {
            P p = pool[cursor];
            cursor = (cursor + 1) % POOL;
            if (!p.alive) return reset(p);
        }
        // pool exhausted: recycle the next slot rather than dropping the effect
        P p = pool[cursor];
        cursor = (cursor + 1) % POOL;
        return reset(p);
    }

    /** Clears the optional fields so a recycled slot cannot inherit the last effect's state. */
    private static P reset(P p) {
        p.alive = true;
        p.gravity = 0; p.drag = 1; p.sizeGrow = 0; p.rot = 0; p.rotV = 0;
        p.text = null; p.image = null; p.font = null; p.above = true;
        return p;
    }

    private double rand(double a, double b) { return a + rng.nextDouble() * (b - a); }

    /* ==================================================================
     *  emitters
     * ================================================================== */

    /** Bright sparks thrown along a direction — the readable part of a landed hit. */
    public void sparks(double x, double y, int count, double dirX, double dirY, Color color, double power) {
        for (int i = 0; i < count; i++) {
            P p = claim();
            p.kind = Kind.SPARK;
            p.x = x; p.y = y;
            double spread = rand(-0.9, 0.9);
            double cos = Math.cos(spread), sin = Math.sin(spread);
            double sx = dirX * cos - dirY * sin, sy = dirX * sin + dirY * cos;
            double speed = rand(0.45, 1.3) * power;
            p.vx = sx * speed; p.vy = sy * speed - rand(0, 1.4);
            p.gravity = 0.55; p.drag = 0.94;
            p.size = rand(3, 7);
            p.life = p.maxLife = (int) rand(14, 30);
            p.color = color;
        }
    }

    /** Dark spatter that arcs and sticks around slightly longer than sparks. */
    public void blood(double x, double y, int count, double dirX, Color color) {
        for (int i = 0; i < count; i++) {
            P p = claim();
            p.kind = Kind.BLOOD;
            p.x = x; p.y = y;
            p.vx = dirX * rand(0.6, 4.5) + rand(-1.4, 1.4);
            p.vy = rand(-6.5, -0.5);
            p.gravity = 0.85; p.drag = 0.99;
            p.size = rand(4, 9);
            p.life = p.maxLife = (int) rand(26, 55);
            p.color = color;
        }
    }

    /** Soft ground puff for landings, dashes and heavy footfalls. */
    public void dust(double x, double y, int count, double dirX, Color color) {
        for (int i = 0; i < count; i++) {
            P p = claim();
            p.kind = Kind.DUST;
            p.x = x + rand(-14, 14); p.y = y + rand(-6, 4);
            p.vx = dirX * rand(0.3, 2.4) + rand(-0.7, 0.7);
            p.vy = rand(-1.5, -0.2);
            p.gravity = -0.02; p.drag = 0.9;
            p.size = rand(12, 26);
            p.sizeGrow = rand(0.5, 1.4);
            p.life = p.maxLife = (int) rand(20, 40);
            p.color = color;
            p.above = false;
        }
    }

    /** Rising motes of light — used around fire, portals and the rage aura. */
    public void embers(double x, double y, int count, Color color) {
        for (int i = 0; i < count; i++) {
            P p = claim();
            p.kind = Kind.EMBER;
            p.x = x + rand(-18, 18); p.y = y + rand(-10, 10);
            p.vx = rand(-0.5, 0.5);
            p.vy = rand(-2.4, -0.7);
            p.gravity = -0.012; p.drag = 0.99;
            p.size = rand(3, 8);
            p.life = p.maxLife = (int) rand(34, 80);
            p.color = color;
        }
    }

    /** Tumbling debris for barrels, armour breaking and stone dying. */
    public void shards(double x, double y, int count, Color color, double power) {
        for (int i = 0; i < count; i++) {
            P p = claim();
            p.kind = Kind.SHARD;
            p.x = x; p.y = y;
            p.vx = rand(-1, 1) * power;
            p.vy = rand(-1.3, -0.25) * power;
            p.gravity = 0.9; p.drag = 0.995;
            p.size = rand(7, 17);
            p.rot = rand(0, Math.PI * 2);
            p.rotV = rand(-0.28, 0.28);
            p.life = p.maxLife = (int) rand(35, 70);
            p.color = color;
        }
    }

    /** Floating combat text. Damage, healing, and the odd shout. */
    public void text(double x, double y, String s, Color color, int size) {
        P p = claim();
        p.kind = Kind.TEXT;
        p.x = x + rand(-14, 14); p.y = y;
        p.vx = rand(-0.5, 0.5);
        p.vy = -2.1;
        p.gravity = 0.055; p.drag = 1;
        p.life = p.maxLife = 52;
        p.color = color;
        p.text = s;
        p.font = new Font("SansSerif", Font.BOLD, size);
    }

    /** Expanding ring for slams, roars and impacts that need to feel wide. */
    public void ring(double x, double y, double startRadius, double growth, int ticks, Color color, double thickness) {
        P p = claim();
        p.kind = Kind.RING;
        p.x = x; p.y = y;
        p.size = startRadius;
        p.sizeGrow = growth;
        p.rotV = thickness;
        p.life = p.maxLife = ticks;
        p.color = color;
    }

    /** A frozen copy of a sprite that fades in place — the dash trail. */
    public void ghost(double x, double y, BufferedImage image, Color tint, int ticks) {
        P p = claim();
        p.kind = Kind.GHOST;
        p.x = x; p.y = y;
        p.image = image;
        p.color = tint;
        p.life = p.maxLife = ticks;
        p.above = false;
    }

    /** Drops everything, used when a level is torn down. */
    public void clear() {
        for (P p : pool) p.alive = false;
    }

    /* ==================================================================
     *  simulation
     * ================================================================== */

    @Override
    public void tick() {
        for (P p : pool) {
            if (!p.alive) continue;
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.size += p.sizeGrow;
            p.rot += p.rotV;
            if (--p.life <= 0) p.alive = false;
        }
    }

    /* ==================================================================
     *  rendering
     * ================================================================== */

    @Override
    public void render(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        for (P p : pool) {
            if (!p.alive || p.above != overlay || p.color == null) continue;
            float fade = Math.max(0f, Math.min(1f, p.life / (float) p.maxLife));
            try {
                switch (p.kind) {
                    case SPARK: {
                        g2.setColor(withAlpha(p.color, fade));
                        double len = Math.min(16, Math.hypot(p.vx, p.vy) * 2.2);
                        double nx = p.vx, ny = p.vy;
                        double m = Math.hypot(nx, ny);
                        if (m > 0.01) { nx /= m; ny /= m; }
                        g2.setStroke(new BasicStroke((float) Math.max(1.5, p.size * fade * 0.6)));
                        g2.drawLine((int) p.x, (int) p.y, (int) (p.x - nx * len), (int) (p.y - ny * len));
                        break;
                    }
                    case BLOOD: {
                        g2.setColor(withAlpha(p.color, fade * 0.95f));
                        double s = p.size * (0.5 + 0.5 * fade);
                        g2.fillOval((int) (p.x - s / 2), (int) (p.y - s / 2), (int) s, (int) s);
                        break;
                    }
                    case DUST: {
                        g2.setColor(withAlpha(p.color, fade * 0.42f));
                        g2.fillOval((int) (p.x - p.size / 2), (int) (p.y - p.size / 2),
                                (int) p.size, (int) p.size);
                        break;
                    }
                    case EMBER: {
                        float a = (float) Math.sin(fade * Math.PI) * 0.95f;
                        g2.setColor(withAlpha(p.color, a));
                        g2.fillOval((int) (p.x - p.size / 2), (int) (p.y - p.size / 2),
                                (int) p.size, (int) p.size);
                        break;
                    }
                    case SHARD: {
                        AffineTransform old = g2.getTransform();
                        g2.translate(p.x, p.y);
                        g2.rotate(p.rot);
                        g2.setColor(withAlpha(p.color, fade));
                        g2.fillRect((int) (-p.size / 2), (int) (-p.size / 3),
                                (int) p.size, (int) Math.max(2, p.size * 0.66));
                        g2.setTransform(old);
                        break;
                    }
                    case TEXT: {
                        g2.setFont(p.font);
                        // a hard shadow keeps numbers legible over bright hit sparks
                        g2.setColor(withAlpha(Color.BLACK, fade * 0.75f));
                        g2.drawString(p.text, (int) p.x + 3, (int) p.y + 3);
                        g2.setColor(withAlpha(p.color, fade));
                        g2.drawString(p.text, (int) p.x, (int) p.y);
                        break;
                    }
                    case RING: {
                        g2.setStroke(new BasicStroke((float) Math.max(1, p.rotV * fade)));
                        g2.setColor(withAlpha(p.color, fade * 0.9f));
                        g2.drawOval((int) (p.x - p.size), (int) (p.y - p.size),
                                (int) (p.size * 2), (int) (p.size * 2));
                        break;
                    }
                    case GHOST: {
                        if (p.image == null) break;
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade * 0.5f));
                        g2.drawImage(p.image, (int) (p.x - p.image.getWidth() / 2.0),
                                (int) (p.y - p.image.getHeight() / 2.0), null);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                        break;
                    }
                }
            } catch (Exception ignored) {
                // a particle mid-recycle is not worth interrupting the frame for
            }
        }
        g2.dispose();
    }

    private static Color withAlpha(Color c, float f) {
        int a = (int) Math.max(0, Math.min(255, c.getAlpha() * f));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    @Override
    public boolean shouldSerialize() { return false; }
}

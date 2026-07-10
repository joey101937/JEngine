package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.RadialGradientPaint;

/**
 * A short-lived, fully hand-drawn burst: an expanding shock ring plus a spray of
 * glowing embers. Purely cosmetic (non-solid, no sprite) and self-destructs when
 * its particles die. Rendered with additive-style translucency for a punchy,
 * responsive "something just happened" pop.
 *
 * @author Joseph
 */
public class Explosion extends GameObject2 {

    private static class Particle {
        double x, y, vx, vy;
        double life, maxLife;
        double size;
        Color color;
    }

    private final Particle[] particles;
    private int age = 0;
    private final int maxAge;
    private double ringRadius = 0;
    private final double ringMax;
    private final boolean shockwave;
    private final Color ringColor;

    private Explosion(DCoordinate loc, int count, double power, Color base, boolean shockwave) {
        super(loc);
        isSolid = false;
        setZLayer(60);
        this.shockwave = shockwave;
        this.ringMax = power * 2.4;
        this.maxAge = (int) (18 + power * 0.6);
        this.ringColor = base;
        particles = new Particle[count];
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            double ang = Math.random() * Math.PI * 2;
            double spd = (0.4 + Math.random()) * power * 0.14;
            p.x = 0; p.y = 0;
            p.vx = Math.cos(ang) * spd;
            p.vy = Math.sin(ang) * spd;
            p.maxLife = p.life = 10 + Math.random() * maxAge;
            p.size = 2 + Math.random() * (power * 0.08);
            p.color = jitter(base);
            particles[i] = p;
        }
    }

    private static Color jitter(Color c) {
        int r = clamp(c.getRed()   + (int) (Math.random() * 90 - 30));
        int g = clamp(c.getGreen() + (int) (Math.random() * 90 - 30));
        int b = clamp(c.getBlue()  + (int) (Math.random() * 90 - 30));
        return new Color(r, g, b);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    @Override
    public void tick() {
        super.tick();
        age++;
        ringRadius += (ringMax - ringRadius) * 0.25;
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            p.x += p.vx;
            p.y += p.vy;
            p.vx *= 0.92;
            p.vy *= 0.92;
            p.life--;
        }
        if (age >= maxAge) destroy();
    }

    @Override
    public void render(Graphics2D g) {
        renderNumber++;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        DCoordinate loc = getLocation();

        // expanding shock ring
        if (shockwave && ringRadius > 2) {
            float alpha = (float) Math.max(0, 1.0 - age / (double) maxAge);
            Color core = new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), (int) (120 * alpha));
            Color edge = new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), 0);
            g2.setPaint(new RadialGradientPaint(new Point2D.Double(loc.x, loc.y), (float) ringRadius,
                    new float[]{0.55f, 1f}, new Color[]{edge, core}));
            g2.fill(new Ellipse2D.Double(loc.x - ringRadius, loc.y - ringRadius, ringRadius * 2, ringRadius * 2));
        }

        // embers
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            float a = (float) Math.max(0, Math.min(1, p.life / p.maxLife));
            double s = p.size * (0.5 + a);
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int) (255 * a)));
            g2.fill(new Ellipse2D.Double(loc.x + p.x - s / 2, loc.y + p.y - s / 2, s, s));
        }
        g2.dispose();
    }

    // world border is irrelevant for a cosmetic effect
    @Override
    public void onCollideWorldBorder(DCoordinate loc) { }

    // This effect has no graphic (it draws itself), so report its own size instead of
    // letting GameObject2.getWidth() hit its null-graphic path and spam the console.
    @Override
    public int getWidth() { return (int) (ringMax * 2); }

    @Override
    public int getHeight() { return (int) (ringMax * 2); }

    /* ===================== factory helpers ===================== */

    public static void small(Game game, Coordinate at, Color color) {
        game.addObject(new Explosion(new DCoordinate(at), 12, 40, color, false));
    }

    public static void medium(Game game, Coordinate at, Color color) {
        game.addObject(new Explosion(new DCoordinate(at), 22, 70, color, true));
        RetroSfx.explosion();
    }

    public static void big(Game game, Coordinate at, Color color) {
        game.addObject(new Explosion(new DCoordinate(at), 46, 130, color, true));
        RetroSfx.bigExplosion();
    }

    /** Tiny cyan/white spark, e.g. bullet impact — no sound. */
    public static void spark(Game game, Coordinate at, Color color) {
        game.addObject(new Explosion(new DCoordinate(at), 6, 24, color, false));
    }

    /** Cheerful pickup shimmer. */
    public static void sparkle(Game game, Coordinate at, Color color) {
        game.addObject(new Explosion(new DCoordinate(at), 16, 46, color, true));
    }
}

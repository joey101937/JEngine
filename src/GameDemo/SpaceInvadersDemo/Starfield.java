package GameDemo.SpaceInvadersDemo;

import Framework.IndependentEffect;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * A parallax starfield that streams downward to sell the illusion that the ship is
 * flying forward at speed. Three depth layers scroll at different rates and the
 * faster stars stretch into streaks. Rendered beneath everything else.
 *
 * @author Joseph
 */
public class Starfield extends IndependentEffect {

    private static class Star {
        double x, y, speed, size;
        int brightness;
    }

    private final int width, height;
    private final Star[] stars;
    private final Random rng = new Random(98765);
    /** extra downward speed multiplier the ship's forward thrust can add for "boost" feel */
    private double warp = 1.0;

    public Starfield(int width, int height) {
        this.width = width;
        this.height = height;
        stars = new Star[220];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = newStar(rng.nextInt(height));
        }
    }

    private Star newStar(int y) {
        Star s = new Star();
        s.x = rng.nextInt(width);
        s.y = y;
        int band = rng.nextInt(3);
        switch (band) {
            case 0: s.speed = 1.2; s.size = 1; s.brightness = 90; break;
            case 1: s.speed = 2.6; s.size = 2; s.brightness = 150; break;
            default: s.speed = 4.6; s.size = 2.5; s.brightness = 220; break;
        }
        return s;
    }

    @Override
    public int getZLayer() { return -100; }

    /** Briefly boosts the scroll speed (e.g. when the player thrusts hard). */
    public void setWarp(double w) { this.warp = Math.max(1.0, w); }

    @Override
    public void tick() {
        for (Star s : stars) {
            s.y += s.speed * warp;
            if (s.y > height) {
                Star fresh = newStar(0);
                s.x = fresh.x; s.y = 0; s.speed = fresh.speed; s.size = fresh.size; s.brightness = fresh.brightness;
            }
        }
        warp += (1.0 - warp) * 0.08; // ease back to normal speed
    }

    @Override
    public void render(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // ride the HUD's screen-shake so the whole starfield jolts on impacts
        if (SpaceInvadersGame.ui != null) {
            Framework.Coordinate shake = SpaceInvadersGame.ui.getShakeOffset();
            g2.translate(shake.x, shake.y);
        }
        for (Star s : stars) {
            int b = s.brightness;
            g2.setColor(new Color(b, b, Math.min(255, b + 30)));
            double streak = s.speed * warp * 1.6;
            if (s.size >= 2) {
                g2.setStroke(new BasicStroke((float) s.size));
                g2.drawLine((int) s.x, (int) (s.y - streak), (int) s.x, (int) s.y);
            } else {
                g2.fillRect((int) s.x, (int) s.y, (int) s.size, (int) s.size);
            }
        }
        g2.dispose();
    }

    @Override
    public boolean shouldSerialize() { return false; }
}

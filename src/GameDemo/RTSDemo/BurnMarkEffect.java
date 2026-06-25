package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
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
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * A world-space scorch mark rendered on the ground where a projectile exploded.
 * Implemented as an {@link Framework.IndependentEffect} so it has no associated
 * game object and is managed directly by the {@link Framework.Game}.
 * <p>
 * The mark is drawn in four layered passes (outer scorch halo, radial-gradient
 * fill, perimeter debris scatter, radial streak lines) using pre-computed
 * {@code Path2D} geometry seeded from the spawn position, so the shape is
 * deterministic and stable across frames. Alpha fades linearly from 1 to 0 over
 * the specified duration, after which the effect removes itself from the game.
 * <p>
 * Alternatively, pass a pre-rendered scorch image to the image constructor and
 * the effect draws that single decal (scaled, randomly rotated, alpha-fading)
 * instead of the procedural geometry.
 * <p>
 * For burn marks that should follow a vehicle and render on its hull, use
 * {@link HullBurnDecal} instead.
 * </p>
 */
public class BurnMarkEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private final transient Game game;
    private final Coordinate worldPos;
    private final int durationTicks;
    private final int zLayer;
    private int ticksElapsed = 0;

    private transient Path2D.Float mainShape;
    private transient Path2D.Float outerScorch;
    private int[] scatterX, scatterY, scatterRX, scatterRY;
    private int[] streakEX, streakEY;
    private float[] streakWidths;
    private transient RadialGradientPaint gradient;

    // Image-decal mode (used instead of the procedural geometry when non-null)
    private final transient BufferedImage decalImage;
    private final double decalScale;
    private final double decalRotation;

    public BurnMarkEffect(Game game, Coordinate worldPos, int radius, int durationTicks) {
        this(game, worldPos, radius, durationTicks, -10);
    }

    /**
     * Image-decal burn mark: draws {@code decalImage} centered on {@code worldPos},
     * scaled so its rendered width is {@code 2 * radius}, randomly rotated and
     * alpha-fading over {@code durationTicks}.
     */
    public BurnMarkEffect(Game game, Coordinate worldPos, BufferedImage decalImage, int radius, int durationTicks, int zLayer) {
        this.game        = game;
        this.worldPos    = new Coordinate(worldPos);
        this.durationTicks = durationTicks;
        this.zLayer      = zLayer;
        this.decalImage  = decalImage;
        Random rand = new Random(worldPos.x * 7919L + worldPos.y * 6271L);
        this.decalRotation = rand.nextDouble() * Math.PI * 2;
        this.decalScale = (decalImage != null && decalImage.getWidth() > 0)
            ? (radius * 2.0) / decalImage.getWidth()
            : 1.0;
    }

    public BurnMarkEffect(Game game, Coordinate worldPos, int radius, int durationTicks, int zLayer) {
        this.game        = game;
        this.worldPos    = new Coordinate(worldPos);
        this.durationTicks = durationTicks;
        this.zLayer      = zLayer;
        this.decalImage  = null;
        this.decalScale  = 1.0;
        this.decalRotation = 0.0;

        Random rand = new Random(worldPos.x * 7919L + worldPos.y * 6271L);
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

    @Override public int     getZLayer()       { return zLayer; }
    @Override public boolean shouldSerialize() { return false; }

    @Override
    public void tick() {
        ticksElapsed++;
        if (ticksElapsed >= durationTicks) {
            game.removeIndependentEffect(this);
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (ticksElapsed >= durationTicks) return;
        float alpha = 1.0f - (float) ticksElapsed / durationTicks;

        if (decalImage != null) {
            AffineTransform saved        = g.getTransform();
            Composite       savedComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            AffineTransform at = new AffineTransform(saved);
            at.translate(worldPos.x, worldPos.y);
            at.rotate(decalRotation);
            at.scale(decalScale, decalScale);
            at.translate(-decalImage.getWidth() / 2.0, -decalImage.getHeight() / 2.0);
            g.setTransform(at);
            g.drawImage(decalImage, 0, 0, null);
            g.setTransform(saved);
            g.setComposite(savedComposite);
            return;
        }

        Composite     oldComposite = g.getComposite();
        Color         oldColor     = g.getColor();
        Paint         oldPaint     = g.getPaint();
        Stroke        oldStroke    = g.getStroke();
        AffineTransform oldTransform = g.getTransform();

        g.translate(worldPos.x, worldPos.y);

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

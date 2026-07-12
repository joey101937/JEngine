package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

/**
 * A faint pair of tank-tread marks stamped onto the ground behind a moving
 * {@link GameObject2}. Implemented as an {@link IndependentEffect} that follows
 * its source: each tick — while its {@code emitWhen} condition holds — it drops a
 * short dark segment under each track (left and right of center), oriented to the
 * source's heading. Each stamp is a short cleat sitting across the track, so
 * successive stamps read as a broken ladder of tread cleats rather than a clean
 * band, with per-mark darkness and length jitter to look churned.
 * <p>
 * Local {@code +Y} is the source's forward/back axis (its sprite faces {@code -Y}),
 * so a cleat's thickness runs along the direction of travel and its width sits
 * across the track. Marks are world-anchored and rendered low (below the units)
 * so vehicles drive over their own tracks. Purely cosmetic; never serialized.
 */
public class TankTreadEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private static final Color TREAD_COLOR = new Color(58, 47, 36); // dark churned-dirt brown

    private final transient Game game;
    private final transient GameObject2 source;
    private final transient BooleanSupplier emitWhen;
    private final double trackHalfWidth;   // lateral distance from center to each track
    private final double treadWidth;       // width of a cleat across the track
    private final double cleatLength;      // thickness of a cleat along the heading
    private final int    emitIntervalTicks;
    private final int    lifeTicks;
    private final double maxAlpha;
    private final int    zLayer;

    private final List<Mark> marks = new ArrayList<>();
    private long lastEmitTick = -1_000_000L;
    // Optional gate: when set, new cleats stop stamping while the source is hidden
    // (e.g. off-screen or in fog of war); already-stamped marks still render. Null means always stamping.
    private transient BooleanSupplier visibleWhen = null;

    /** One stamped tread cleat, world-anchored at birth, with a little per-mark churn. */
    private static final class Mark {
        final double x, y, rotDeg, alphaMul, lengthMul;
        final long birthTick;
        Mark(double x, double y, double rotDeg, long birthTick, double alphaMul, double lengthMul) {
            this.x = x; this.y = y; this.rotDeg = rotDeg; this.birthTick = birthTick;
            this.alphaMul = alphaMul; this.lengthMul = lengthMul;
        }
    }

    /**
     * @param trackHalfWidth    lateral offset from center to each track, in world pixels
     * @param treadWidth        width of a cleat across the track, in world pixels
     * @param cleatLength       thickness of a cleat along the heading, in world pixels (small — the gaps between cleats read as tread)
     * @param emitIntervalTicks ticks between stamps while emitting (spacing a bit above cleatLength leaves the ladder gaps)
     * @param lifeTicks         how long each mark lasts before it has fully faded
     * @param maxAlpha          peak opacity of a fresh mark (0..1) — lower reads fainter
     * @param zLayer            render layer (use a low value so tracks sit under the units)
     */
    public TankTreadEffect(Game game, GameObject2 source, BooleanSupplier emitWhen,
                           double trackHalfWidth, double treadWidth, double cleatLength,
                           int emitIntervalTicks, int lifeTicks, double maxAlpha, int zLayer) {
        this.game = game;
        this.source = source;
        this.emitWhen = emitWhen;
        this.trackHalfWidth = trackHalfWidth;
        this.treadWidth = treadWidth;
        this.cleatLength = cleatLength;
        this.emitIntervalTicks = Math.max(1, emitIntervalTicks);
        this.lifeTicks = lifeTicks;
        this.maxAlpha = maxAlpha;
        this.zLayer = zLayer;
    }

    @Override public int     getZLayer()       { return zLayer; }
    @Override public boolean shouldSerialize() { return false; }

    /**
     * Sets an optional emission gate; while it returns false and the source is still alive, no new
     * cleats are stamped (e.g. pass {@code unit::shouldRender} so a unit in fog lays no new tracks).
     * Marks already stamped keep rendering and fade out normally.
     * @return this, for chaining at the call site
     */
    public TankTreadEffect setVisibleWhen(BooleanSupplier visibleWhen) {
        this.visibleWhen = visibleWhen;
        return this;
    }

    /** True unless the source is alive and its emission gate currently says hidden. */
    private boolean sourceVisible() {
        if (visibleWhen == null || source == null || !source.isAlive()) return true;
        return visibleWhen.getAsBoolean();
    }

    @Override
    public void tick() {
        long now = game.getGameTickNumber();
        marks.removeIf(m -> now - m.birthTick >= lifeTicks);

        boolean sourceGone = source == null || !source.isAlive();
        if (!sourceGone && sourceVisible() && emitWhen.getAsBoolean() && now - lastEmitTick >= emitIntervalTicks) {
            emitMarks(now);
            lastEmitTick = now;
        }

        if (sourceGone && marks.isEmpty()) {
            game.removeIndependentEffect(this);
        }
    }

    private void emitMarks(long now) {
        double rot = source.getRotation();
        Coordinate loc = source.getPixelLocation();
        // Seed from the emit tick and position so the jitter is stable and matches across machines.
        Random rand = new Random(now * 1103515245L + loc.x * 31L + loc.y * 17L);
        double[] sides = {-trackHalfWidth, trackHalfWidth};
        for (double side : sides) {
            DCoordinate offset = new DCoordinate(side, 0);
            offset.adjustForRotation(rot);
            double alphaMul = 0.55 + rand.nextDouble() * 0.45;   // uneven darkness cleat-to-cleat
            double lengthMul = 0.8 + rand.nextDouble() * 0.45;   // slightly ragged cleat lengths
            marks.add(new Mark(loc.x + offset.x, loc.y + offset.y, rot, now, alphaMul, lengthMul));
        }
    }

    @Override
    public void render(Graphics2D g) {
        // Note: no visibility gate here — already-stamped marks keep rendering and fade out even once
        // the source slips into fog; the gate only stops new stamps (see tick).
        if (marks.isEmpty()) return;
        long now = game.getGameTickNumber();
        Composite       oldComposite = g.getComposite();
        AffineTransform oldTransform = g.getTransform();
        Object          oldAA        = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(TREAD_COLOR);

        RoundRectangle2D seg = new RoundRectangle2D.Double(0, 0, 1, 1, 1, 1);
        double arc = cleatLength * 0.5;
        for (Mark m : marks) {
            double age = now - m.birthTick;
            if (age < 0 || age >= lifeTicks) continue;
            // ease-out fade so cleats hold briefly then thin away, times the per-mark churn
            double a = (1.0 - age / lifeTicks) * (1.0 - age / lifeTicks) * maxAlpha * m.alphaMul;
            if (a <= 0.01) continue;
            double len = cleatLength * m.lengthMul;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) a));
            g.setTransform(oldTransform);
            g.translate(m.x, m.y);
            g.rotate(Math.toRadians(m.rotDeg));
            seg.setRoundRect(-treadWidth / 2, -len / 2, treadWidth, len, arc, arc);
            g.fill(seg);
        }

        g.setTransform(oldTransform);
        g.setComposite(oldComposite);
        if (oldAA != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }
}

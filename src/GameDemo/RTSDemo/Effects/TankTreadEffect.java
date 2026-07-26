package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;

/**
 * A faint pair of tank-tread marks stamped onto the ground behind a moving
 * {@link GameObject2}. Implemented as an {@link IndependentEffect} that follows
 * its source: while its {@code emitWhen} condition holds and the source has
 * covered enough ground since the last stamp, it drops a short dark segment
 * under each track (left and right of center), oriented to the source's heading.
 * Each stamp is a short cleat sitting across the track, so successive stamps read
 * as a broken ladder of tread cleats rather than a clean band, with per-mark
 * darkness and length jitter to look churned. A cleat is two-tone — a dark core
 * ringed by pale displaced soil — so it stays legible on dark terrain, where a
 * dark-only mark has nothing to contrast against.
 * <p>
 * Local {@code +Y} is the source's forward/back axis (its sprite faces {@code -Y}),
 * so a cleat's thickness runs along the direction of travel and its width sits
 * across the track. Marks are world-anchored and rendered low (below the units)
 * so vehicles drive over their own tracks. Purely cosmetic; never serialized.
 * <p>
 * Rendering is blit-based: cleats are baked once into small pre-rotated stamp
 * images ({@link StampSet}), shared by every effect with the same cleat
 * dimensions, so a frame draws images at fixed transform rather than filling an
 * antialiased rounded rect per mark. Each stamp bakes in one of a few churn
 * variants (paired length and darkness), which leaves the per-frame opacity as
 * the pure age fade — that decreases monotonically along the mark list, so the
 * quantized fade steps walk one direction and the composite is swapped a couple
 * dozen times per frame instead of once per mark. Marks outside the camera's
 * field of view are skipped entirely.
 */
public class TankTreadEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private static final Color TREAD_COLOR = new Color(52, 42, 32);   // dark churned-dirt brown
    private static final Color CHURN_COLOR = new Color(133, 118, 96); // muted soil the cleat pushes out to its sides
    /** Width of the rim as a fraction of cleat width, so it scales with the vehicle. */
    private static final double CHURN_RIM_RATIO = 0.2;
    /** Rim opacity relative to the cleat's, keeping it a hint of turned earth rather than an outline. */
    private static final double CHURN_ALPHA = 0.45;

    /** Pre-baked stamp orientations; 64 buckets keeps the worst-case corner error well under a pixel. */
    private static final int ROT_BUCKETS = 64;
    /** Churn variants, each pairing a cleat length and a darkness so the ladder stays uneven. */
    private static final double[] VARIANT_LENGTH = {0.82, 1.20, 0.95, 1.11, 0.88, 1.03};
    private static final double[] VARIANT_ALPHA  = {0.62, 0.97, 0.75, 0.58, 1.00, 0.83};
    private static final int VARIANTS = VARIANT_LENGTH.length;
    private static final double LONGEST_VARIANT = Arrays.stream(VARIANT_LENGTH).max().getAsDouble();
    /** Fade quantization; each step is a shared AlphaComposite, so the render pipeline revalidates rarely. */
    private static final int ALPHA_STEPS = 32;
    /** Ceiling on live marks, so an unusually long-lived source can't grow the list without bound. */
    private static final int MAX_MARKS = 512;
    /** Fraction of a cleat's length the source must travel before it stamps again. */
    private static final double EMIT_DISTANCE_RATIO = 0.5;

    private static final Map<Long, StampSet> STAMP_SETS = new ConcurrentHashMap<>();

    private final transient Game game;
    private final transient GameObject2 source;
    private final transient BooleanSupplier emitWhen;
    private final double trackHalfWidth;   // lateral distance from center to each track
    private final double minEmitDistance;  // world distance the source must cover between stamps
    private final int    emitIntervalTicks;
    private final int    lifeTicks;
    private final int    zLayer;

    private final transient StampSet stamps;
    private final transient Composite[] fadeComposites; // indexed by fade step, 0 unused (fully faded)
    private final transient ArrayDeque<Mark> marks = new ArrayDeque<>();
    private long lastEmitTick = -1_000_000L;
    private double lastEmitX, lastEmitY;
    private boolean hasStamped = false;
    // Optional gate: when set, new cleats stop stamping while the source is hidden
    // (e.g. off-screen or in fog of war); already-stamped marks still render. Null means always stamping.
    private transient BooleanSupplier visibleWhen = null;

    /** One stamped tread cleat, world-anchored at birth, referencing the baked stamp it draws with. */
    private static final class Mark {
        final double x, y;
        final long birthTick;
        final byte rotBucket, variant;
        Mark(double x, double y, int rotBucket, int variant, long birthTick) {
            this.x = x; this.y = y; this.birthTick = birthTick;
            this.rotBucket = (byte) rotBucket; this.variant = (byte) variant;
        }
    }

    /**
     * The baked cleat images for one cleat size: every rotation bucket crossed with every churn
     * variant, filled in lazily as headings are actually driven. Shared across all effects using
     * the same cleat dimensions (i.e. every tank of a given type), and safe to build from the
     * render threads — a race just bakes the same image twice.
     */
    private static final class StampSet {
        final AtomicReferenceArray<BufferedImage> images = new AtomicReferenceArray<>(ROT_BUCKETS * VARIANTS);
        final double treadWidth, cleatLength, rim;
        final int size, halfSize;

        StampSet(double treadWidth, double cleatLength) {
            this.treadWidth = treadWidth;
            this.cleatLength = cleatLength;
            this.rim = treadWidth * CHURN_RIM_RATIO;
            double longest = cleatLength * LONGEST_VARIANT; // sized so the longest variant fits at any rotation
            int raw = (int) Math.ceil(Math.hypot(treadWidth + 2 * rim, longest + 2 * rim)) + 2;
            this.size = raw + (raw & 1); // even, so the baked center sits exactly on halfSize
            this.halfSize = size / 2;
        }

        BufferedImage get(int rotBucket, int variant) {
            int index = rotBucket * VARIANTS + variant;
            BufferedImage img = images.get(index);
            if (img == null) {
                img = bake(rotBucket, variant);
                images.compareAndSet(index, null, img);
            }
            return img;
        }

        private BufferedImage bake(int rotBucket, int variant) {
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.translate(size / 2.0, size / 2.0);
            g.rotate(rotBucket * (Math.PI * 2.0 / ROT_BUCKETS));
            double len = cleatLength * VARIANT_LENGTH[variant];
            double arc = cleatLength * 0.5;
            double alpha = VARIANT_ALPHA[variant];

            // Pale rim first, stroked just outside the cleat's outline so it abuts the dark core rather
            // than tinting it. It is what carries the mark on dark terrain, where the core has no contrast.
            g.setColor(CHURN_COLOR);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (alpha * CHURN_ALPHA)));
            g.setStroke(new BasicStroke((float) rim));
            double rw = treadWidth + rim, rl = len + rim;
            g.draw(new RoundRectangle2D.Double(-rw / 2, -rl / 2, rw, rl, arc + rim / 2, arc + rim / 2));

            g.setColor(TREAD_COLOR);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
            g.fill(new RoundRectangle2D.Double(-treadWidth / 2, -len / 2, treadWidth, len, arc, arc));
            g.dispose();
            return img;
        }
    }

    /** Stamp sets are shared at quarter-pixel granularity, which is finer than a cleat edge can show. */
    private static StampSet stampSetFor(double treadWidth, double cleatLength) {
        long wq = Math.round(treadWidth * 4);
        long lq = Math.round(cleatLength * 4);
        return STAMP_SETS.computeIfAbsent((wq << 32) | (lq & 0xffffffffL),
                k -> new StampSet(wq / 4.0, lq / 4.0));
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
        this.emitIntervalTicks = Math.max(1, emitIntervalTicks);
        this.lifeTicks = lifeTicks;
        this.zLayer = zLayer;
        this.minEmitDistance = cleatLength * EMIT_DISTANCE_RATIO;
        this.stamps = stampSetFor(treadWidth, cleatLength);
        this.fadeComposites = new Composite[ALPHA_STEPS + 1];
        for (int i = 1; i <= ALPHA_STEPS; i++) {
            fadeComposites[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) (maxAlpha * i / ALPHA_STEPS));
        }
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
        // Marks sit in the deque in birth order, so expiry is a pop off the front.
        while (!marks.isEmpty() && now - marks.peekFirst().birthTick >= lifeTicks) {
            marks.pollFirst();
        }

        boolean sourceGone = source == null || !source.isAlive();
        if (!sourceGone && sourceVisible() && emitWhen.getAsBoolean() && now - lastEmitTick >= emitIntervalTicks) {
            emitMarks(now);
        }

        if (sourceGone && marks.isEmpty()) {
            game.removeIndependentEffect(this);
        }
    }

    private void emitMarks(long now) {
        Coordinate loc = source.getPixelLocation();
        // A crawling or shoved unit would otherwise restamp cleats on top of each other.
        double movedX = loc.x - lastEmitX, movedY = loc.y - lastEmitY;
        if (hasStamped && movedX * movedX + movedY * movedY < minEmitDistance * minEmitDistance) {
            return;
        }

        double rot = source.getRotation();
        int rotBucket = Math.floorMod((int) Math.round(rot * ROT_BUCKETS / 360.0), ROT_BUCKETS);
        double rad = Math.toRadians(rot);
        double offX = Math.cos(rad) * trackHalfWidth, offY = Math.sin(rad) * trackHalfWidth;
        // Seed from the emit tick and world position so the churn is stable and matches across machines.
        long seed = now * 1103515245L + loc.x * 31L + loc.y * 17L;
        addMark(loc.x - offX, loc.y - offY, rotBucket, variantFor(seed), now);
        addMark(loc.x + offX, loc.y + offY, rotBucket, variantFor(seed + 1), now);

        lastEmitTick = now;
        lastEmitX = loc.x;
        lastEmitY = loc.y;
        hasStamped = true;
    }

    private void addMark(double x, double y, int rotBucket, int variant, long now) {
        if (marks.size() >= MAX_MARKS) {
            marks.pollFirst();
        }
        marks.addLast(new Mark(x, y, rotBucket, variant, now));
    }

    /** Allocation-free deterministic pick of a churn variant (splitmix-style mix of the seed). */
    private static int variantFor(long seed) {
        long z = seed * 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) Math.floorMod(z ^ (z >>> 31), (long) VARIANTS);
    }

    @Override
    public void render(Graphics2D g) {
        // Note: no visibility gate here — already-stamped marks keep rendering and fade out even once
        // the source slips into fog; the gate only stops new stamps (see tick).
        if (marks.isEmpty()) return;
        long now = game.getGameTickNumber();
        Rectangle view = game.getCamera().getFieldOfView();
        int pad = stamps.size;
        double minX = view.x - pad, maxX = view.x + view.width + pad;
        double minY = view.y - pad, maxY = view.y + view.height + pad;

        Composite oldComposite = g.getComposite();
        int lastStep = -1;
        for (Mark m : marks) {
            double age = now - m.birthTick;
            if (age < 0 || age >= lifeTicks) continue;
            if (m.x < minX || m.x > maxX || m.y < minY || m.y > maxY) continue;
            // ease-out fade so cleats hold briefly then thin away; the per-mark churn is baked into the stamp
            double fade = 1.0 - age / lifeTicks;
            int step = (int) (fade * fade * ALPHA_STEPS + 0.5);
            if (step <= 0) continue;
            if (step != lastStep) {
                g.setComposite(fadeComposites[step]);
                lastStep = step;
            }
            g.drawImage(stamps.get(m.rotBucket, m.variant),
                    (int) (m.x - stamps.halfSize), (int) (m.y - stamps.halfSize), null);
        }

        if (lastStep != -1) {
            g.setComposite(oldComposite);
        }
    }
}

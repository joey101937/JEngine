package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.RTSGame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

/**
 * A continuous exhaust trail of Java2D smoke that follows a moving
 * {@link GameObject2} from a fixed offset behind it. Implemented as an
 * {@link IndependentEffect}: it holds a reference to its source object, and each
 * tick — while its {@code emitWhen} condition holds — it drops a small
 * {@link SmokePuff} at the source's rear, rotated to match the source's facing.
 * <p>
 * Emitted puffs are world-anchored (they drift and fade in place rather than
 * chasing the source), so they string out into a trail as the source drives
 * away. Local {@code +Y} is treated as "behind" to match the unit sprite
 * convention where {@code -Y} is forward. The effect self-removes once its
 * source is gone and its last puff has faded. Purely cosmetic; never serialized.
 */
public class ExhaustTrailEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private final transient Game game;
    private final transient GameObject2 source;
    private final transient BooleanSupplier emitWhen;
    private final double rearOffset;        // local +Y distance behind the source center
    private final double sideOffset;        // local X offset (e.g. tail-pipe to one side)
    private final double size;              // base puff radius in world pixels
    private final int    emitIntervalTicks; // ticks between puffs while emitting
    private final int    zLayer;
    private final double lifeScale;         // multiplier on puff lifetime (shorter life -> shorter trail)

    private final List<SmokePuff> puffs = new ArrayList<>();
    // Small negative sentinel so the first eligible tick always emits without overflowing (now - this).
    private long lastEmitTick = -1_000_000L;

    /**
     * @param game              the game to attach to
     * @param source            the object the trail follows
     * @param emitWhen          emit a puff only while this returns true (e.g. moving and not wrecked)
     * @param rearOffset        distance behind the source center to emit from, in world pixels
     * @param sideOffset        lateral offset of the tail pipe from center, in world pixels
     * @param size              base puff radius in world pixels
     * @param emitIntervalTicks ticks between puffs while emitting
     * @param zLayer            render layer
     */
    public ExhaustTrailEffect(Game game, GameObject2 source, BooleanSupplier emitWhen,
                              double rearOffset, double sideOffset, double size,
                              int emitIntervalTicks, int zLayer) {
        this(game, source, emitWhen, rearOffset, sideOffset, size, emitIntervalTicks, zLayer, 1.0);
    }

    /**
     * @param lifeScale multiplier on each puff's lifetime; below 1.0 gives a shorter trail
     *                  (useful for faster vehicles whose puffs would otherwise string out too far)
     */
    public ExhaustTrailEffect(Game game, GameObject2 source, BooleanSupplier emitWhen,
                              double rearOffset, double sideOffset, double size,
                              int emitIntervalTicks, int zLayer, double lifeScale) {
        this.game = game;
        this.source = source;
        this.emitWhen = emitWhen;
        this.rearOffset = rearOffset;
        this.sideOffset = sideOffset;
        this.size = size;
        this.emitIntervalTicks = Math.max(1, emitIntervalTicks);
        this.zLayer = zLayer;
        this.lifeScale = lifeScale;
    }

    @Override public int     getZLayer()       { return zLayer; }
    @Override public boolean shouldSerialize() { return false; }

    @Override
    public void tick() {
        long now = game.getGameTickNumber();
        puffs.removeIf(p -> p.isDead(now));

        boolean sourceGone = source == null || !source.isAlive();
        if (!sourceGone && emitWhen.getAsBoolean() && now - lastEmitTick >= emitIntervalTicks) {
            emitPuff(now);
            lastEmitTick = now;
        }

        // Once the source is gone, linger only long enough for the last puffs to fade.
        if (sourceGone && puffs.isEmpty()) {
            game.removeIndependentEffect(this);
        }
    }

    private void emitPuff(long now) {
        // Local offset: +Y is behind the source (its sprite faces -Y). Rotate into world space.
        DCoordinate offset = new DCoordinate(sideOffset, rearOffset);
        offset.adjustForRotation(source.getRotation());
        Coordinate loc = source.getPixelLocation();

        // Seed from the emit tick and world position so the jitter is stable and matches across machines.
        Random rand = new Random(now * 1103515245L + loc.x * 31L + loc.y * 17L);
        double jitterX = (rand.nextDouble() - 0.5) * size * 0.4;
        double jitterY = (rand.nextDouble() - 0.5) * size * 0.4;
        double px = loc.x + offset.x + jitterX;
        double py = loc.y + offset.y + jitterY;

        int life = (int) (RTSGame.desiredTPS * (0.3 + rand.nextDouble() * 0.22) * lifeScale);
        double startR = size * (0.35 + rand.nextDouble() * 0.2);
        double endR = size * (1.0 + rand.nextDouble() * 0.6);
        // Puffs drift back past the tail and rise a little, like settling exhaust.
        DCoordinate drift = new DCoordinate(0, size * (0.03 + rand.nextDouble() * 0.03));
        drift.adjustForRotation(source.getRotation());
        double driftX = drift.x;
        double driftY = drift.y - size * (0.02 + rand.nextDouble() * 0.02);
        double alpha = 0.30 + rand.nextDouble() * 0.20;
        int gray = 120 + rand.nextInt(45);
        puffs.add(new SmokePuff(px, py, now, life, startR, endR, driftX, driftY, alpha, gray));
    }

    @Override
    public void render(Graphics2D g) {
        long now = game.getGameTickNumber();
        Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (SmokePuff puff : puffs) {
            puff.render(g, now);
        }
        if (oldAA != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }
}

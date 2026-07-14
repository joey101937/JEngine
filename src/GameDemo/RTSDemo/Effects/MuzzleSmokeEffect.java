package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.RTSGame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A quick puff of gun smoke that jets out of a barrel tip when a weapon fires
 * and dissipates in under a second. Implemented as an {@link IndependentEffect}
 * so it has no backing game object and is managed directly by the {@link Game}.
 * <p>
 * A small cluster of {@link SmokePuff}s is seeded deterministically from the
 * muzzle position and tick, so the shape is stable frame-to-frame and matches
 * across machines. Every puff drifts along the shot direction (with a little
 * spread and rise), so the whole poof reads as being pushed out of the barrel.
 * Being cosmetic, it never serializes and removes itself once every puff has
 * faded.
 */
public class MuzzleSmokeEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private final transient Game game;
    private final int zLayer;
    private final long startTick;
    private final int totalDurationTicks;
    private final List<SmokePuff> puffs = new ArrayList<>();

    /**
     * @param game     the game to attach to
     * @param muzzle   world-space position of the barrel tip
     * @param dirX     shot direction (unit vector) in world space
     * @param dirY     shot direction (unit vector) in world space
     * @param size     rough radius of the poof in world pixels (a tank barrel reads well around 6-9)
     * @param zLayer   render layer (draw above the firing unit so the smoke sits on top)
     */
    public MuzzleSmokeEffect(Game game, Coordinate muzzle, double dirX, double dirY, double size, int zLayer) {
        this.game = game;
        this.zLayer = zLayer;
        this.startTick = game.getGameTickNumber();

        Random rand = new Random(muzzle.x * 7919L + muzzle.y * 6271L + startTick * 31L + 41L);
        int count = 4 + rand.nextInt(3);
        int longest = 0;
        for (int i = 0; i < count; i++) {
            // Spread each puff slightly around the muzzle and along the barrel line.
            double alongInit = size * (rand.nextDouble() * 0.4);   // start a touch out in front of the tip
            double lateral = (rand.nextDouble() - 0.5) * size * 0.5;
            double px = muzzle.x + dirX * alongInit - dirY * lateral;
            double py = muzzle.y + dirY * alongInit + dirX * lateral;

            int spawnDelay = (int) (i * (RTSGame.desiredTPS * 0.015)); // brief stagger so it billows out
            int life = (int) (RTSGame.desiredTPS * (0.35 + rand.nextDouble() * 0.35)); // < 1s
            double startR = size * (0.35 + rand.nextDouble() * 0.2);
            double endR = size * (0.9 + rand.nextDouble() * 0.5);

            // Drift forward along the shot, decaying visually via the puff's own fade; add slight lateral scatter and rise.
            double speed = size * (0.06 + rand.nextDouble() * 0.05);
            double driftX = dirX * speed + (rand.nextDouble() - 0.5) * size * 0.02;
            double driftY = dirY * speed + (rand.nextDouble() - 0.5) * size * 0.02 - size * (0.01 + rand.nextDouble() * 0.012);
            double alpha = 0.30 + rand.nextDouble() * 0.18;
            int gray = 150 + rand.nextInt(45);
            puffs.add(new SmokePuff(px, py, startTick + spawnDelay, life,
                    startR, endR, driftX, driftY, alpha, gray));
            longest = Math.max(longest, spawnDelay + life);
        }
        this.totalDurationTicks = longest;
    }

    @Override public int     getZLayer()       { return zLayer; }
    @Override public boolean shouldSerialize() { return false; }

    @Override
    public void tick() {
        if (game.getGameTickNumber() - startTick >= totalDurationTicks) {
            game.removeIndependentEffect(this);
        }
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

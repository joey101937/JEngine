package GameDemo.RTSDemo.Effects;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.RTSGame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A short-lived, fully Java2D-drawn puff of smoke that blooms at a world
 * position and dissipates. Implemented as an {@link IndependentEffect} so it has
 * no backing game object and is managed directly by the {@link Game}.
 * <p>
 * A cluster of {@link SmokePuff}s is seeded deterministically from the spawn
 * position and tick, so the shape is stable frame-to-frame and matches across
 * machines. Being cosmetic, it never serializes and removes itself once every
 * puff has faded. Good for a quick "poof" — e.g. when a vehicle is wrecked into
 * rubble.
 */
public class SmokePoofEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    private final transient Game game;
    private final int zLayer;
    private final long startTick;
    private final int totalDurationTicks;
    private final List<SmokePuff> puffs = new ArrayList<>();

    /**
     * @param game     the game to attach to
     * @param worldPos world-space center of the poof
     * @param size     rough radius of the poof in world pixels (a tank reads well around 18-26)
     * @param zLayer   render layer (draw above the wreck so the smoke sits on top)
     */
    public SmokePoofEffect(Game game, Coordinate worldPos, double size, int zLayer) {
        this.game = game;
        this.zLayer = zLayer;
        this.startTick = game.getGameTickNumber();

        Random rand = new Random(worldPos.x * 7919L + worldPos.y * 6271L + startTick * 31L + 17L);
        int count = 5 + rand.nextInt(3);
        int longest = 0;
        for (int i = 0; i < count; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = size * (0.1 + rand.nextDouble() * 0.5);
            double px = worldPos.x + Math.cos(angle) * dist;
            double py = worldPos.y + Math.sin(angle) * dist;
            int spawnDelay = (int) (i * (RTSGame.desiredTPS * 0.02)); // brief stagger so it billows out
            int life = (int) (RTSGame.desiredTPS * (0.7 + rand.nextDouble() * 0.5));
            double startR = size * (0.3 + rand.nextDouble() * 0.2);
            double endR = size * (0.85 + rand.nextDouble() * 0.5);
            double driftX = (rand.nextDouble() - 0.5) * size * 0.010;
            double driftY = -(size * (0.010 + rand.nextDouble() * 0.014)); // rises as it dissipates
            double alpha = 0.40 + rand.nextDouble() * 0.22;
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

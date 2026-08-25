package GameDemo.Minotaur;

import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Turns the raw images from {@link MinotaurArt} into the engine's {@link Sprite} and
 * {@link Sequence} objects, and owns the one copy of each that everything else borrows.
 *
 * Sequences are stateful, so nothing here is handed out directly: callers ask for a
 * {@code copy} and get their own playhead over the same underlying frames, which is what
 * lets two imps animate out of phase without doubling the memory.
 *
 * @author Joseph
 */
public final class MinotaurAssets {

    private MinotaurAssets() {}

    private static boolean built = false;

    /** A full four-animation character built from the minotaur sheets. */
    public static class ActorSet {
        public Sequence idleR, idleL, runR, runL, swingR, swingL, deathR, deathL;

        public Sequence idle(boolean right) { return copy(right ? idleR : idleL); }
        public Sequence run(boolean right)  { return copy(right ? runR : runL); }
        public Sequence swing(boolean right){ return copy(right ? swingR : swingL); }
        public Sequence death(boolean right){ return copy(right ? deathR : deathL); }
    }

    /** The player. */
    public static ActorSet hero;
    /** A larger minotaur turned by the labyrinth — an elite melee enemy. */
    public static ActorSet cursed;
    /** The Warlord: the boss, same silhouette blown up and burnt black. */
    public static ActorSet warlord;

    // procedural enemies
    public static Sequence impR, impL;
    public static Sequence batR, batL;
    public static Sequence spitterR, spitterL, spitterCastR, spitterCastL;
    public static Sequence golemR, golemL, golemPunchR, golemPunchL;
    public static Sequence golemBrokenR, golemBrokenL, golemBrokenPunchR, golemBrokenPunchL;

    // effects. Note there is no slash arc here: the minotaur swing sheet draws its own, and
    // laying a second crescent over it only ever read as a doubled trail.
    public static Sequence barrelBreak;
    public static Sprite barrel;
    public static Sprite fireball, bossOrb;
    public static Sprite healthPickup, ragePickup;
    public static Sprite shockRingSmall, shockRingLarge;

    /** Sprite dimensions the actors need for their collision boxes. */
    public static final int HERO_FOOT_OFFSET = MinotaurArt.FOOT_OFFSET;

    /* ==================================================================
     *  build
     * ================================================================== */

    public static void initialize() {
        if (built) return;
        built = true;

        final String dir = "DemoAssets/SideScroll/Minotaur/";
        BufferedImage[] rawIdle  = Graphic.loadSequence(dir + "MinotaurIdle_Right");
        BufferedImage[] rawRun   = Graphic.loadSequence(dir + "MinotaurRun_Right");
        BufferedImage[] rawSwing = Graphic.loadSequence(dir + "MinotaurSwing_Right");
        BufferedImage[] rawDeath = Graphic.loadSequence(dir + "MinotaurDeath_Right");

        // Only the right-facing sheets are used; left is a mirror of the normalised canvas.
        // That guarantees the character does not shift sideways when it turns, and it gives
        // us a left-facing death animation the original art never had.
        BufferedImage[] idle  = MinotaurArt.normalizeIdle(rawIdle);
        BufferedImage[] run   = MinotaurArt.normalizeRun(rawRun);
        BufferedImage[] swing = MinotaurArt.normalizeSwing(rawSwing);
        BufferedImage[] death = MinotaurArt.normalizeDeath(rawDeath);

        // The swing sequence is four frames, so its frame delay sets how long a visible attack
        // takes. The player's is timed to finish inside one combo link — a slower sequence would
        // still be playing frame two when the next hit already landed, and the character would
        // look like it was swinging far less often than the player is actually attacking.
        hero = buildActorSet(idle, run, swing, death, null, 0, 0, 46);
        cursed = buildActorSet(idle, run, swing, death, new Color(96, 40, 130), 0.5, 0.78, 76);
        warlord = buildActorSet(idle, run, swing, death, new Color(150, 24, 30), 0.55, 0.62, 92);

        impR = seq(MinotaurArt.impFrames(), 110, true);
        impL = mirrorOf(impR, 110, true);

        batR = seq(MinotaurArt.batFrames(), 70, true);
        batL = mirrorOf(batR, 70, true);

        spitterR = seq(MinotaurArt.spitterFrames(false), 170, true);
        spitterL = mirrorOf(spitterR, 170, true);
        spitterCastR = seq(MinotaurArt.spitterFrames(true), 95, false);
        spitterCastL = mirrorOf(spitterCastR, 95, false);

        golemR = seq(MinotaurArt.golemFrames(false, false), 190, true);
        golemL = mirrorOf(golemR, 190, true);
        golemPunchR = seq(MinotaurArt.golemFrames(false, true), 95, false);
        golemPunchL = mirrorOf(golemPunchR, 95, false);
        golemBrokenR = seq(MinotaurArt.golemFrames(true, false), 150, true);
        golemBrokenL = mirrorOf(golemBrokenR, 150, true);
        golemBrokenPunchR = seq(MinotaurArt.golemFrames(true, true), 80, false);
        golemBrokenPunchL = mirrorOf(golemBrokenPunchR, 80, false);

        barrel = new Sprite(MinotaurArt.barrel());
        barrelBreak = seq(MinotaurArt.barrelBreak(), 70, false);

        fireball = new Sprite(MinotaurArt.energyBall(46, new Color(120, 255, 205), new Color(30, 190, 150)));
        bossOrb = new Sprite(MinotaurArt.energyBall(74, new Color(255, 170, 120), new Color(230, 60, 40)));
        healthPickup = new Sprite(MinotaurArt.pickupOrb(new Color(255, 78, 96)));
        ragePickup = new Sprite(MinotaurArt.pickupOrb(new Color(255, 190, 70)));
        shockRingSmall = new Sprite(MinotaurArt.shockRing(200, new Color(255, 214, 150)));
        shockRingLarge = new Sprite(MinotaurArt.shockRing(420, new Color(255, 150, 90)));
    }

    private static ActorSet buildActorSet(BufferedImage[] idle, BufferedImage[] run,
                                          BufferedImage[] swing, BufferedImage[] death,
                                          Color tint, double blend, double brightness,
                                          int swingFrameDelay) {
        if (tint != null) {
            idle = MinotaurArt.recolor(idle, tint, blend, brightness);
            run = MinotaurArt.recolor(run, tint, blend, brightness);
            swing = MinotaurArt.recolor(swing, tint, blend, brightness);
            death = MinotaurArt.recolor(death, tint, blend, brightness);
        }
        ActorSet set = new ActorSet();
        set.idleR = seq(idle, 150, true);
        set.idleL = mirrorOf(set.idleR, 150, true);
        set.runR = seq(run, 62, true);
        set.runL = mirrorOf(set.runR, 62, true);
        set.swingR = seq(swing, swingFrameDelay, false);
        set.swingL = mirrorOf(set.swingR, swingFrameDelay, false);
        set.deathR = seq(death, 115, false);
        set.deathL = mirrorOf(set.deathR, 115, false);
        return set;
    }

    /* ==================================================================
     *  sequence plumbing
     * ================================================================== */

    private static final Map<Sequence, BufferedImage[]> SOURCE_FRAMES = new ConcurrentHashMap<>(); // concurrent: MinotaurGame.swapTo builds the next floor on a loader thread while the outgoing one is still drawing

    private static Sequence seq(BufferedImage[] frames, int frameDelay, boolean looping) {
        Sequence s = new Sequence(frames);
        s.setFrameDelay(frameDelay);
        s.setLooping(looping);
        SOURCE_FRAMES.put(s, frames);
        return s;
    }

    private static Sequence mirrorOf(Sequence template, int frameDelay, boolean looping) {
        return seq(MinotaurArt.mirror(SOURCE_FRAMES.get(template)), frameDelay, looping);
    }

    /**
     * A private playhead over a shared template. Every actor calls this instead of using a
     * template directly, so restarting one creature's attack cannot rewind another's.
     */
    public static Sequence copy(Sequence template) {
        Sequence out = template.copyMaintainSource();
        out.restart();
        return out;
    }

    /** Pixel size of one frame of a template, useful for laying out collision boxes. */
    public static int frameWidth(Sequence template) {
        BufferedImage[] f = SOURCE_FRAMES.get(template);
        return f == null ? 0 : f[0].getWidth();
    }

    public static int frameHeight(Sequence template) {
        BufferedImage[] f = SOURCE_FRAMES.get(template);
        return f == null ? 0 : f[0].getHeight();
    }

    /* ==================================================================
     *  per-level terrain art, built on demand and shared between levels
     *  that use the same palette
     * ================================================================== */

    private static final Map<String, Sprite> TERRAIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Sequence> TERRAIN_SEQ_CACHE = new ConcurrentHashMap<>();

    public static Sprite platform(int w, int h, MinotaurArt.Palette p, boolean capped) {
        String key = "plat|" + w + "|" + h + "|" + p.name + "|" + capped;
        return TERRAIN_CACHE.computeIfAbsent(key, k -> new Sprite(MinotaurArt.platform(w, h, p, capped)));
    }

    public static Sprite ledge(int w, MinotaurArt.Palette p) {
        return TERRAIN_CACHE.computeIfAbsent("ledge|" + w + "|" + p.name,
                k -> new Sprite(MinotaurArt.ledge(w, p)));
    }

    public static Sprite spikes(int w, MinotaurArt.Palette p) {
        return TERRAIN_CACHE.computeIfAbsent("spike|" + w + "|" + p.name,
                k -> new Sprite(MinotaurArt.spikes(w, p)));
    }

    public static Sprite lavaBody(int w, int h) {
        return TERRAIN_CACHE.computeIfAbsent("lavaBody|" + w + "|" + h,
                k -> new Sprite(MinotaurArt.lavaBody(w, h)));
    }

    public static Sprite pillar(int heightTiles, MinotaurArt.Palette p) {
        return TERRAIN_CACHE.computeIfAbsent("pillar|" + heightTiles + "|" + p.name,
                k -> new Sprite(MinotaurArt.pillar(heightTiles, p)));
    }

    public static Sequence lava(int w, MinotaurArt.Palette p) {
        Sequence template = TERRAIN_SEQ_CACHE.computeIfAbsent("lava|" + w + "|" + p.name,
                k -> seq(MinotaurArt.lava(w, p), 150, true));
        return copy(template);
    }

    public static Sequence brazier(MinotaurArt.Palette p) {
        Sequence template = TERRAIN_SEQ_CACHE.computeIfAbsent("brazier|" + p.name,
                k -> seq(MinotaurArt.brazier(p), 110, true));
        return copy(template);
    }

    public static Sequence portal(MinotaurArt.Palette p, boolean open) {
        Sequence template = TERRAIN_SEQ_CACHE.computeIfAbsent("portal|" + p.name + "|" + open,
                k -> seq(MinotaurArt.portalFrames(p, open), open ? 90 : 200, true));
        return copy(template);
    }
}

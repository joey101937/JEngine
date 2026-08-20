package GameDemo.Minotaur;

import Framework.DCoordinate;
import Framework.Game;
import Framework.Main;
import Framework.Window;
import GameDemo.Minotaur.Actors.PlayerMinotaur;
import GameDemo.Minotaur.Effects.CameraAnchor;
import GameDemo.Minotaur.Effects.MinotaurHUD;
import GameDemo.Minotaur.Effects.Particles;
import GameDemo.Minotaur.Levels.LevelDef;
import GameDemo.Minotaur.Levels.LevelLibrary;
import GameDemo.Minotaur.Levels.MinotaurLevel;
import java.awt.Color;
import java.awt.RenderingHints;

/**
 * Entry point and run state for <b>Minotaur</b> — a side-scrolling action game built on JEngine.
 *
 * <h3>The game</h3>
 * Four floors of a labyrinth, fought through with a three-hit combo, a heavy armour-breaking
 * swing, an invulnerable dash and a rage roar that clears a room. Five enemy types that each
 * ask a different question, and a three-phase boss at the bottom.
 *
 * <h3>The shape of the code</h3>
 * A level is a {@link MinotaurLevel} (a {@link Game}) built from a {@link LevelDef} blueprint,
 * and this class owns the statics everything else reaches for — the current level, the player,
 * the HUD, the particle layers and the camera anchor. Levels are built and retired on a loader
 * thread so the running game never blocks on generating the next floor's backdrop, and the
 * outgoing level is disposed once it is genuinely off-screen, which is what keeps a run through
 * all four floors from stacking four multi-megabyte backdrops in memory.
 *
 * <h3>Controls</h3>
 * A/D move · SPACE jump (again in the air for a second jump) · S+SPACE drop through a ledge ·
 * LEFT CLICK swing · RIGHT CLICK heavy swing · SHIFT dash · E rage roar · R retry · M mute ·
 * F1 debug view · X options.
 *
 * @author Joseph
 */
public class MinotaurGame {

    public static final int TPS = 60;
    private static final int STARTING_LIVES = 2;

    /* ---------------- live references ---------------- */

    public static volatile MinotaurLevel level;
    public static volatile PlayerMinotaur player;
    public static volatile MinotaurHUD hud;
    /** Particles drawn over the actors. */
    public static volatile Particles fx;
    /** Particles drawn under the actors — dust and after-images. */
    public static volatile Particles fxUnder;
    public static volatile CameraAnchor cameraAnchor;

    /* ---------------- run state ---------------- */

    public static volatile int score = 0;
    public static volatile int lives = STARTING_LIVES;
    public static volatile int levelIndex = 0;

    private static volatile boolean gameOver = false;
    private static volatile boolean victory = false;
    private static volatile boolean loading = false;

    public static void main(String[] args) {
        Main.ticksPerSecond = TPS;
        Main.enableLerping = true;
        Main.lerpType = "reactive";
        Main.tickThreadCount = 1;
        // renderThreadCount is left on the engine default (parallel): a render pass here is not
        // just blits — per-object transform setup, the particle pass, the parallax geometry and
        // the HUD layout are all real work that spreads across cores. Every renderable in this
        // game draws through its own Graphics2D copy, so sharing the surface is safe.
        Main.ignoreSubobjectCollision = true;
        Main.ignoreCollisionsForStillObjects = true;
        // Pixel art must scale by whole pixels or it turns to mush, so image interpolation is
        // forced to nearest-neighbour: that is what keeps the three-times-magnified sprites hard
        // when the Warlord is drawn at two and a half times again on top of that. Everything
        // else stays on the engine defaults, including shape and text anti-aliasing, because the
        // HUD and the light effects are not pixel art.
        Main.renderingHintSetter = g -> {
            Game.applyDefaultRenderingHints(g);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        };

        MinotaurAssets.initialize();
        MinotaurSfx.initialize();

        score = 0;
        lives = STARTING_LIVES;
        levelIndex = 0;
        gameOver = false;
        victory = false;

        MinotaurLevel first = buildLevel(levelIndex, -1);
        Window.initialize(first);
        announceFloor(first);
    }

    /* ==================================================================
     *  level lifecycle
     * ================================================================== */

    /**
     * Creates a floor and publishes it, together with a fresh player, camera anchor and input
     * handler. Nothing here starts the game loop; the caller decides whether this is the first
     * level (window creation) or a swap.
     *
     * @param index       floor to build
     * @param healthCarry health the player arrives with, or -1 for full
     */
    private static MinotaurLevel buildLevel(int index, int healthCarry) {
        LevelDef def = LevelLibrary.get(index);
        MinotaurLevel next = new MinotaurLevel(def);

        PlayerMinotaur fresh = new PlayerMinotaur(new DCoordinate(def.spawnX, def.spawnY));
        if (healthCarry > 0) fresh.setCurrentHealth(healthCarry);
        CameraAnchor anchor = new CameraAnchor(fresh);

        next.hud.resetBars();
        next.addObject(fresh);
        next.addObject(anchor);
        anchor.snap();
        next.getCamera().setTarget(anchor);
        next.testObject = fresh;
        next.setInputHandler(new MinotaurInput());

        // Published last and together. Building a floor takes long enough to span many ticks of
        // the level being replaced, and everything in the game reaches for these statics — so
        // they are swapped in one step once the new world is fully assembled, rather than
        // leaving the outgoing level pointing at half of the incoming one.
        level = next;
        player = fresh;
        hud = next.hud;
        fx = next.fxOver;
        fxUnder = next.fxUnder;
        cameraAnchor = anchor;
        return next;
    }

    private static void announceFloor(MinotaurLevel lvl) {
        if (lvl.hud != null) {
            lvl.hud.flashBanner(lvl.def.title.toUpperCase(), lvl.def.subtitle,
                    new Color(255, 226, 180), 190);
        }
    }

    /**
     * Swaps in a different floor. Runs on its own thread: building a level renders a
     * multi-megabyte backdrop, and doing that inside a tick would stall the game loop that
     * called it. The outgoing level is frozen immediately so play cannot continue during the
     * hand-off, and disposed once the swap has settled.
     */
    private static void swapTo(int index, int healthCarry, String bannerText, String bannerSub) {
        if (loading) return;
        loading = true;
        MinotaurLevel outgoing = level;
        if (outgoing != null) {
            outgoing.hitStop = Integer.MAX_VALUE;   // freeze everything in the old world
            if (outgoing.hud != null && bannerText != null) {
                outgoing.hud.flashBanner(bannerText, bannerSub, new Color(255, 226, 180), 120);
            }
        }
        Thread loader = new Thread(() -> {
            try {
                MinotaurLevel next = buildLevel(index, healthCarry);
                Window.setCurrentGame(next);
                announceFloor(next);
                if (outgoing != null && outgoing != next) {
                    // let the retired loop finish the frame it is on before its images go away
                    Main.wait(300);
                    outgoing.dispose();
                    // dispose deliberately leaves the window alone, so the dead canvas is
                    // detached here instead — otherwise a full run leaves four of them parented
                    javax.swing.SwingUtilities.invokeLater(() ->
                            Window.panel.remove(outgoing.getCanvas()));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                loading = false;
            }
        }, "Minotaur level loader");
        loader.setDaemon(true);
        loader.start();
    }

    /* ==================================================================
     *  progression
     * ================================================================== */

    /** Called by the exit portal once the player steps through it. */
    public static void advanceLevel() {
        if (loading || victory || gameOver) return;
        int next = levelIndex + 1;
        if (next >= LevelLibrary.COUNT) {
            declareVictory();
            return;
        }
        levelIndex = next;
        // clearing a floor tops you up to at least this much, so a bruising fight never leaves
        // the next floor unwinnable from the first step
        int carry = player == null ? -1 : Math.max(player.getCurrentHealth(),
                (int) (player.getMaxHealth() * 0.65));
        MinotaurSfx.levelClear();
        swapTo(levelIndex, carry, "DESCENDING", "Floor " + roman(levelIndex + 1));
    }

    /** Called by the Warlord when its death animation completes. */
    public static void onBossDefeated() {
        declareVictory();
    }

    private static void declareVictory() {
        if (victory) return;
        victory = true;
        MinotaurSfx.victory();
        if (hud != null) hud.clearBanner();
    }

    /** Called by the player a moment after its death animation starts. */
    public static void onPlayerDeath() {
        if (gameOver || victory) return;
        gameOver = true;
        MinotaurSfx.gameOver();
        if (hud != null) hud.clearBanner();
    }

    /** The R key. Retries the current floor, or starts a whole new run once lives run out. */
    public static void requestRestart() {
        if (loading) return;
        if (victory) {
            score = 0;
            lives = STARTING_LIVES;
            levelIndex = 0;
            victory = false;
            gameOver = false;
            swapTo(0, -1, null, null);
            return;
        }
        if (!gameOver) return;
        gameOver = false;
        if (lives > 0) {
            lives--;
            swapTo(levelIndex, -1, null, null);
        } else {
            score = 0;
            lives = STARTING_LIVES;
            levelIndex = 0;
            swapTo(0, -1, null, null);
        }
    }

    /* ==================================================================
     *  shared helpers
     * ================================================================== */

    public static void addScore(int amount) {
        score += amount;
    }

    public static boolean isGameOver() { return gameOver; }

    public static boolean isVictory() { return victory; }

    public static String levelTitle() {
        MinotaurLevel l = level;
        if (l == null) return "The Labyrinth";
        return l.def.subtitle + "  ·  " + l.def.title;
    }

    private static String roman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            default: return String.valueOf(n);
        }
    }
}

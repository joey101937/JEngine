package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import java.awt.Color;

/**
 * Runs the flow of the game: spawns escalating waves of aliens, throws a boss into
 * the mix every few waves, handles the pauses between waves, and owns win/lose
 * transitions. Every third wave is a boss encounter, so a boss is always reached.
 *
 * @author Joseph
 */
public class WaveManager extends IndependentEffect {

    private enum State { INTERMISSION, WAVE_ACTIVE, BOSS_ACTIVE, GAME_OVER }

    private State state = State.INTERMISSION;
    private int level = 0;
    private int interTimer;
    private int bossesDefeated = 0;
    private Boss activeBoss = null;

    public WaveManager() {
        interTimer = SpaceInvadersGame.TPS * 2; // brief run-in before wave 1
    }

    private int tps() { return SpaceInvadersGame.TPS; }

    public int getLevel() { return level; }
    public Boss getActiveBoss() { return activeBoss; }
    public boolean isGameOver() { return state == State.GAME_OVER; }
    public int getBossesDefeated() { return bossesDefeated; }

    @Override
    public void tick() {
        switch (state) {
            case INTERMISSION:
                if (--interTimer <= 0) startNextWave();
                break;
            case WAVE_ACTIVE:
                if (countAliens() == 0) beginIntermission("WAVE CLEARED", new Color(120, 255, 180), false);
                break;
            case BOSS_ACTIVE:
                // the boss triggers the transition itself via onBossDefeated()
                break;
            case GAME_OVER:
                break;
        }
    }

    /* ===================== wave construction ===================== */

    private void startNextWave() {
        level++;
        if (level % 3 == 0) {
            spawnBoss();
        } else {
            spawnFormation();
            state = State.WAVE_ACTIVE;
            SpaceInvadersGame.ui.flashMessage("WAVE " + level, new Color(150, 220, 255), (int) (tps() * 1.5));
        }
    }

    private void spawnFormation() {
        int w = SpaceInvadersGame.game.getWorldWidth();
        int cols = 8;
        int rows = 2 + Math.min(3, level / 2);
        double spacingX = w / (double) (cols + 1);
        int topY = SpaceInvadersGame.game.worldBorder + 40;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = spacingX * (c + 1);
                double y = topY + r * 64; // formation across the upper arena
                Alien a;
                int roll = Main.generateRandomInt(0, 100);
                if (level >= 2 && r == rows - 1 && roll < 22) {
                    a = new TankAlien(new DCoordinate(x, y));
                } else if (roll < 18 + level * 3) {
                    a = new DiverAlien(new DCoordinate(x, y));
                } else {
                    a = new GruntAlien(new DCoordinate(x, y));
                }
                SpaceInvadersGame.game.addObject(a);
            }
        }
    }

    private void spawnBoss() {
        int w = SpaceInvadersGame.game.getWorldWidth();
        activeBoss = new Boss(new DCoordinate(w / 2.0, 160), bossesDefeated + 1);
        SpaceInvadersGame.game.addObject(activeBoss);
        state = State.BOSS_ACTIVE;
        RetroSfx.bossWarn();
        SpaceInvadersGame.ui.flashMessage("WARNING: DREADNOUGHT", new Color(255, 80, 80), (int) (tps() * 2.2));
        SpaceInvadersGame.ui.shake(12);
    }

    private void beginIntermission(String msg, Color color, boolean victory) {
        state = State.INTERMISSION;
        interTimer = (int) (tps() * (victory ? 3.5 : 2.5));
        if (victory) RetroSfx.victory(); else RetroSfx.waveClear();
        SpaceInvadersGame.ui.flashMessage(msg, color, (int) (tps() * 2));
    }

    /* ===================== callbacks ===================== */

    public void onBossDefeated() {
        bossesDefeated++;
        activeBoss = null;
        beginIntermission("SECTOR CLEARED!", new Color(255, 220, 120), true);
    }

    public void onPlayerDefeated() {
        state = State.GAME_OVER;
        RetroSfx.gameOver();
        SpaceInvadersGame.ui.onGameOver();
    }

    /* ===================== restart ===================== */

    /** Wipes the arena and starts a fresh run. Triggered from the input handler. */
    public void restart() {
        for (GameObject2 o : SpaceInvadersGame.game.getAllObjects()) {
            if (o instanceof Alien || o instanceof Laser || o instanceof PowerUp || o instanceof Explosion) {
                o.destroy();
            }
        }
        activeBoss = null;
        level = 0;
        bossesDefeated = 0;
        SpaceInvadersGame.ui.reset();

        PlayerShip fresh = new PlayerShip(new DCoordinate(
                SpaceInvadersGame.game.getWorldWidth() / 2.0,
                SpaceInvadersGame.game.getWorldHeight() - 90));
        SpaceInvadersGame.player = fresh;
        SpaceInvadersGame.game.addObject(fresh);
        SpaceInvadersGame.game.setInputHandler(SpaceInvadersGame.input);

        state = State.INTERMISSION;
        interTimer = tps() * 2;
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        // the WaveManager is logic-only; all on-screen text is drawn by the HUD
    }

    private int countAliens() {
        int n = 0;
        for (GameObject2 o : SpaceInvadersGame.game.getAllObjects()) {
            if (o instanceof Alien) n++;
        }
        return n;
    }

    @Override
    public boolean shouldSerialize() { return false; }
}

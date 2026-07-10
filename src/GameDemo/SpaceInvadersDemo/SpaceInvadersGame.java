package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import Framework.Game;
import Framework.Main;
import Framework.Window;

/**
 * Entry point and shared references for the Space Invaders demo — a showcase of the
 * engine driving a responsive arcade shooter with momentum flight, escalating alien
 * waves, collectible power-ups, a stocked "Nova Bomb" special, a multi-phase boss,
 * fully hand-drawn Java2D art, and a procedural chiptune sound engine.
 *
 * Controls: WASD/arrows to fly, SPACE to fire, E to detonate a Nova Bomb, R to
 * restart after a game over, X for the options menu.
 *
 * @author Joseph
 */
public class SpaceInvadersGame {

    public static final int TPS = 60;
    public static final int WORLD_W = 1200;
    public static final int WORLD_H = 820;

    public static Game game;
    public static PlayerShip player;
    public static SpaceInvadersUI ui;
    public static WaveManager driver;
    public static SpaceInvadersInput input;
    public static Starfield starfield;

    /** difficulty scalar derived from the current wave (0 on wave 1). */
    public static int difficulty() {
        return driver == null ? 0 : Math.max(0, driver.getLevel() - 1);
    }

    public static void main(String[] args) {
        Main.ticksPerSecond = TPS;
        Main.enableLerping = true;

        SIAssets.initialize(WORLD_W, WORLD_H);
        RetroSfx.initialize();

        game = new Game(SIAssets.background);
        game.setName("Space Invaders");
        game.worldBorder = 26;
        Window.initialize(game);

        ui = new SpaceInvadersUI();
        driver = new WaveManager();
        input = new SpaceInvadersInput();
        starfield = new Starfield(WORLD_W, WORLD_H);

        player = new PlayerShip(new DCoordinate(WORLD_W / 2.0, WORLD_H - 90));
        game.addObject(player);

        game.addIndependentEffect(starfield);
        game.addIndependentEffect(ui);
        game.addIndependentEffect(driver);
        game.setInputHandler(input);
    }
}

package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import java.awt.Color;

/**
 * A fast, aggressive scout that steers toward the player's horizontal position as
 * it descends and fires aimed shots. Fragile but dangerous — it will happily dive
 * straight into you.
 *
 * @author Joseph
 */
public class DiverAlien extends Alien {

    private final double descent;

    public DiverAlien(DCoordinate spawn) {
        super(spawn, SIAssets.diver, 35, 150);
        dropChance = 0.13;
        descent = 1.0 + SpaceInvadersGame.difficulty() * 0.12;
    }

    @Override
    public void tick() {
        super.tick();
        PlayerShip p = player();
        if (p != null) {
            double dx = p.getLocation().x - getLocation().x;
            velocity.x = Framework.Main.clamp(dx * 0.03, 2.6, -2.6);
        } else {
            velocity.x *= 0.9;
        }
        velocity.y = descent;

        if (fireTimer <= 0) {
            shootAtPlayer(12);
            fireTimer = (int) (tps() * 2.0) + Framework.Main.generateRandomInt(0, tps());
        }
    }

    @Override
    protected Color explosionColor() { return new Color(120, 255, 150); }
}

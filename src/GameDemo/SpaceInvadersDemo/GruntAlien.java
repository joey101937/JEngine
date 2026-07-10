package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import java.awt.Color;

/**
 * The rank-and-file invader: drifts side to side on a sine wave while creeping
 * downward, taking the occasional pot-shot straight down.
 *
 * @author Joseph
 */
public class GruntAlien extends Alien {

    private double phase = Math.random() * Math.PI * 2;
    private final double descent;

    public GruntAlien(DCoordinate spawn) {
        super(spawn, SIAssets.grunt, 50, 100);
        dropChance = 0.09;
        descent = 0.32 + SpaceInvadersGame.difficulty() * 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        phase += 0.04;
        velocity.x = Math.sin(phase) * 1.7;
        velocity.y = descent;

        if (fireTimer <= 0) {
            shootDown(10);
            fireTimer = (int) (tps() * (2.6 - Math.min(1.4, SpaceInvadersGame.difficulty() * 0.2)))
                    + Framework.Main.generateRandomInt(0, tps());
        }
    }

    @Override
    protected Color explosionColor() { return new Color(210, 120, 255); }
}

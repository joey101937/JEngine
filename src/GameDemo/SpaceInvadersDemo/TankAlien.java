package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import java.awt.Color;

/**
 * A heavily armored bruiser: slow, sways gently, soaks up a lot of fire and
 * answers with aimed three-round bursts.
 *
 * @author Joseph
 */
public class TankAlien extends Alien {

    private double phase = Math.random() * Math.PI * 2;
    private int burstLeft = 0;
    private int burstTimer = 0;

    public TankAlien(DCoordinate spawn) {
        super(spawn, SIAssets.tank, 160, 250);
        dropChance = 0.24;
    }

    @Override
    public void tick() {
        super.tick();
        phase += 0.02;
        velocity.x = Math.sin(phase) * 0.8;
        velocity.y = 0.24 + SpaceInvadersGame.difficulty() * 0.03;

        // fire a tight burst, then a long reload
        if (burstLeft > 0) {
            if (burstTimer <= 0) {
                shootAtPlayer(14);
                burstLeft--;
                burstTimer = tps() / 6;
            } else {
                burstTimer--;
            }
        } else if (fireTimer <= 0) {
            burstLeft = 3;
            burstTimer = 0;
            fireTimer = (int) (tps() * 3.2) + Framework.Main.generateRandomInt(0, tps());
        }
    }

    @Override
    protected Color explosionColor() { return new Color(255, 150, 60); }
}

package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import java.awt.Color;

/**
 * The dreadnought boss. It sweeps across the top of the arena and cycles through
 * three escalating attack phases as its health drops — aimed fans, then radial
 * bursts, then an enraged storm of both. Its glowing core is the weak point.
 *
 * @author Joseph
 */
public class Boss extends Alien {

    private double sweepPhase = 0;
    private int spawnTicks = 0;
    private final double hoverY;
    private final int bossNumber;
    private boolean dying = false;

    public Boss(DCoordinate spawn, int bossNumber) {
        super(spawn, SIAssets.boss, 260 + (bossNumber - 1) * 150, 5000);
        this.bossNumber = bossNumber;
        this.hoverY = spawn.y;
        dropChance = 0; // handled explicitly on death
        setZLayer(3);
    }

    public double getHealthRatio() { return Math.max(0, health / (double) maxHealth); }
    public int getBossNumber() { return bossNumber; }

    private double phaseFactor() {
        // 0 at full health, 1 near death — used to speed everything up
        return 1.0 - getHealthRatio();
    }

    @Override
    public void tick() {
        super.tick();
        if (dying) return;

        // grand entrance: scale up over the first half-second
        if (spawnTicks < tps() / 2) {
            spawnTicks++;
            setRenderScale(0.25 + 0.75 * (spawnTicks / (double) (tps() / 2)));
        }

        // horizontal sweep, faster as it takes damage
        sweepPhase += 0.012 + phaseFactor() * 0.02;
        double margin = getWidth() / 2.0 + 60;
        double span = (getHostGame().getWorldWidth() - margin * 2) / 2.0;
        double centerX = getHostGame().getWorldWidth() / 2.0;
        double targetX = centerX + Math.sin(sweepPhase) * span;
        velocity.x = (targetX - getLocation().x) * 0.08;
        velocity.y = (hoverY - getLocation().y) * 0.05;

        // enrage glow in the final third
        if (getHealthRatio() < 0.33 && hitFlashTicks == 0) {
            setRenderBrightness(1.15 + 0.15 * Math.sin(getGameTickNumber() * 0.3));
        }

        if (fireTimer <= 0) fireAttack();
    }

    private void fireAttack() {
        double ratio = getHealthRatio();
        if (ratio > 0.66) {
            // Phase 1: slow aimed 3-way fan
            aimedFan(3, 18, 16);
            fireTimer = (int) (tps() * 1.6);
        } else if (ratio > 0.33) {
            // Phase 2: alternate aimed fans and radial bursts
            if ((getGameTickNumber() / (tps())) % 2 == 0) {
                aimedFan(5, 16, 15);
            } else {
                radial(8, 0, 14);
            }
            fireTimer = (int) (tps() * 1.2);
        } else {
            // Phase 3: enraged — radial storm plus an aimed volley
            radial(12, getGameTickNumber() * 0.15, 14);
            aimedFan(3, 12, 15);
            fireTimer = (int) (tps() * 0.85);
        }
    }

    private void aimedFan(int count, double spreadDeg, int dmg) {
        PlayerShip p = player();
        double baseAng = (p != null)
                ? Math.atan2(p.getLocation().y - getLocation().y, p.getLocation().x - getLocation().x)
                : Math.PI / 2; // straight down if no target
        double start = baseAng - Math.toRadians(spreadDeg * (count - 1) / 2.0);
        for (int i = 0; i < count; i++) {
            double ang = start + Math.toRadians(spreadDeg) * i;
            fireOrb(Math.cos(ang), Math.sin(ang), dmg);
        }
        RetroSfx.enemyShoot();
    }

    private void radial(int count, double offsetRad, int dmg) {
        for (int i = 0; i < count; i++) {
            double ang = offsetRad + Math.PI * 2 * i / count;
            fireOrb(Math.cos(ang), Math.sin(ang), dmg);
        }
        RetroSfx.enemyShoot();
    }

    private void fireOrb(double dx, double dy, int dmg) {
        DCoordinate from = getLocation();
        DCoordinate dest = from.copy().add(new DCoordinate(dx * 300, dy * 300));
        Laser orb = new Laser(from.copy(), dest, false, dmg, SIAssets.bossOrb, new Color(255, 120, 60));
        getHostGame().addObject(orb);
    }

    /* the boss does not shatter on contact — it bullies the player instead */
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (other instanceof PlayerShip && health > 0) {
            ((PlayerShip) other).takeHit(30, getPixelLocation());
        }
    }

    @Override
    public void takeDamage(int dmg) {
        if (dying) return;
        RetroSfx.bossHit();
        super.takeDamage(dmg);
    }

    @Override
    protected void onKilled() {
        dying = true;
        SpaceInvadersGame.ui.addScore(scoreValue);
        // reward the player for the kill
        PowerUp.drop(getHostGame(), getPixelLocation(), PowerUp.Type.NOVA);
        PowerUp.drop(getHostGame(), getPixelLocation().add(new Framework.Coordinate(50, 0)), PowerUp.Type.SHIELD);
        // a cascade of explosions across the hull
        for (int i = 0; i < 10; i++) {
            final int n = i;
            addTickDelayedEffect(i * 5, game -> {
                Framework.Coordinate c = getPixelLocation().copy();
                c.x += Framework.Main.generateRandomInt(-getWidth() / 2, getWidth() / 2);
                c.y += Framework.Main.generateRandomInt(-getHeight() / 2, getHeight() / 2);
                if (n % 3 == 0) Explosion.big(game, c, new Color(255, 150, 60));
                else Explosion.medium(game, c, new Color(255, 200, 120));
            });
        }
        SpaceInvadersGame.driver.onBossDefeated();
    }
}

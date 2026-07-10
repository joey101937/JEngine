package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import java.awt.Color;

/**
 * Shared behaviour for every hostile ship: taking damage (with a hit-flash), dying
 * (explosion + score + a chance to drop a power-up), shooting, and "breaching" the
 * bottom of the play area. Movement/firing cadence is left to subclasses.
 *
 * @author Joseph
 */
public abstract class Alien extends GameObject2 {

    protected int health;
    protected final int maxHealth;
    protected final int scoreValue;
    protected int fireTimer;
    protected int hitFlashTicks = 0;
    protected double dropChance = 0.10;
    private boolean escaped = false;

    protected Alien(DCoordinate spawn, Sprite sprite, int health, int scoreValue) {
        super(spawn);
        this.health = this.maxHealth = health;
        this.scoreValue = scoreValue;
        setGraphic(sprite);
        isSolid = true;
        preventOverlap = false; // arcade swarm: aliens overlap freely, but still register hits
        movementType = MovementType.RawVelocity;
        setZLayer(3);
        this.fireTimer = 40 + Framework.Main.generateRandomInt(0, 120);
    }

    protected int tps() { return SpaceInvadersGame.TPS; }

    protected PlayerShip player() {
        PlayerShip p = SpaceInvadersGame.player;
        return (p != null && p.isAlive()) ? p : null;
    }

    /* ===================== shared tick ===================== */

    @Override
    public void tick() {
        super.tick();
        if (hitFlashTicks > 0) {
            hitFlashTicks--;
            setRenderBrightness(1.0 + hitFlashTicks * 0.3);
            setRenderScale(1.0 + hitFlashTicks * 0.04);
        } else {
            setRenderBrightness(1.0);
            setRenderScale(1.0);
        }
        if (fireTimer > 0) fireTimer--;
        checkBreach();
    }

    /** Aliens that slip past the bottom ram the player's line and are lost. */
    private void checkBreach() {
        if (escaped) return;
        double limit = getHostGame().getWorldHeight() - getHostGame().worldBorder - 2;
        if (getLocation().y >= limit) {
            escaped = true;
            PlayerShip p = player();
            if (p != null) p.takeHit(12, getPixelLocation());
            Explosion.medium(getHostGame(), getPixelLocation(), new Color(255, 90, 90));
            destroy();
        }
    }

    /* ===================== combat ===================== */

    public void takeDamage(int dmg) {
        if (health <= 0) return;
        health -= dmg;
        hitFlashTicks = 5;
        if (health <= 0) {
            onKilled();
            destroy();
        }
    }

    protected void onKilled() {
        SpaceInvadersGame.ui.addScore(scoreValue);
        Explosion.medium(getHostGame(), getPixelLocation(), explosionColor());
        maybeDropPowerUp();
    }

    protected Color explosionColor() { return new Color(255, 170, 80); }

    protected void maybeDropPowerUp() {
        if (Framework.Main.generateRandomDoubleLocally(0, 1) < dropChance) {
            PowerUp.drop(getHostGame(), getPixelLocation(), rollDropType());
        }
    }

    private PowerUp.Type rollDropType() {
        int roll = Framework.Main.generateRandomInt(0, 100);
        if (roll < 35) return PowerUp.Type.RAPID;
        if (roll < 65) return PowerUp.Type.SPREAD;
        if (roll < 85) return PowerUp.Type.SHIELD;
        return PowerUp.Type.NOVA;
    }

    /* ===================== shooting ===================== */

    protected void shootDown(int dmg) {
        DCoordinate muzzle = getLocation().add(new DCoordinate(0.0, getHeight() / 2.0));
        DCoordinate dest = muzzle.copy().add(new DCoordinate(0.0, 200));
        fireEnemyLaser(muzzle, dest, dmg);
    }

    protected void shootAtPlayer(int dmg) {
        PlayerShip p = player();
        if (p == null) { shootDown(dmg); return; }
        DCoordinate muzzle = getLocation().add(new DCoordinate(0.0, getHeight() / 2.0));
        fireEnemyLaser(muzzle, p.getLocation(), dmg);
    }

    protected void fireEnemyLaser(DCoordinate from, DCoordinate to, int dmg) {
        Laser bolt = new Laser(from.copy(), to.copy(), false, dmg, SIAssets.enemyLaser, new Color(255, 120, 150));
        getHostGame().addObject(bolt);
        RetroSfx.enemyShoot();
    }

    /* ===================== ramming ===================== */

    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (other instanceof PlayerShip && health > 0) {
            PlayerShip p = (PlayerShip) other;
            p.takeHit(18, getPixelLocation());
            takeDamage(maxHealth); // ships shatter on impact
        }
    }
}

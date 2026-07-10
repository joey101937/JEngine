package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Main;
import java.awt.Color;

/**
 * The player's fighter. Flown with WASD using momentum: thrust accelerates the
 * ship and it coasts/drifts when you let go, giving a sense of inertia while the
 * starfield streams past. Fires upward, collects power-ups, and can unleash a
 * stocked Nova Bomb on command.
 *
 * @author Joseph
 */
public class PlayerShip extends GameObject2 {

    // ---- movement tuning ----
    private static final double ACCEL = 0.55;
    private static final double MAX_SPEED = 6.6;
    private static final double FRICTION = 0.87;

    // ---- input intent (set by the input handler) ----
    public volatile boolean up, down, left, right, firing, novaHeld;

    // ---- combat stats ----
    public int maxHealth = 100;
    public int health = maxHealth;
    public int lives = 3;

    private int fireCooldown = 0;
    private static final int BASE_FIRE_INTERVAL = 11;
    private static final int RAPID_FIRE_INTERVAL = 5;

    // ---- power-up state ----
    private int rapidTicks = 0;
    private int spreadTicks = 0;
    private int shieldTicks = 0;
    private int novaCharges = 0;
    public static final int MAX_NOVA = 3;

    // ---- feedback timers ----
    private int invulnTicks = 0;
    private int hitFlashTicks = 0;
    private boolean dead = false;

    public PlayerShip(DCoordinate spawn) {
        super(spawn);
        setGraphic(SIAssets.player);
        isSolid = true;
        preventOverlap = true;
        movementType = MovementType.RawVelocity;
        setZLayer(10);
        setName("Player");
    }

    private int tps() { return SpaceInvadersGame.TPS; }

    /* ===================== tick ===================== */

    @Override
    public void tick() {
        super.tick();

        // ---- momentum-based thrust ----
        double ax = 0, ay = 0;
        if (left)  ax -= ACCEL;
        if (right) ax += ACCEL;
        if (up)    ay -= ACCEL;
        if (down)  ay += ACCEL;
        velocity.x += ax;
        velocity.y += ay;
        if (ax == 0) velocity.x *= FRICTION;
        if (ay == 0) velocity.y *= FRICTION;
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y);
        if (speed > MAX_SPEED) {
            velocity.x = velocity.x / speed * MAX_SPEED;
            velocity.y = velocity.y / speed * MAX_SPEED;
        }
        if (Math.abs(velocity.x) < 0.03) velocity.x = 0;
        if (Math.abs(velocity.y) < 0.03) velocity.y = 0;

        // subtle bank toward travel direction (visual only — hitbox stays upright)
        setRotation(Main.clamp(velocity.x * 3.0, 20, -20));

        // ---- weapons ----
        if (rapidTicks > 0) rapidTicks--;
        if (spreadTicks > 0) spreadTicks--;
        if (shieldTicks > 0) shieldTicks--;
        if (fireCooldown > 0) fireCooldown--;
        if (firing && fireCooldown <= 0) fire();

        if (novaHeld) {
            novaHeld = false; // edge-consume so one press = one blast
            triggerNova();
        }

        // ---- feedback decay ----
        if (invulnTicks > 0) {
            invulnTicks--;
            setRenderOpacity((getGameTickNumber() / 4) % 2 == 0 ? 0.35f : 1f);
        } else {
            setRenderOpacity(1f);
        }
        if (hitFlashTicks > 0) {
            hitFlashTicks--;
            setRenderBrightness(1.0 + hitFlashTicks * 0.25);
            setRenderScale(1.0 + hitFlashTicks * 0.03);
        } else {
            setRenderBrightness(1.0);
            setRenderScale(1.0);
        }
    }

    private void fire() {
        fireCooldown = rapidTicks > 0 ? RAPID_FIRE_INTERVAL : BASE_FIRE_INTERVAL;
        DCoordinate nose = getLocation().add(new DCoordinate(0.0, -getHeight() / 2.0 - 4));
        boolean spread = spreadTicks > 0;
        if (spread) {
            for (double deg : new double[]{-16, 0, 16}) {
                launchLaser(nose, deg, SIAssets.spreadLaser, new Color(255, 230, 120));
            }
            RetroSfx.spreadShoot();
        } else {
            launchLaser(nose, 0, SIAssets.playerLaser, new Color(150, 255, 255));
            RetroSfx.shoot();
        }
    }

    private void launchLaser(DCoordinate nose, double deg, Framework.GraphicalAssets.Sprite sprite, Color c) {
        double rad = Math.toRadians(deg);
        DCoordinate dest = nose.copy().add(new DCoordinate(Math.sin(rad) * 200, -Math.cos(rad) * 200));
        Laser bolt = new Laser(nose.copy(), dest, true, 25, sprite, c);
        getHostGame().addObject(bolt);
    }

    /* ===================== power-ups ===================== */

    public void grantPowerUp(PowerUp.Type type) {
        switch (type) {
            case RAPID:  rapidTicks = 9 * tps(); break;
            case SPREAD: spreadTicks = 11 * tps(); break;
            case SHIELD: shieldTicks = 8 * tps(); break;
            case NOVA:
                novaCharges = Math.min(MAX_NOVA, novaCharges + 1);
                RetroSfx.novaReady();
                break;
        }
        SpaceInvadersGame.ui.flashMessage(type.label + "!", type.color, tps());
    }

    private void triggerNova() {
        if (novaCharges <= 0) {
            return;
        }
        novaCharges--;
        RetroSfx.nova();
        SpaceInvadersGame.ui.novaFlash();
        SpaceInvadersGame.ui.shake(16);
        Explosion.big(getHostGame(), getPixelLocation(), new Color(255, 140, 230));
        // sweep the field: wipe enemy fire, heavily damage every alien/boss
        for (GameObject2 obj : getHostGame().getAllObjects()) {
            if (obj instanceof Laser && !((Laser) obj).friendly) {
                obj.destroy();
            } else if (obj instanceof Alien) {
                Explosion.small(getHostGame(), obj.getPixelLocation(), new Color(255, 180, 240));
                ((Alien) obj).takeDamage(120);
            }
        }
    }

    /* ===================== damage ===================== */

    public void takeHit(int dmg, Coordinate source) {
        if (dead) return;
        if (shieldTicks > 0) {
            RetroSfx.shieldHit();
            Explosion.spark(getHostGame(), source != null ? source : getPixelLocation(), new Color(120, 255, 180));
            SpaceInvadersGame.ui.shake(4);
            return;
        }
        if (invulnTicks > 0) return;

        health -= dmg;
        hitFlashTicks = 6;
        invulnTicks = tps() / 2;
        RetroSfx.playerHit();
        SpaceInvadersGame.ui.shake(10);
        Explosion.spark(getHostGame(), getPixelLocation(), new Color(255, 120, 120));

        if (health <= 0) {
            loseLife();
        }
    }

    private void loseLife() {
        lives--;
        Explosion.big(getHostGame(), getPixelLocation(), new Color(120, 220, 255));
        if (lives <= 0) {
            dead = true;
            SpaceInvadersGame.driver.onPlayerDefeated();
            destroy();
        } else {
            // respawn at the bottom-center with generous i-frames
            health = maxHealth;
            invulnTicks = 2 * tps();
            velocity = new DCoordinate(0, 0);
            setLocation(getHostGame().getWorldWidth() / 2.0, getHostGame().getWorldHeight() - 90);
        }
    }

    /** A ship rammed us. */
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        // damage is driven by the alien's onCollide so it happens exactly once per pair
    }

    /* ===================== queries for the HUD ===================== */

    public boolean hasShield()   { return shieldTicks > 0; }
    public int shieldTicks()     { return shieldTicks; }
    public boolean rapidActive() { return rapidTicks > 0; }
    public int rapidTicks()      { return rapidTicks; }
    public boolean spreadActive(){ return spreadTicks > 0; }
    public int spreadTicks()     { return spreadTicks; }
    public int novaCharges()     { return novaCharges; }
    public boolean isInvulnerable() { return invulnTicks > 0; }
}

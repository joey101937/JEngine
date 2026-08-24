package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;

/**
 * Spitter — a robed caster that never wants to be next to you. It backs off when crowded,
 * closes when ignored, and lobs an arcing bolt that has to be jumped or dashed rather than
 * simply walked away from. Its job is to punish standing still.
 *
 * The cast has a visible wind-up animation and only fires with line of sight, so terrain is a
 * real answer to it.
 *
 * @author Joseph
 */
public class Spitter extends Enemy {

    private static final double PREFERRED_RANGE = 430, TOO_CLOSE = 300, TOO_FAR = 620;
    private static final int CAST_TICKS = 20, FIRE_TICK = 15, CAST_COOLDOWN = 96;

    private int castTicks = 0;
    private int castCooldown;
    private Sequence idleR, idleL, castR, castL;
    private boolean lastFacing = true;
    private boolean casting = false;

    public Spitter(DCoordinate location) {
        super(location);
        setName("Spitter");
        bodyWidth = 50;
        bodyHeight = 94;
        footOffset = 51;
        setMaxHealth(34);
        setCurrentHealth(34);
        contactDamage = 5;
        scoreValue = 85;
        healthDropChance = 34;
        knockbackResist = 1.0;
        aggroRange = 1000;
        bloodColor = new Color(40, 120, 100);
        deathBurstColor = new Color(120, 255, 200);
        castCooldown = 40 + Framework.Main.generateRandomIntLocally(0, 60);
        idleR = MinotaurAssets.copy(MinotaurAssets.spitterR);
        idleL = MinotaurAssets.copy(MinotaurAssets.spitterL);
        castR = MinotaurAssets.copy(MinotaurAssets.spitterCastR);
        castL = MinotaurAssets.copy(MinotaurAssets.spitterCastL);
        setGraphic(idleR);
    }

    @Override
    protected void enemyTick(PlayerMinotaur player) {
        if (player == null || player.isDead()) {
            vx *= 0.8;
            return;
        }
        double dx = player.getLocation().x - getLocation().x;
        double dist = Math.abs(dx);
        double dir = Math.signum(dx);
        if (dir == 0) dir = 1;
        facingRight = dir > 0;

        if (casting) {
            vx *= 0.7;
            if (castTicks == FIRE_TICK) fireAt(player);
            if (++castTicks >= CAST_TICKS) {
                casting = false;
                castCooldown = CAST_COOLDOWN + Framework.Main.generateRandomIntLocally(0, 40);
                applyAnimation();
            }
            return;
        }

        // hold the ring: retreat when crowded, advance when the player disengages
        double move = 0;
        if (dist < TOO_CLOSE) move = -dir * 3.1;
        else if (dist > TOO_FAR) move = dir * 2.3;
        else move = -dir * 0.3 * Math.signum(PREFERRED_RANGE - dist);
        if (move != 0 && grounded && !groundAheadOf(Math.signum(move))) move = 0;
        vx += (move - vx) * 0.28;

        if (castCooldown > 0) castCooldown--;
        else if (dist < 780 && hasLineOfSight(player)) {
            casting = true;
            castTicks = 0;
            applyAnimation();
        }

        if (facingRight != lastFacing) {
            lastFacing = facingRight;
            applyAnimation();
        }
    }

    private void applyAnimation() {
        Sequence next = casting ? (facingRight ? castR : castL) : (facingRight ? idleR : idleL);
        if (casting) next.restart();
        setGraphic(next);
    }

    private void fireAt(PlayerMinotaur player) {
        DCoordinate origin = core();
        origin.x += facingRight ? 34 : -34;
        DCoordinate target = player.core();

        // solve a lazy arc: fixed flight time, gravity fills in the rest
        double flight = 42;
        double gravity = 0.32;
        double vx0 = (target.x - origin.x) / flight;
        double vy0 = (target.y - origin.y) / flight - 0.5 * gravity * flight;
        vx0 = Math.max(-14, Math.min(14, vx0));
        vy0 = Math.max(-15, Math.min(6, vy0));

        getHostGame().addObject(new Projectile(origin, vx0, vy0, gravity,
                MinotaurAssets.fireball, 46, Team.PLAYER, 9, 10, -8,
                new Color(120, 255, 205), 220));
        MinotaurSfx.fireball();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.embers(origin.x, origin.y, 5, new Color(120, 255, 205));
        }
    }

    @Override
    protected void idleTick() {
        vx = 0;
    }

    @Override
    public int getWidth() { return 78; }

    @Override
    public int getHeight() { return 102; }

}

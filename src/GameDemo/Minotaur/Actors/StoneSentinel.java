package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * Stone Sentinel — the wall the combo is for. While its plating holds, light hits ring off it
 * for a fraction of their damage and the player is nudged toward the slower, riskier heavy
 * swing; once broken it turns faster and angrier, its core burns orange, and it becomes a race.
 *
 * That armour state is the only reason the game has two attack buttons, so it is signposted
 * hard: a different hit sound, a different sprite, an explosion of shards at the moment it
 * gives way.
 *
 * @author Joseph
 */
public class StoneSentinel extends Enemy {

    private static final double WALK_SPEED = 2.0, ENRAGED_SPEED = 3.2;
    private static final int PUNCH_WINDUP = 21, PUNCH_ACTIVE = 6, PUNCH_RECOVER = 30;
    private static final int PUNCH_COOLDOWN = 34;
    private static final double PUNCH_RANGE = 215;
    /** Fraction of a non-heavy hit that gets through the plating. */
    private static final double ARMOR_ABSORB = 0.34;
    private static final int ARMOR_INTEGRITY = 62;

    private boolean armored = true;
    private int armorHealth = ARMOR_INTEGRITY;

    private boolean punching = false;
    private int punchTicks = 0;
    private int punchCooldown = 0;
    private boolean punchLanded = false;

    private Sequence walkR, walkL, punchR, punchL;
    private boolean lastFacing = true;

    public StoneSentinel(DCoordinate location) {
        super(location);
        setName("Stone Sentinel");
        bodyWidth = 86;
        bodyHeight = 116;
        footOffset = 54;
        setMaxHealth(120);
        setCurrentHealth(120);
        contactDamage = 0;
        scoreValue = 180;
        healthDropChance = 62;
        healthDropAmount = 34;
        knockbackResist = 0.28;
        aggroRange = 900;
        bloodColor = new Color(120, 118, 120);
        deathBurstColor = new Color(200, 196, 190);
        rebuildAnimations();
        setGraphic(walkR);
    }

    private void rebuildAnimations() {
        walkR = MinotaurAssets.copy(armored ? MinotaurAssets.golemR : MinotaurAssets.golemBrokenR);
        walkL = MinotaurAssets.copy(armored ? MinotaurAssets.golemL : MinotaurAssets.golemBrokenL);
        punchR = MinotaurAssets.copy(armored ? MinotaurAssets.golemPunchR : MinotaurAssets.golemBrokenPunchR);
        punchL = MinotaurAssets.copy(armored ? MinotaurAssets.golemPunchL : MinotaurAssets.golemBrokenPunchL);
    }

    /* ==================================================================
     *  armour
     * ================================================================== */

    @Override
    public int damage(int amount, double kx, double ky, boolean heavy, SSActor source) {
        if (isDead()) return 0;
        if (armored) {
            armorHealth -= heavy ? amount : Math.max(1, amount / 3);
            if (!heavy) {
                // glancing blows ring off, and say so
                MinotaurSfx.hitArmor();
                if (MinotaurGame.fx != null) {
                    DCoordinate c = core();
                    MinotaurGame.fx.sparks(c.x, c.y, 7, source != null && source.getLocation().x < getLocation().x ? 1 : -1,
                            -0.6, new Color(210, 230, 255), 7);
                }
                amount = Math.max(1, (int) (amount * ARMOR_ABSORB));
            }
            if (armorHealth <= 0) breakArmor();
        }
        return super.damage(amount, kx, ky, heavy, source);
    }

    private void breakArmor() {
        armored = false;
        knockbackResist = 0.5;
        contactDamage = 8;
        rebuildAnimations();
        setGraphic(facingRight ? walkR : walkL);
        MinotaurSfx.hitHeavy();
        DCoordinate c = core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.shards(c.x, c.y, 16, new Color(150, 146, 142), 8);
            MinotaurGame.fx.sparks(c.x, c.y, 18, 0, -1, new Color(255, 160, 80), 11);
            MinotaurGame.fx.text(c.x, c.y - 70, "ARMOUR BROKEN", new Color(255, 170, 80), 26);
        }
        if (MinotaurGame.level != null) {
            MinotaurGame.level.hitStop(8);
            MinotaurGame.level.shake(13, 20);
        }
    }

    /* ==================================================================
     *  behaviour
     * ================================================================== */

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

        if (punching) {
            tickPunch(player, dir);
            return;
        }
        if (punchCooldown > 0) punchCooldown--;

        facingRight = dir > 0;
        double speed = armored ? WALK_SPEED : ENRAGED_SPEED;
        boolean edgeAhead = grounded && !groundAheadOf(dir);
        vx += ((edgeAhead ? 0 : dir * speed) - vx) * 0.2;

        if (dist < PUNCH_RANGE && punchCooldown <= 0
                && Math.abs(player.getLocation().y - getLocation().y) < 170) {
            punching = true;
            punchTicks = 0;
            punchLanded = false;
            setGraphic(facingRight ? punchR : punchL);
            (facingRight ? punchR : punchL).restart();
        } else if (facingRight != lastFacing) {
            lastFacing = facingRight;
            setGraphic(facingRight ? walkR : walkL);
        }
    }

    private void tickPunch(PlayerMinotaur player, double dir) {
        vx *= 0.82;
        if (punchTicks == PUNCH_WINDUP - 6 && MinotaurGame.fx != null) {
            // a bloom on the core is the last warning before the fist lands
            DCoordinate c = core();
            MinotaurGame.fx.embers(c.x, c.y, 4, armored ? new Color(120, 210, 255) : new Color(255, 130, 60));
        }
        if (punchTicks >= PUNCH_WINDUP && punchTicks < PUNCH_WINDUP + PUNCH_ACTIVE && !punchLanded) {
            Rectangle box = attackBox(PUNCH_RANGE, bodyHeight * 1.1, 10);
            if (player.bodyRect().intersects(box)) {
                punchLanded = true;
                double kdir = player.getLocation().x >= getLocation().x ? 1 : -1;
                player.damage(armored ? 14 : 18, kdir * 14, -8, true, this);
            }
            if (punchTicks == PUNCH_WINDUP) {
                MinotaurSfx.slam();
                DCoordinate f = feet();
                if (MinotaurGame.fx != null) {
                    MinotaurGame.fx.dust(f.x + (facingRight ? 70 : -70), f.y, 9, dir,
                            new Color(200, 196, 186));
                }
                if (MinotaurGame.level != null) MinotaurGame.level.shake(7, 12);
            }
        }
        if (++punchTicks >= PUNCH_WINDUP + PUNCH_ACTIVE + PUNCH_RECOVER) {
            punching = false;
            punchCooldown = PUNCH_COOLDOWN;
            setGraphic(facingRight ? walkR : walkL);
        }
    }

    @Override
    protected void onDeathEffects() {
        DCoordinate c = core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.shards(c.x, c.y, 22, new Color(140, 136, 132), 9);
            MinotaurGame.fx.ring(c.x, c.y, 18, 8, 22, new Color(255, 150, 70), 6);
        }
        if (MinotaurGame.level != null) MinotaurGame.level.shake(12, 18);
    }

    @Override
    protected void idleTick() {
        vx = 0;
    }

    @Override
    public int getWidth() { return 228; }

    @Override
    public int getHeight() { return 144; }

}

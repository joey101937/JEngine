package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurArt;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * The Warlord — the thing at the centre of the labyrinth, and the same minotaur the player is,
 * three times the size and burnt black.
 *
 * The fight is a rotation of four telegraphed actions chosen by range, with the roster growing
 * as its health drops:
 * <ul>
 *   <li><b>Cleave</b> at close range — two or three swings, punished by dashing behind it.</li>
 *   <li><b>Charge</b> from far — fast, unsteerable, and it stuns itself on the arena wall.
 *       That stun is the fight's main damage window.</li>
 *   <li><b>Slam</b> at mid range — leaps and lands, sending shockwaves along the floor that
 *       have to be jumped.</li>
 *   <li><b>Summon</b> from phase two, <b>Volley</b> from phase three.</li>
 * </ul>
 * Every action has a wind-up during which the Warlord glows, so the fight is a reading test
 * rather than a reflex test.
 *
 * @author Joseph
 */
public class BossWarlord extends Enemy {

    /** Health fractions at which the Warlord gains a new action. Also drawn on the HUD bar. */
    public static final double[] PHASE_THRESHOLDS = { 0.34, 0.67 };

    private static final double SCALE = 2.55;
    private static final double WALK_SPEED = 3.4;

    private enum Action { IDLE, CLEAVE, CHARGE, SLAM, SUMMON, VOLLEY, STUNNED, INTRO, DYING }

    private Action action = Action.IDLE;
    private int actionTick = 0;
    private int actionCooldown = 70;
    private int cleavesLeft = 0;
    private boolean strikeLanded = false;
    private int phase = 1;
    private int auraTimer = 0;

    private final MinotaurAssets.ActorSet art = MinotaurAssets.warlord;
    private String currentAnim = "";

    public BossWarlord(DCoordinate location) {
        super(location);
        setName("The Warlord");
        setScale(SCALE);
        bodyWidth = 148;
        bodyHeight = 296;
        footOffset = (int) (MinotaurAssets.HERO_FOOT_OFFSET * SCALE);
        setMaxHealth(1000);
        setCurrentHealth(1000);
        contactDamage = 0;
        scoreValue = 4000;
        healthDropChance = 0;
        knockbackResist = 0.06;
        aggroRange = 1000;
        maxFallSpeed = 30;
        separates = false;
        bloodColor = new Color(120, 20, 26);
        deathBurstColor = new Color(255, 120, 70);
        setZLayer(18);
        setAnimation("idle");
    }

    public int getPhase() { return phase; }

    @Override
    public boolean countsTowardClear() { return true; }

    /* ==================================================================
     *  entrance
     * ================================================================== */

    @Override
    protected void onWake() {
        action = Action.INTRO;
        actionTick = 0;
        MinotaurSfx.bossRoar();
        if (MinotaurGame.hud != null) {
            MinotaurGame.hud.flashBanner("THE WARLORD", "It has been waiting for you",
                    new Color(255, 90, 80), 200);
        }
        if (MinotaurGame.level != null) MinotaurGame.level.shake(26, 60);
        if (MinotaurGame.fx != null) {
            DCoordinate c = core();
            MinotaurGame.fx.ring(c.x, c.y, 60, 22, 40, new Color(255, 110, 70), 10);
            MinotaurGame.fx.embers(c.x, c.y, 40, new Color(255, 140, 60));
        }
    }

    /* ==================================================================
     *  behaviour
     * ================================================================== */

    @Override
    protected void enemyTick(PlayerMinotaur player) {
        updatePhase();
        tickAura();

        if (action == Action.DYING) {
            vx *= 0.9;
            if (++actionTick > 150) {
                MinotaurGame.onBossDefeated();
                destroy();
            }
            return;
        }
        if (action == Action.INTRO) {
            vx = 0;
            if (++actionTick > 90) action = Action.IDLE;
            return;
        }
        if (player == null || player.isDead()) {
            vx *= 0.85;
            setAnimation("idle");
            return;
        }

        double dx = player.getLocation().x - getLocation().x;
        double dist = Math.abs(dx);
        double dir = Math.signum(dx);
        if (dir == 0) dir = 1;

        switch (action) {
            case CLEAVE:  tickCleave(player, dir); return;
            case CHARGE:  tickCharge(player, dir); return;
            case SLAM:    tickSlam(player, dir);   return;
            case SUMMON:  tickSummon();            return;
            case VOLLEY:  tickVolley(player);      return;
            case STUNNED: tickStunned();           return;
            default: break;
        }

        facingRight = dir > 0;
        if (actionCooldown > 0) {
            actionCooldown--;
            // close distance between actions rather than standing there
            double approach = dist > 240 ? dir * WALK_SPEED : (dist < 150 ? -dir * 1.6 : 0);
            if (grounded && approach != 0 && !groundAheadOf(Math.signum(approach))) approach = 0;
            vx += (approach - vx) * 0.18;
            setAnimation(Math.abs(vx) > 0.6 ? "run" : "idle");
            return;
        }
        chooseAction(dist);
    }

    private void updatePhase() {
        double frac = getCurrentHealth() / (double) getMaxHealth();
        int want = frac > PHASE_THRESHOLDS[1] ? 1 : (frac > PHASE_THRESHOLDS[0] ? 2 : 3);
        if (want > phase) {
            phase = want;
            MinotaurSfx.bossRoar();
            grantInvulnerability(24);
            if (MinotaurGame.hud != null) {
                MinotaurGame.hud.flashBanner("PHASE " + phase, phase == 2
                        ? "It calls for help" : "It stops holding back",
                        new Color(255, 140, 90), 130);
            }
            if (MinotaurGame.level != null) MinotaurGame.level.shake(20, 40);
            if (MinotaurGame.fx != null) {
                DCoordinate c = core();
                MinotaurGame.fx.ring(c.x, c.y, 50, 20, 32, new Color(255, 120, 70), 9);
                MinotaurGame.fx.embers(c.x, c.y, 30, new Color(255, 150, 70));
            }
        }
    }

    private void tickAura() {
        if (--auraTimer > 0 || MinotaurGame.fx == null) return;
        auraTimer = phase >= 3 ? 2 : 5;
        DCoordinate f = feet();
        MinotaurGame.fx.embers(f.x + Framework.Main.generateRandomDoubleLocally(-70, 70),
                f.y - 30, 1, new Color(255, 120 + phase * 20, 60));
    }

    private void chooseAction(double dist) {
        int roll = Framework.Main.generateRandomIntLocally(0, 99);
        if (dist < 320) {
            action = (roll < 68) ? Action.CLEAVE : Action.SLAM;
        } else if (dist > 720) {
            if (phase >= 3 && roll < 34) action = Action.VOLLEY;
            else if (phase >= 2 && roll < 52) action = Action.SUMMON;
            else action = Action.CHARGE;
        } else {
            if (phase >= 3 && roll < 26) action = Action.VOLLEY;
            else if (roll < 52) action = Action.SLAM;
            else action = Action.CHARGE;
        }
        actionTick = 0;
        strikeLanded = false;
        cleavesLeft = phase >= 2 ? 3 : 2;
        if (action == Action.CLEAVE) MinotaurSfx.swingHeavy();
    }

    /* ------------------------------ cleave ------------------------------ */

    private static final int CLEAVE_WINDUP = 20, CLEAVE_ACTIVE = 6, CLEAVE_RECOVER = 22;
    private static final double CLEAVE_RANGE = 330;

    private void tickCleave(PlayerMinotaur player, double dir) {
        if (actionTick == 0) {
            facingRight = dir > 0;
            setAnimation("swing", true);
        }
        vx *= 0.84;
        if (actionTick == CLEAVE_WINDUP) {
            MinotaurSfx.swingHeavy();
            vx = (facingRight ? 1 : -1) * 7;
            // the swing sprite draws the arc itself; embers just mark how far the reach goes
            if (MinotaurGame.fx != null) {
                double swingDir = facingRight ? 1 : -1;
                DCoordinate l = getLocation();
                MinotaurGame.fx.sparks(l.x + swingDir * CLEAVE_RANGE * 0.4,
                        l.y + footOffset - bodyHeight * 0.5, 18, swingDir, -0.2,
                        new Color(255, 170, 110), 13);
            }
        }
        if (actionTick >= CLEAVE_WINDUP && actionTick < CLEAVE_WINDUP + CLEAVE_ACTIVE && !strikeLanded) {
            Rectangle box = attackBox(CLEAVE_RANGE, bodyHeight * 0.85, 30);
            if (player.bodyRect().intersects(box)) {
                strikeLanded = true;
                double kdir = player.getLocation().x >= getLocation().x ? 1 : -1;
                player.damage(20, kdir * 16, -9, true, this);
            }
        }
        if (++actionTick >= CLEAVE_WINDUP + CLEAVE_ACTIVE + CLEAVE_RECOVER) {
            if (--cleavesLeft > 0) {
                actionTick = 0;
                strikeLanded = false;
                setAnimation("swing", true);
            } else {
                endAction(phase >= 3 ? 40 : 66);
            }
        }
    }

    /* ------------------------------ charge ------------------------------ */

    private static final int CHARGE_WINDUP = 30, CHARGE_MAX = 90;

    private void tickCharge(PlayerMinotaur player, double dir) {
        if (actionTick == 0) {
            facingRight = dir > 0;
            setAnimation("idle");
            if (MinotaurGame.fx != null) {
                DCoordinate f = feet();
                MinotaurGame.fx.dust(f.x, f.y, 10, -dir, new Color(210, 180, 150));
            }
        }
        if (actionTick < CHARGE_WINDUP) {
            // paws the ground before it commits — the clearest tell in the fight
            vx *= 0.8;
            if (actionTick % 6 == 0 && MinotaurGame.fx != null) {
                DCoordinate f = feet();
                MinotaurGame.fx.dust(f.x + (facingRight ? 60 : -60), f.y, 3, facingRight ? -1 : 1,
                        new Color(220, 190, 160));
            }
            if (actionTick == CHARGE_WINDUP - 1) {
                MinotaurSfx.bossRoar();
                setAnimation("run");
            }
        } else {
            vx = (facingRight ? 1 : -1) * 17.5;
            if (actionTick % 3 == 0 && MinotaurGame.fx != null) {
                DCoordinate f = feet();
                MinotaurGame.fx.dust(f.x, f.y, 3, facingRight ? -1 : 1, new Color(210, 185, 155));
            }
            if (!strikeLanded && player.bodyRect().intersects(bodyRect())) {
                strikeLanded = true;
                double kdir = player.getLocation().x >= getLocation().x ? 1 : -1;
                player.damage(24, kdir * 19, -10, true, this);
            }
        }
        if (++actionTick >= CHARGE_WINDUP + CHARGE_MAX) endAction(50);
    }

    @Override
    protected void onWallContact() {
        if (action == Action.CHARGE && actionTick > CHARGE_WINDUP) {
            action = Action.STUNNED;
            actionTick = 0;
            MinotaurSfx.slam();
            DCoordinate c = core();
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.shards(c.x, c.y, 16, new Color(160, 150, 140), 9);
                MinotaurGame.fx.ring(c.x, c.y, 30, 12, 22, new Color(255, 200, 150), 7);
            }
            if (MinotaurGame.level != null) {
                MinotaurGame.level.hitStop(9);
                MinotaurGame.level.shake(24, 34);
            }
        }
    }

    private void tickStunned() {
        vx *= 0.8;
        setAnimation("idle");
        if (actionTick % 8 == 0 && MinotaurGame.fx != null) {
            DCoordinate c = core();
            MinotaurGame.fx.embers(c.x, c.y - 90, 2, new Color(255, 230, 140));
        }
        if (++actionTick > 96) endAction(24);
    }

    /* ------------------------------ slam ------------------------------ */

    private static final int SLAM_WINDUP = 24;

    private void tickSlam(PlayerMinotaur player, double dir) {
        if (actionTick == 0) {
            facingRight = dir > 0;
            setAnimation("idle");
        }
        if (actionTick == SLAM_WINDUP) {
            vy = -21;
            vx = Math.max(-11, Math.min(11, (player.getLocation().x - getLocation().x) / 20.0));
            setAnimation("run");
        }
        if (actionTick < SLAM_WINDUP) {
            vx *= 0.85;
        } else if (grounded && actionTick > SLAM_WINDUP + 8) {
            landSlam();
            endAction(56);
            return;
        }
        if (++actionTick > SLAM_WINDUP + 120) endAction(50);
    }

    private void landSlam() {
        MinotaurSfx.slam();
        DCoordinate f = feet();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.dust(f.x, f.y, 20, 0, new Color(220, 195, 165));
            MinotaurGame.fx.ring(f.x, f.y, 30, 20, 26, new Color(255, 180, 110), 10);
            MinotaurGame.fx.shards(f.x, f.y, 12, new Color(150, 130, 110), 8);
        }
        if (MinotaurGame.level != null) {
            MinotaurGame.level.hitStop(7);
            MinotaurGame.level.shake(26, 36);
        }
        // two waves along the floor: the attack that forces a jump
        for (int side = -1; side <= 1; side += 2) {
            Projectile wave = new Projectile(new DCoordinate(f.x + side * 60, f.y - 46),
                    side * 11.0, 0, 0, MinotaurAssets.shockRingSmall, 200,
                    Team.PLAYER, 16, 20, -12, new Color(255, 180, 110), 130);
            wave.ignoreTerrain = true;
            getHostGame().addObject(wave);
        }
    }

    /* ------------------------------ summon ------------------------------ */

    private void tickSummon() {
        if (actionTick == 0) setAnimation("swing", true);
        vx *= 0.8;
        if (actionTick == 22) {
            MinotaurSfx.roar();
            DCoordinate c = core();
            int count = phase >= 3 ? 3 : 2;
            for (int i = 0; i < count; i++) {
                double ox = Framework.Main.generateRandomDoubleLocally(-260, 260);
                Imp imp = new Imp(new DCoordinate(c.x + ox, c.y - 120));
                imp.wake();
                getHostGame().addObject(imp);
                if (MinotaurGame.fx != null) {
                    MinotaurGame.fx.embers(c.x + ox, c.y - 120, 10, new Color(255, 110, 90));
                }
            }
            if (MinotaurGame.level != null) MinotaurGame.level.shake(9, 16);
        }
        if (++actionTick > 60) endAction(80);
    }

    /* ------------------------------ volley ------------------------------ */

    private void tickVolley(PlayerMinotaur player) {
        if (actionTick == 0) setAnimation("swing", true);
        vx *= 0.85;
        if (actionTick >= 20 && actionTick <= 40 && (actionTick - 20) % 10 == 0) {
            DCoordinate origin = core();
            DCoordinate target = player.core();
            double dx = target.x - origin.x, dy = target.y - origin.y;
            double d = Math.max(1, Math.hypot(dx, dy));
            double spread = ((actionTick - 30) / 10.0) * 0.22;
            double cos = Math.cos(spread), sin = Math.sin(spread);
            double ux = (dx / d) * cos - (dy / d) * sin;
            double uy = (dx / d) * sin + (dy / d) * cos;
            getHostGame().addObject(new Projectile(origin, ux * 12.5, uy * 12.5, 0.06,
                    MinotaurAssets.bossOrb, 74, Team.PLAYER, 13, 14, -9,
                    new Color(255, 140, 90), 200));
            MinotaurSfx.fireball();
        }
        if (++actionTick > 62) endAction(70);
    }

    /* ------------------------------ shared ------------------------------ */

    private void endAction(int cooldown) {
        action = Action.IDLE;
        actionTick = 0;
        actionCooldown = Math.max(10, cooldown - (phase - 1) * 14);
        strikeLanded = false;
        setAnimation("idle");
    }

    @Override
    protected void onDamaged(int amount, boolean heavy, SSActor source) {
        super.onDamaged(amount, heavy, source);
        // a charging Warlord cannot be staggered, which is what makes the charge scary
    }

    @Override
    protected void onKilled() {
        MinotaurGame.addScore(scoreValue);
        action = Action.DYING;
        actionTick = 0;
        contactDamage = 0;
        setAnimation("death");
        MinotaurSfx.bossRoar();
        DCoordinate c = core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.blood(c.x, c.y, 40, 0, bloodColor);
            MinotaurGame.fx.embers(c.x, c.y, 50, new Color(255, 150, 80));
            MinotaurGame.fx.ring(c.x, c.y, 60, 16, 50, new Color(255, 190, 120), 12);
        }
        if (MinotaurGame.level != null) {
            MinotaurGame.level.hitStop(14);
            MinotaurGame.level.shake(32, 70);
        }
    }

    private void setAnimation(String name) {
        setAnimation(name, false);
    }

    /** {@code force} replays a non-looping sequence that is already current — see the player. */
    private void setAnimation(String name, boolean force) {
        String key = name + (facingRight ? "R" : "L");
        if (key.equals(currentAnim)) {
            if (force && getGraphic() instanceof Sequence s) s.restart();
            return;
        }
        currentAnim = key;
        Sequence next;
        switch (name) {
            case "run":   next = art.run(facingRight); break;
            case "swing": next = art.swing(facingRight); break;
            case "death": next = art.death(facingRight); break;
            default:      next = art.idle(facingRight); break;
        }
        next.restart();
        setGraphic(next);
    }

    @Override
    protected void idleTick() {
        vx = 0;
    }

    @Override
    public int getWidth() { return (int) (MinotaurArt.SHEET_W * MinotaurArt.U * getScale()); }

    @Override
    public int getHeight() { return (int) (MinotaurArt.SHEET_H * MinotaurArt.U * getScale()); }

    @Override
    public void updateHitbox() {
        // combat uses the body box
    }
}

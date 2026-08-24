package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurArt;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * The player.
 *
 * <h3>Feel</h3>
 * Everything here exists to close the gap between pressing a key and seeing the result:
 * <ul>
 *   <li><b>Coyote time and jump buffering</b> — a jump pressed slightly before landing, or
 *       slightly after walking off a ledge, still fires. Nearly every "the jump didn't
 *       register" complaint is one of these two windows being missing.</li>
 *   <li><b>Variable jump height</b> — releasing the key early cuts upward speed, so the same
 *       button covers a hop and a full leap.</li>
 *   <li><b>Buffered attacks</b> — an attack pressed during the tail of the previous swing is
 *       queued rather than dropped, which is what makes the three-hit chain feel like a combo
 *       instead of a timing test.</li>
 *   <li><b>Hit-stop</b> — the world freezes for a few frames on contact. It costs nothing and
 *       does more for the weight of a hit than any amount of particles.</li>
 *   <li><b>Dash i-frames</b> — the dash is an answer to pressure, not just movement.</li>
 * </ul>
 *
 * Input arrives asynchronously from the AWT thread, so presses are recorded as flags and
 * consumed once per tick; the simulation never reads a key mid-frame.
 *
 * @author Joseph
 */
public class PlayerMinotaur extends SSActor {

    /* ---------------- tuning ---------------- */

    private static final double MAX_RUN = 7.7;
    private static final double ACCEL_GROUND = 1.55, ACCEL_AIR = 0.82;
    private static final double FRICTION_GROUND = 1.9, FRICTION_AIR = 0.10;
    private static final double JUMP_VELOCITY = -17.6;
    private static final double JUMP_CUT = 0.42;
    private static final int COYOTE_TICKS = 8;
    private static final int JUMP_BUFFER_TICKS = 8;

    private static final double DASH_SPEED = 21.5;
    private static final int DASH_TICKS = 11, DASH_COOLDOWN = 32, DASH_IFRAMES = 13;

    public static final int RAGE_MAX = 100;
    private static final int ROAR_TICKS = 30, ROAR_STRIKE_TICK = 9;
    private static final double ROAR_RADIUS = 330;

    private static final int COMBO_CHAIN_TICKS = 20;

    /**
     * Windup / active / recovery in ticks, plus reach, damage, knockback and mobility per step.
     *
     * At 60 ticks per second a three-tick wind-up puts the blade live 50ms after the click, which
     * is what makes the attack feel immediate. The <i>cadence</i> is set separately, by how deep
     * into recovery the next link may be chained: extra clicks inside that recovery buy nothing,
     * so mashing and clean rhythm produce exactly the same output. The third swing is a
     * committed finisher that cannot be chained out of at all, which is the beat the string
     * rests on before it can start again.
     *
     * <h3>Why light beats heavy on paper</h3>
     * The whole string is 70 damage in 46 ticks against the heavy's 40 in 36, so light is the
     * damage-per-second option and heavy is the burst: longer reach, far more knockback, armour
     * breaking, and a lunge that closes the gap. Light also out-reaches every enemy's contact
     * box, because "I have to walk into the thing to poke it" is what made it feel pointless.
     *
     * <h3>Mobility</h3>
     * The last column is the share of full run speed the character may still steer at during the
     * swing. Light swings run at full tilt; only the finisher and the heavy plant the feet, and
     * even they let go of the reins once the blade has passed.
     */
    private static final int[][] LIGHT = {
        // windup, active, recover, reach, damage, knockX, knockY, mobility%
        {  3, 4,  7, 148, 16,  9,  0, 100 },
        {  3, 4,  7, 152, 20, 10,  0, 100 },
        {  5, 5, 14, 172, 34, 16, -4,  78 },
    };
    private static final int[] HEAVY = { 10, 6, 20, 190, 40, 18, -6, 58 };

    /** Ticks into recovery before the next link is accepted. This is the combo's metronome. */
    private static final int CHAIN_OPENS_AFTER = 3;
    /** A queued attack is forgotten after this long, so an old click cannot fire much later. */
    private static final int ATTACK_BUFFER_LIFETIME = 14;

    /* ---------------- state ---------------- */

    private enum State { IDLE, RUN, AIR, ATTACK, DASH, ROAR, HURT, DEAD }

    private State state = State.IDLE;

    // input, written by the AWT thread and consumed once per tick
    public volatile int moveInput = 0;
    public volatile boolean crouchInput = false;
    private volatile boolean jumpPressed = false;
    private volatile boolean jumpHeld = false;
    private volatile boolean lightPressed = false;
    private volatile boolean heavyPressed = false;
    private volatile boolean dashPressed = false;
    private volatile boolean roarPressed = false;
    /** Set by a mouse attack to swing toward the cursor; null leaves facing to movement. */
    public volatile Boolean facingOverride = null;

    private int jumpBuffer = 0;
    private boolean airJumpAvailable = true;
    private boolean jumpCutUsed = false;

    private int attackTick = 0;
    private int[] attackDef = null;
    private boolean attackIsHeavy = false;
    private boolean attackBuffered = false;
    private boolean attackBufferedHeavy = false;
    private int attackBufferTicks = 0;
    private final List<SSActor> hitThisSwing = new ArrayList<>();

    private int comboStep = 0;
    private int comboChain = 0;
    private int comboHits = 0;
    private int comboDisplay = 0;

    private int dashTicks = 0, dashCooldown = 0, dashDir = 1;
    private int roarTick = 0;
    private int hurtTicks = 0;
    private int deathTicks = 0;
    private int dropThroughTicks = 0;
    private int rage = 0;
    private boolean rageWasReady = false;
    private int auraTimer = 0;

    private DCoordinate lastSafeGround;

    private final MinotaurAssets.ActorSet art = MinotaurAssets.hero;
    private Sequence currentSequence;
    private String currentAnim = "";

    public PlayerMinotaur(DCoordinate location) {
        super(location);
        setName("Minotaur");
        team = Team.PLAYER;
        bodyWidth = 58;
        bodyHeight = 118;
        footOffset = MinotaurAssets.HERO_FOOT_OFFSET;
        setMaxHealth(180);
        setCurrentHealth(180);
        maxFallSpeed = 27;
        setZLayer(20);
        lastSafeGround = location.copy();
        setAnimation("idle");
    }

    /* ==================================================================
     *  input surface
     * ================================================================== */

    public void pressJump() { jumpPressed = true; jumpHeld = true; }
    public void releaseJump() { jumpHeld = false; }
    public void pressLight() { lightPressed = true; }
    public void pressHeavy() { heavyPressed = true; }
    public void pressDash() { dashPressed = true; }
    public void pressRoar() { roarPressed = true; }

    /** Wipes held input — used when the level changes so a key held during the swap does not stick. */
    public void clearInput() {
        moveInput = 0;
        crouchInput = false;
        jumpHeld = false;
        jumpPressed = lightPressed = heavyPressed = dashPressed = roarPressed = false;
        facingOverride = null;
    }

    /* ==================================================================
     *  HUD surface
     * ================================================================== */

    public int getRage() { return rage; }
    public boolean isRageReady() { return rage >= RAGE_MAX; }
    public int getComboHits() { return comboHits; }
    public int getComboDisplayTicks() { return comboDisplay; }

    public void addRage(int amount) {
        if (dead) return;
        rage = Math.min(RAGE_MAX, rage + amount);
        if (isRageReady() && !rageWasReady) {
            rageWasReady = true;
            MinotaurSfx.rageReady();
            if (MinotaurGame.hud != null) MinotaurGame.hud.onRageReady();
        }
    }

    /* ==================================================================
     *  main tick
     * ================================================================== */

    @Override
    protected void actorTick() {
        consumeInputFlags();

        if (comboDisplay > 0 && --comboDisplay == 0) comboHits = 0;
        if (comboChain > 0 && --comboChain == 0) comboStep = 0;
        if (dashCooldown > 0) dashCooldown--;
        if (dropThroughTicks > 0 && --dropThroughTicks == 0) dropThrough = false;
        if (jumpBuffer > 0) jumpBuffer--;

        if (state == State.DEAD) {
            tickDead();
            return;
        }
        if (grounded && !overHazard()) {
            lastSafeGround = getLocation().copy();
        }
        tickRageAura();

        switch (state) {
            case ATTACK: tickAttack(); break;
            case DASH:   tickDash();   break;
            case ROAR:   tickRoar();   break;
            case HURT:   tickHurt();   break;
            default:     tickFree();   break;
        }
        updateAnimation();
    }

    private void consumeInputFlags() {
        if (jumpPressed) {
            jumpPressed = false;
            jumpBuffer = JUMP_BUFFER_TICKS;
        }
        // a queued attack ages out, so a click made well before the chain window does not
        // surface as a phantom swing long after the player stopped asking for one
        if (attackBuffered && --attackBufferTicks <= 0) attackBuffered = false;
        if (lightPressed) {
            lightPressed = false;
            attackBuffered = true;
            attackBufferedHeavy = false;
            attackBufferTicks = ATTACK_BUFFER_LIFETIME;
        }
        if (heavyPressed) {
            heavyPressed = false;
            attackBuffered = true;
            attackBufferedHeavy = true;
            attackBufferTicks = ATTACK_BUFFER_LIFETIME;
        }
    }

    /* ------------------------------ free movement ------------------------------ */

    private void tickFree() {
        if (roarPressed) {
            roarPressed = false;
            if (isRageReady()) { startRoar(); return; }
        }
        if (dashPressed) {
            dashPressed = false;
            if (dashCooldown <= 0) { startDash(); return; }
        }
        if (attackBuffered) {
            attackBuffered = false;
            startAttack(attackBufferedHeavy);
            return;
        }

        applyHorizontalInput(ACCEL_GROUND, ACCEL_AIR, FRICTION_GROUND, FRICTION_AIR);
        handleJump();

        state = grounded ? (Math.abs(vx) > 0.4 ? State.RUN : State.IDLE) : State.AIR;
        if (moveInput != 0) facingRight = moveInput > 0;
    }

    private void applyHorizontalInput(double accelGround, double accelAir,
                                      double frictionGround, double frictionAir) {
        applyHorizontalInput(accelGround, accelAir, frictionGround, frictionAir, 1.0);
    }

    /**
     * @param speedShare fraction of {@link #MAX_RUN} this state may steer up to. Speed already
     *                   above that — carried out of a dash or a heavy lunge — is bled off gently
     *                   rather than clamped, so a swing never yanks the character to a stop.
     */
    private void applyHorizontalInput(double accelGround, double accelAir,
                                      double frictionGround, double frictionAir,
                                      double speedShare) {
        double accel = grounded ? accelGround : accelAir;
        double friction = grounded ? frictionGround : frictionAir;
        if (moveInput != 0) {
            double target = moveInput * MAX_RUN * speedShare;
            if (Math.abs(vx) > Math.abs(target) && Math.signum(vx) == moveInput) {
                vx *= 0.94;
                return;
            }
            // turning is given extra bite so a reversal is immediate rather than a slide
            double rate = (Math.signum(vx) != 0 && Math.signum(vx) != moveInput) ? accel * 2.1 : accel;
            if (vx < target) vx = Math.min(target, vx + rate);
            else if (vx > target) vx = Math.max(target, vx - rate);
        } else {
            if (vx > 0) vx = Math.max(0, vx - friction);
            else if (vx < 0) vx = Math.min(0, vx + friction);
        }
    }

    private void handleJump() {
        boolean canGroundJump = grounded || ticksSinceGrounded <= COYOTE_TICKS;
        if (jumpBuffer > 0) {
            if (crouchInput && grounded && standingOnLedgeOnly()) {
                jumpBuffer = 0;
                dropThrough = true;
                dropThroughTicks = 14;
                vy = 2;
            } else if (canGroundJump) {
                jumpBuffer = 0;
                doJump(JUMP_VELOCITY, false);
            } else if (airJumpAvailable) {
                jumpBuffer = 0;
                airJumpAvailable = false;
                doJump(JUMP_VELOCITY * 0.92, true);
            }
        }
        // releasing early trims the arc; only once per jump so it cannot be spammed mid-fall
        if (!jumpHeld && vy < 0 && !jumpCutUsed) {
            jumpCutUsed = true;
            vy *= JUMP_CUT;
        }
        if (grounded) {
            airJumpAvailable = true;
            jumpCutUsed = false;
        }
    }

    private void doJump(double velocity, boolean isAirJump) {
        vy = velocity;
        grounded = false;
        ticksSinceGrounded = COYOTE_TICKS + 1;
        jumpCutUsed = false;
        DCoordinate f = feet();
        if (isAirJump) {
            MinotaurSfx.doubleJump();
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.ring(f.x, f.y - 10, 12, 5.5, 16, new Color(255, 226, 170), 4);
                MinotaurGame.fx.dust(f.x, f.y, 7, 0, new Color(220, 205, 180));
            }
        } else {
            MinotaurSfx.jump();
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.dust(f.x, f.y, 6, -Math.signum(vx), new Color(210, 195, 170));
            }
        }
    }

    private boolean standingOnLedgeOnly() {
        if (MinotaurGame.level == null) return false;
        DCoordinate l = getLocation();
        int px = (int) l.x, py = (int) (l.y + footOffset + 6);
        for (Rectangle r : MinotaurGame.level.solids) if (r.contains(px, py)) return false;
        for (Rectangle r : MinotaurGame.level.oneWays) if (r.contains(px, py)) return true;
        return false;
    }

    /* ------------------------------ attacks ------------------------------ */

    private void startAttack(boolean heavy) {
        attackIsHeavy = heavy;
        attackDef = heavy ? HEAVY : LIGHT[Math.min(comboStep, LIGHT.length - 1)];
        attackTick = 0;
        hitThisSwing.clear();
        state = State.ATTACK;
        Boolean override = facingOverride;
        facingOverride = null;
        if (override != null) facingRight = override;
        else if (moveInput != 0) facingRight = moveInput > 0;
        // A swing costs no momentum: the run carries straight into it and the attack table's
        // mobility column decides how hard the character may still steer while the blade is out.
        setAnimation("swing", true);
        // Stretch the four swing frames across this particular attack. The swing sequence does
        // not loop, so a fixed frame rate leaves the graphic parked on the follow-through frame
        // for whatever is left of a long recovery — most visible on the finisher and the heavy,
        // which are exactly the attacks that should look like they are still moving.
        stretchSwingOver(attackDef[0] + attackDef[1] + attackDef[2]);
        if (heavy) MinotaurSfx.swingHeavy(); else MinotaurSfx.swingLight();
    }

    private void tickAttack() {
        int windup = attackDef[0], active = attackDef[1], recover = attackDef[2];
        int reach = attackDef[3], damageAmount = attackDef[4];
        double knockX = attackDef[5], knockY = attackDef[6];
        double mobility = attackDef[7] / 100.0;

        // The heavy is the one attack that commits: its lunge owns the character until the blade
        // has passed, and steering comes back for the recovery. Light swings steer throughout.
        boolean committed = attackIsHeavy && attackTick < windup + active;
        if (committed) {
            vx *= grounded ? 0.97 : 0.995;
        } else {
            applyHorizontalInput(ACCEL_GROUND * 0.85, ACCEL_AIR * 0.9,
                    FRICTION_GROUND * 0.6, FRICTION_AIR, mobility);
        }

        if (attackTick == windup) {
            spawnSwingFlourish(reach);
            if (attackIsHeavy) {
                // the heavy step lunges, which both looks committed and closes distance
                vx = (facingRight ? 1 : -1) * 8.5;
            }
        }
        if (attackTick >= windup && attackTick < windup + active) {
            resolveHits(reach, damageAmount, knockX, knockY);
        }
        attackTick++;

        // Once the blade has passed, a jump or a dash may cancel out of the recovery, so swinging
        // at something that is closing on you never costs the escape.
        if (attackTick >= windup + active) {
            if (dashPressed && dashCooldown <= 0) {
                dashPressed = false;
                attackDef = null;
                startDash();
                return;
            }
            if (jumpBuffer > 0 && (grounded || ticksSinceGrounded <= COYOTE_TICKS || airJumpAvailable)) {
                finishAttack();
                handleJump();
                return;
            }
        }

        if (attackTick >= windup + active + recover) {
            finishAttack();
        } else if (canChainFromCurrentAttack()
                && attackTick >= windup + active + CHAIN_OPENS_AFTER) {
            if (attackBuffered) {
                attackBuffered = false;
                boolean heavyNext = attackBufferedHeavy;
                advanceComboStep();
                startAttack(heavyNext);
            }
        }
    }

    /** Retimes the current swing so its frames finish exactly as the attack does. */
    private void stretchSwingOver(int attackTicks) {
        if (currentSequence == null || currentSequence.getFrameCount() <= 0) return;
        int msPerTick = 1000 / MinotaurGame.TPS;
        int perFrame = Math.max(28, attackTicks * msPerTick / currentSequence.getFrameCount());
        currentSequence.setFrameDelay(perFrame);
        currentSequence.restart();
    }

    /** The finisher and the heavy swing end the string; everything else links forward. */
    private boolean canChainFromCurrentAttack() {
        return !attackIsHeavy && comboStep < LIGHT.length - 1;
    }

    private void advanceComboStep() {
        if (attackIsHeavy) comboStep = 0;
        else comboStep = (comboStep + 1) % LIGHT.length;
        comboChain = COMBO_CHAIN_TICKS;
    }

    private void finishAttack() {
        advanceComboStep();
        attackDef = null;
        state = grounded ? State.IDLE : State.AIR;
    }

    /**
     * The swing sprite already draws its own arc, so nothing here adds a second crescent — that
     * only ever read as a doubled trail. The heavy swing instead gets a burst of embers along the
     * path of the blade and a kick of dust from the lunge, which marks it as the bigger attack
     * without competing with the animation.
     */
    private void spawnSwingFlourish(int reach) {
        if (!attackIsHeavy || MinotaurGame.fx == null) return;
        DCoordinate l = getLocation();
        double dir = facingRight ? 1 : -1;
        double cx = l.x + dir * reach * 0.32;
        double cy = l.y + footOffset - bodyHeight * 0.55;
        MinotaurGame.fx.sparks(cx, cy, 12, dir, -0.25, new Color(255, 206, 130), 7);
        MinotaurGame.fx.embers(cx, cy, 6, new Color(255, 176, 96));
        MinotaurGame.fx.dust(feet().x, feet().y, 5, -dir, new Color(224, 208, 182));
    }

    private void resolveHits(int reach, int damageAmount, double knockX, double knockY) {
        Rectangle box = attackBox(reach, bodyHeight * 0.92, 12);
        for (SSActor target : actorsIn(box, Team.ENEMY)) {
            if (hitThisSwing.contains(target)) continue;
            hitThisSwing.add(target);

            double dir = target.getLocation().x >= getLocation().x ? 1 : -1;
            int dealt = target.damage(damageAmount, dir * knockX, knockY, attackIsHeavy, this);
            if (dealt <= 0) continue;

            onHitLanded(target, dealt, dir);
        }
    }

    private void onHitLanded(SSActor target, int dealt, double dir) {
        comboHits++;
        comboDisplay = 62;
        comboChain = COMBO_CHAIN_TICKS;
        // the light string is the rage engine — three quick links out-earn one heavy swing
        addRage(attackIsHeavy ? 12 : 9);
        MinotaurGame.addScore(dealt);

        DCoordinate c = target.core();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.sparks(c.x, c.y, attackIsHeavy ? 16 : 10, dir, -0.35,
                    new Color(255, 236, 176), attackIsHeavy ? 12 : 8);
            MinotaurGame.fx.text(c.x, c.y - 34, String.valueOf(dealt),
                    attackIsHeavy ? new Color(255, 186, 90) : new Color(255, 244, 214),
                    attackIsHeavy ? 34 : 26);
        }
        if (attackIsHeavy) {
            MinotaurSfx.hitHeavy();
            level().hitStop(7);
            level().shake(11, 16);
        } else {
            MinotaurSfx.hitLight();
            level().hitStop(3);
            level().shake(5, 9);
        }
        // a landed hit refunds a slice of the dash, rewarding aggression
        dashCooldown = Math.max(0, dashCooldown - 6);
    }

    /* ------------------------------ dash ------------------------------ */

    private void startDash() {
        dashDir = moveInput != 0 ? moveInput : (facingRight ? 1 : -1);
        facingRight = dashDir > 0;
        dashTicks = DASH_TICKS;
        dashCooldown = DASH_COOLDOWN;
        grantInvulnerability(DASH_IFRAMES);
        gravityEnabled = false;
        state = State.DASH;
        vy = 0;
        MinotaurSfx.dash();
        DCoordinate f = feet();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.dust(f.x, f.y, 10, -dashDir, new Color(226, 210, 186));
        }
    }

    private void tickDash() {
        vx = dashDir * DASH_SPEED;
        vy = 0;
        if (dashTicks % 2 == 0 && MinotaurGame.fx != null && getGraphic() != null) {
            MinotaurGame.fx.ghost(getLocation().x, getLocation().y,
                    getGraphic().getCurrentImage(), new Color(255, 255, 255), 12);
        }
        if (--dashTicks <= 0) {
            gravityEnabled = true;
            vx *= 0.55;
            state = grounded ? State.IDLE : State.AIR;
            // a dash cancelled straight into an attack is the intended advanced option
            if (attackBuffered) {
                attackBuffered = false;
                startAttack(attackBufferedHeavy);
            }
        }
    }

    /* ------------------------------ rage roar ------------------------------ */

    private void startRoar() {
        state = State.ROAR;
        roarTick = 0;
        vx = 0;
        rage = 0;
        rageWasReady = false;
        grantInvulnerability(ROAR_TICKS);
        MinotaurSfx.roar();
        setAnimation("swing", true);
        stretchSwingOver(ROAR_TICKS);
    }

    private void tickRoar() {
        vx *= 0.6;
        if (roarTick == ROAR_STRIKE_TICK) {
            DCoordinate c = core();
            for (SSActor target : actorsNear(c, ROAR_RADIUS, Team.ENEMY)) {
                double dir = target.getLocation().x >= c.x ? 1 : -1;
                int dealt = target.damage(36, dir * 18, -9, true, this);
                if (dealt > 0) {
                    MinotaurGame.addScore(dealt);
                    if (MinotaurGame.fx != null) {
                        MinotaurGame.fx.text(target.core().x, target.core().y - 30,
                                String.valueOf(dealt), new Color(255, 170, 80), 32);
                    }
                }
            }
            heal((int) (getMaxHealth() * 0.18));
            if (MinotaurGame.hud != null) MinotaurGame.hud.onPlayerHeal();
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.ring(c.x, c.y, 40, 17, 26, new Color(255, 190, 110), 9);
                MinotaurGame.fx.ring(c.x, c.y, 20, 12, 34, new Color(255, 240, 190), 5);
                MinotaurGame.fx.embers(c.x, c.y, 34, new Color(255, 190, 90));
                MinotaurGame.fx.dust(feet().x, feet().y, 16, 0, new Color(235, 215, 185));
            }
            level().hitStop(9);
            level().shake(22, 32);
        }
        if (++roarTick >= ROAR_TICKS) {
            state = grounded ? State.IDLE : State.AIR;
        }
    }

    private void tickRageAura() {
        if (!isRageReady() || MinotaurGame.fx == null) return;
        if (--auraTimer > 0) return;
        auraTimer = 4;
        DCoordinate f = feet();
        MinotaurGame.fx.embers(f.x, f.y - 40, 1, new Color(255, 196, 90));
    }

    /* ------------------------------ damage and death ------------------------------ */

    @Override
    protected void onDamaged(int amount, boolean heavy, SSActor source) {
        addRage(Math.max(2, amount / 3));
        comboHits = 0;
        comboDisplay = 0;
        comboStep = 0;
        grantInvulnerability(58);
        hurtTicks = heavy ? 13 : 9;
        if (state != State.DASH) {
            state = State.HURT;
            gravityEnabled = true;
        }
        MinotaurSfx.playerHurt();
        if (MinotaurGame.hud != null) MinotaurGame.hud.onPlayerHurt();
        if (MinotaurGame.fx != null) {
            DCoordinate c = core();
            MinotaurGame.fx.blood(c.x, c.y, 12, Math.signum(vx) == 0 ? 1 : Math.signum(vx),
                    new Color(150, 26, 32));
            MinotaurGame.fx.text(c.x, c.y - 40, "-" + amount, new Color(255, 96, 96), 28);
        }
        level().hitStop(5);
        level().shake(14, 18);
    }

    private void tickHurt() {
        vx *= grounded ? 0.86 : 0.98;
        if (--hurtTicks <= 0) {
            state = grounded ? State.IDLE : State.AIR;
        }
    }

    @Override
    protected void onFellOutOfWorld() {
        // Pits cost health and a respawn rather than a life, which keeps momentum up. The
        // relocation is unconditional while the damage still honours invulnerability — a dunk
        // in lava on the way down must not be able to leave the player parked at the bottom of
        // the world waiting for their i-frames to expire.
        if (state == State.DEAD) return;
        damage(16, 0, 0, true, null);
        if (dead) return;
        setLocation(lastSafeGround.x, lastSafeGround.y - 40);
        vx = 0;
        vy = 0;
        grantInvulnerability(70);
        if (MinotaurGame.cameraAnchor != null) MinotaurGame.cameraAnchor.snap();
    }

    @Override
    protected void onKilled() {
        state = State.DEAD;
        deathTicks = 0;
        vx = 0;
        comboHits = 0;
        setAnimation("death");
        MinotaurSfx.enemyDie();
        if (MinotaurGame.fx != null) {
            DCoordinate c = core();
            MinotaurGame.fx.blood(c.x, c.y, 26, 0, new Color(140, 22, 28));
        }
        level().shake(16, 30);
    }

    private void tickDead() {
        vx *= 0.86;
        if (++deathTicks == 46) {
            MinotaurGame.onPlayerDeath();
        }
    }

    @Override
    protected void onLanded() {
        double impact = landingImpact;
        if (impact > 9) {
            MinotaurSfx.land();
            DCoordinate f = feet();
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.dust(f.x, f.y, (int) Math.min(14, impact), 0, new Color(220, 205, 180));
            }
            if (impact > 19) level().shake(5, 8);
        }
        if (state == State.AIR) state = State.IDLE;
    }

    private boolean overHazard() {
        if (MinotaurGame.level == null) return false;
        Rectangle body = bodyRect();
        for (var h : MinotaurGame.level.hazards) {
            if (body.intersects(h.bounds)) return true;
        }
        return false;
    }

    /* ------------------------------ animation ------------------------------ */

    private void updateAnimation() {
        switch (state) {
            case DEAD:   setAnimation("death"); break;
            case ATTACK:
            case ROAR:   setAnimation("swing"); break;
            case DASH:   setAnimation("run");   break;
            case AIR:    setAnimation("run");   break;
            case RUN:    setAnimation("run");   break;
            default:     setAnimation("idle");  break;
        }
        // a subtle pop on landing and a stretch while rising sell the weight without new art
        if (state == State.AIR) {
            setRenderScale(1.0 + Math.max(-0.06, Math.min(0.06, -vy * 0.004)));
        } else if (grounded && ticksSinceGrounded == 0 && landingImpact > 9) {
            setRenderScale(0.9);
        } else {
            setRenderScale(getRenderScale() + (1.0 - getRenderScale()) * 0.3);
        }
    }

    private void setAnimation(String name) {
        setAnimation(name, false);
    }

    /**
     * @param force replays the sequence even when it is already the current one. Attacks must
     *              force: the swing is non-looping, so a second swing in the same direction
     *              would otherwise leave the graphic parked on the last frame and the character
     *              would land hits without ever visibly swinging again.
     */
    private void setAnimation(String name, boolean force) {
        String key = name + (facingRight ? "R" : "L");
        if (key.equals(currentAnim) && !force) return;
        if (key.equals(currentAnim)) {
            currentSequence.restart();
            return;
        }
        currentAnim = key;
        switch (name) {
            case "run":   currentSequence = art.run(facingRight); break;
            case "swing": currentSequence = art.swing(facingRight); break;
            case "death": currentSequence = art.death(facingRight); break;
            default:      currentSequence = art.idle(facingRight); break;
        }
        currentSequence.restart();
        setGraphic(currentSequence);
    }

    /* ------------------------------ engine glue ------------------------------ */

    @Override
    public int getWidth() { return MinotaurArt.SHEET_W * MinotaurArt.U; }

    @Override
    public int getHeight() { return MinotaurArt.SHEET_H * MinotaurArt.U; }

}

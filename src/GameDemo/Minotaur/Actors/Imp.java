package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import java.awt.Color;

/**
 * Imp — fast, fragile, and dangerous in numbers. Closes to melee on foot, then crouches for a
 * clearly telegraphed leap. The crouch is the whole design: it is the window in which the
 * player is supposed to swing or dash, so it is held long enough to react to and paired with a
 * visible squash.
 *
 * @author Joseph
 */
public class Imp extends Enemy {

    private enum Mode { CHASE, CROUCH, LEAP, RECOVER }

    private static final double WALK_SPEED = 3.6;
    private static final int CROUCH_TICKS = 17, RECOVER_TICKS = 26, LEAP_COOLDOWN = 46;

    private Mode mode = Mode.CHASE;
    private int modeTicks = 0;
    private int leapCooldown = 0;
    private Sequence seqR, seqL;
    private boolean lastFacing = true;

    public Imp(DCoordinate location) {
        super(location);
        setName("Imp");
        bodyWidth = 46;
        bodyHeight = 66;
        footOffset = 36;
        setMaxHealth(28);
        setCurrentHealth(28);
        contactDamage = 7;
        scoreValue = 60;
        healthDropChance = 24;
        knockbackResist = 1.05;
        bloodColor = new Color(150, 32, 34);
        deathBurstColor = new Color(255, 120, 80);
        seqR = MinotaurAssets.copy(MinotaurAssets.impR);
        seqL = MinotaurAssets.copy(MinotaurAssets.impL);
        setGraphic(seqR);
    }

    @Override
    protected void enemyTick(PlayerMinotaur player) {
        if (player == null || player.isDead()) {
            vx *= 0.85;
            return;
        }
        if (leapCooldown > 0) leapCooldown--;
        double dx = player.getLocation().x - getLocation().x;
        double dir = Math.signum(dx);
        if (dir == 0) dir = 1;

        switch (mode) {
            case CHASE: {
                facingRight = dir > 0;
                boolean edgeAhead = grounded && !groundAheadOf(dir);
                vx = edgeAhead ? 0 : dir * WALK_SPEED;
                if (grounded && leapCooldown <= 0 && Math.abs(dx) < 230
                        && Math.abs(player.getLocation().y - getLocation().y) < 190) {
                    mode = Mode.CROUCH;
                    modeTicks = CROUCH_TICKS;
                    vx = 0;
                }
                break;
            }
            case CROUCH: {
                vx *= 0.6;
                facingRight = dir > 0;
                // squashing down is the tell that a leap is coming
                setRenderScale(0.82 + 0.12 * (modeTicks / (double) CROUCH_TICKS));
                if (--modeTicks <= 0) {
                    mode = Mode.LEAP;
                    modeTicks = 60;
                    vx = dir * 12.5;
                    vy = -12.8;
                    setRenderScale(1.12);
                    if (MinotaurGame.fx != null) {
                        MinotaurGame.fx.dust(feet().x, feet().y, 5, -dir, new Color(200, 150, 130));
                    }
                }
                break;
            }
            case LEAP: {
                setRenderScale(getRenderScale() + (1.0 - getRenderScale()) * 0.15);
                if (grounded || --modeTicks <= 0) {
                    mode = Mode.RECOVER;
                    modeTicks = RECOVER_TICKS;
                    leapCooldown = LEAP_COOLDOWN;
                }
                break;
            }
            case RECOVER: {
                vx *= grounded ? 0.7 : 0.99;
                if (--modeTicks <= 0) mode = Mode.CHASE;
                break;
            }
        }

        if (facingRight != lastFacing) {
            lastFacing = facingRight;
            setGraphic(facingRight ? seqR : seqL);
        }
    }

    @Override
    protected void idleTick() {
        vx = 0;
        setRenderScale(1.0);
    }

    @Override
    public int getWidth() { return 78; }

    @Override
    public int getHeight() { return 78; }

    @Override
    public void updateHitbox() {
        // combat uses the body box
    }
}

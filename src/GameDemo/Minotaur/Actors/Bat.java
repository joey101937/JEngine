package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import java.awt.Color;

/**
 * Cave bat — the answer to a player who only ever looks left and right. Hovers above and to
 * the side of its quarry, then commits to a locked-in dive. Because the dive aims at where the
 * player <i>was</i> when it started, it can be walked out of; because it cannot be steered
 * mid-flight, it is fair.
 *
 * @author Joseph
 */
public class Bat extends Enemy {

    private enum Mode { HOVER, DIVE, CLIMB }

    private Mode mode = Mode.HOVER;
    private int modeTicks = 0;
    private int diveCooldown;
    private double bobPhase;
    private double homeY;
    private double diveTargetX, diveTargetY;
    private Sequence seqR, seqL;
    private boolean lastFacing = true;

    public Bat(DCoordinate location) {
        super(location);
        setName("Bat");
        bodyWidth = 56;
        bodyHeight = 44;
        footOffset = 20;
        setMaxHealth(20);
        setCurrentHealth(20);
        contactDamage = 6;
        scoreValue = 50;
        healthDropChance = 20;
        knockbackResist = 1.2;
        gravityEnabled = false;
        aggroRange = 780;
        bloodColor = new Color(96, 40, 110);
        deathBurstColor = new Color(180, 130, 240);
        homeY = location.y;
        bobPhase = location.x * 0.01;
        diveCooldown = 40 + Framework.Main.generateRandomIntLocally(0, 70);
        seqR = MinotaurAssets.copy(MinotaurAssets.batR);
        seqL = MinotaurAssets.copy(MinotaurAssets.batL);
        setGraphic(seqR);
    }

    @Override
    protected void enemyTick(PlayerMinotaur player) {
        bobPhase += 0.09;
        if (player == null || player.isDead()) {
            vx *= 0.9;
            vy = Math.sin(bobPhase) * 0.9;
            return;
        }
        DCoordinate target = player.core();

        switch (mode) {
            case HOVER: {
                // sit off to one side and above, so the dive comes in at an angle
                double side = getLocation().x < target.x ? -1 : 1;
                double wantX = target.x + side * 210;
                double wantY = Math.min(homeY, target.y - 190);
                vx += Math.signum(wantX - getLocation().x) * 0.3;
                vy += Math.signum(wantY - getLocation().y) * 0.26;
                vx *= 0.9;
                vy *= 0.9;
                vy += Math.sin(bobPhase) * 0.32;
                facingRight = target.x > getLocation().x;
                if (--diveCooldown <= 0 && Math.abs(target.x - getLocation().x) < 520) {
                    mode = Mode.DIVE;
                    modeTicks = 52;
                    diveTargetX = target.x;
                    diveTargetY = target.y + 20;
                }
                break;
            }
            case DIVE: {
                double dx = diveTargetX - getLocation().x, dy = diveTargetY - getLocation().y;
                double d = Math.max(1, Math.hypot(dx, dy));
                vx += (dx / d) * 1.25;
                vy += (dy / d) * 1.25;
                vx = clamp(vx, 13);
                vy = clamp(vy, 13);
                facingRight = vx > 0;
                if (--modeTicks <= 0 || d < 40) {
                    mode = Mode.CLIMB;
                    modeTicks = 34;
                }
                break;
            }
            case CLIMB: {
                vy -= 0.65;
                vy = clamp(vy, 9);
                vx *= 0.94;
                if (--modeTicks <= 0) {
                    mode = Mode.HOVER;
                    diveCooldown = 90 + Framework.Main.generateRandomIntLocally(0, 70);
                }
                break;
            }
        }

        if (facingRight != lastFacing) {
            lastFacing = facingRight;
            setGraphic(facingRight ? seqR : seqL);
        }
    }

    private static double clamp(double v, double max) {
        return Math.max(-max, Math.min(max, v));
    }

    @Override
    protected void idleTick() {
        bobPhase += 0.06;
        vx = 0;
        vy = Math.sin(bobPhase) * 0.8;
    }

    @Override
    protected void onWallContact() {
        if (mode == Mode.DIVE) {
            mode = Mode.CLIMB;
            modeTicks = 30;
        }
    }

    @Override
    public int getWidth() { return 90; }

    @Override
    public int getHeight() { return 66; }

    @Override
    public void updateHitbox() {
        // combat uses the body box
    }
}

package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GameObject2;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * A dropped orb. Falls to the ground, bobs, and once the player is close enough it stops
 * waiting and flies at them — collecting should never cost precision platforming.
 *
 * @author Joseph
 */
public class Pickup extends GameObject2 {

    public enum Type { HEALTH, RAGE }

    private static final double MAGNET_RANGE = 260;

    private final Type type;
    private final int amount;
    private double vx, vy;
    private boolean landed = false;
    private int life = 60 * 26;      // orbs expire so a skipped fight does not litter the level
    private int bob = 0;

    public Pickup(DCoordinate location, Type type, int amount, double launchX, double launchY) {
        super(location);
        this.type = type;
        this.amount = amount;
        this.vx = launchX;
        this.vy = launchY;
        setName(type == Type.HEALTH ? "HealthOrb" : "RageOrb");
        isSolid = false;
        preventOverlap = false;
        movementType = MovementType.RawVelocity;
        setZLayer(12);
        setGraphic(type == Type.HEALTH ? MinotaurAssets.healthPickup : MinotaurAssets.ragePickup);
    }

    @Override
    public void tick() {
        PlayerMinotaur player = MinotaurGame.player;
        DCoordinate here = getLocation();

        if (player != null && !player.isDead()) {
            DCoordinate target = player.core();
            double dx = target.x - here.x, dy = target.y - here.y;
            double dist = Math.hypot(dx, dy);
            if (dist < 46) {
                collect(player);
                return;
            }
            if (dist < MAGNET_RANGE) {
                // accelerate toward the player rather than snapping, so the pull reads
                double pull = 1.15 * (1 - dist / MAGNET_RANGE) + 0.35;
                vx += (dx / dist) * pull;
                vy += (dy / dist) * pull;
                vx *= 0.9;
                vy *= 0.9;
                velocity.x = vx;
                velocity.y = vy;
                if (--life <= 0) destroy();
                return;
            }
        }

        if (!landed) {
            vy += 0.7;
            vx *= 0.97;
            if (restingOnGround(here.x, here.y + vy)) {
                landed = true;
                vx = 0;
                vy = 0;
            }
        } else {
            bob++;
            vy = Math.sin(bob * 0.09) * 0.7;
        }
        velocity.x = vx;
        velocity.y = vy;

        if (life % 9 == 0 && MinotaurGame.fx != null) {
            MinotaurGame.fx.embers(here.x, here.y, 1,
                    type == Type.HEALTH ? new Color(255, 110, 120) : new Color(255, 200, 90));
        }
        if (--life <= 0) destroy();
    }

    private boolean restingOnGround(double x, double y) {
        if (MinotaurGame.level == null) return false;
        for (Rectangle r : MinotaurGame.level.solids) {
            if (r.contains((int) x, (int) (y + 18))) return true;
        }
        for (Rectangle r : MinotaurGame.level.oneWays) {
            if (r.contains((int) x, (int) (y + 18))) return true;
        }
        return false;
    }

    private void collect(PlayerMinotaur player) {
        if (type == Type.HEALTH) {
            player.heal(amount);
            if (MinotaurGame.hud != null) MinotaurGame.hud.onPlayerHeal();
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.text(player.core().x, player.core().y - 40, "+" + amount,
                        new Color(120, 255, 150), 26);
            }
        } else {
            player.addRage(amount);
        }
        MinotaurSfx.pickup();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.embers(getLocation().x, getLocation().y, 8,
                    type == Type.HEALTH ? new Color(255, 120, 130) : new Color(255, 210, 110));
        }
        destroy();
    }

    @Override
    public int getWidth() { return 40; }

    @Override
    public int getHeight() { return 40; }

    @Override
    public void updateHitbox() {
        // collection is a distance check
    }
}

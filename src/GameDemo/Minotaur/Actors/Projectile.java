package GameDemo.Minotaur.Actors;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * A thrown or conjured shot. Arcs under its own gravity setting, trails embers, and bursts on
 * the first thing it touches — a body of the opposing team or the level itself.
 *
 * @author Joseph
 */
public class Projectile extends GameObject2 {

    private final SSActor.Team targetTeam;
    private final int damage;
    private final double knockX, knockY;
    private final Color trail;
    private final int size;
    private double vxx, vyy;
    private final double arc;
    private int life;
    private int trailTimer = 0;
    private boolean spent = false;
    /** Set for shockwaves, which travel along the floor and must not burst on it. */
    public boolean ignoreTerrain = false;

    public Projectile(DCoordinate origin, double vx, double vy, double gravity,
                      Sprite sprite, int size, SSActor.Team targetTeam,
                      int damage, double knockX, double knockY, Color trail, int lifeTicks) {
        super(origin);
        this.vxx = vx;
        this.vyy = vy;
        this.arc = gravity;
        this.targetTeam = targetTeam;
        this.damage = damage;
        this.knockX = knockX;
        this.knockY = knockY;
        this.trail = trail;
        this.size = size;
        this.life = lifeTicks;
        setName("Projectile");
        isSolid = false;
        preventOverlap = false;
        movementType = MovementType.RawVelocity;
        setZLayer(24);
        setGraphic(sprite);
    }

    public Rectangle bounds() {
        DCoordinate l = getLocation();
        int r = (int) (size * 0.42);
        return new Rectangle((int) l.x - r, (int) l.y - r, r * 2, r * 2);
    }

    @Override
    public void tick() {
        if (spent) return;
        if (MinotaurGame.level != null && MinotaurGame.level.hitStop > 0) {
            velocity.x = 0;
            velocity.y = 0;
            return;
        }
        vyy += arc;
        velocity.x = vxx;
        velocity.y = vyy;
        setRotation(Math.toDegrees(Math.atan2(vyy, vxx)));

        if (--trailTimer <= 0) {
            trailTimer = 2;
            if (MinotaurGame.fx != null) {
                MinotaurGame.fx.embers(getLocation().x, getLocation().y, 1, trail);
            }
        }

        Rectangle box = bounds();

        if (targetTeam == SSActor.Team.PLAYER) {
            PlayerMinotaur p = MinotaurGame.player;
            if (p != null && !p.isDead() && p.bodyRect().intersects(box)) {
                double dir = p.getLocation().x >= getLocation().x ? 1 : -1;
                if (p.damage(damage, dir * knockX, knockY, false, null) > 0) {
                    burst();
                    return;
                }
            }
        } else if (MinotaurGame.level != null) {
            for (GameObject2 o : getHostGame().getAllObjects()) {
                if (o instanceof SSActor a && a.getTeam() == targetTeam && !a.isDead()
                        && a.bodyRect().intersects(box)) {
                    double dir = a.getLocation().x >= getLocation().x ? 1 : -1;
                    a.damage(damage, dir * knockX, knockY, false, null);
                    burst();
                    return;
                }
            }
        }

        if (!ignoreTerrain && MinotaurGame.level != null) {
            for (Rectangle r : MinotaurGame.level.solids) {
                if (r.intersects(box)) { burst(); return; }
            }
        }
        if (--life <= 0) burst();
    }

    private void burst() {
        if (spent) return;
        spent = true;
        MinotaurSfx.fireImpact();
        if (MinotaurGame.fx != null) {
            DCoordinate l = getLocation();
            MinotaurGame.fx.sparks(l.x, l.y, 14, 0, -1, trail, 8);
            MinotaurGame.fx.ring(l.x, l.y, 8, 5, 14, trail, 4);
        }
        destroy();
    }

    @Override
    public int getWidth() { return size; }

    @Override
    public int getHeight() { return size; }

    @Override
    public void updateHitbox() {
        // hits are resolved against bounds()
    }
}

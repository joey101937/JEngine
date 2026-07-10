package GameDemo.SpaceInvadersDemo;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.UtilityObjects.Projectile;
import java.awt.Color;

/**
 * A bolt of energy. The same class serves the player and the enemies; the
 * {@code friendly} flag decides who it can hurt. Player shots damage aliens/boss,
 * enemy shots damage the player, and shots never harm their own side.
 *
 * @author Joseph
 */
public class Laser extends Projectile {

    public boolean friendly;
    public int damage;
    private final Color impactColor;
    private boolean consumed = false;

    public Laser(DCoordinate start, DCoordinate destination, boolean friendly, int damage, Sprite sprite, Color impactColor) {
        super(start, destination);
        this.friendly = friendly;
        this.damage = damage;
        this.impactColor = impactColor;
        setGraphic(sprite);
        this.baseSpeed = friendly ? 12 : 7.5;
        this.maxRange = 1600;
        setZLayer(friendly ? 6 : 5);
        setName(friendly ? "PlayerLaser" : "EnemyLaser");
    }

    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (consumed) return;

        if (friendly) {
            if (other instanceof Alien) {
                consumed = true;
                Explosion.spark(getHostGame(), getPixelLocation(), impactColor);
                ((Alien) other).takeDamage(damage);
                destroy();
            }
            // friendly fire against the player is ignored
        } else {
            if (other instanceof PlayerShip) {
                consumed = true;
                ((PlayerShip) other).takeHit(damage, getPixelLocation());
                destroy();
            }
            // enemy shots pass harmlessly through other enemies
        }
    }
}

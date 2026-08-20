package GameDemo.Minotaur.Terrain;

import java.awt.Rectangle;

/**
 * A region of the level that hurts anything standing in it — spike beds and lava.
 * Purely data: {@link GameDemo.Minotaur.Actors.SSActor} tests these while resolving movement,
 * so a hazard costs nothing per tick beyond a rectangle intersection.
 *
 * @author Joseph
 */
public class Hazard {

    public final Rectangle bounds;
    public final int damage;

    public Hazard(Rectangle bounds, int damage) {
        this.bounds = bounds;
        this.damage = damage;
    }
}

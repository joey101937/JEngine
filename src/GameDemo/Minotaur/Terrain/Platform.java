package GameDemo.Minotaur.Terrain;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;
import java.awt.Rectangle;

/**
 * A piece of standable level geometry. The image is centred on the object like any other
 * graphic, but the authoritative shape is {@link #bounds} — the level hands that rectangle to
 * the actors' own sweep, so what is drawn and what is collided with can never drift apart.
 *
 * Declared non-solid: nothing in this game uses the engine's collision resolution.
 *
 * @author Joseph
 */
public class Platform extends GameObject2 {

    public final Rectangle bounds;
    /** One-way ledges are landed on from above and passed through from below. */
    public final boolean oneWay;

    public Platform(Rectangle bounds, Graphic graphic, boolean oneWay) {
        super(new DCoordinate(bounds.getCenterX(), bounds.getCenterY()));
        this.bounds = bounds;
        this.oneWay = oneWay;
        setGraphic(graphic);
        setName(oneWay ? "Ledge" : "Platform");
        isSolid = false;
        preventOverlap = false;
        isInvisible = graphic == null;
        setZLayer(5);
    }

    @Override
    public int getWidth() { return bounds.width; }

    @Override
    public int getHeight() { return bounds.height; }

    @Override
    public void updateHitbox() {
        // no hitbox needed; the level owns this shape
    }
}

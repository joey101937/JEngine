package GameDemo.Minotaur.Terrain;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;

/**
 * Scenery: pillars, braziers, bones. Draws and does nothing else, so a level can be dressed
 * without adding anything to the collision or combat passes.
 *
 * @author Joseph
 */
public class Decor extends GameObject2 {

    private final int w, h;

    /**
     * @param groundY world Y the graphic stands on, so decor lines up with the floor regardless
     *                of its height
     */
    public Decor(double x, double groundY, Graphic graphic, int zLayer) {
        super(new DCoordinate(x, groundY));
        this.w = graphic.getCurrentImage().getWidth();
        this.h = graphic.getCurrentImage().getHeight();
        // anchored at the bottom centre, so this object's location is the point it stands on
        if (graphic instanceof Sprite s) {
            s.setAnchor(w / 2.0, h);
        } else if (graphic instanceof Sequence q) {
            q.setAnchor(w / 2.0, h);
        }
        setGraphic(graphic);
        setName("Decor");
        isSolid = false;
        preventOverlap = false;
        setZLayer(zLayer);
    }

    @Override
    public int getWidth() { return w; }

    @Override
    public int getHeight() { return h; }

    @Override
    public void updateHitbox() {
        // decorative only
    }
}

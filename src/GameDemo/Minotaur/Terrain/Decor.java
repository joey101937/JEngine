package GameDemo.Minotaur.Terrain;

import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Graphic;

/**
 * Scenery: pillars, braziers, bones. Draws and does nothing else, so a level can be dressed
 * without adding anything to the collision or combat passes.
 *
 * @author Joseph
 */
public class Decor extends GameObject2 {

    private final int w, h;

    /**
     * @param anchorBottomY world Y the graphic stands on, so decor lines up with the floor
     *                      regardless of its height
     */
    public Decor(double x, double anchorBottomY, Graphic graphic, int zLayer) {
        super(new DCoordinate(x, anchorBottomY - graphic.getCurrentImage().getHeight() / 2.0));
        this.w = graphic.getCurrentImage().getWidth();
        this.h = graphic.getCurrentImage().getHeight();
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

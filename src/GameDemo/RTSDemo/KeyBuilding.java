
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class KeyBuilding extends GameObject2 {
    public static final Sprite mainSprite = new Sprite(RTSAssetManager.building);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.buildingShadow);
    
    public int owningTeam = -1;
    
    public KeyBuilding(int x, int y) {
        super(x, y);
        this.setGraphic(mainSprite);
        this.isSolid = true;
    }
    
    
    @Override
    public void render(Graphics2D g) {
        // render shadow
        int shadowOffsetX = 4;
        int shadowOffsetY = 8;
        Coordinate pixelLocation = getPixelLocation();
        pixelLocation.x += shadowOffsetX;
        pixelLocation.y += shadowOffsetY;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = pixelLocation.x - toRender.getWidth() / 2;
        int renderY = pixelLocation.y - toRender.getHeight() / 2;
        g.rotate(Math.toRadians(getRotation()), pixelLocation.x, pixelLocation.y);
        g.drawImage(toRender, renderX, renderY, null);
        g.setTransform(old);
        super.render(g);
    }
    
}

package GameDemo.RTSDemo.SceneryObjects;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import GameDemo.RTSDemo.FogOfWar.SightBlocker;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

public class GreenShippingContainer extends GameObject2 implements SceneryObject, SightBlocker {
    private static final long serialVersionUID = 1L;

    public static final Sprite bodySprite = new Sprite(RTSAssetManager.greenShippingContainer);
    public static final Sprite shadowSprite = Sprite.generateShadowSprite(bodySprite.getImage(), .6);
    public static final double VISUAL_SCALE = .5;

    static {
        bodySprite.applyAlphaEdgeBlurSelf(1);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(2);
    }

    public GreenShippingContainer(int x, int y) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
    }

    public GreenShippingContainer(int x, int y, int rotation) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
        this.setRotation(rotation);
    }

    @Override
    public void onGameEnter() {
        super.onGameEnter();
        SceneryObject.register(this, getHostGame());
    }

    @Override
    public void onPostDeserialization() {
        this.setGraphic(bodySprite);
    }

    @Override
    public Rectangle getBlockerBounds() {
        Coordinate center = getPixelLocation();
        int w = getWidth();
        int h = getHeight();
        return new Rectangle(center.x - w / 2, center.y - h / 2, w, h);
    }

    @Override
    public void render(Graphics2D g) {
        int shadowOffsetX = 5;
        int shadowOffsetY = 10;
        Coordinate pixelLocation = getPixelLocation();
        Coordinate shadowLoc = new Coordinate(pixelLocation.x + shadowOffsetX, pixelLocation.y + shadowOffsetY);
        AffineTransform old = g.getTransform();
        VolatileImage shadowImg = shadowSprite.getCurrentVolatileImage();
        g.rotate(Math.toRadians(getRotation()), shadowLoc.x, shadowLoc.y);
        g.drawImage(shadowImg, shadowLoc.x - shadowImg.getWidth() / 2, shadowLoc.y - shadowImg.getHeight() / 2, null);
        g.setTransform(old);

        super.render(g);
    }
}

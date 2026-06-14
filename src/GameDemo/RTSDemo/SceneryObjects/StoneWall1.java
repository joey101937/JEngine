package GameDemo.RTSDemo.SceneryObjects;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

public class StoneWall1 extends GameObject2 implements SceneryObject {
    private static final long serialVersionUID = 1L;

    public static final Sprite bodySprite   = new Sprite(RTSAssetManager.stoneWall1);
    public static final Sprite shadowSprite = Sprite.generateShadowSprite(RTSAssetManager.stoneWall1, .7);
    public static final double VISUAL_SCALE = .65;

    static {
        bodySprite.applyAlphaEdgeBlurSelf(1);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(2);
    }

    public StoneWall1(int x, int y) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
    }

    public StoneWall1(int x, int y, int rotation) {
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
    public void render(Graphics2D g) {
        Coordinate renderLoc = getRenderLocation();
        VolatileImage shadowImg = shadowSprite.getCurrentVolatileImage();
        AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(getRotation()), renderLoc.x + 2, renderLoc.y + 4);
        g.drawImage(shadowImg, renderLoc.x + 2 - shadowImg.getWidth() / 2, renderLoc.y + 4 - shadowImg.getHeight() / 2, null);
        g.setTransform(old);

        super.render(g);
    }

    @Override
    public int getPathingPadding() {
        return 0;
    }
}

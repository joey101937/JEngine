package GameDemo.RTSDemo.SceneryObjects;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import GameDemo.RTSDemo.FogOfWar.SightBlocker;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

public class Tree2 extends GameObject2 implements SceneryObject, SightBlocker {
    private static final long serialVersionUID = 1L;

    public static final Sprite bodySprite   = new Sprite(RTSAssetManager.tree2);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.tree2Shadow);
    public static final double VISUAL_SCALE = .8;

    static {
        bodySprite.applyAlphaEdgeBlurSelf(1);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(1);
        shadowSprite.setOpacity(.9);
    }

    public Tree2(int x, int y) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
        this.hitbox = new Hitbox(this, Math.max(getWidth(), getHeight()) / 2);
    }

    public Tree2(int x, int y, int rotation) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
        this.setRotation(rotation);
        this.hitbox = new Hitbox(this, Math.max(getWidth(), getHeight()) / 2);
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
        Coordinate renderLoc = getRenderLocation();
        VolatileImage shadowImg = shadowSprite.getCurrentVolatileImage();
        AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(getRotation()), renderLoc.x, renderLoc.y);
        g.drawImage(shadowImg, renderLoc.x - shadowImg.getWidth() / 2, renderLoc.y - shadowImg.getHeight() / 2, null);
        g.setTransform(old);

        super.render(g);
    }
    
    @Override
    public int getPathingPadding() {
        return 0;
    }
}

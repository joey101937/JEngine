package GameDemo.RTSDemo.SceneryObjects;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import GameDemo.RTSDemo.FogOfWar.SightBlocker;
import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

public class MetalShack extends GameObject2 implements SceneryObject, SightBlocker {
    private static final long serialVersionUID = 1L;

    public static final Sprite bodySprite = new Sprite(RTSAssetManager.metalShack);
    public static final Sprite shadowSprite = Sprite.generateShadowSprite(bodySprite.getImage(), .6);
    public static Sprite ridgeShadowSprite;
    public static final double VISUAL_SCALE = 1;

    static {
        shadowSprite.scaleTo(VISUAL_SCALE);
        bodySprite.applyAlphaEdgeBlurSelf(1);
        shadowSprite.applyAlphaEdgeBlurSelf(2);
        ridgeShadowSprite = generateRidgeShadow(bodySprite.getImage());
        ridgeShadowSprite.scaleTo(VISUAL_SCALE);
    }

    public MetalShack(int x, int y) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
        SceneryObject.register(this);
    }

    public MetalShack(int x, int y, int rotation) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
        this.setRotation(rotation);
        SceneryObject.register(this);
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
        int shadowOffsetX = 20;
        int shadowOffsetY = 40;
        Coordinate pixelLocation = getPixelLocation();
        Coordinate shadowLoc = new Coordinate(pixelLocation.x + shadowOffsetX, pixelLocation.y + shadowOffsetY);
        AffineTransform old = g.getTransform();
        VolatileImage shadowImg = shadowSprite.getCurrentVolatileImage();
        g.rotate(Math.toRadians(getRotation()), shadowLoc.x, shadowLoc.y);
        g.drawImage(shadowImg, shadowLoc.x - shadowImg.getWidth() / 2, shadowLoc.y - shadowImg.getHeight() / 2, null);
        g.setTransform(old);

        super.render(g);

        // Ridge shadow — side-to-side ridge, so gradient varies along the long axis (local-Y).
        // Shift is projected onto local-Y axis so it tracks the world shadow direction.
        VolatileImage ridgeImg = ridgeShadowSprite.getCurrentVolatileImage();
        double rot = Math.toRadians(getRotation());
        // World shadow (-40, 40) projected onto local-Y axis (-sinRot, cosRot)
        float localShift = (float)(-40 * -Math.sin(rot) + 40 * Math.cos(rot)) * 0.35f;
        old = g.getTransform();
        Coordinate renderLoc = getRenderLocation();
        g.rotate(rot, renderLoc.x, renderLoc.y);
        g.drawImage(ridgeImg,
                (int)(renderLoc.x - ridgeImg.getWidth()  / 2),
                (int)(renderLoc.y - ridgeImg.getHeight() / 2 + localShift), null);
        g.setTransform(old);
    }

    // Side-to-side ridge: gradient always varies along Y so the dark band is horizontal
    // (spanning the full width), regardless of aspect ratio.
    private static Sprite generateRidgeShadow(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        float bandHalfWidth = h * 0.20f;
        float maxAlpha = 0.24f;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int srcAlpha = (src.getRGB(x, y) >> 24) & 0xFF;
                if (srcAlpha < 10) continue;

                float t = Math.abs(y - h / 2f) / bandHalfWidth;
                if (t >= 1f) continue;

                float alpha = (1f - t * t) * maxAlpha * (srcAlpha / 255f);
                int a = Math.min(255, (int) (alpha * 255));
                out.setRGB(x, y, (a << 24));
            }
        }

        return new Sprite(out);
    }

    @Override
    public int getPathingPadding() {
        return 25;
    }
}

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

public class Hangar extends GameObject2 implements SceneryObject, SightBlocker {
    private static final long serialVersionUID = 1L;

    // bodySprite is the main hangar structure — drives the hitbox and collision
    public static final Sprite bodySprite = new Sprite(RTSAssetManager.hangarBase);
    public static final Sprite bodyShadowSprite = Sprite.generateShadowSprite(bodySprite.getImage(), .7);
    // floorSprite is a wider visual underlay — no collision, renders beneath the body
    public static final Sprite floorSprite = new Sprite(RTSAssetManager.hangarFloor);
    // Ridge shadow generated after body is scaled and blurred
    public static Sprite ridgeShadowSprite;
    public static final double VISUAL_SCALE = 1;

    static {
        bodySprite.scaleTo(VISUAL_SCALE);
        bodyShadowSprite.scaleTo(VISUAL_SCALE);
        bodySprite.applyAlphaEdgeBlurSelf(1);
        floorSprite.applyAlphaEdgeBlurSelf(1);
        bodyShadowSprite.applyAlphaEdgeBlurSelf(2);
        ridgeShadowSprite = generateRidgeShadow(bodySprite.getImage());
        // Bake the saturation tint into the body sprite once.
        bodySprite.setSaturation(1.5);
    }

    public Hangar(int x, int y) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(bodySprite);
        this.isSolid = true;
        this.setZLayer(-1);
    }

    public Hangar(int x, int y, int rotation) {
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

    private transient Rectangle cachedBlockerBounds;

    @Override
    public Rectangle getBlockerBounds() {
        if (cachedBlockerBounds == null) {
            Coordinate center = getPixelLocation();
            int w = getWidth();
            int h = getHeight();
            cachedBlockerBounds = new Rectangle(center.x - w / 2, center.y - h / 2, w, h);
        }
        return cachedBlockerBounds;
    }

    @Override
    public void render(Graphics2D g) {
        Coordinate renderLoc = getRenderLocation();
        AffineTransform old;

        // 1. Floor underlay — wider than the body, no shadow, no collision impact
        VolatileImage floorImg = floorSprite.getCurrentVolatileImage();
        old = g.getTransform();
        g.rotate(Math.toRadians(getRotation()), renderLoc.x, renderLoc.y);
        g.drawImage(floorImg, renderLoc.x - floorImg.getWidth() / 2, renderLoc.y - floorImg.getHeight() / 2, null);
        g.setTransform(old);

        // 2. Body shadow
        int shadowOffsetX = 20;
        int shadowOffsetY = 40;
        Coordinate pixelLocation = getPixelLocation();
        Coordinate shadowLoc = new Coordinate(pixelLocation.x + shadowOffsetX, pixelLocation.y + shadowOffsetY);
        VolatileImage shadowImg = bodyShadowSprite.getCurrentVolatileImage();
        old = g.getTransform();
        g.rotate(Math.toRadians(getRotation()), shadowLoc.x, shadowLoc.y);
        g.drawImage(shadowImg, shadowLoc.x - shadowImg.getWidth() / 2, shadowLoc.y - shadowImg.getHeight() / 2, null);
        g.setTransform(old);

        // 3. Body — drives hitbox via super.render
        super.render(g);

        // 4. Ridge shadow — faint gradient stripe on the roof surface for depth.
        // After rotating the graphics context the coordinate system is the building's
        // local space, so translating in local-X shifts perpendicular to the spine.
        // We project the world shadow direction onto the local-X axis to get the
        // correct shift direction and magnitude at any rotation.
        VolatileImage ridgeImg = ridgeShadowSprite.getCurrentVolatileImage();
        double rot = Math.toRadians(getRotation());
        // World shadow direction (-40, 40) projected onto local-X axis (cosRot, sinRot)
        float localShift = (float)(-40 * Math.cos(rot) + 40 * Math.sin(rot)) * 0.35f;
        boolean spineAlongHeight = ridgeImg.getHeight() >= ridgeImg.getWidth();
        float shiftX = spineAlongHeight ? localShift : 0;
        float shiftY = spineAlongHeight ? 0 : localShift;
        old = g.getTransform();
        g.rotate(rot, renderLoc.x, renderLoc.y);
        g.drawImage(ridgeImg,
                (int)(renderLoc.x - ridgeImg.getWidth()  / 2 + shiftX),
                (int)(renderLoc.y - ridgeImg.getHeight() / 2 + shiftY), null);
        g.setTransform(old);
    }

    // Generates a symmetric centered gradient band along the spine of the roof.
    // No directional shift is baked in — the shift is applied at render time
    // based on the world shadow direction projected onto the building's local axis.
    private static Sprite generateRidgeShadow(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        boolean spineAlongHeight = h >= w;
        float halfSpan = spineAlongHeight ? w / 2f : h / 2f;
        float bandHalfWidth = halfSpan * 0.22f;
        float maxAlpha = 0.28f;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int srcAlpha = (src.getRGB(x, y) >> 24) & 0xFF;
                if (srcAlpha < 10) continue;

                float perp = spineAlongHeight ? x - w / 2f : y - h / 2f;
                float t = Math.abs(perp) / bandHalfWidth;
                if (t >= 1f) continue;

                float alpha = (1f - t * t) * maxAlpha * (srcAlpha / 255f);
                int a = Math.min(255, (int) (alpha * 255));
                out.setRGB(x, y, (a << 24));
            }
        }

        return new Sprite(out);
    }
}

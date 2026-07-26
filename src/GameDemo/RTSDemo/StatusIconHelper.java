package GameDemo.RTSDemo;

import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.Units.TankUnit;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class StatusIconHelper extends IndependentEffect{
    private static final long serialVersionUID = 1L;

    private static final int statusIconSize = 20;
    private static final int iconVerticalOffset = 20;
    /**
     * Icons are baked at a multiple of the size they are drawn at so they stay sharp when the
     * camera zoom and the resolution scale push them past 1:1 on screen.
     */
    private static final int bakeSupersample = 2;
    private static final int edgeSoftness = 1;
    private static final double iconOpacity = 1;
    private static final Sprite immobilizationIcon;
    private static final Sprite shieldIcon;
    private transient Game game;

    static {
        immobilizationIcon = bakeIcon(RTSAssetManager.immobilizationIcon);
        shieldIcon = bakeIcon(RTSAssetManager.shieldIcon);
    }

    /**
     * Shrinks a full size icon asset down to the resolution it is actually drawn at, then softens
     * and fades it. Doing the shrink once here rather than letting drawImage resize every frame is
     * what keeps the edges clean, and it means the softening lands on the pixels that reach the
     * screen instead of being averaged away by a later downscale.
     */
    private static Sprite bakeIcon(BufferedImage source) {
        Sprite icon = new Sprite(downsample(source, statusIconSize * bakeSupersample));
        icon.applyAlphaEdgeBlurSelf(edgeSoftness);
        icon.setOpacity(iconOpacity);
        return icon;
    }

    /**
     * High quality shrink to a square target size. Halving repeatedly keeps every source pixel
     * contributing to the result, and working in premultiplied alpha keeps the colour of fully
     * transparent pixels from bleeding into the icon's edge as a dark fringe.
     */
    private static BufferedImage downsample(BufferedImage source, int target) {
        BufferedImage current = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D init = current.createGraphics();
        init.drawImage(source, 0, 0, null);
        init.dispose();

        int width = current.getWidth();
        int height = current.getHeight();
        while (width > target * 2 && height > target * 2) {
            width /= 2;
            height /= 2;
            current = resize(current, width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        return resize(current, target, target, BufferedImage.TYPE_INT_ARGB);
    }

    private static BufferedImage resize(BufferedImage source, int width, int height, int type) {
        BufferedImage output = new BufferedImage(width, height, type);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return output;
    }

    public StatusIconHelper (Game g) {
        game = g;
    }

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
    }

    @Override
    public int getZLayer() {
        return 2;
    }

    @Override
    public void render(Graphics2D g) {
        // Drawn from the on-screen set rather than every object in the world: this runs every
        // frame, and the quadtree lookup skips the bulk of the map before any per-unit work.
        for(GameObject2 go : game.getObjectsOnScreen(false)) {
            if(go.isOnScreen() && go instanceof RTSUnit u && !u.isRubble && u.isVisible(ExternalCommunicator.localTeam)) {
                boolean sandbagged = u instanceof TankUnit tank && tank.sandbagActive;
                Sprite icon = null;
                if(sandbagged) {
                    icon = shieldIcon;
                } else if(u.isImmobilized) {
                    icon = immobilizationIcon;
                }
                if(icon != null) {
                    VolatileImage image = icon.getCurrentVolatileImage();
                    double drawWidth = image.getWidth() / (double) bakeSupersample;
                    double drawHeight = image.getHeight() / (double) bakeSupersample;
                    DCoordinate loc = u.getRenderLocation();
                    // The softening pass pads the image, so that padding is centred out to keep the
                    // visible icon where it has always sat. Sub pixel placement keeps it from
                    // twitching against the unit it is pinned to while that unit lerps between ticks.
                    AffineTransform transform = AffineTransform.getTranslateInstance(
                            loc.x - (u.getWidth() / 2.0) + (statusIconSize - drawWidth) / 2.0,
                            loc.y - (u.getHeight() / 2.0) - iconVerticalOffset + (statusIconSize - drawHeight) / 2.0
                    );
                    transform.scale(1.0 / bakeSupersample, 1.0 / bakeSupersample);
                    g.drawImage(image, transform, null);
                }
            }
        }
    }

    @Override
    public void tick() {

    }

}

package GameDemo.RTSDemo.FogOfWar;

import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSGame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class FogOfWarEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    public static boolean enabled = true;
    private transient Game game;

    public static final int FOG_ALPHA = 140;
    private static final int BLUR_RADIUS = 3;

    private volatile transient BufferedImage fogImage;

    // Latest fog image for the local team, shared so other HUD elements (the
    // minimap) can render the same fog the main view uses. Low-resolution
    // (one pixel per fog tile); scale up with bilinear interpolation.
    private static volatile BufferedImage latestFogImage;

    public static BufferedImage getLatestFogImage() {
        return latestFogImage;
    }

    public FogOfWarEffect(Game g) {
        this.game = g;
    }

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
        if (RTSGame.fogOfWarGrid == null) {
            RTSGame.fogOfWarGrid = new FogOfWarGrid(g.getWorldWidth(), g.getWorldHeight());
        }
    }

    @Override
    public void tick() {
        game.setBackgroundClip(null);
        if (game.getGameTickNumber() % 10 != 0) return;
        if (RTSGame.fogOfWarGrid == null) return;
        // The grid feeds unit targeting, which has to match on every machine in a multiplayer
        // match, so it keeps updating regardless of the toggle. `enabled` is a display setting:
        // it reveals the map to the local player without changing what units can see.
        RTSGame.fogOfWarGrid.update(game);
        if (!enabled) return;
        fogImage = buildFogImage(RTSGame.fogOfWarGrid, ExternalCommunicator.localTeam);
        latestFogImage = fogImage;
    }

    private BufferedImage buildFogImage(FogOfWarGrid grid, int team) {
        int gw = grid.getGridW();
        int gh = grid.getGridH();
        int ts = FogOfWarGrid.TILE_SIZE;

        float[] src = new float[gw * gh];
        float[] tmp = new float[gw * gh];

        for (int ty = 0; ty < gh; ty++) {
            for (int tx = 0; tx < gw; tx++) {
                src[ty * gw + tx] = grid.isTileVisible(team, tx * ts, ty * ts) ? 0f : 1f;
            }
        }

        boxBlurH(src, tmp, gw, gh, BLUR_RADIUS);
        boxBlurV(tmp, src, gw, gh, BLUR_RADIUS);

        // Pixels are filled into a detached array and pushed in with setRGB rather than written
        // straight into the raster's data buffer. Reaching into the buffer marks the image
        // untrackable for the lifetime of the image, which permanently bars Java2D from caching
        // it in video memory - and this image is blitted scaled and alpha-blended across the
        // whole viewport every frame, so it needs to stay accelerated.
        int[] pixels = new int[gw * gh];
        for (int i = 0; i < pixels.length; i++) {
            int a = Math.min(255, (int) (src[i] * FOG_ALPHA));
            pixels[i] = (a << 24); // black with varying alpha
        }
        BufferedImage img = new BufferedImage(gw, gh, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, gw, gh, pixels, 0, gw);
        return img;
    }

    private void boxBlurH(float[] src, float[] dst, int w, int h, int r) {
        float div = 2 * r + 1;
        for (int ty = 0; ty < h; ty++) {
            for (int tx = 0; tx < w; tx++) {
                float sum = 0;
                for (int k = -r; k <= r; k++) {
                    sum += src[ty * w + Math.max(0, Math.min(w - 1, tx + k))];
                }
                dst[ty * w + tx] = sum / div;
            }
        }
    }

    private void boxBlurV(float[] src, float[] dst, int w, int h, int r) {
        float div = 2 * r + 1;
        for (int tx = 0; tx < w; tx++) {
            for (int ty = 0; ty < h; ty++) {
                float sum = 0;
                for (int k = -r; k <= r; k++) {
                    sum += src[Math.max(0, Math.min(h - 1, ty + k)) * w + tx];
                }
                dst[ty * w + tx] = sum / div;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!enabled) return;
        BufferedImage img = fogImage;
        if (img == null) return;

        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, game.getWorldWidth(), game.getWorldHeight(), null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
    }

    @Override
    public int getZLayer() {
        return 1000;
    }
}

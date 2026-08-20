package GameDemo.Minotaur.Terrain;

import Framework.DCoordinate;
import Framework.GameObject2;
import GameDemo.Minotaur.MinotaurArt;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.MinotaurSfx;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * The way out. Stays sealed and cold while anything on the floor is still alive, then opens
 * with a sound, a banner and a column of embers so the player knows the fight is over without
 * having to read the HUD.
 *
 * @author Joseph
 */
public class ExitPortal extends GameObject2 {

    private final MinotaurArt.Palette palette;
    private boolean open = false;
    private boolean consumed = false;
    private int emberTimer = 0;

    private static final int W = 150, H = 230;

    public ExitPortal(double x, double groundY, MinotaurArt.Palette palette) {
        super(new DCoordinate(x, groundY - H / 2.0));
        this.palette = palette;
        setName("ExitPortal");
        isSolid = false;
        preventOverlap = false;
        setZLayer(4);
        setGraphic(MinotaurAssets.portal(palette, false));
    }

    public boolean isOpen() { return open; }

    public Rectangle bounds() {
        DCoordinate l = getLocation();
        return new Rectangle((int) (l.x - 52), (int) (l.y - H / 2.0), 104, H);
    }

    @Override
    public void tick() {
        if (!open && MinotaurGame.level != null && MinotaurGame.level.enemiesRemaining() == 0) {
            openGate();
        }
        if (open) {
            if (--emberTimer <= 0) {
                emberTimer = 3;
                if (MinotaurGame.fx != null) {
                    MinotaurGame.fx.embers(getLocation().x, getLocation().y + 60, 2, palette.accent);
                }
            }
            if (!consumed && MinotaurGame.player != null && !MinotaurGame.player.isDead()
                    && MinotaurGame.player.bodyRect().intersects(bounds())) {
                consumed = true;
                MinotaurGame.advanceLevel();
            }
        }
    }

    private void openGate() {
        open = true;
        setGraphic(MinotaurAssets.portal(palette, true));
        MinotaurSfx.portalOpen();
        if (MinotaurGame.fx != null) {
            MinotaurGame.fx.ring(getLocation().x, getLocation().y, 40, 9, 40, palette.accent, 6);
            MinotaurGame.fx.embers(getLocation().x, getLocation().y, 26, palette.accent);
        }
        if (MinotaurGame.hud != null) {
            MinotaurGame.hud.flashBanner("FLOOR CLEARED", "The gate has opened — head " +
                    (MinotaurGame.player != null && MinotaurGame.player.getLocation().x < getLocation().x
                            ? "east" : "west"),
                    new Color(255, 214, 120), 170);
        }
        if (MinotaurGame.level != null) MinotaurGame.level.shake(9, 26);
    }

    @Override
    public int getWidth() { return W; }

    @Override
    public int getHeight() { return H; }

    @Override
    public void updateHitbox() {
        // overlap is tested against bounds() directly
    }
}

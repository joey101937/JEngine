package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Units.TankUnit;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author guydu
 */
public class StatusIconHelper extends IndependentEffect{
    private static final long serialVersionUID = 1L;

    private static final int statusIconWidth = 20;
    private static final int statusIconHeight = 20;
    private transient Game game;

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
            if(go.isOnScreen() && go instanceof RTSUnit u && !u.isRubble) {
                boolean sandbagged = u instanceof TankUnit tank && tank.sandbagActive;
                BufferedImage icon = null;
                if(sandbagged) {
                    icon = RTSAssetManager.shieldIcon;
                } else if(u.isImmobilized) {
                    icon = RTSAssetManager.immobilizationIcon;
                }
                if(icon != null) {
                    Coordinate loc = u.getRenderLocation().toCoordinate();
                    g.drawImage(
                            icon,
                            loc.x - (u.getWidth() / 2),
                            loc.y - (u.getHeight() / 2) - 20,
                            statusIconWidth,
                            statusIconHeight,
                            null
                    );
                }
            }
        }
    }

    @Override
    public void tick() {
        
    }
    
}

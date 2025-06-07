package GameDemo.RTSDemo;

import Framework.GameObject2;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Units.TankUnit;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class StatusIconHelper extends IndependentEffect{
    private static final int statusIconWidth = 20;
    private static final int statusIconHeight = 20;
    
    @Override
    public int getZLayer() {
        return 2;
    }

    @Override
    public void render(Graphics2D g) {
        // RTSGame.navigationManager.render(g);
        for(GameObject2 go : RTSGame.game.getAllObjects()) {
            if(go.isOnScreen() && go instanceof RTSUnit u && !u.isRubble) {
                if(u.isImmobilized && !(u instanceof TankUnit tank && tank.sandbagActive)) {  
                    g.drawImage(
                            RTSAssetManager.immobilizationIcon,
                            u.getPixelLocation().x - (u.getWidth() / 2),
                            u.getPixelLocation().y - (u.getHeight() / 2) - 20,
                            statusIconWidth,
                            statusIconHeight,
                            null
                    );
                }
                if(u instanceof TankUnit tank && tank.sandbagActive) {
                     g.drawImage(
                            RTSAssetManager.shieldIcon,
                            u.getPixelLocation().x - (u.getWidth() / 2),
                            u.getPixelLocation().y - (u.getHeight() / 2) - 20,
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

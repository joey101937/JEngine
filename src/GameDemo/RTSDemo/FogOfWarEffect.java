
package GameDemo.RTSDemo;

import Framework.GameObject2;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author guydu
 */
public class FogOfWarEffect extends IndependentEffect {
    
    private Area area = new Area();

    @Override
    public void render(Graphics2D g) {
        if(area == null) return;
        g.setClip(area);
        var camera = RTSGame.game.getCamera();
        g.drawImage(
                RTSAssetManager.grassBG,
                -camera.getPixelLocation().x ,
                -camera.getPixelLocation().y,
                -camera.getPixelLocation().x + camera.getFieldOfView().width,
                -camera.getPixelLocation().y + camera.getFieldOfView().height,
                -camera.getPixelLocation().x,
                -camera.getPixelLocation().y,
                -camera.getPixelLocation().x + camera.getFieldOfView().width,
                -camera.getPixelLocation().y + camera.getFieldOfView().height,
                null
        );
        g.setClip(null);
    }

    @Override
    public void tick() {
        if(RTSGame.game.getGameTickNumber() % 5 != 0) return;
        area = new Area();
        var gameObjects = RTSGame.game.getAllObjects();
        for(GameObject2 go : gameObjects) {
            if(go instanceof RTSUnit unit && unit.team == ExternalCommunicator.localTeam) {
                Ellipse2D.Double visibilityCircle = new Ellipse2D.Double(
                    unit.location.x - unit.sightRadius,
                    unit.location.y - unit.sightRadius,
                    2 * unit.sightRadius,
                    2 * unit.sightRadius
                );
                
                if(!visibilityCircle.intersects(RTSGame.game.getCamera().getFieldOfView())) {
                    continue;
                }
                
                var visArea = new Area(visibilityCircle);
                
                area.add(visArea);
            }
        }
    }
    
    
    @Override
    public int getZLayer() {
        return Integer.MIN_VALUE;
    }

}

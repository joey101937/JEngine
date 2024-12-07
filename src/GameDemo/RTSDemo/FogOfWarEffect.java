
package GameDemo.RTSDemo;

import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.stream.Collectors;

/**
 *
 * @author guydu
 */
public class FogOfWarEffect extends IndependentEffect {
    private static final int UNITS_PER_SUBAREA = 7;
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
        if(1==1) return;
        if(RTSGame.game.getGameTickNumber() % 5 != 0) return;
        area = new Area();
        var gameObjects = RTSGame.game.getAllObjects();
        var localUnits = gameObjects.stream()
            .filter(go -> go instanceof RTSUnit && ((RTSUnit) go).team == ExternalCommunicator.localTeam)
            .map(go -> (RTSUnit) go)
            .collect(Collectors.toList());

        for (int i = 0; i < localUnits.size(); i += UNITS_PER_SUBAREA) {
            Area subArea = new Area();
            for (int j = i; j < Math.min(i + UNITS_PER_SUBAREA, localUnits.size()); j++) {
                RTSUnit unit = localUnits.get(j);
                Ellipse2D.Double visibilityCircle = new Ellipse2D.Double(
                    unit.location.x - unit.sightRadius,
                    unit.location.y - unit.sightRadius,
                    2 * unit.sightRadius,
                    2 * unit.sightRadius
                );
                
                if (visibilityCircle.intersects(RTSGame.game.getCamera().getFieldOfView())) {
                    subArea.add(new Area(visibilityCircle));
                }
            }
            area.add(subArea);
        }
        RTSGame.game.setBackgroundClip(createInverseArea(area, RTSGame.game.getWorldWidth(), RTSGame.game.getWorldHeight()));
    }
    
    
    @Override
    public int getZLayer() {
        return Integer.MIN_VALUE;
    }
    
    public static Area createInverseArea(Area visibilityArea, int canvasWidth, int canvasHeight) {
        // Create an area covering the entire canvas
        Area inverseArea = new Area(new Rectangle(0, 0, canvasWidth, canvasHeight));
        // Subtract the visible area from the full area to get the inverse
        inverseArea.subtract(visibilityArea);
        return inverseArea;
    }

}

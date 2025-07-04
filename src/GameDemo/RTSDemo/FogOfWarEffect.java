
package GameDemo.RTSDemo;

import Framework.CoreLoop.Handler;
import Framework.GraphicalAssets.Graphic;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.MultiplayerTest.ExternalCommunicator;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author guydu
 */
public class FogOfWarEffect extends IndependentEffect {
    private static final int UNITS_PER_SUBAREA = 7;
    private Area area = new Area();
    private boolean enabled = false;
    public static ExecutorService fogRenderService = Handler.newMinSizeCachedThreadPool(4);

    @Override
    public void render(Graphics2D g) {
        var camera = RTSGame.game.getCamera();
        var fov = camera.getFieldOfView();
        if(area == null) return;
            g.setClip(area);
            if(area.contains(fov))
            g.drawImage(
                    RTSAssetManager.grassBG,
                    -camera.getPixelLocation().x ,
                    -camera.getPixelLocation().y,
                    -camera.getPixelLocation().x + fov.width,
                    -camera.getPixelLocation().y + fov.height,
                    -camera.getPixelLocation().x,
                    -camera.getPixelLocation().y,
                    -camera.getPixelLocation().x + fov.width,
                    -camera.getPixelLocation().y + fov.height,
                    null
            );

            Graphic.renderLargeImageInParts(g, RTSAssetManager.grassBG, RTSGame.game.getCamera(), fogRenderService);
        g.setClip(null);
    }

    @Override
    public void tick() {
        if(!enabled) return;
        if(RTSGame.game.getGameTickNumber() % 5 != 0) return;
        area = new Area();
        var camera = RTSGame.game.getCamera();
        var fov = camera.getFieldOfView();
        var gameObjects = RTSGame.game.getObjectsOnScreen(true);
        var localUnits = gameObjects.stream()
            .filter(go -> go instanceof RTSUnit && ((RTSUnit) go).team == ExternalCommunicator.localTeam)
            .map(go -> (RTSUnit) go)
            .collect(Collectors.toList());

        for (int i = 0; i < localUnits.size(); i += UNITS_PER_SUBAREA) {
            if(area.contains(fov)) break; // nd early if fov is entirely covered
            Area subArea = new Area();
            for (int j = i; j < Math.min(i + UNITS_PER_SUBAREA, localUnits.size()); j++) {
                RTSUnit unit = localUnits.get(j);
                // todo use unit renderLocation instead of unit location
                Ellipse2D.Double visibilityCircle = new Ellipse2D.Double(
                    unit.getLocation().x - unit.sightRadius,
                    unit.getLocation().y - unit.sightRadius,
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

    
        private static class BackgroundRenderTask implements Runnable {

        Consumer c;

        public BackgroundRenderTask(Consumer c) {
            this.c = c;
        }

        @Override
        public void run() {
            c.accept(null);
        }

    }
}

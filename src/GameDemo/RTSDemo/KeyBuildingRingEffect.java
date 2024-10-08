package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

public class KeyBuildingRingEffect extends IndependentEffect {
    
    @Override
    public int getZLayer() {
        return -200;
    }

    @Override
    public void render(Graphics2D g) {
        for (GameObject2 obj : getGame().getAllObjects()) {
            if (obj instanceof KeyBuilding) {
                KeyBuilding building = (KeyBuilding) obj;
                renderCaptureRing(g, building);
            }
        }
    }

    @Override
    public void tick() {
        // No tick logic needed for this effect
    }

    private void renderCaptureRing(Graphics2D g, KeyBuilding building) {
        Coordinate pixelLocation = building.getPixelLocation();
        Color ringColor = building.owningTeam == -1 ? Color.GRAY : RTSUnit.getColorFromTeam(building.owningTeam);
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(ringColor);
        g.fillOval(pixelLocation.x - building.captureRadius, pixelLocation.y - building.captureRadius, building.captureRadius * 2, building.captureRadius * 2);
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.drawOval(pixelLocation.x - building.captureRadius, pixelLocation.y - building.captureRadius, building.captureRadius * 2, building.captureRadius * 2);
        
        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}

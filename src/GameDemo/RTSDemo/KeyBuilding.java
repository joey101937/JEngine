
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class KeyBuilding extends GameObject2 {
    public static final Sprite mainSprite = new Sprite(RTSAssetManager.building);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.buildingShadow);
    
    public int owningTeam = -1;
    private int captureRadius = 200;
    private double captureProgress = 0;
    private static final double CAPTURE_RATE = 0.001;
    private static final double CAPTURE_THRESHOLD = 1.0;
    
    public KeyBuilding(int x, int y) {
        super(x, y);
        this.setGraphic(mainSprite);
        this.isSolid = true;
    }
    
    @Override
    public void tick() {
        super.tick();
        updateCaptureStatus();
    }
    
    private void updateCaptureStatus() {
        ArrayList<RTSUnit> nearbyUnits = getNearbyUnits();
        int[] teamCounts = countTeams(nearbyUnits);
        
        int dominantTeam = getDominantTeam(teamCounts);
        
        if (dominantTeam != -1 && dominantTeam != owningTeam) {
            captureProgress += CAPTURE_RATE;
            if (captureProgress >= CAPTURE_THRESHOLD) {
                owningTeam = dominantTeam;
                captureProgress = 0;
            }
        } else {
            captureProgress = Math.max(0, captureProgress - CAPTURE_RATE);
        }
    }
    
    private ArrayList<RTSUnit> getNearbyUnits() {
        ArrayList<RTSUnit> nearbyUnits = new ArrayList<>();
        for (GameObject2 obj : getHostGame().getAllObjects()) {
            if (obj instanceof RTSUnit && distanceFrom(obj) <= captureRadius) {
                nearbyUnits.add((RTSUnit) obj);
            }
        }
        return nearbyUnits;
    }
    
    private int[] countTeams(ArrayList<RTSUnit> units) {
        int[] teamCounts = new int[3]; // Assuming 3 teams
        for (RTSUnit unit : units) {
            if (unit.team >= 0 && unit.team < 3) {
                teamCounts[unit.team]++;
            }
        }
        return teamCounts;
    }
    
    private int getDominantTeam(int[] teamCounts) {
        int maxCount = 0;
        int dominantTeam = -1;
        for (int i = 0; i < teamCounts.length; i++) {
            if (teamCounts[i] > maxCount) {
                maxCount = teamCounts[i];
                dominantTeam = i;
            } else if (teamCounts[i] == maxCount) {
                dominantTeam = -1; // Tie, no dominant team
            }
        }
        return dominantTeam;
    }
    
    @Override
    public void render(Graphics2D g) {
        // render shadow
        int shadowOffsetX = 4;
        int shadowOffsetY = 8;
        Coordinate pixelLocation = getPixelLocation();
        pixelLocation.x += shadowOffsetX;
        pixelLocation.y += shadowOffsetY;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = pixelLocation.x - toRender.getWidth() / 2;
        int renderY = pixelLocation.y - toRender.getHeight() / 2;
        g.rotate(Math.toRadians(getRotation()), pixelLocation.x, pixelLocation.y);
        g.drawImage(toRender, renderX, renderY, null);
        g.setTransform(old);
        super.render(g);
        
        // Render capture ring
        renderCaptureRing(g);
    }
    
    private void renderCaptureRing(Graphics2D g) {
        Coordinate pixelLocation = getPixelLocation();
        Color ringColor = owningTeam == -1 ? Color.GRAY : RTSUnit.getColorFromTeam(owningTeam);
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(ringColor);
        g.fillOval(pixelLocation.x - captureRadius, pixelLocation.y - captureRadius, captureRadius * 2, captureRadius * 2);
        
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.drawOval(pixelLocation.x - captureRadius, pixelLocation.y - captureRadius, captureRadius * 2, captureRadius * 2);
        
        // Reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}

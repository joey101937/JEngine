
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class KeyBuilding extends GameObject2 {
    public static final Sprite mainSprite = new Sprite(RTSAssetManager.building);
    public static final Sprite shadowSprite = Sprite.generateShadowSprite(mainSprite.getImage(), .5); // new Sprite(RTSAssetManager.buildingShadow);
    private static final double CAPTURE_RATE = 0.01;
    private static final double CAPTURE_THRESHOLD = 1.0;
    public static final double VISUAL_SCALE = 1;
    
    static {
        shadowSprite.scaleTo(VISUAL_SCALE);
    }
    
    public int owningTeam = -1;
    public int captureRadius = 1000;
    public double captureProgress = 0;
    public SpawnLocation spawnLocation;
    
    public KeyBuilding(int x, int y) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(mainSprite);
        this.isSolid = true;
        this.spawnLocation = new SpawnLocation(new Coordinate(x, y).add(400, -400), 90 );
        this.setZLayer(5);
    }
    
    public KeyBuilding(int x, int y, int team) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(mainSprite);
        this.isSolid = true;
        owningTeam = team;
        this.spawnLocation = new SpawnLocation(new Coordinate(x, y).add(400, 0), 90 );
        this.setZLayer(5);
    }
    
    public KeyBuilding(int x, int y, int team, int spawnX, int spawnY, double rotation) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(mainSprite);
        this.isSolid = true;
        owningTeam = team;
        this.spawnLocation = new SpawnLocation(new Coordinate(x, y).add(spawnX, spawnY), rotation );
        this.setZLayer(5);
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
        int shadowOffsetX = -40;
        int shadowOffsetY = 40;
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
    }
    
    
    public static KeyBuilding getClosest(Coordinate target, int team) {
        KeyBuilding closest = null;
        for (GameObject2 go : RTSGame.game.getAllObjects()) {
            if (go instanceof KeyBuilding kb && kb.owningTeam == team) {
                if (closest == null || kb.distanceFrom(target) < closest.distanceFrom(target)) {
                    closest = kb;
                }
            }
        }
        return closest;
    }
    
    
    public static class SpawnLocation {
        public Coordinate topLeft;
        public double rotation;
        
        public SpawnLocation(Coordinate t, double r) {
            topLeft = t;
            rotation = r;
        }
    }
}

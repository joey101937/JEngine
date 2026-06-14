
package GameDemo.RTSDemo;

import Framework.Coordinate;
import GameDemo.RTSDemo.FogOfWar.SightBlocker;
import Framework.Game;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import GameDemo.RTSDemo.SceneryObjects.SceneryObject;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 *
 * @author guydu
 */
public class KeyBuilding extends GameObject2 implements SightBlocker, ReinforcementPoint, MinimapRenderable, SceneryObject {
    private static final long serialVersionUID = 1L;
    public static final Sprite mainSprite = new Sprite(RTSAssetManager.building);
    public static final Sprite shadowSprite = Sprite.generateShadowSprite(mainSprite.getImage(), .5);
    private static final double CAPTURE_RATE = 0.01;
    private static final double CAPTURE_THRESHOLD = 1.0;
    public static final double VISUAL_SCALE = .7;
    
    
    private static ArrayList<KeyBuilding> allKeyBuildings = new ArrayList<>();
    
    public static ArrayList<KeyBuilding> getKeybuildings (Game g) {
        var out = new ArrayList<KeyBuilding>();
        for(KeyBuilding kb : allKeyBuildings) {
            if(kb.getHostGame() == g) {
                out.add(kb);
            }
        }
        return out;
    }
    
    static {
        shadowSprite.scaleTo(VISUAL_SCALE);
        mainSprite.applyAlphaEdgeBlurSelf(1);
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
        allKeyBuildings.add(this);
    }

    public KeyBuilding(int x, int y, int team) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(mainSprite);
        this.isSolid = true;
        owningTeam = team;
        this.spawnLocation = new SpawnLocation(new Coordinate(x, y).add(400, 0), 90 );
        this.setZLayer(5);
        allKeyBuildings.add(this);
    }

    public KeyBuilding(int x, int y, int team, int spawnX, int spawnY, double rotation) {
        super(x, y);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(mainSprite);
        this.isSolid = true;
        owningTeam = team;
        this.spawnLocation = new SpawnLocation(new Coordinate(x, y).add(spawnX, spawnY), rotation );
        this.setZLayer(5);
        allKeyBuildings.add(this);
    }

    @Override
    public void onGameEnter() {
        super.onGameEnter();
        SceneryObject.register(this, getHostGame());
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
    }

    private transient Rectangle cachedBlockerBounds;

    @Override
    public Rectangle getBlockerBounds() {
        if (cachedBlockerBounds == null) {
            Coordinate center = getPixelLocation();
            int w = getWidth();
            int h = getHeight();
            cachedBlockerBounds = new Rectangle(center.x - w / 2, center.y - h / 2, w, h);
        }
        return cachedBlockerBounds;
    }

    @Override
    public void onPostDeserialization() {
        // Restore graphics after deserialization
        this.setGraphic(mainSprite);
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
    
    
    @Override
    public int getOwningTeam() {
        return owningTeam;
    }

    @Override
    public double getCaptureRadius() {
        return captureRadius;
    }

    @Override
    public SpawnLocation getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public java.awt.Color getMinimapColor() {
        return owningTeam >= 0 ? RTSUnit.getColorFromTeam(owningTeam) : java.awt.Color.GRAY;
    }
}

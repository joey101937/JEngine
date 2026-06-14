package GameDemo.RTSDemo.MapEditor;

import GameDemo.RTSDemo.KeyBuilding;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.SceneryObjects.BuildingGreen1;
import GameDemo.RTSDemo.SceneryObjects.GreenShippingContainer;
import GameDemo.RTSDemo.SceneryObjects.Hangar;
import GameDemo.RTSDemo.SceneryObjects.MetalShack;
import GameDemo.RTSDemo.SceneryObjects.OrangeWoodHouse;
import GameDemo.RTSDemo.SceneryObjects.PropaneTank;
import GameDemo.RTSDemo.SceneryObjects.Bush1;
import GameDemo.RTSDemo.SceneryObjects.Bush2;
import GameDemo.RTSDemo.SceneryObjects.Bush3;
import GameDemo.RTSDemo.SceneryObjects.Tree1;
import GameDemo.RTSDemo.SceneryObjects.Tree2;
import GameDemo.RTSDemo.SceneryObjects.Tree3;
import GameDemo.RTSDemo.Units.Apache;
import GameDemo.RTSDemo.Units.Bazookaman;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.Landmine;
import GameDemo.RTSDemo.Units.LightTank;
import GameDemo.RTSDemo.Units.Rifleman;
import GameDemo.RTSDemo.Units.TankUnit;
import GameDemo.RTSDemo.Units.TransportHelicopter;
import GameDemo.RTSDemo.Units.Truck;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines every placeable object type.  visualScale references each class's
 * VISUAL_SCALE constant directly (compile-time inlining — no loading side effects).
 * All images come from RTSAssetManager raw fields; we always apply visualScale
 * explicitly via resample() to avoid double-scaling.
 */
public enum EditorObjectType {

    // Units                                                                                             zLayer
    TANK           ("TankUnit",            "Tank",             Category.UNIT,     TankUnit.VISUAL_SCALE,            1),
    LIGHT_TANK     ("LightTank",           "Light Tank",       Category.UNIT,     LightTank.VISUAL_SCALE,           1),
    TRUCK          ("Truck",               "Truck",            Category.UNIT,     Truck.VISUAL_SCALE,               1),
    HELICOPTER     ("Hellicopter",         "Helicopter",       Category.UNIT,     Hellicopter.VISUAL_SCALE,        11),
    APACHE         ("Apache",              "Apache",           Category.UNIT,     Apache.VISUAL_SCALE,             11),
    TRANSPORT_HELI ("TransportHelicopter", "Transport Heli",   Category.UNIT,     TransportHelicopter.VISUAL_SCALE,11),
    RIFLEMAN       ("Rifleman",            "Rifleman",         Category.UNIT,     Rifleman.VISUAL_SCALE,            1),
    BAZOOKAMAN     ("Bazookaman",          "Bazookaman",       Category.UNIT,     Bazookaman.VISUAL_SCALE,          1),
    LANDMINE       ("Landmine",            "Landmine",         Category.UNIT,     Landmine.VISUAL_SCALE,           -1),

    // Buildings
    KEY_BUILDING   ("KeyBuilding",         "Key Building",     Category.BUILDING, KeyBuilding.VISUAL_SCALE,         5),

    // Scenery
    HANGAR              ("Hangar",                "Hangar",            Category.SCENERY, Hangar.VISUAL_SCALE,                -1),
    BUILDING_GREEN1     ("BuildingGreen1",        "Green Building",    Category.SCENERY, BuildingGreen1.VISUAL_SCALE,        -1),
    ORANGE_WOOD_HOUSE   ("OrangeWoodHouse",       "Orange House",      Category.SCENERY, OrangeWoodHouse.VISUAL_SCALE,       -1),
    PROPANE_TANK        ("PropaneTank",           "Propane Tank",      Category.SCENERY, PropaneTank.VISUAL_SCALE,           -1),
    GREEN_CONTAINER     ("GreenShippingContainer","Shipping Container",Category.SCENERY, GreenShippingContainer.VISUAL_SCALE,-1),
    METAL_SHACK         ("MetalShack",            "Metal Shack",       Category.SCENERY, MetalShack.VISUAL_SCALE,            -1),
    TREE1               ("Tree1",                 "Tree 1",            Category.SCENERY, Tree1.VISUAL_SCALE,                 -1),
    TREE2               ("Tree2",                 "Tree 2",            Category.SCENERY, Tree2.VISUAL_SCALE,                 -1),
    TREE3               ("Tree3",                 "Tree 3",            Category.SCENERY, Tree3.VISUAL_SCALE,                 -1),
    BUSH1               ("Bush1",                 "Bush 1",            Category.SCENERY, Bush1.VISUAL_SCALE,                 -1),
    BUSH2               ("Bush2",                 "Bush 2",            Category.SCENERY, Bush2.VISUAL_SCALE,                 -1),
    BUSH3               ("Bush3",                 "Bush 3",            Category.SCENERY, Bush3.VISUAL_SCALE,                 -1);

    public enum Category { UNIT, BUILDING, SCENERY }

    public final String className;
    public final String displayName;
    public final Category category;
    public final double visualScale;
    public final int defaultZLayer;

    EditorObjectType(String className, String displayName, Category category, double visualScale, int defaultZLayer) {
        this.className = className;
        this.displayName = displayName;
        this.category = category;
        this.visualScale = visualScale;
        this.defaultZLayer = defaultZLayer;
    }

    public boolean hasTeam()    { return category == Category.UNIT || category == Category.BUILDING; }
    public boolean hasHp()      { return category == Category.UNIT; }

    public static EditorObjectType fromClassName(String name) {
        for (EditorObjectType t : values()) {
            if (t.className.equals(name)) return t;
        }
        return null;
    }

    /** Primary (body) raw image from RTSAssetManager — always unscaled. */
    public BufferedImage getRawImage(int team) {
        switch (this) {
            case TANK:           return RTSAssetManager.getTankChasis(team);
            case LIGHT_TANK:     return RTSAssetManager.getLightTankHull(team);
            case TRUCK:          return RTSAssetManager.getTruckHull(team);
            case HELICOPTER:     return RTSAssetManager.getHellicopterBody(team);
            case APACHE:         return RTSAssetManager.getApacheBody(team);
            case TRANSPORT_HELI: return RTSAssetManager.getTransportHeliBody(team);
            case RIFLEMAN:       { BufferedImage[] f = RTSAssetManager.getRifleIdle(team); return f != null ? f[0] : null; }
            case BAZOOKAMAN:     { BufferedImage[] f = RTSAssetManager.getBazookaIdle(team); return f != null ? f[0] : null; }
            case LANDMINE:       return RTSAssetManager.getLandmine(team);
            case KEY_BUILDING:   return RTSAssetManager.building;
            case HANGAR:         return RTSAssetManager.hangarBase;
            case BUILDING_GREEN1:     return RTSAssetManager.buildingGreen1;
            case ORANGE_WOOD_HOUSE:   return RTSAssetManager.orangeWoodHouse;
            case PROPANE_TANK:        return RTSAssetManager.propaneTank;
            case GREEN_CONTAINER:     return RTSAssetManager.greenShippingContainer;
            case METAL_SHACK:         return RTSAssetManager.metalShack;
            case BUSH1:               return RTSAssetManager.bush1;
            case BUSH2:               return RTSAssetManager.bush2;
            case BUSH3:               return RTSAssetManager.bush3;
            case TREE1:               return RTSAssetManager.tree1;
            case TREE2:               return RTSAssetManager.tree2;
            case TREE3:               return RTSAssetManager.tree3;
            default: return null;
        }
    }

    /**
     * Primary body image scaled to in-game visual size.
     * Used for hit-testing and palette thumbnails (not for full composite rendering).
     */
    private final Map<Integer, BufferedImage> scaledCache = new HashMap<>();

    public BufferedImage getScaledImage(int team) {
        int key = hasTeam() ? team : 0;
        return scaledCache.computeIfAbsent(key, t -> {
            BufferedImage raw = getRawImage(hasTeam() ? t : 0);
            if (raw == null) return null;
            return resample(raw, visualScale);
        });
    }

    public BufferedImage getThumbnail(int maxSize) {
        BufferedImage img = getScaledImage(0);
        if (img == null) return null;
        double scale = Math.min((double) maxSize / img.getWidth(), (double) maxSize / img.getHeight());
        return resample(img, scale);
    }

    public static BufferedImage resample(BufferedImage src, double scale) {
        if (src == null || scale <= 0) return null;
        int w = Math.max(1, (int) (src.getWidth()  * scale));
        int h = Math.max(1, (int) (src.getHeight() * scale));
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}

package GameDemo.RTSDemo.MapEditor;

public class PlacedObject {
    // Default reinforcement spawn point for a fresh Key Building, mirroring the
    // KeyBuilding(x, y, team) constructor so maps saved before spawn editing load unchanged.
    public static final int    DEFAULT_SPAWN_OFFSET_X = 400;
    public static final int    DEFAULT_SPAWN_OFFSET_Y = 0;
    public static final double DEFAULT_SPAWN_ROTATION = 90;

    public String type;        // class name, e.g. "TankUnit", "Hangar"
    public int x, y;
    public double rotation;    // degrees, 0-359
    public int team;           // -1 to 5; ignored for pure scenery
    public int hpPercent;      // 1-100; ignored for scenery/buildings
    public int zLayer = Integer.MIN_VALUE; // MIN_VALUE = use class default (omitted from JSON)

    // Reinforcement spawn point, relative to the object centre; only used by spawn-point types (Key Buildings).
    public int    spawnOffsetX  = DEFAULT_SPAWN_OFFSET_X;
    public int    spawnOffsetY  = DEFAULT_SPAWN_OFFSET_Y;
    public double spawnRotation = DEFAULT_SPAWN_ROTATION;

    public PlacedObject() {}

    public PlacedObject(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.team = 0;
        this.hpPercent = 100;
        this.rotation = 0;
    }
}

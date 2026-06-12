package GameDemo.RTSDemo.MapEditor;

public class PlacedObject {
    public String type;        // class name, e.g. "TankUnit", "Hangar"
    public int x, y;
    public double rotation;    // degrees, 0-359
    public int team;           // -1 to 4; ignored for pure scenery
    public int hpPercent;      // 1-100; ignored for scenery/buildings

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

package GameDemo.RTSDemo;

import Framework.Coordinate;

public class SpawnLocation implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public Coordinate topLeft;
    public double rotation;

    public SpawnLocation(Coordinate t, double r) {
        topLeft = t;
        rotation = r;
    }
}

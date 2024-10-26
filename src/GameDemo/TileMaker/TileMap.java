package GameDemo.TileMaker;

import java.io.Serializable;

/**
 * This represents the state of the program. Can be saved to disk and reloaded.
 * @author guydu
 */
public class TileMap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public Tile[][] tileGrid;
    public String name = "Untitled";
    public String backgroundName = "DemoAssets/TankGame/grassTerrain_mega3.png";
}

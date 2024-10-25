package GameDemo.TileMaker;

import Framework.Game;

/**
 *
 * @author guydu
 */
public class TileMaker {
    
    public static int TILE_SIZE = 100;
    public static Tile[][] tilegrid;
    public static Game game;
    
    public static void main(String[] args) {
        game = new Game(TileAssetManager.tileBackground);
    }
}

package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.Game;
import Framework.Window;
import GameDemo.TileMaker.Tiles.GreenTile;

/**
 *
 * @author guydu
 */
public class TileMaker {
    
    public static int TILE_SIZE = 120;
    public static Tile[][] tileGrid;
    public static Game game;
    public static Tile baseTile = new GreenTile(0, 0);
    
    public static void main(String[] args) {
        game = new Game(TileAssetManager.tileBackground);
        int gridWidth = TileAssetManager.tileBackground.getWidth() / TILE_SIZE;
        int gridHeight = TileAssetManager.tileBackground.getHeight() / TILE_SIZE;
        tileGrid = new Tile[gridWidth][gridHeight];
        
        for(int y = 0; y < gridHeight; y++) {
            for(int x = 0; x < gridWidth; x++) {
                tileGrid[x][y] = baseTile.createCopy(x * TILE_SIZE, y * TILE_SIZE);
                tileGrid[x][y].setTranslucent(true);
                tileGrid[x][y].gridLocation = new Coordinate(x,y);
            }
        }
        game.addIndependentEffect(new TileRenderer());
        Window.initialize(game);
        game.setInputHandler(new TileInputHandler());
    }
}

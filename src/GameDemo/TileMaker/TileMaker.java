package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.Game;
import Framework.GraphicalAssets.Graphic;
import Framework.Main;
import Framework.UI_Elements.Examples.Minimap;
import Framework.Window;
import java.io.IOException;

/**
 *
 * @author guydu
 */
public class TileMaker {
    
    public static int TILE_SIZE = 120;
    public static TileMap tilemap = new TileMap();
    // public static Tile[][] tileGrid;
    public static Game game;
    public static Minimap minimap;
    public static TilePicker tilePicker;
    public static TaskBar taskBar;
    public static Tile baseTile;
    public static String tileDirectory = Main.assets+"DemoAssets/Tiles";
    // public static String MapTitle = "Untitled";
    
    public static void main(String[] args) throws IOException {
        Tileset.initialize(tileDirectory);
        baseTile = Tileset.library.get(0);
        game = new Game(Graphic.load(tilemap.backgroundName));
        game.getCamera().camSpeed = 10;
        minimap = new Minimap(game, new Coordinate(0,0));
        tilePicker = new TilePicker(game, new Coordinate(game.getWindowWidth()-300, 0));
        taskBar = new TaskBar(game, new Coordinate(0, game.getWindowHeight() - 46));
        int gridWidth = TileAssetManager.tileBackground.getWidth() / TILE_SIZE;
        int gridHeight = TileAssetManager.tileBackground.getHeight() / TILE_SIZE;
        System.out.println("grid width is " + gridWidth);
        System.out.println("grid height is " + gridHeight);
        tilemap.tileGrid = new Tile[gridWidth][gridHeight];
        
        for(int y = 0; y < gridHeight; y++) {
            for(int x = 0; x < gridWidth; x++) {
                tilemap.tileGrid[x][y] = baseTile.createCopy(x * TILE_SIZE, y * TILE_SIZE);
                tilemap.tileGrid[x][y].gridLocation = new Coordinate(x,y);
            }
        }
        game.addIndependentEffect(new TileRenderer());
        Window.initializeFullScreen(game);
        Window.addUIElement(minimap);
        Window.addUIElement(tilePicker);
        Window.addUIElement(taskBar);
        game.setInputHandler(new TileInputHandler());
    }
}

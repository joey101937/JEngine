package GameDemo.TileMaker.Tiles;

import Framework.GraphicalAssets.Sprite;
import GameDemo.TileMaker.Tile;
import GameDemo.TileMaker.TileAssetManager;

/**
 *
 * @author guydu
 */
public class GreenTile extends Tile {
    
    public GreenTile(int x, int y) {
        super(x, y);
        this.setMainSprite(new Sprite(TileAssetManager.tileGreen));
        this.setSelectedSprite(new Sprite(TileAssetManager.tileGreenSelected));
        this.setGraphic(getMainSprite());
    }
    
}

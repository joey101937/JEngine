package GameDemo.TileMaker.Tiles;

import Framework.GraphicalAssets.Sprite;
import GameDemo.TileMaker.Tile;
import GameDemo.TileMaker.TileAssetManager;

/**
 *
 * @author guydu
 */
public class BlueTile extends Tile {
    
    public BlueTile(int x, int y) {
        super(x, y);
        this.setMainSprite(new Sprite(TileAssetManager.tileBlue));
        this.setSelectedSprite(new Sprite(TileAssetManager.tileBlueSelected));
    }
    
}

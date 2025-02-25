package GameDemo.RTSDemo.Pathfinding;

import Framework.Game;
import Framework.IndependentEffect;
import Framework.Main;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class TileManager extends IndependentEffect {
    public static int updateInterval = Main.ticksPerSecond;

    public Game game;
    public TileMap tileMap;
    
    public TileManager(Game g) {
        game = g;
        tileMap = new TileMap(g.getWorldWidth(), g.getWorldHeight());
    }
    
    
    @Override
    public void render(Graphics2D g) {
        
    }

    @Override
    public void tick() {
        if(game.getGameTickNumber() % updateInterval != 0) return;
        System.out.println("calculating....");
        tileMap.updateOccupationMap(game);
    }
    
}

package GameDemo.RTSDemo.FogOfWar;

import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSGame;
import java.awt.Color;
import java.awt.Graphics2D;

public class FogOfWarEffect extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    public static boolean enabled = true;
    private transient Game game;

    private static final Color FOG_COLOR = new Color(0, 0, 0, 140);

    public FogOfWarEffect(Game g) {
        this.game = g;
    }

    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;
        if (RTSGame.fogOfWarGrid == null) {
            RTSGame.fogOfWarGrid = new FogOfWarGrid(g.getWorldWidth(), g.getWorldHeight());
        }
    }

    @Override
    public void tick() {
        game.setBackgroundClip(null);
        if (!enabled) return;
        if (game.getGameTickNumber() % 5 != 0) return;
        if (RTSGame.fogOfWarGrid != null) {
            RTSGame.fogOfWarGrid.update(game);
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!enabled || RTSGame.fogOfWarGrid == null) return;

        var fov = game.getCamera().getFieldOfView();
        int localTeam = ExternalCommunicator.localTeam;
        FogOfWarGrid grid = RTSGame.fogOfWarGrid;

        int txMin = Math.max(0, fov.x / FogOfWarGrid.TILE_SIZE);
        int tyMin = Math.max(0, fov.y / FogOfWarGrid.TILE_SIZE);
        int txMax = Math.min(grid.getGridW() - 1, (fov.x + fov.width)  / FogOfWarGrid.TILE_SIZE + 1);
        int tyMax = Math.min(grid.getGridH() - 1, (fov.y + fov.height) / FogOfWarGrid.TILE_SIZE + 1);

        Color saved = g.getColor();
        g.setColor(FOG_COLOR);
        for (int ty = tyMin; ty <= tyMax; ty++) {
            for (int tx = txMin; tx <= txMax; tx++) {
                if (!grid.isTileVisible(localTeam, tx * FogOfWarGrid.TILE_SIZE, ty * FogOfWarGrid.TILE_SIZE)) {
                    g.fillRect(
                            tx * FogOfWarGrid.TILE_SIZE,
                            ty * FogOfWarGrid.TILE_SIZE,
                            FogOfWarGrid.TILE_SIZE,
                            FogOfWarGrid.TILE_SIZE);
                }
            }
        }
        g.setColor(saved);
    }

    @Override
    public int getZLayer() {
        return 1000;
    }
}

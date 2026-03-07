package GameDemo.PushDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.Main;
import Framework.UtilityObjects.BlockObject;
import Framework.Window;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Entry point for the Push System demo.
 * Spawns a red player-controlled block and several pushable blocks.
 * A floating control panel lets the user configure push behavior in real-time.
 */
public class PushDemoGame {

    private static final int WORLD_W = 1600;
    private static final int WORLD_H = 900;

    public static void main(String[] args) {
        Main.ticksPerSecond = 60;
        Main.ignoreCollisionsForStillObjects = false;

        BufferedImage bg = buildBackground();
        Game game = new Game(bg);
        game.setName("Push Demo");
        game.worldBorder = 50;

        setupScene(game);

        game.setInputHandler(new PushDemoInputHandler());
        Game.scaleForResolution();
        Window.initialize(game);
        game.start();
        Window.setCurrentGame(game);

        new PushControlPanel(game);
    }

    private static void setupScene(Game game) {
        // Player block (red)
        PushBlock player = new PushBlock(new Coordinate(WORLD_W / 2, WORLD_H / 2));
        player.setName("Player");
        game.addObject(player);
        game.testObject = player;
        game.getCamera().setTarget(player);

        // Pushable blocks in a scattered arrangement
        int[][] positions = {
            {400, 300}, {600, 250}, {800, 350}, {1000, 300}, {1200, 280},
            {350, 550}, {550, 600}, {750, 500}, {950, 580}, {1150, 520},
            {450, 720}, {700, 680}, {900, 730}, {1100, 700},
            {300, 420}, {1250, 450},
        };

        for (int[] pos : positions) {
            PushableBlock block = new PushableBlock(new Coordinate(pos[0], pos[1]));
            game.addObject(block);
        }

        // Static wall pillars for interesting gameplay
        int[] wallPositions = {500, 300, 500, 600, 900, 200, 900, 700, 1100, 450};
        for (int i = 0; i < wallPositions.length; i += 2) {
            BlockObject wall = new BlockObject(new Coordinate(wallPositions[i], wallPositions[i + 1]), 20, 120);
            wall.setColor(new Color(80, 80, 80));
            wall.isSolid = true;
            wall.preventOverlap = true;
            game.addObject(wall);
        }
    }

    private static BufferedImage buildBackground() {
        BufferedImage img = new BufferedImage(WORLD_W, WORLD_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(28, 28, 36));
        g.fillRect(0, 0, WORLD_W, WORLD_H);

        // Draw subtle grid
        g.setColor(new Color(38, 38, 50));
        for (int x = 0; x < WORLD_W; x += 80) {
            g.drawLine(x, 0, x, WORLD_H);
        }
        for (int y = 0; y < WORLD_H; y += 80) {
            g.drawLine(0, y, WORLD_W, y);
        }
        g.dispose();
        return img;
    }
}

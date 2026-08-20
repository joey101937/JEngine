package GameDemo.Minotaur.Levels;

import GameDemo.Minotaur.MinotaurArt;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * The blueprint for one floor, authored in tiles rather than pixels.
 *
 * A level is described here as data and turned into objects by {@link MinotaurLevel}, which
 * keeps layout work honest: everything a floor contains is visible in one readable block of
 * chained calls in {@link LevelLibrary}, and the same description can be rebuilt from scratch
 * on a retry without any of it having been mutated by play.
 *
 * One tile is {@link MinotaurArt#TILE} pixels. Y grows downward, and {@code floorTile} is the
 * row the main ground surface sits on.
 *
 * @author Joseph
 */
public class LevelDef {

    public enum Spawn { IMP, BAT, SPITTER, SENTINEL, CURSED, BOSS, BARREL, HEALTH }

    public enum DecorKind { PILLAR, BRAZIER }

    public static class SpawnDef {
        public final Spawn kind;
        public final double x, y;
        public SpawnDef(Spawn kind, double x, double y) { this.kind = kind; this.x = x; this.y = y; }
    }

    public static class DecorDef {
        public final DecorKind kind;
        public final double x, groundY;
        public final int size;
        public final boolean foreground;
        public DecorDef(DecorKind kind, double x, double groundY, int size, boolean foreground) {
            this.kind = kind; this.x = x; this.groundY = groundY;
            this.size = size; this.foreground = foreground;
        }
    }

    public static class SignDef {
        public final double x, y;
        public final String text;
        public SignDef(double x, double y, String text) { this.x = x; this.y = y; this.text = text; }
    }

    public final String title, subtitle;
    public final MinotaurArt.Palette palette;
    public final int worldWidth, worldHeight, floorY;
    public final long seed;

    public double spawnX, spawnY;
    public double exitX;
    /** Set for the boss floor; suppresses the "enemies remain" objective wording. */
    public boolean isBossFloor = false;

    public final List<Rectangle> solids = new ArrayList<>();
    public final List<Rectangle> ledges = new ArrayList<>();
    public final List<Rectangle> spikes = new ArrayList<>();
    public final List<Rectangle> lava = new ArrayList<>();
    public final List<SpawnDef> spawns = new ArrayList<>();
    public final List<DecorDef> decor = new ArrayList<>();
    public final List<SignDef> signs = new ArrayList<>();

    public LevelDef(String title, String subtitle, MinotaurArt.Palette palette,
                    int widthTiles, int heightTiles, int floorTile, long seed) {
        this.title = title;
        this.subtitle = subtitle;
        this.palette = palette;
        this.worldWidth = widthTiles * MinotaurArt.TILE;
        this.worldHeight = heightTiles * MinotaurArt.TILE;
        this.floorY = floorTile * MinotaurArt.TILE;
        this.seed = seed;
        this.spawnX = 3 * MinotaurArt.TILE;
        this.spawnY = floorY - 120;
        this.exitX = worldWidth - 4 * MinotaurArt.TILE;
    }

    private static int t(double tiles) { return (int) Math.round(tiles * MinotaurArt.TILE); }

    /* ==================================================================
     *  geometry
     * ================================================================== */

    /** A run of main floor, from the floor line down to the bottom of the world. */
    public LevelDef ground(double xTile, double widthTiles) {
        solids.add(new Rectangle(t(xTile), floorY, t(widthTiles), worldHeight - floorY));
        return this;
    }

    /** A solid block: walls, raised terraces, ceilings. */
    public LevelDef block(double xTile, double yTile, double widthTiles, double heightTiles) {
        solids.add(new Rectangle(t(xTile), t(yTile), t(widthTiles), t(heightTiles)));
        return this;
    }

    /** A thin one-way ledge: landed on from above, jumped through from below. */
    public LevelDef ledge(double xTile, double yTile, double widthTiles) {
        ledges.add(new Rectangle(t(xTile), t(yTile), t(widthTiles), 24));
        return this;
    }

    /** A spike bed sitting on the floor line. */
    public LevelDef spikes(double xTile, double widthTiles) {
        return spikesAt(xTile, floorY / (double) MinotaurArt.TILE, widthTiles);
    }

    public LevelDef spikesAt(double xTile, double yTile, double widthTiles) {
        spikes.add(new Rectangle(t(xTile), t(yTile) - 42, t(widthTiles), 42));
        return this;
    }

    /** A pool of lava. Fills from the given row down to the bottom of the world. */
    public LevelDef lava(double xTile, double yTile, double widthTiles) {
        lava.add(new Rectangle(t(xTile), t(yTile), t(widthTiles), worldHeight - t(yTile)));
        return this;
    }

    /* ==================================================================
     *  contents
     * ================================================================== */

    public LevelDef enemy(Spawn kind, double xTile, double yTile) {
        spawns.add(new SpawnDef(kind, t(xTile), t(yTile)));
        return this;
    }

    /** Places an enemy standing on the main floor. */
    public LevelDef enemyOnFloor(Spawn kind, double xTile) {
        spawns.add(new SpawnDef(kind, t(xTile), floorY - 130));
        return this;
    }

    public LevelDef pillar(double xTile, double groundYTile, int heightTiles, boolean foreground) {
        decor.add(new DecorDef(DecorKind.PILLAR, t(xTile), t(groundYTile), heightTiles, foreground));
        return this;
    }

    public LevelDef brazier(double xTile, double groundYTile) {
        decor.add(new DecorDef(DecorKind.BRAZIER, t(xTile), t(groundYTile), 1, false));
        return this;
    }

    public LevelDef sign(double xTile, double yTile, String text) {
        signs.add(new SignDef(t(xTile), t(yTile), text));
        return this;
    }

    public LevelDef spawnAt(double xTile) {
        spawnX = t(xTile);
        spawnY = floorY - 120;
        return this;
    }

    public LevelDef exitAt(double xTile) {
        exitX = t(xTile);
        return this;
    }

    public LevelDef bossFloor() {
        isBossFloor = true;
        return this;
    }
}

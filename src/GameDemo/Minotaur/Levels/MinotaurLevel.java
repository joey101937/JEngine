package GameDemo.Minotaur.Levels;

import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.UtilityObjects.BlockObject;
import GameDemo.Minotaur.MinotaurArt;
import GameDemo.Minotaur.MinotaurAssets;
import GameDemo.Minotaur.MinotaurGame;
import GameDemo.Minotaur.Actors.Bat;
import GameDemo.Minotaur.Actors.Barrel;
import GameDemo.Minotaur.Actors.BossWarlord;
import GameDemo.Minotaur.Actors.CursedMinotaur;
import GameDemo.Minotaur.Actors.Enemy;
import GameDemo.Minotaur.Actors.Imp;
import GameDemo.Minotaur.Actors.Pickup;
import GameDemo.Minotaur.Actors.SSActor;
import GameDemo.Minotaur.Actors.Spitter;
import GameDemo.Minotaur.Actors.StoneSentinel;
import GameDemo.Minotaur.Effects.MinotaurHUD;
import GameDemo.Minotaur.Effects.Parallax;
import GameDemo.Minotaur.Effects.Particles;
import GameDemo.Minotaur.Terrain.Decor;
import GameDemo.Minotaur.Terrain.ExitPortal;
import GameDemo.Minotaur.Terrain.Hazard;

import GameDemo.Minotaur.Terrain.Platform;
import GameDemo.Minotaur.Terrain.Sign;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One playable floor, built from a {@link LevelDef}.
 *
 * The collision geometry lives here as plain rectangles rather than on the objects that draw
 * it, for two reasons: actors sweep against it every tick and a flat list is the cheapest
 * thing to sweep, and it means a wall's visual can be chunked into as many tiles as the art
 * needs without multiplying the collision work.
 *
 * The level also owns hit-stop — the few frames of full-world freeze that a solid hit buys.
 * It is decremented after the object pass so the freeze lasts exactly as many ticks as asked.
 *
 * @author Joseph
 */
public class MinotaurLevel extends Game {

    /** Widths the ground texture is chunked into. Two sizes so the tiling does not read as a loop. */
    private static final int[] CHUNK_WIDTHS = { 576, 528 };
    private static final int CHUNK_HEIGHT = 288;

    public final LevelDef def;
    public final MinotaurArt.Palette palette;

    /** Full blockers. Read every tick by every actor, never mutated after construction. */
    public final List<Rectangle> solids;
    /** Ledges that catch feet from above only. */
    public final List<Rectangle> oneWays;
    public final List<Hazard> hazards;

    public final double deathY;

    /** Ticks of world freeze remaining. Actors skip their logic while this is positive. */
    public volatile int hitStop = 0;

    public final Particles fxUnder, fxOver;
    public final MinotaurHUD hud;

    private ExitPortal portal;
    private BossWarlord boss;
    private final int initialEnemyCount;
    private volatile int cachedEnemiesRemaining;

    public MinotaurLevel(LevelDef def) {
        super(MinotaurArt.background(def.worldWidth, def.worldHeight, def.palette));
        this.def = def;
        this.palette = def.palette;
        this.deathY = def.worldHeight - 30;
        setName("Minotaur — " + def.title);
        worldBorder = 0;
        alwaysRenderFullBackground = false;

        List<Rectangle> s = new ArrayList<>(def.solids);
        // invisible bookends so a charge has something to slam into and nobody walks off the map
        s.add(new Rectangle(-160, -600, 160, def.worldHeight + 600));
        s.add(new Rectangle(def.worldWidth, -600, 160, def.worldHeight + 600));
        solids = Collections.unmodifiableList(s);
        oneWays = Collections.unmodifiableList(new ArrayList<>(def.ledges));

        List<Hazard> h = new ArrayList<>();
        for (Rectangle r : def.spikes) h.add(new Hazard(r, 11));
        for (Rectangle r : def.lava) h.add(new Hazard(r, 16));
        hazards = Collections.unmodifiableList(h);

        fxUnder = new Particles(false);
        fxOver = new Particles(true);
        hud = new MinotaurHUD(this);

        addIndependentEffect(new Parallax(this, palette, def.floorY + 40, def.seed));
        addIndependentEffect(fxUnder);
        addIndependentEffect(fxOver);
        addIndependentEffect(hud);

        buildTerrain();
        buildDecor();
        int enemies = buildSpawns();
        this.initialEnemyCount = enemies;
        this.cachedEnemiesRemaining = enemies;

        portal = new ExitPortal(def.exitX, def.floorY, palette);
        addObject(portal);
    }

    /* ==================================================================
     *  construction
     * ================================================================== */

    private void buildTerrain() {
        // Collision first, and from the merged lists rather than the authored ones, so the
        // invisible bookends at either end of the world are solid too. The visuals below are all
        // non-solid: a wall is drawn as a row of tiles but must behave as one surface.
        for (Rectangle r : solids) addTerrainBody(r, false);
        for (Rectangle r : oneWays) addTerrainBody(r, true);

        for (Rectangle r : def.solids) {
            addChunkedVisual(r);
        }
        for (Rectangle r : def.ledges) {
            addObject(new Platform(r, MinotaurAssets.ledge(alignToPixelGrid(r.width), palette), true));
        }
        for (Rectangle r : def.spikes) {
            addObject(new Decor(r.getCenterX(), r.y + r.height,
                    MinotaurAssets.spikes(alignToPixelGrid(r.width), palette), 6));
        }
        for (Rectangle r : def.lava) {
            int bodyH = Math.max(48, r.height - 24);
            int drawn = 0;
            while (drawn < r.width) {
                int w = Math.min(CHUNK_WIDTHS[0], r.width - drawn);
                int aligned = alignToPixelGrid(w);
                // body first, then the animated surface sitting on top of it
                addObject(new Decor(r.x + drawn + w / 2.0, r.y + 24 + bodyH,
                        MinotaurAssets.lavaBody(aligned, alignToPixelGrid(bodyH)), 5));
                addObject(new Decor(r.x + drawn + w / 2.0, r.y + 36,
                        MinotaurAssets.lava(aligned, palette), 6));
                drawn += w;
            }
        }
    }

    /**
     * The solid body of one piece of level geometry — invisible, immovable, and the only thing an
     * actor is actually stopped by. A {@code BlockObject} is already a solid box that maintains
     * its own hitbox, so terrain needs no class of its own.
     *
     * @param oneWay true for a ledge: catches an actor falling onto it, lets one rising from
     *               below pass straight through
     */
    private void addTerrainBody(Rectangle r, boolean oneWay) {
        BlockObject body = new BlockObject(
                new DCoordinate(r.getCenterX(), r.getCenterY()), r.width, r.height);
        body.setName(oneWay ? "Ledge" : "Terrain");
        body.isInvisible = true;
        body.setZLayer(0);
        if (oneWay) body.oneWayBlockDirection = new DCoordinate(0, 1);
        addObject(body);
    }

    /**
     * Draws one collision rectangle as a grid of tiles. Only the topmost row is capped with the
     * bright lip that signals "you can stand here", which is what tells a wall from a floor.
     */
    private void addChunkedVisual(Rectangle r) {
        int y = r.y;
        int rowIndex = 0;
        while (y < r.y + r.height) {
            int h = Math.min(CHUNK_HEIGHT, r.y + r.height - y);
            int x = r.x;
            int col = 0;
            while (x < r.x + r.width) {
                int w = Math.min(CHUNK_WIDTHS[col % CHUNK_WIDTHS.length], r.x + r.width - x);
                addObject(new Platform(new Rectangle(x, y, w, h),
                        MinotaurAssets.platform(alignToPixelGrid(w), alignToPixelGrid(h),
                                palette, rowIndex == 0),
                        false));
                x += w;
                col++;
            }
            y += h;
            rowIndex++;
        }
    }

    /** The art is authored at 1/U scale, so requested sizes are rounded to whole authored pixels. */
    private static int alignToPixelGrid(int size) {
        return Math.max(MinotaurArt.U * 4, (size / MinotaurArt.U) * MinotaurArt.U);
    }

    private void buildDecor() {
        for (LevelDef.DecorDef d : def.decor) {
            switch (d.kind) {
                case PILLAR:
                    addObject(new Decor(d.x, d.groundY, MinotaurAssets.pillar(d.size, palette),
                            d.foreground ? 40 : 2));
                    break;
                case BRAZIER:
                    addObject(new Decor(d.x, d.groundY, MinotaurAssets.brazier(palette), 7));
                    break;
            }
        }
        for (LevelDef.SignDef s : def.signs) {
            addObject(new Sign(new DCoordinate(s.x, s.y), s.text));
        }
    }

    private int buildSpawns() {
        int count = 0;
        for (LevelDef.SpawnDef s : def.spawns) {
            DCoordinate at = new DCoordinate(s.x, s.y);
            GameObject2 spawned;
            switch (s.kind) {
                case IMP:      spawned = new Imp(at); break;
                case BAT:      spawned = new Bat(at); break;
                case SPITTER:  spawned = new Spitter(at); break;
                case SENTINEL: spawned = new StoneSentinel(at); break;
                case CURSED:   spawned = new CursedMinotaur(at); break;
                case BOSS:     spawned = boss = new BossWarlord(at); break;
                case BARREL:   spawned = new Barrel(at); break;
                case HEALTH:   spawned = new Pickup(at, Pickup.Type.HEALTH, 40, 0, 0); break;
                default: continue;
            }
            // Spawn points are authored at a fixed height above the main floor, which puts a tall
            // actor inside any raised block standing there. Seat ground-bound actors on whatever
            // is actually beneath them; fliers keep the height they were placed at.
            if (spawned instanceof SSActor a && !(spawned instanceof Bat)) {
                a.seatOnSurfaceBelow(solids);
            }
            addObject(spawned);
            if (spawned instanceof Enemy e && e.countsTowardClear()) count++;
        }
        return count;
    }

    /* ==================================================================
     *  runtime
     * ================================================================== */

    @Override
    public synchronized void tick() {
        super.tick();
        if (hitStop > 0) hitStop--;
        cachedEnemiesRemaining = countEnemies();
    }

    private int countEnemies() {
        int n = 0;
        for (GameObject2 o : getAllObjects()) {
            if (o instanceof Enemy e && !e.isDead() && e.countsTowardClear()) n++;
        }
        return n;
    }

    /**
     * Live enemies gating the exit. Reports the authored count for the first few ticks, because
     * objects added during construction are not in the handler until its first pass completes —
     * without that guard the gate would open on frame one of every level.
     */
    public int enemiesRemaining() {
        if (getGameTickNumber() < 4) return initialEnemyCount;
        return cachedEnemiesRemaining;
    }

    /** Freezes the world for a few ticks. Requests do not stack; the longest wins. */
    public void hitStop(int ticks) {
        if (ticks > hitStop) hitStop = ticks;
    }

    public void shake(double magnitude, int ticks) {
        if (MinotaurGame.cameraAnchor != null) MinotaurGame.cameraAnchor.shake(magnitude, ticks);
    }

    public BossWarlord getBoss() { return boss; }

    public ExitPortal getPortal() { return portal; }

    public String getTitle() { return def.title; }

    public double groundY() { return def.floorY; }
}

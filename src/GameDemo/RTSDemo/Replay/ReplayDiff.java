package GameDemo.RTSDemo.Replay;

import Framework.Collision.CollisionResolver;
import Framework.Collision.Resolvers;
import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.Commands.MoveCommand;
import GameDemo.RTSDemo.MapEditor.MapData;
import GameDemo.RTSDemo.MapEditor.MapLoader;
import GameDemo.RTSDemo.MapEditor.MapSerializer;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSUnitIdHelper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runs a recorded match, or a bare map, forward without a window and prints a fingerprint of the
 * world at checkpoints.
 *
 * <h3>What it is for</h3>
 * The simulation is deterministic lockstep: the same start plus the same commands on the same
 * ticks must produce the same game. That makes it a regression test for anything that touches
 * movement, collision, pathing or combat — run it before a change, run it after, and diff the
 * output. Identical fingerprints mean the change was behaviour-neutral; differing ones show the
 * exact tick where the two runs parted and by how much.
 *
 * This is the check that a claim like "collision now rests flush against walls" cannot make on its
 * own: resting a pixel closer moves a unit, which moves the next fight, which moves everything
 * after it. Only replaying a real match shows how far that propagates.
 *
 * <h3>Usage</h3>
 * <pre>
 *   java GameDemo.RTSDemo.Replay.ReplayDiff &lt;file.replay | map.json&gt; [ticks] [checkpointEvery]
 *
 *   java ... ReplayDiff replays/1234.replay 3000        &gt; before.txt
 *   ...make the change...
 *   java ... ReplayDiff replays/1234.replay 3000        &gt; after.txt
 *   diff before.txt after.txt
 * </pre>
 *
 * A bare {@code .json} map runs with no command stream, which still exercises movement, pathing,
 * target acquisition and combat between whatever the map places in range of each other. A
 * {@code .replay} additionally replays the recorded orders.
 *
 * @author Joseph
 */
public class ReplayDiff {

    private static final int DEFAULT_TICKS = 2000;
    private static final int DEFAULT_CHECKPOINT = 250;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: ReplayDiff <file.replay | map.json> [ticks] [checkpointEvery] [axiscancel|clamped]");
            System.exit(2);
        }
        File file = new File(args[0]);
        int ticks = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_TICKS;
        int checkpointEvery = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_CHECKPOINT;

        if (!file.exists()) {
            System.err.println("no such file: " + file.getAbsolutePath());
            System.exit(2);
        }

        // Optional 4th argument overrides every unit's resolver, so two runs can be compared
        // without editing and rebuilding the engine between them.
        String resolverName = args.length > 3 ? args[3].toLowerCase() : null;

        long t0 = System.currentTimeMillis();
        RTSAssetManager.initialize();
        long tAssets = System.currentTimeMillis();

        MapData mapData;
        List<String> commands = new ArrayList<>();
        if (file.getName().endsWith(".replay")) {
            ReplayManager.ParsedReplay replay = ReplayManager.parseReplayFile(file);
            mapData = replay.mapData;
            commands = replay.commandStrings;
            RTSGame.desiredTPS = replay.tps;
            Main.setRandomSeed(replay.seed);
            System.out.println("# replay  " + file.getName()
                    + "  map=" + replay.mapName + "  tps=" + replay.tps
                    + "  commands=" + commands.size());
        } else {
            mapData = MapSerializer.fromJson(Files.readString(file.toPath()));
            System.out.println("# map     " + file.getName() + "  (no command stream)");
        }

        // Local playback: no network, and no replay gate since commands are injected directly.
        ExternalCommunicator.isMultiplayer = false;
        ReplayManager.isReplayMode = false;
        Main.ticksPerSecond = RTSGame.desiredTPS;

        BufferedImage bg = MapLoader.loadBackground(mapData.background);
        if (bg == null) bg = RTSAssetManager.grassBG;
        Game game = new Game(bg);
        long tGame = System.currentTimeMillis();
        RTSGame.setup(game);                 // headless-safe; setupUI is the part that needs a window
        long tSetup = System.currentTimeMillis();
        RTSUnitIdHelper.reset();             // IDs must regenerate from _1 to match recorded commands
        MapLoader.loadIntoGame(mapData, game);
        long tMap = System.currentTimeMillis();
        RTSGame.game = game;

        System.out.printf("# startup: assets %.1fs, game %.1fs, setup %.1fs, map %.1fs%n",
                (tAssets - t0) / 1000.0, (tGame - tAssets) / 1000.0,
                (tSetup - tGame) / 1000.0, (tMap - tSetup) / 1000.0);
        // Commands resolve their subject through Window.currentGame. Assigning the field directly
        // points them at this world without Window.setCurrentGame's canvas and pause handling,
        // none of which exists here.
        Window.currentGame = game;

        int injected = 0;
        for (String mp : commands) {
            var cmd = ReplayManager.parseCommand(mp);
            if (cmd != null) {
                RTSGame.commandHandler.addCommand(cmd, false);
                injected++;
            }
        }
        if (!commands.isEmpty()) {
            System.out.println("# injected " + injected + " of " + commands.size() + " commands");
        }

        // One tick so the handler publishes its first snapshot; getAllObjects reads that, so
        // before it runs the world looks empty.
        game.tick();

        if (resolverName != null) {
            CollisionResolver forced = switch (resolverName) {
                case "axiscancel" -> Resolvers.AXIS_CANCEL;
                case "clamped" -> Resolvers.CLAMPED;
                default -> throw new IllegalArgumentException("resolver must be axiscancel or clamped");
            };
            int n = 0;
            for (GameObject2 o : game.getAllObjects()) {
                o.collisionResolver = forced;
                n++;
            }
            System.out.println("# forced " + resolverName + " on " + n + " objects");
        }

        if (commands.isEmpty()) {
            int ordered = orderTeamsTogether(game);
            System.out.println("# no command stream: ordered " + ordered
                    + " units at the opposing team to produce a battle");
        }

        System.out.println("# ticks=" + ticks + " checkpointEvery=" + checkpointEvery);
        System.out.println("#");
        System.out.println("# tick  simTick   units   alive   fingerprint");

        for (int t = 1; t <= ticks; t++) {
            game.tick();
            if (t % checkpointEvery == 0) {
                System.out.println(fingerprint(game, t));
            }
        }

        System.out.println("#");
        System.out.println("# final state, one line per unit:");
        for (String line : unitLines(game)) {
            System.out.println(line);
        }
    }

    /**
     * Sends each team at the other team's centre of mass, turning any map into a battle.
     *
     * A map on its own is a poor regression subject: the teams start apart, nothing is ordered to
     * move, and the fingerprint is identical at every checkpoint whatever the collision code does.
     * Marching them together exercises movement, pathing, unit-on-unit collision and combat, which
     * is the behaviour worth diffing. Orders are issued as ordinary commands on a fixed tick, so a
     * run stays reproducible.
     *
     * @return how many units were ordered
     */
    private static int orderTeamsTogether(Game game) {
        List<RTSUnit> units = unitsOf(game);
        double[] sumX = new double[2], sumY = new double[2];
        int[] count = new int[2];
        for (RTSUnit u : units) {
            if (u.team < 0 || u.team > 1 || u.isRubble) continue;
            sumX[u.team] += u.getLocation().x;
            sumY[u.team] += u.getLocation().y;
            count[u.team]++;
        }
        if (count[0] == 0 || count[1] == 0) return 0;

        long executeTick = game.getGameTickNumber() + 10;
        int ordered = 0;
        for (RTSUnit u : units) {
            if (u.team < 0 || u.team > 1 || u.isRubble) continue;
            int enemy = 1 - u.team;
            Coordinate target = new Coordinate(
                    (int) (sumX[enemy] / count[enemy]), (int) (sumY[enemy] / count[enemy]));
            RTSGame.commandHandler.addCommand(
                    new MoveCommand(executeTick, u.ID, target, "0"), false);
            ordered++;
        }
        return ordered;
    }

    /**
     * One line summarising the whole world. Positions are quantised to a tenth of a pixel so that
     * the fingerprint tracks real divergence rather than floating point noise in the last bit,
     * while still catching a unit resting one pixel closer to a wall.
     */
    private static String fingerprint(Game game, int tick) {
        List<RTSUnit> units = unitsOf(game);
        long hash = 1469598103934665603L;                 // FNV-1a 64
        int alive = 0;
        for (RTSUnit u : units) {
            if (!u.isRubble) alive++;
            hash = mix(hash, u.ID);
            hash = mix(hash, Math.round(u.getLocation().x * 10));
            hash = mix(hash, Math.round(u.getLocation().y * 10));
            hash = mix(hash, u.currentHealth);
            hash = mix(hash, Math.round(u.getRotationRealTime() * 10));
            hash = mix(hash, u.isRubble ? 1 : 0);
        }
        return String.format("%6d %8d %7d %7d   %016x",
                tick, game.getGameTickNumber(), units.size(), alive, hash);
    }

    /** Per-unit detail, for reading where two runs actually diverged. */
    private static List<String> unitLines(Game game) {
        List<String> out = new ArrayList<>();
        for (RTSUnit u : unitsOf(game)) {
            out.add(String.format("%-28s team=%d  x=%9.1f y=%9.1f  hp=%5d  rot=%6.1f %s",
                    u.ID, u.team, u.getLocation().x, u.getLocation().y,
                    u.currentHealth, u.getRotationRealTime(), u.isRubble ? "RUBBLE" : ""));
        }
        return out;
    }

    /** Every unit in the world, in a stable order so the fingerprint does not depend on iteration. */
    private static List<RTSUnit> unitsOf(Game game) {
        List<RTSUnit> units = new ArrayList<>();
        for (GameObject2 o : game.getAllObjects()) {
            if (o instanceof RTSUnit u) units.add(u);
        }
        units.sort(Comparator.comparing(u -> u.ID));
        return units;
    }

    private static long mix(long hash, long value) {
        hash ^= value;
        return hash * 1099511628211L;
    }

    private static long mix(long hash, String value) {
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 1099511628211L;
        }
        return hash;
    }
}

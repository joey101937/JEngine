
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.SerializationManager;
import Framework.Window;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.RTSUnitIdHelper;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Determinism test that validates save/load functionality
 * Tests that:
 * 1. Games can be loaded from an existing save file (test_quicksave.dat)
 * 2. Loaded games progress identically across multiple runs
 * 3. Movement commands issued after loading produce consistent, deterministic results
 *
 * @author JEngine
 */
public class DeterminismTest6 {
    private static String unitToString (RTSUnit unit) {
        return "" + unit.getLocation() + "" + unit.getRotation() + "" + unit.currentHealth;
    }

    public static List<String> run (boolean show) {
        // Reset ID helper to ensure deterministic ID generation across test runs
        RTSUnitIdHelper.reset();

        // Create minimal game setup
        Game game = new Game(RTSAssetManager.grassBG);
        RTSGame.game = game;
        Main.setRandomSeed(10);
        Window.currentGame = game;
        RTSGame.setup(game);
        RTSGame.setupUI(game);

        // Immediately load from existing save file
        String saveFilePath = "saves/mp_resync_server.dat";
        System.out.println("Loading game from " + saveFilePath + "...");

        SerializationManager.loadGameState(game, saveFilePath);

        // Wait for load to complete (tick-delayed effects)
        for (int i = 0; i < 5; i++) {
            game.tick();
        }

        System.out.println("Load completed at tick " + game.getGameTickNumber());

        // Count units loaded
        long team0Count = game.getAllObjects().stream()
                .filter(obj -> obj instanceof RTSUnit)
                .map(obj -> (RTSUnit)obj)
                .filter(unit -> unit.team == 0)
                .count();
        long team1Count = game.getAllObjects().stream()
                .filter(obj -> obj instanceof RTSUnit)
                .map(obj -> (RTSUnit)obj)
                .filter(unit -> unit.team == 1)
                .count();
        System.out.println("Loaded " + team0Count + " team 0 units and " + team1Count + " team 1 units");

        // Issue movement commands after loading
        issueCommandsAfterLoad(game);

        System.out.println("Running game to completion...");

        // Continue until one team is eliminated or timeout
        boolean done = false;
        while (!done) {
            game.tick();

            boolean greenAlive = false;
            boolean redAlive = false;
            for (GameObject2 go : game.getAllObjects()) {
                if (go instanceof RTSUnit unit) {
                    if (unit.team == 0) {
                        greenAlive = true;
                    }
                    if (unit.team == 1) {
                        redAlive = true;
                    }
                }
            }
            done = !greenAlive || !redAlive || game.getGameTickNumber() > 10000;
        }

        if(show) Window.initialize(game);
        return game.getAllObjects().stream().filter(x -> x instanceof RTSUnit).map(x -> ((RTSUnit)x).toTransportString()).toList();
    }

    public static void main(String[] args) {
       RTSAssetManager.initialize();
       var res = run(true);
       System.out.println("res " + res);
    }

    /**
     * Gets all units sorted deterministically by team and then by name
     */
    private static List<RTSUnit> getSortedUnits(Game game) {
        return game.getAllObjects().stream()
                .filter(obj -> obj instanceof RTSUnit)
                .map(obj -> (RTSUnit)obj)
                .sorted(Comparator.comparingInt((RTSUnit u) -> u.team)
                        .thenComparing(u -> u.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets units for a specific team, sorted deterministically
     */
    private static List<RTSUnit> getSortedUnitsForTeam(Game game, int team) {
        return getSortedUnits(game).stream()
                .filter(unit -> unit.team == team)
                .collect(Collectors.toList());
    }

    /**
     * Movement commands issued after loading the save
     * These should produce deterministic results
     */
    public static void issueCommandsAfterLoad(Game game) {
        // Get all team 0 units (sorted deterministically)
        List<RTSUnit> team0Units = getSortedUnitsForTeam(game, 0);

        // Move units 5-10 towards an engagement point
        for (int i = 5; i < Math.min(10, team0Units.size()); i++) {
            team0Units.get(i).setDesiredLocation(new Coordinate(3200, 1600));
        }

        // Move units 10-15 to flanking position
        for (int i = 10; i < Math.min(15, team0Units.size()); i++) {
            team0Units.get(i).setDesiredLocation(new Coordinate(3000, 2000));
        }

        // Get all team 1 units (sorted deterministically)
        List<RTSUnit> team1Units = getSortedUnitsForTeam(game, 1);

        // Move units 5-10 towards an engagement point
        for (int i = 5; i < Math.min(10, team1Units.size()); i++) {
            team1Units.get(i).setDesiredLocation(new Coordinate(3200, 2400));
        }

        // Move units 10-15 to flanking position
        for (int i = 10; i < Math.min(15, team1Units.size()); i++) {
            team1Units.get(i).setDesiredLocation(new Coordinate(3000, 1000));
        }

        System.out.println("Issued post-load commands to " + team0Units.size() + " team 0 units and " + team1Units.size() + " team 1 units");
    }
}

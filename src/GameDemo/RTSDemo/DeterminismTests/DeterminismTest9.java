package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.SerializationManager;
import Framework.Window;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.RTSUnitIdHelper;
import GameDemo.RTSDemo.Units.TankUnit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Determinism test that validates save/load behavior with tank formations.
 * Tests the scenario where:
 * 1. A game is created with 16 tank units on each side (2x8 formation)
 * 2. Game state is saved
 * 3. Game state is loaded (simulating the multiplayer resume scenario)
 * 4. Movement commands are issued via ExternalCommunicator.interperateMessage
 * 5. Units battle and state is captured for determinism verification
 *
 * This test specifically targets potential desync issues that can occur
 * when loading game state and issuing commands in multiplayer scenarios.
 *
 * @author guydu
 */
public class DeterminismTest9 {
    private volatile List<String> capturedState = null;
    private final CountDownLatch completionLatch = new CountDownLatch(1);
    private final boolean show;
    private final boolean loadFromSave;
    private static final String SAVE_FILE_PATH = "saves/determinism_test9.dat";
    private static final int TARGET_TICK = 6500;

    public DeterminismTest9(boolean show, boolean loadFromSave) {
        this.show = show;
        this.loadFromSave = loadFromSave;
    }

    public List<String> run() {
        // Reset ID helper to ensure deterministic ID generation across test runs
        RTSUnitIdHelper.reset();

        // Create fresh game
        Game game = new Game(RTSAssetManager.grassBG);
        RTSGame.game = game;
        Main.setRandomSeed(10);

        System.out.println("Creating 16 tanks per side in 2x8 formation...");

        // Create 16 tanks for team 0 (top) - 2 rows of 8
        int tankSpacing = 120;
        int startX = 1000;

        // Top team - first row (facing downward - rotation 180)
        for (int i = 0; i < 8; i++) {
            TankUnit tank = new TankUnit(startX + (i * tankSpacing), 400, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }
        // Top team - second row (facing downward - rotation 180)
        for (int i = 0; i < 8; i++) {
            TankUnit tank = new TankUnit(startX + (i * tankSpacing), 520, 0);
            tank.setRotation(180);
            game.addObject(tank);
        }

        // Bottom team - first row (default rotation 0 - facing upward)
        for (int i = 0; i < 8; i++) {
            game.addObject(new TankUnit(startX + (i * tankSpacing), 2600, 1));
        }
        // Bottom team - second row (default rotation 0 - facing upward)
        for (int i = 0; i < 8; i++) {
            game.addObject(new TankUnit(startX + (i * tankSpacing), 2720, 1));
        }

        // Setup game and associate with window
        if (this.show) {
            if (Window.mainWindow == null) {
                Window.initialize(game);
            } else {
                Window.setCurrentGame(game);
            }
        } else {
            Window.currentGame = game;
        }
        RTSGame.setup(game);
        RTSGame.setupUI(game);

        // Tick once to initialize
        game.tick();

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
        System.out.println("Created " + team0Count + " team 0 units and " + team1Count + " team 1 units");

        // Always save the game state (for consistency)
        System.out.println("Saving game state to " + SAVE_FILE_PATH + "...");
        new File("saves").mkdirs();
        SerializationManager.saveGameState(game, SAVE_FILE_PATH);

        // Wait for save to complete (saveGameState uses tick-delayed effect)
        game.tick();
        System.out.println("Save completed at tick " + game.getGameTickNumber());

        // Conditionally load from save based on parameter
        if (loadFromSave) {
            System.out.println("loadFromSave=true: Clearing game and reloading from save...");
            // game.handler.getAllObjects().clear();
            RTSUnitIdHelper.reset();

            // Reload the game state
            SerializationManager.loadGameState(game, SAVE_FILE_PATH);

            // Wait for load to complete (tick-delayed effects)
            for (int i = 0; i < 5; i++) {
                game.tick();
            }

            System.out.println("Load completed at tick " + game.getGameTickNumber());

            // Verify units loaded correctly
            team0Count = game.getAllObjects().stream()
                    .filter(obj -> obj instanceof RTSUnit)
                    .map(obj -> (RTSUnit)obj)
                    .filter(unit -> unit.team == 0)
                    .count();
            team1Count = game.getAllObjects().stream()
                    .filter(obj -> obj instanceof RTSUnit)
                    .map(obj -> (RTSUnit)obj)
                    .filter(unit -> unit.team == 1)
                    .count();
            System.out.println("Loaded " + team0Count + " team 0 units and " + team1Count + " team 1 units");
        } else {
            System.out.println("loadFromSave=false: Continuing with existing units (no reload)");
        }

        // Issue movement commands via ExternalCommunicator (multiplayer command path)
        issueMovementCommands();

        // Set up handler to check for target tick
        game.setHandleSyncTick(g -> {
            long tickNumber = g.getGameTickNumber();
            if (tickNumber >= TARGET_TICK && capturedState == null) {
                // Capture state at target tick
                capturedState = g.getAllObjects().stream()
                    .filter(x -> x instanceof RTSUnit)
                    .map(x -> ((RTSUnit)x).toTransportString())
                    .toList();

                System.out.println("Captured state at tick " + tickNumber + " with " + capturedState.size() + " units");

                // Signal completion
                completionLatch.countDown();
            }
            if (tickNumber >= TARGET_TICK) {
                g.setPaused(true);
            }
        });

        System.out.println("Starting simulation" + (show ? " with rendering..." : " without rendering..."));

        // Run simulation
        if (this.show) {
            Window.initialize(game);
        } else {
            for (int i = 0; i < TARGET_TICK; i++) {
                game.tick();
            }
        }

        // Wait for completion (with timeout)
        try {
            boolean completed = completionLatch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("WARNING: Test timed out waiting for tick " + TARGET_TICK);
                if (capturedState == null) {
                    capturedState = game.getAllObjects().stream()
                        .filter(x -> x instanceof RTSUnit)
                        .map(x -> ((RTSUnit)x).toTransportString())
                        .toList();
                }
            }
            System.out.println("Simulation completed");
        } catch (InterruptedException e) {
            System.out.println("Test interrupted");
            if (capturedState == null) {
                capturedState = new ArrayList<>();
            }
        }

        return capturedState != null ? capturedState : new ArrayList<>();
    }

    /**
     * Issues movement commands via ExternalCommunicator.interperateMessage
     * This simulates the multiplayer command path where commands go through
     * the external communicator rather than direct method calls.
     */
    private static void issueMovementCommands() {
        System.out.println("Issuing movement commands via ExternalCommunicator...");

        // Get all units and issue commands to make them move PAST each other
        // Team 0 (top, starts at ~800-920) moves down past team 1 to y=2500
        // Team 1 (bottom, starts at ~2200-2320) moves up past team 0 to y=700
        // This ensures they will engage in battle as they pass through each other

        // Format: "m:UnitID,x,y,tick,commandHash"

        // Team 0 units - move them all to y=2500 (past team 1's starting position)
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_1,1000,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_2,1120,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_3,1240,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_4,1360,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_5,1480,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_6,1600,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_7,1720,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_8,1840,2500,1000,test");

        ExternalCommunicator.interperateMessage("m:TankUnit_T0_9,1000,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_10,1120,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_11,1240,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_12,1360,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_13,1480,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_14,1600,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_15,1720,2500,1000,test");
        ExternalCommunicator.interperateMessage("m:TankUnit_T0_16,1840,2500,1000,test");

        // Team 1 units - move them all to y=700 (past team 0's starting position)
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_1,1000,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_2,1120,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_3,1240,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_4,1360,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_5,1480,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_6,1600,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_7,1720,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_8,1840,700,1000,test1");

        ExternalCommunicator.interperateMessage("m:TankUnit_T1_9,1000,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_10,1120,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_11,1240,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_12,1360,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_13,1480,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_14,1600,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_15,1720,700,1000,test1");
        ExternalCommunicator.interperateMessage("m:TankUnit_T1_16,1840,700,1000,test1");

        System.out.println("Movement commands issued - units will move past each other and battle");
    }

    public static void main(String[] args) {
        RTSAssetManager.initialize();
        DeterminismTest9 test = new DeterminismTest9(true, true);
        var res = test.run();
        System.out.println("Result with " + res.size() + " units:");
        res.forEach(System.out::println);
    }

    /**
     * Static helper method for orchestrator
     */
    public static List<String> run(boolean show, boolean loadFromSave) {
        return new DeterminismTest9(show, loadFromSave).run();
    }
}

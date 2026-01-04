package GameDemo.RTSDemo.DeterminismTests;

import Framework.Game;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.RTSUnitIdHelper;
import GameDemo.RTSDemo.Units.Hellicopter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Determinism test that runs with actual rendering to verify that the render
 * function does not lead to non-determinism. This test sets up 4 helicopters
 * on each side (like the multiplayer server), submits a short command history,
 * and runs until tick 3000 where it captures state for comparison.
 *
 * @author guydu
 */
public class DeterminismTest7 {
    private volatile List<String> capturedState = null;
    private final CountDownLatch completionLatch = new CountDownLatch(1);
    private final boolean show;

    public DeterminismTest7(boolean show) {
        this.show = show;
    }

    public List<String> run() {
        // Reset ID helper to ensure deterministic ID generation across test runs
        RTSUnitIdHelper.reset();

        Game game = new Game(RTSAssetManager.grassBG);
        RTSGame.game = game;
        Main.setRandomSeed(10);
        
        // Set up 4 helicopters on each side, just like Server.java
        int lineSize = 4;
        int spacer = 160;
        for (int i = 0; i < lineSize; i++) {
            game.addObject(new Hellicopter(200 + (i * spacer), 200, 0));
        }

        for (int i = 0; i < lineSize; i++) {
            game.addObject(new Hellicopter(200 + (i * spacer), 3000, 1));
        }
        

        if(this.show) {
            if(Window.mainWindow == null) {
                Window.initialize(game);
            } else {
                Window.setCurrentGame(game);
            }
        } else {
            // no show just associate game
            Window.currentGame = game;
        }
        RTSGame.setup(game);
        RTSGame.setupUI(game);

        game.tick();

        // Submit a short command history
        populateCommands();

        // Set up handler to check for tick 3000
        game.setHandleSyncTick(g -> {
            long tickNumber = g.getGameTickNumber();
            if (tickNumber >= 3000 && capturedState == null) {
                // Capture state at tick 3000
                capturedState = game.getAllObjects().stream()
                    .filter(x -> x instanceof RTSUnit)
                    .map(x -> ((RTSUnit)x).toTransportString())
                    .toList();

                System.out.println("Captured state at tick " + tickNumber + " with " + capturedState.size() + " units");

                // Signal completion
                completionLatch.countDown();
               
            }
            if (tickNumber >= 3090) {
                 game.setPaused(true);
            }
        });

        System.out.println("Starting test with rendering...");

        // Initialize window - this starts the rendering and tick loop
        if(this.show) {
            Window.initialize(game);
        } else {
            for(int i = 0; i < 3000; i++) {
                game.tick();
            }
        }

        // Wait for completion (with timeout)
        try {
            boolean completed = completionLatch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("WARNING: Test timed out waiting for tick 3000");
                // Capture whatever state we have
                if (capturedState == null) {
                    capturedState = game.getAllObjects().stream()
                        .filter(x -> x instanceof RTSUnit)
                        .map(x -> ((RTSUnit)x).toTransportString())
                        .toList();
                }
            }
            System.out.println("completed");
        } catch (InterruptedException e) {
            System.out.println("Test interrupted");
            if (capturedState == null) {
                capturedState = new ArrayList<>();
            }
        }

        return capturedState != null ? capturedState : new ArrayList<>();
    }

    public static void main(String[] args) {
       RTSAssetManager.initialize();
       DeterminismTest7 test = new DeterminismTest7(true);
       var res = test.run();
       System.out.println("Result with " + res.size() + " units:");
       res.forEach(System.out::println);
    }

    /**
     * Static helper method for backwards compatibility
     */
    public static List<String> run(boolean show) {
        return new DeterminismTest7(show).run();
    }

    /**
     * Short command history
     */
    private static void populateCommands() {
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1048,181,1468,RYIWFYOMP");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1090,440,1568,9RU30PRLB");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1001,545,1648,A3HNKB56Q");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,824,385,1964,B2DHRCWNJ");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1124,402,2039,Z26D9A1TU");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1256,604,2100,SNI40A94F");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,897,621,2156,BVD0EAHBH");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,882,404,2231,91LJ7YS2K");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1615,403,2914,PBMNTU893");
//        ExternalCommunicator.interperateMessage("m:Hellicopter_T0_4,1633,555,2928,Z6IE9ADKS");
    }
}

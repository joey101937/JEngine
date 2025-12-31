package GameDemo.RTSDemo.DeterminismTests;

import GameDemo.RTSDemo.RTSAssetManager;
import java.util.HashMap;
import java.util.List;

/**
 * Orchestrator test that runs DeterminismTest6 multiple times
 * and verifies that save/load behavior is deterministic across runs
 *
 * @author JEngine
 */
public class DeterminismTestOrchestratorTest6 {
    public static void main(String[] args) {
        RTSAssetManager.initialize();

        System.out.println("Starting determinism test orchestrator for save/load functionality...");
        System.out.println("Running 6 simulations...");

        var results = List.of(
            DeterminismTest6.run(false),
            DeterminismTest6.run(false),
            DeterminismTest6.run(false),
            DeterminismTest6.run(false),
            DeterminismTest6.run(false),
            DeterminismTest6.run(false)
        );

        HashMap<String,Integer> resultsMap = new HashMap<>();
        for(List<String> res : results) {
            String signature = String.join(",", res);
            int current = resultsMap.getOrDefault(signature, 0);
            resultsMap.put(signature, current + 1);
        }

        System.out.println("\n=== Test Results ===");
        System.out.println("Unique result signatures: " + resultsMap.keySet().size());
        System.out.println("Result counts: " + resultsMap.values());

        if(resultsMap.keySet().size() != 1) {
            System.out.println("\n*** ALERT: Non-determinism detected in save/load! ***");
            System.out.println("Expected all runs to produce identical results, but got " + resultsMap.keySet().size() + " different outcomes.");
            System.exit(1);
        } else {
            System.out.println("\n*** SUCCESS: All simulations produced identical results! ***");
            System.out.println("Save/load functionality is deterministic.");
            System.exit(0);
        }
    }
}

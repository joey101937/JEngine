package GameDemo.RTSDemo.DeterminismTests;

import GameDemo.RTSDemo.RTSAssetManager;
import java.util.HashMap;
import java.util.List;

/**
 * Orchestrator for DeterminismTest7 that runs the test 4 times with rendering
 * and compares the results to verify determinism.
 *
 * @author guydu
 */
public class DeterminismTestOrchestratorTest7 {
    public static void main(String[] args) {
        RTSAssetManager.initialize();

        System.out.println("Running determinism test 4 times with rendering enabled...");
        System.out.println("Each run will simulate until tick 5000\n");

        var results = List.of(
            runWithIndex(1, true),
            runWithIndex(2, true),
            runWithIndex(3, true),
            runWithIndex(4, true)
        );

        System.out.println("\n=== ANALYZING RESULTS ===\n");

        HashMap<String, Integer> resultsMap = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            List<String> res = results.get(i);
            String signature = String.join(",", res);
            int current = resultsMap.getOrDefault(signature, 0);
            resultsMap.put(signature, current + 1);
            System.out.println("Run " + (i + 1) + " signature length: " + signature.length() + " characters");
        }

        System.out.println("\n=== VERDICT ===");
        System.out.println("Unique result signatures found: " + resultsMap.keySet().size());

        if (resultsMap.keySet().size() != 1) {
            System.out.println("❌ ALERT: Non-determinism detected!");
            System.out.println("Different results were produced across runs:");
            resultsMap.forEach((sig, count) ->
                System.out.println(sig + "  - Signature variant appeared " + count + " time(s)")
            );
        } else {
            System.out.println("✓ All runs produced identical results - determinism verified!");
            System.out.println("  All 4 runs matched with " + results.get(0).size() + " units in final state");
        }
    }

    private static List<String> runWithIndex(int index, boolean show) {
        System.out.println("Starting run " + index + "...");
        long startTime = System.currentTimeMillis();
        List<String> result = DeterminismTest7.run(show);
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Run " + index + " completed in " + (duration / 1000.0) + "s with " + result.size() + " units");
        return result;
    }
}

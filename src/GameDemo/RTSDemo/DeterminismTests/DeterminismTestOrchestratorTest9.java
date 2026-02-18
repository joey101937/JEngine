package GameDemo.RTSDemo.DeterminismTests;

import GameDemo.RTSDemo.RTSAssetManager;
import java.util.HashMap;
import java.util.List;

/**
 * Orchestrator for DeterminismTest9 that runs the test multiple times with and
 * without save loading to detect determinism issues.
 *
 * Test strategy:
 * 1. Run twice WITHOUT loading save (baseline - uses fresh units)
 * 2. Run twice WITH loading save (test scenario - loads from saved state)
 * 3. Verify both no-load runs match each other (baseline determinism)
 * 4. Verify both load runs match each other (load determinism)
 * 5. Compare no-load vs load to detect if loading causes different behavior
 *
 * Expected outcome:
 * - All 4 runs should produce identical results
 * - If no-load runs match but differ from load runs, there's a save/load bug
 * - If runs within same group differ, there's a general determinism bug
 *
 * @author guydu
 */
public class DeterminismTestOrchestratorTest9 {
    public static void main(String[] args) {
        RTSAssetManager.initialize();

        System.out.println("=== DETERMINISM TEST 9 ORCHESTRATOR ===");
        System.out.println("Testing save/load determinism with 16 tanks per side");
        System.out.println("Strategy: Compare fresh units vs loaded units\n");

        // Run without loading (baseline)
        System.out.println("=== PHASE 1: Running WITHOUT save loading (baseline) ===\n");
        List<String> noLoadRun1 = runWithIndex(1, true, false);
        List<String> noLoadRun2 = runWithIndex(2, true, false);

        // Run with loading (test scenario)
        System.out.println("\n=== PHASE 2: Running WITH save loading (test scenario) ===\n");
        List<String> loadRun1 = runWithIndex(3, true, true);
        List<String> loadRun2 = runWithIndex(4, true, true);

        // Analyze results
        System.out.println("\n=== ANALYZING RESULTS ===\n");

        String noLoadSig1 = String.join(",", noLoadRun1);
        String noLoadSig2 = String.join(",", noLoadRun2);
        String loadSig1 = String.join(",", loadRun1);
        String loadSig2 = String.join(",", loadRun2);

        boolean noLoadMatch = noLoadSig1.equals(noLoadSig2);
        boolean loadMatch = loadSig1.equals(loadSig2);
        boolean crossMatch = noLoadSig1.equals(loadSig1);

        System.out.println("No-load Run 1: " + noLoadRun1.size() + " units, signature length: " + noLoadSig1.length());
        System.out.println("No-load Run 2: " + noLoadRun2.size() + " units, signature length: " + noLoadSig2.length());
        System.out.println("Load Run 1:    " + loadRun1.size() + " units, signature length: " + loadSig1.length());
        System.out.println("Load Run 2:    " + loadRun2.size() + " units, signature length: " + loadSig2.length());

        System.out.println("\n=== VERDICT ===\n");

        System.out.println("1. No-load runs match each other: " + (noLoadMatch ? "✅ YES" : "❌ NO"));
        System.out.println("2. Load runs match each other:    " + (loadMatch ? "✅ YES" : "❌ NO"));
        System.out.println("3. No-load vs Load match:         " + (crossMatch ? "✅ YES" : "❌ NO"));

        if (noLoadMatch && loadMatch && crossMatch) {
            System.out.println("\n✅ PERFECT: All runs match - complete determinism verified!");
            System.out.println("   Save/load does not cause any desync issues.");
        } else if (noLoadMatch && loadMatch && !crossMatch) {
            System.out.println("\n⚠️ SAVE/LOAD BUG DETECTED!");
            System.out.println("   No-load runs are deterministic (match each other)");
            System.out.println("   Load runs are deterministic (match each other)");
            System.out.println("   BUT: Loading produces DIFFERENT results than not loading!");
            System.out.println("   This indicates a bug in save/load serialization.");
            showDetailedComparison(noLoadRun1, loadRun1, "No-load Run 1", "Load Run 1");
        } else if (!noLoadMatch || !loadMatch) {
            System.out.println("\n❌ GENERAL DETERMINISM BUG DETECTED!");
            if (!noLoadMatch) {
                System.out.println("   No-load runs don't match each other - baseline is non-deterministic!");
                showDetailedComparison(noLoadRun1, noLoadRun2, "No-load Run 1", "No-load Run 2");
            }
            if (!loadMatch) {
                System.out.println("   Load runs don't match each other - load is non-deterministic!");
                showDetailedComparison(loadRun1, loadRun2, "Load Run 1", "Load Run 2");
            }
        }

        System.exit(0);
    }

    private static List<String> runWithIndex(int index, boolean show, boolean loadFromSave) {
        System.out.println("=== Starting run " + index + " (loadFromSave=" + loadFromSave + ") ===");
        long startTime = System.currentTimeMillis();
        List<String> result = DeterminismTest9.run(show, loadFromSave);
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Run " + index + " completed in " + (duration / 1000.0) + "s with " + result.size() + " units");
        return result;
    }

    private static void showDetailedComparison(List<String> list1, List<String> list2, String name1, String name2) {
        System.out.println("\n=== DETAILED COMPARISON (" + name1 + " vs " + name2 + ") ===");
        System.out.println(name1 + " has " + list1.size() + " units");
        System.out.println(name2 + " has " + list2.size() + " units");

        int maxSize = Math.max(list1.size(), list2.size());
        int differences = 0;
        int maxDiffsToShow = 10;

        for (int i = 0; i < maxSize && differences < maxDiffsToShow; i++) {
            if (i >= list1.size()) {
                System.out.println("Unit " + i + " missing in " + name1);
                differences++;
            } else if (i >= list2.size()) {
                System.out.println("Unit " + i + " missing in " + name2);
                differences++;
            } else if (!list1.get(i).equals(list2.get(i))) {
                System.out.println("Unit " + i + " differs:");
                System.out.println("  " + name1 + ": " + list1.get(i));
                System.out.println("  " + name2 + ": " + list2.get(i));
                differences++;
            }
        }

        if (differences == 0) {
            System.out.println("First " + Math.min(maxDiffsToShow, maxSize) + " units are identical, differences occur later");
        } else if (differences >= maxDiffsToShow) {
            System.out.println("Showing first " + maxDiffsToShow + " differences (more may exist)");
        }
    }
}

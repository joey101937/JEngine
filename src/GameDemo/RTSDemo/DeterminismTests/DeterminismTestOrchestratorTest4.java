package GameDemo.RTSDemo.DeterminismTests;

import GameDemo.RTSDemo.RTSAssetManager;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author guydu
 */
public class DeterminismTestOrchestratorTest4 {
    public static void main(String[] args) {
        RTSAssetManager.initialize();
        var results = List.of(
            DeterminismTest4.run(false),
            DeterminismTest4.run(false),
            DeterminismTest4.run(false),
            DeterminismTest4.run(false),
            DeterminismTest4.run(false),
            DeterminismTest4.run(false)
        );
        
        HashMap<String,Integer> resultsMap = new HashMap<>();
        for(List<String> res : results) {
            String signature = String.join(",", res);
            int current = resultsMap.getOrDefault(signature, 0);
            resultsMap.put(signature, current + 1);
        }
        
        System.out.println("resultsMap" + resultsMap.keySet());
        
        System.out.println(resultsMap.values());
        
        if(resultsMap.values().size() != 1) {
            System.out.println("ALERT: nondeterminism found");
        } else {
            System.out.println("Looks good");
            System.exit(0);
        }
    }
}

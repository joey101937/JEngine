/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.DeterminismTests;

import GameDemo.RTSDemo.RTSAssetManager;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author guydu
 */
public class DeterminismTestOrchestratorTest3 {
    public static void main(String[] args) {
        RTSAssetManager.initialize();
        var results = List.of(
            DeterminismTest3.run(false),
            DeterminismTest3.run(false),
            DeterminismTest3.run(false),
            DeterminismTest3.run(false),
            DeterminismTest3.run(false),
            DeterminismTest3.run(false),
            DeterminismTest3.run(false)
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
        }
    }
}

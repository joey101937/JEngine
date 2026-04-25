/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework;

import java.util.HashMap;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author guydu
 */
public class RandomFromSeedTest {
    @Test
    public void intFromSeed () {
      var results = List.of(
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314),
              Main.generateRandomIntFromSeed(-50, 50, 12314)
              );
      
      for(int i = 0; i < results.size() - 1; i++) {
          assert results.get(i) == results.get(i + 1);
      }
    }
    
    public static void main(String[] args) {
     HashMap<Integer, Integer> map = new HashMap<>();
      for(int i =0; i < 100; i++) {
          int res = Main.generateRandomIntFromSeed(0, 100, (10000 + i) * 0x9e3779b97f4a7c15L);
          map.put(res, map.getOrDefault(res, 0) + 1);
          System.out.println("generated " + res);
      }
    }
    
    
        @Test
    public void intFromSeedNegative () {
      HashMap<Integer, Integer> map = new HashMap<>();
      for(int i =0; i < 100; i++) {
          int res = Main.generateRandomIntFromSeed(0, 100, 10000);
          map.put(res, map.getOrDefault(res, 0) + 1);
          System.out.println("generated " + res);
      }
      for(Integer val : map.values()) {
          assert val <= 1;
      }
    }
    
     @Test
    public void doubleFromSeed () {
      var results = List.of(
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314),
              Main.generateRandomDoubleFromSeed(-50, 50, 12314)
              );
      for(int i = 0; i < results.size() - 1; i++) {
          assert results.get(i).equals(results.get(i + 1));
      }
    }
}

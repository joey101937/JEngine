/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework;

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

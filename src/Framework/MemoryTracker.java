/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

/**
 *
 * @author Joseph
 */
public class MemoryTracker implements Runnable{

    private MemoryTracker(){}
    
    public static void start(){
    Thread t = new Thread(new MemoryTracker());
    t.start();
    }
    
    
    @Override
    public void run() {
        Runtime rt = Runtime.getRuntime();
        while (true) {
            Main.wait(100);
            System.out.println("free memory: " + rt.freeMemory() + " / "  + rt.maxMemory());
            
        }

    }
    
}

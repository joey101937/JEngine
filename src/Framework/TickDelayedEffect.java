/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework;

import java.util.function.Consumer;

/**
 *
 * @author guydu
 */
public class TickDelayedEffect {
    public Consumer consumer;
    public long targetTick;
    
    public TickDelayedEffect(long target, Consumer c) {
        this.consumer = c;
        this.targetTick = target;
    }
}

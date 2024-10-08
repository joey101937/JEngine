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

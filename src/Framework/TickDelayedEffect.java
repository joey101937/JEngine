package Framework;

import java.util.function.Consumer;

/**
 *
 * @author guydu
 */
public class TickDelayedEffect {
    public Consumer<Game> consumer;
    public long targetTick;
    
    public TickDelayedEffect(long target, Consumer<Game> c) {
        this.consumer = c;
        this.targetTick = target;
    }
}

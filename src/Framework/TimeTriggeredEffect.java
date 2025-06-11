package Framework;

import java.util.function.Consumer;

/**
 *
 * @author guydu
 */
public class TimeTriggeredEffect {
    public Consumer consumer;
    public long targetMillisecond;
    
    /**
     * Effect will run at the start of the first tick after the given time in Milliseconds
     * @param targetMs Millisecond to run on
     * @param c thing to run
     */
    public TimeTriggeredEffect(long targetMs, Consumer c) {
        this.consumer = c;
        this.targetMillisecond = targetMs;
    }
}

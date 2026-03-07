package Framework;

import java.util.function.Consumer;

/**
 * Represents an external push force applied to a GameObject2.
 * Pushes are combined with the object's intrinsic velocity each tick using
 * a weighted average based on each force's strength value.
 */
public class Push implements java.io.Serializable {

    /** Direction x component (raw, will be normalized with y to produce unit vector) */
    public double x;
    /** Direction y component (raw, will be normalized with x to produce unit vector) */
    public double y;
    /** Magnitude of this push in pixels per tick */
    public double speed;
    /** Weight of this push relative to intrinsic velocity and other pushes */
    public double strength;
    /** How many ticks this push lasts. -1 means infinite until manually removed */
    public int tickDuration;

    private int ticksApplied = 0;
    /** Optional function called each tick to evolve this push over time */
    private transient Consumer<Push> updater;

    public Push(double x, double y, double speed, double strength, int tickDuration) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.strength = strength;
        this.tickDuration = tickDuration;
    }

    public Push(double x, double y, double speed, double strength, int tickDuration, Consumer<Push> updater) {
        this(x, y, speed, strength, tickDuration);
        this.updater = updater;
    }

    public void setUpdater(Consumer<Push> updater) {
        this.updater = updater;
    }

    /**
     * @return true if this push has exceeded its tick duration
     */
    public boolean isExpired() {
        return tickDuration >= 0 && ticksApplied >= tickDuration;
    }

    /**
     * Called once per tick after movement is applied. Runs the updater if present
     * and increments the tick counter.
     */
    public void tick() {
        if (updater != null) updater.accept(this);
        ticksApplied++;
    }

    public int getTicksApplied() {
        return ticksApplied;
    }

    /**
     * Returns the movement vector this push contributes per tick,
     * before strength weighting is applied.
     * The direction (x, y) is normalized and scaled by speed.
     */
    public DCoordinate getMovementVector() {
        double len = Math.sqrt(x * x + y * y);
        if (len == 0 || speed == 0) return new DCoordinate(0, 0);
        return new DCoordinate((x / len) * speed, (y / len) * speed);
    }
}

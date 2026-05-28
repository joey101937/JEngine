package Framework;

import java.awt.Graphics2D;

/**
 * A visual effect that is attached to a specific {@link GameObject2} and rendered
 * immediately after that object (and before its above-SubObjects) by the Handler's
 * render loop. This guarantees correct draw-order layering without requiring a
 * magic z-layer number.
 * <p>
 * Hooks are fired by {@code RenderTask} — no changes to individual object render
 * methods are needed. Expired hooks are pruned automatically each frame.
 * The hook list on {@code GameObject2} is transient, so hooks do not survive
 * serialization and should only be used for cosmetic effects.
 * </p>
 */
public abstract class RenderHook {

    /**
     * Called each render frame while this hook is alive. The supplied
     * {@code Graphics2D} context is a fresh copy (via {@code g.create()}) so
     * state changes do not leak to other render calls.
     *
     * @param g    graphics context to draw into
     * @param host the {@link GameObject2} this hook is attached to
     */
    public abstract void render(Graphics2D g, GameObject2 host);

    /**
     * @return {@code true} when this hook should be removed and no longer rendered
     */
    public abstract boolean isExpired();
}

package GameDemo.RTSDemo;

/**
 * Which kind of enemy a unit favours when several are in range at once and no
 * preferred target has been given. NEUTRAL simply shoots whatever is nearest.
 *
 * @author Joseph
 */
public enum TargetPriority {
    NEUTRAL("Neutral"),
    INFANTRY("Infantry"),
    VEHICLES("Vehicles");

    public final String displayName;

    TargetPriority(String displayName) {
        this.displayName = displayName;
    }

    /** Next entry in the toggle cycle: Neutral -&gt; Infantry -&gt; Vehicles -&gt; Neutral. */
    public TargetPriority next() {
        return values()[(ordinal() + 1) % values().length];
    }
}

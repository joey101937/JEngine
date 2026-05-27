package GameDemo.RTSDemo.FogOfWar;

/** Units implementing this interface see through all SightBlockers (e.g. air units flying over buildings). */
public interface SightBlockerImmune {
    default boolean isSightBlockerImmune() { return true; }
}

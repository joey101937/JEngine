package Framework.Collision;

import Framework.GameObject2;

/**
 * Receives each object touched during collision resolution.
 *
 * Resolution and reporting are deliberately separate. A resolver that narrows down how far it may
 * move probes several candidate positions, so firing collision events from inside that search
 * would fire them repeatedly, and for positions the object never actually reaches. Routing them
 * through a sink instead lets a resolver report each contact exactly once, whatever it had to try
 * along the way.
 *
 * @author Joseph
 */
@FunctionalInterface
public interface ContactSink {

    /** Called once per distinct object touched, whether or not it blocked the movement. */
    void contact(GameObject2 other);
}

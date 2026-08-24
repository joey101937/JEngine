package Framework.Collision;

import Framework.DCoordinate;
import Framework.GameObject2;
import java.util.List;

/**
 * Decides how much of a proposed movement an object actually gets when something is in the way.
 *
 * <h3>Policy, not participation</h3>
 * A resolver answers <i>how a block resolves</i>. It does not decide <i>who blocks whom</i> —
 * that is {@code isSolid}, {@code preventOverlap} and {@code movesFreelyThrough} on the objects
 * themselves, and a resolver is expected to honour them. Keeping the two apart is what lets a
 * side-scroller and a top-down game share one collision system while resolving contact
 * differently.
 *
 * <h3>Implementations are shared and immutable</h3>
 * One instance serves any number of objects, so configuration belongs in the constructor and
 * nothing may be stored per-object between calls. See {@link Resolvers} for the stock set. A game
 * may supply its own without forking the framework — though needing one usually means the engine
 * is missing something that should be promoted rather than left in the game.
 *
 * <h3>Contacts and blocking are different sets</h3>
 * Every candidate the object touches must be passed to the {@link ContactSink}, including ones
 * that do not block: a trigger volume ({@code preventOverlap == false}) fires {@code onCollide}
 * precisely because the event set is larger than the blocker set. Report each contact exactly
 * once, however many candidate positions were probed to reach an answer.
 *
 * @author Joseph
 */
public interface CollisionResolver {

    /**
     * @param self              the object being moved; also the object contacts are reported against
     * @param proposedMovement  the movement it would make with nothing in the way
     * @param parts             {@code self} plus any subobjects taking part, already filtered by
     *                          {@link Framework.Main#ignoreSubobjectCollision}
     * @param candidates        nearby objects (and their subobjects) worth testing against
     * @param contacts          sink for everything touched, blocking or not
     * @return the movement {@code self} may actually make, and which axes were cut short
     */
    CollisionResult resolve(GameObject2 self, DCoordinate proposedMovement,
                            List<GameObject2> parts, List<GameObject2> candidates,
                            ContactSink contacts);

    /**
     * Whether this policy grants part of a blocked movement rather than none of it.
     *
     * Impassable terrain is still resolved as its own stage rather than as another thing the
     * resolver consults, so it asks this in order to narrow movement the same way — otherwise an
     * object would rest flush against a wall but stop a whole tick short of a cliff edge, which
     * reads as the same bug in a different place. Merging terrain into the resolver proper is
     * only forced once a resolver works with real surface normals, since two clamps along one ray
     * compose correctly but two <i>slides</i> do not.
     */
    default boolean clampsPartialMovement() {
        return false;
    }
}

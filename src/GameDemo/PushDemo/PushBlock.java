package GameDemo.PushDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Push;
import Framework.UtilityObjects.BlockObject;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Red player-controlled block. Moves with WASD and applies configurable
 * pushes to objects it collides with.
 */
public class PushBlock extends BlockObject {

    private final Map<String, Push> lastAppliedPushPerTarget = new HashMap<>();

    public PushBlock(Coordinate location) {
        super(location, 44, 44);
        setColor(Color.RED);
        isSolid = true;
        preventOverlap = true;
        collisionSliding = true;
        movementType = MovementType.SpeedRatio;
        baseSpeed = 5;
    }

    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (!fromMyTick) return;
        if (other instanceof PushBlock) return;

        // Only apply a new push once the previous one from this block has expired
        Push existing = lastAppliedPushPerTarget.get(other.ID);
        if (existing != null && !existing.isExpired()) return;

        DCoordinate myLoc = getLocationAsOfLastTick();
        DCoordinate otherLoc = other.getLocationAsOfLastTick();
        double dx = otherLoc.x - myLoc.x;
        double dy = otherLoc.y - myLoc.y;

        // Fallback direction if centers are exactly the same
        if (dx == 0 && dy == 0) dx = 1;

        Push push = PushControlPanel.createPush(dx, dy);
        other.addPush(push);
        lastAppliedPushPerTarget.put(other.ID, push);
    }
}

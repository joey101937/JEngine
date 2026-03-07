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
 * A solid block that can receive pushes from PushBlock.
 * When pushed into other PushableBlocks, it cascades a weaker push to them.
 */
public class PushableBlock extends BlockObject {

    private static final double CASCADE_FACTOR = 0.5;
    private static final double MIN_SPEED_TO_CASCADE = 0.4;

    private static final Color[] COLORS = {
        new Color(70, 130, 180),
        new Color(60, 179, 113),
        new Color(218, 165, 32),
        new Color(147, 112, 219),
        new Color(205, 133, 63),
    };
    private static int colorIndex = 0;

    private final Map<String, Push> lastCascadePerTarget = new HashMap<>();

    public PushableBlock(Coordinate location) {
        super(location, 44, 44);
        setColor(COLORS[colorIndex % COLORS.length]);
        colorIndex++;
        isSolid = true;
        preventOverlap = true;
        collisionSliding = true;
        movementType = MovementType.RawVelocity;
        intrinsicStrength = 0.05;
    }

    public PushableBlock(DCoordinate location) {
        this(location.toCoordinate());
    }

    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (!fromMyTick) return;
        if (other instanceof PushBlock) return;
        if (getPushes().isEmpty()) return;

        // Only cascade if already applying a cascade to this target
        Push existing = lastCascadePerTarget.get(other.ID);
        if (existing != null && !existing.isExpired()) return;

        // Use my current movement speed to size the cascade
        DCoordinate myMovement = getMovementNextTick();
        double mySpeed = Math.sqrt(myMovement.x * myMovement.x + myMovement.y * myMovement.y);
        if (mySpeed < MIN_SPEED_TO_CASCADE) return;

        double maxStrength = getPushes().stream()
                .filter(p -> !p.isExpired())
                .mapToDouble(p -> p.strength)
                .max().orElse(0);
        if (maxStrength == 0) return;

        DCoordinate myLoc = getLocationAsOfLastTick();
        DCoordinate otherLoc = other.getLocationAsOfLastTick();
        double dx = otherLoc.x - myLoc.x;
        double dy = otherLoc.y - myLoc.y;
        if (dx == 0 && dy == 0) dx = 1;

        Push cascade = new Push(dx, dy, mySpeed * CASCADE_FACTOR, maxStrength * CASCADE_FACTOR, 40,
                p -> { p.speed *= 0.96; p.strength *= 0.96; });
        other.addPush(cascade);
        lastCascadePerTarget.put(other.ID, cascade);
    }
}

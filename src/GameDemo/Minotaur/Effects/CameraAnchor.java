package GameDemo.Minotaur.Effects;

import Framework.DCoordinate;
import Framework.GameObject2;
import java.util.Random;

/**
 * What the camera actually follows. Nothing tracks the player directly, because a camera
 * bolted to a character that dashes and gets knocked around is unreadable.
 *
 * This is an invisible object that eases toward the player, leans ahead in the direction they
 * are moving so there is room to see what is coming, drops its gaze when they fall, and can be
 * kicked for screen shake. Because it is a real {@link GameObject2}, the engine's frame
 * interpolation smooths its motion between ticks for free.
 *
 * @author Joseph
 */
public class CameraAnchor extends GameObject2 {

    private GameObject2 target;
    private double lookAhead = 0;
    private double shakeMag = 0;
    private int shakeTicks = 0;
    private int shakeTotal = 1;
    private final Random rng = new Random(7);

    /** How far ahead of the player the camera leans at full run speed. */
    public double lookAheadDistance = 110;
    /** How high above the player the view sits, so more ground is visible than sky. */
    public double verticalBias = 44;
    /** 0..1 — how much of the remaining horizontal gap is closed each tick. */
    public double followEase = 0.14;
    /**
     * Vertical easing is much softer than horizontal, and only engages outside a dead zone.
     * A platformer character is airborne constantly; a camera that answers every hop turns the
     * whole screen into the jump arc.
     */
    public double verticalEase = 0.06;
    public double verticalDeadZone = 90;

    public CameraAnchor(GameObject2 target) {
        super(target.getLocation());
        this.target = target;
        setName("CameraAnchor");
        isInvisible = true;
        isSolid = false;
        preventOverlap = false;
        movementType = MovementType.RawVelocity;
    }

    public void setTarget(GameObject2 t) {
        this.target = t;
    }

    /** Kicks the view. Repeated kicks take the strongest rather than stacking into nausea. */
    public void shake(double magnitude, int ticks) {
        if (magnitude > shakeMag || shakeTicks <= 0) {
            shakeMag = Math.max(shakeMag, magnitude);
            shakeTicks = Math.max(shakeTicks, ticks);
            shakeTotal = Math.max(1, shakeTicks);
        }
    }

    @Override
    public void tick() {
        if (target == null) return;
        DCoordinate goal = target.getLocation();

        // Lean toward where the player is heading. The lead only builds once they are actually
        // running, and eases in slowly, so tapping a direction or bouncing off a wall does not
        // swing the view across the screen.
        double speedFraction = Math.abs(target.velocity.x) < 2.5 ? 0
                : Math.max(-1, Math.min(1, target.velocity.x / 6.5));
        double desiredLead = speedFraction * lookAheadDistance;
        lookAhead += (desiredLead - lookAhead) * 0.028;

        double wantX = goal.x + lookAhead;
        // falling fast pulls the view down so the landing is visible before it happens
        double fallBias = Math.max(0, Math.min(70, (target.velocity.y - 8) * 5));
        double wantY = goal.y - verticalBias + fallBias;

        DCoordinate here = getLocation();
        double nx = here.x + (wantX - here.x) * followEase;

        // hold the vertical still until the player leaves a band around the current framing
        double dy = wantY - here.y;
        double ny = here.y;
        if (Math.abs(dy) > verticalDeadZone) {
            double excess = dy - Math.signum(dy) * verticalDeadZone;
            ny += excess * verticalEase;
        }

        if (shakeTicks > 0) {
            double falloff = shakeTicks / (double) shakeTotal;
            double amp = shakeMag * falloff * falloff;
            nx += (rng.nextDouble() * 2 - 1) * amp;
            ny += (rng.nextDouble() * 2 - 1) * amp;
            if (--shakeTicks <= 0) shakeMag = 0;
        }

        setLocation(nx, ny);
    }

    /** Drops the anchor straight onto the target — used when a level starts or the player respawns. */
    public void snap() {
        if (target == null) return;
        lookAhead = 0;
        shakeTicks = 0;
        shakeMag = 0;
        DCoordinate goal = target.getLocation();
        setLocation(goal.x, goal.y - verticalBias);
        setLocationAsOfLastTick(getLocation());
    }

    @Override
    public int getWidth() { return 2; }

    @Override
    public int getHeight() { return 2; }
}

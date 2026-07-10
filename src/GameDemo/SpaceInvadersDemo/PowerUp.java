package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import java.awt.Color;

/**
 * A collectible that drifts down the screen. Flying into it with the player ship
 * grants its effect. Passive types (rapid fire, spread, shield) apply immediately
 * as timed buffs; NOVA instead stocks a charge the player unleashes on command.
 *
 * @author Joseph
 */
public class PowerUp extends GameObject2 {

    public enum Type {
        RAPID("RAPID FIRE",  new Color(120, 200, 255)),
        SPREAD("SPREAD SHOT", new Color(255, 210, 90)),
        SHIELD("SHIELD",     new Color(120, 255, 180)),
        NOVA("NOVA BOMB",    new Color(255, 120, 220));

        public final String label;
        public final Color color;
        Type(String label, Color color) { this.label = label; this.color = color; }
    }

    public final Type type;
    private boolean consumed = false;
    private double driftPhase = Math.random() * Math.PI * 2;
    private final double startX;

    public PowerUp(DCoordinate spawn, Type type) {
        super(spawn);
        this.type = type;
        this.startX = spawn.x;
        setGraphic(SIAssets.powerups[type.ordinal()]);
        isSolid = true;
        preventOverlap = false; // never blocks the ship, just registers the pickup
        movementType = MovementType.RawVelocity;
        velocity = new DCoordinate(0, 1.5);
        setZLayer(4);
        setName("PowerUp-" + type);
    }

    @Override
    public void tick() {
        super.tick();
        // gentle horizontal sway plus a pulsing glow so pickups read as "alive"
        driftPhase += 0.06;
        setXCoordinate(startX + Math.sin(driftPhase) * 26);
        setRenderScale(1.0 + 0.12 * Math.sin(getGameTickNumber() * 0.15));
    }

    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (consumed || !(other instanceof PlayerShip)) return;
        consumed = true;
        ((PlayerShip) other).grantPowerUp(type);
        RetroSfx.powerUp();
        Explosion.sparkle(getHostGame(), getPixelLocation(), type.color);
        destroy();
    }

    @Override
    public void onCollideWorldBorder(DCoordinate loc) {
        // fell past the bottom of the play area — gone
        if (loc.y >= getHostGame().getWorldHeight() - getHostGame().worldBorder - 1) {
            destroy();
        }
    }

    /** Convenience used by the wave driver / alien drops. */
    public static void drop(Framework.Game game, Coordinate at, Type type) {
        game.addObject(new PowerUp(new DCoordinate(at), type));
    }
}

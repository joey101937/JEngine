package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sprite;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;

/**
 *
 * @author guydu
 */
public class Truck extends RTSUnit {
    public static double VISUAL_SCALE = .4;
    public static double TRUCK_SPEED = RTSGame.tickAdjust(2.8);

    public static Sprite hullSprite = null;
    public static Sprite hullSpriteRed = null;
    public static Sprite hullSpriteDamaged = null;
    public static Sprite hullSpriteDamagedRed = null;
    public static Sprite hullSpriteDestroyed = null;
    public static Sprite hullShadow = null;
    public static Sprite wheelSprite = null;

    private static final int[][] WHEEL_OFFSETS = {{-24, -51}, {24, -51}};
    private static final double MAX_WHEEL_ANGLE = 30.0;
    private static final double WHEEL_TURN_RATE = RTSGame.tickAdjust(4);

    private double hullRotationSpeed = 0.0;
    private double wheelRotation = 0;
    private double previousHullRotation = 0;

    static {
        initGraphics();
    }

    public static void initGraphics() {
        hullSprite = new Sprite(RTSAssetManager.truckHull);
        hullSpriteRed = new Sprite(RTSAssetManager.truckHullRed);
        hullSpriteDamaged = new Sprite(RTSAssetManager.truckHullDamaged);
        hullSpriteDamagedRed = new Sprite(RTSAssetManager.truckHullDamagedRed);
        hullShadow = Sprite.generateShadowSprite(hullSprite.getImage(), .7);
        hullShadow.scaleTo(VISUAL_SCALE);
        hullSprite.applyAlphaEdgeBlurSelf(1);
        hullSpriteRed.applyAlphaEdgeBlurSelf(1);
        hullShadow.applyAlphaEdgeBlurSelf(2);

        wheelSprite = new Sprite(RTSAssetManager.truckWheel);
        wheelSprite.applyAlphaEdgeBlurSelf(1);
    }

    public final Sprite getHullSprite() {
        if (isRubble) {
            return hullSpriteDestroyed;
        }
        if (currentHealth > maxHealth * .33) {
            return team == 0 ? hullSprite : hullSpriteRed;
        }
        return team == 0 ? hullSpriteDamaged : hullSpriteDamagedRed;
    }

    public Truck(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(getHullSprite());
        this.isSolid = true;
        this.rotationSpeed = RTSGame.tickAdjust(2.5);
    }

    @Override
    protected double getEffectiveRotationSpeed(double desiredRotation) {
        double angle = Math.abs(desiredRotation);
        return rotationSpeed * Math.max(0.1, Math.min(1.0, angle / 30.0));
    }

    @Override
    protected void applyHullRotation(double desiredRotation) {
        double maxSpeed = getEffectiveRotationSpeed(desiredRotation);
        final double accel = RTSGame.tickAdjust(0.15);
        double targetSpeed = Math.abs(desiredRotation) < 0.01 ? 0.0 : Math.copySign(maxSpeed, desiredRotation);

        if (hullRotationSpeed < targetSpeed) {
            hullRotationSpeed = Math.min(hullRotationSpeed + accel, targetSpeed);
        } else if (hullRotationSpeed > targetSpeed) {
            hullRotationSpeed = Math.max(hullRotationSpeed - accel, targetSpeed);
        }

        if (Math.abs(desiredRotation) <= Math.abs(hullRotationSpeed)) {
            rotate(desiredRotation);
            hullRotationSpeed = 0;
        } else {
            rotate(hullRotationSpeed);
        }
    }

    @Override
    public void tick() {
        super.tick();
        setGraphic(getHullSprite());

        double hullRot = getRotation();
        double rotDelta = hullRot - previousHullRotation;
        while (rotDelta > 180) rotDelta -= 360;
        while (rotDelta < -180) rotDelta += 360;
        previousHullRotation = hullRot;

        double wheelOffset = (rotDelta / rotationSpeed) * MAX_WHEEL_ANGLE;
        wheelOffset = Math.max(-MAX_WHEEL_ANGLE, Math.min(MAX_WHEEL_ANGLE, wheelOffset));
        double targetWheelRot = hullRot + wheelOffset;

        double wheelDiff = targetWheelRot - wheelRotation;
        while (wheelDiff > 180) wheelDiff -= 360;
        while (wheelDiff < -180) wheelDiff += 360;
        double step = Math.min(Math.abs(wheelDiff), WHEEL_TURN_RATE);
        wheelRotation += wheelDiff > 0 ? step : -step;
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;
        if (isSolid) {
            drawShadow(g, hullShadow, 5, 9);
        }
        if (isSelected()) {
            drawRubbleProximityIndicators(g);
        }
        drawWheels(g);
        super.render(g);
    }

    private void drawWheels(Graphics2D g) {
        if (wheelSprite == null || wheelSprite.getCurrentVolatileImage() == null) return;
        VolatileImage img = wheelSprite.getCurrentVolatileImage();
        Coordinate truckLoc = getRenderLocation();
        double renderRot = getRenderRotation();

        for (int[] offset : WHEEL_OFFSETS) {
            DCoordinate wheelOffset = new DCoordinate(offset[0], offset[1]);
            wheelOffset.adjustForRotation(renderRot);
            int wx = truckLoc.x + (int) Math.round(wheelOffset.x);
            int wy = truckLoc.y + (int) Math.round(wheelOffset.y);

            Graphics2D wg = (Graphics2D) g.create();
            wg.rotate(Math.toRadians(wheelRotation), wx, wy);
            AffineTransform at = wg.getTransform();
            at.translate(wx, wy);
            at.scale(VISUAL_SCALE, VISUAL_SCALE);
            at.translate(-wx, -wy);
            wg.setTransform(at);
            wg.drawImage(img, wx - img.getWidth() / 2, wy - img.getHeight() / 2, null);
            wg.dispose();
        }
    }

    @Override
    public double getSpeed() {
        Coordinate nextWaypoint = getNextWaypoint();
        if (nextWaypoint == null || isCloseEnoughToDesired()) {
            return super.getSpeed();
        }
        double angle = Math.abs(rotationNeededToFace(nextWaypoint));
        double angleFactor = Math.max(0.0, Math.min(1.0, (90.0 - angle) / 60.0));
        return super.getSpeed() * angleFactor;
    }

    @Override
    public int getWidth() {
        return (int) (hullSprite.getWidth() * VISUAL_SCALE);
    }

    @Override
    public int getHeight() {
        return (int) (hullSprite.getHeight() * VISUAL_SCALE);
    }

    @Override
    public java.awt.image.BufferedImage getSelectionImage() {
        return GameDemo.RTSDemo.RTSAssetManager.landmineSelectionImage;
    }

}

package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.PathingLayer;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 * the hellicopter itself is invisible, instead we render the subobject to
 * simply movement and rotation
 *
 * @author guydu
 */
public class Hellicopter extends RTSUnit {

    public static final double VISUAL_SCALE = .34;

    public static volatile Sprite baseSprite = null;
    public static volatile Sprite baseSpriteRed = null;
    public static volatile Sprite destroyedSprite = null;
    public static volatile Sprite destroyedSpriteRed = null;
    public static volatile Sprite shadowSprite = null;
    public static volatile Sequence attackSequence = null;
    public static volatile Sequence attackSequenceRed = null;
    public static volatile Sprite bladesSprite = null;
    public static volatile Sprite bladesSpriteRed = null;

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (baseSprite != null) {
            return;
        }
        baseSprite = new Sprite(RTSAssetManager.hellicopter);
        baseSpriteRed = new Sprite(RTSAssetManager.hellicopterRed);
        destroyedSprite = new Sprite(RTSAssetManager.hellicopterDestroyed);
        destroyedSpriteRed = new Sprite(RTSAssetManager.hellicopterDestroyedRed);
        shadowSprite = Sprite.generateShadowSprite(RTSAssetManager.hellicopter, .7);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(4);
        attackSequence = new Sequence(RTSAssetManager.hellicopterAttack, "heliAttack");
        attackSequenceRed = new Sequence(RTSAssetManager.hellicopterAttackRed, "helliAttackRed");
        bladesSprite = new Sprite(RTSAssetManager.hellicopterBlades);
        bladesSpriteRed = new Sprite(RTSAssetManager.hellicopterBladesRed);
        // blades are rendered manually so need explicit scaling
        bladesSprite.scaleTo(VISUAL_SCALE);
        bladesSpriteRed.scaleTo(VISUAL_SCALE);
        baseSprite.applyAlphaEdgeBlurSelf(2);
        baseSpriteRed.applyAlphaEdgeBlurSelf(2);
        bladesSprite.applyAlphaEdgeBlurSelf(2);
        bladesSpriteRed.applyAlphaEdgeBlurSelf(2);
        attackSequence.setFrameDelay(40);
        attackSequenceRed.setFrameDelay(40);
    }

    public HellicopterTurret turret;
    public long lastFireTick = 0;
    public int attackInterval = RTSGame.desiredTPS * 2;
    public int elevation = 149;
    private double hullRotationSpeed = 0.0;
    public long scheduledDestructionAtTick = 0;
    public long pendingBulletSpawnAtTick = 0;
    public RTSUnit pendingBulletTarget = null;

    public Hellicopter(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        this.setZLayer(11);
        this.plane = 2;
        this.isSolid = true;
        this.setBaseSpeed(RTSGame.tickAdjust(4.5));
        this.rotationSpeed = RTSGame.tickAdjust(3.2);
        turret = new HellicopterTurret(new Coordinate(0, 0));
        this.addSubObject(turret);
        this.canAttackAir = true;
        this.pathingModifiers.put(PathingLayer.Type.water, 1.0);
        this.setRenderBrightness(1.25);
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
    }

    @Override
    protected double getEffectiveRotationSpeed(double desiredRotation) {
        double angle = Math.abs(desiredRotation);
        return rotationSpeed * Math.max(0.1, Math.min(1.0, angle / 30.0));
    }

    @Override
    protected void applyHullRotation(double desiredRotation) {
        double maxSpeed = getEffectiveRotationSpeed(desiredRotation);
        final double accel = RTSGame.tickAdjust(0.18);
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
    public double getSpeed() {
        Coordinate nextWaypoint = getNextWaypoint();
        if (nextWaypoint == null || isCloseEnoughToDesired()) {
            return super.getSpeed();
        }
        double angle = Math.abs(rotationNeededToFace(nextWaypoint));
        // stops at 90°; raise numerator/denominator together to start moving at wider angles
        // denominator controls how quickly speed ramps up as angle closes (lower = sharper ramp)
        double angleFactor = Math.max(0.0, Math.min(1.0, (160 - angle) / 160));
        return super.getSpeed() * angleFactor;
    }

    @Override
    public void onPostDeserialization() {
        // Restore graphics after deserialization
        if(isRubble) {
            this.setGraphic(team == 0 ? destroyedSprite : destroyedSpriteRed);
        } else {
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        }
        if (turret != null) {
            if(isRubble) {
                turret.setGraphic(team == 0 ? destroyedSprite : destroyedSpriteRed);
            } else {
                turret.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            }
        }
       
    }

    @Override
    public int getWidth() {
        if (isRubble) {
            return super.getWidth() / 2;
        } else {
            return super.getWidth()/2;
        }
    }

    @Override
    public int getHeight() {
        if (isRubble) {
            return super.getHeight() / 2;
        } else {
            return super.getHeight()/2;
        }
    }

    public void fireDelayed(RTSUnit targetUnit, int delay) {
        pendingBulletSpawnAtTick = getHostGame().getGameTickNumber() + delay;
        pendingBulletTarget = targetUnit;
    }

    @Override
    public void tick() {
        // Check for scheduled destruction
        if (scheduledDestructionAtTick > 0 && getHostGame().getGameTickNumber() >= scheduledDestructionAtTick) {
            new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
            this.destroy();
            return;
        }

        // Check for pending bullet spawn
        if (pendingBulletSpawnAtTick > 0 && getHostGame().getGameTickNumber() >= pendingBulletSpawnAtTick) {
            if (pendingBulletTarget != null && pendingBulletTarget.isAlive() && getHostGame() != null) {
                Coordinate center = getPixelLocation();
                Coordinate leftOffset = new Coordinate(-30, -30);
                Coordinate rightOffset = new Coordinate(30, -30);

                leftOffset.adjustForRotation(turret.getRotation());
                rightOffset.adjustForRotation(turret.getRotation());

                getHostGame().addObject(new HellicopterBullet(this, center.copy().add(leftOffset), pendingBulletTarget));
                getHostGame().addObject(new HellicopterBullet(this, center.copy().add(rightOffset), pendingBulletTarget));
                if (isOnScreen()) {
                    RTSSoundManager.get().play(
                        RTSSoundManager.HELICOPTER_ATTACK,
                        Main.generateRandomDoubleLocally(.65, .75),
                        Main.generateRandomIntLocally(0, 200));
                } else {
                    RTSSoundManager.get().play(
                        RTSSoundManager.HELICOPTER_ATTACK,
                        Main.generateRandomDoubleLocally(.55, .6),
                        Main.generateRandomIntLocally(0, 200));
                }
            }
            pendingBulletSpawnAtTick = 0;
            pendingBulletTarget = null;
        }

        if (isRubble && elevation > 1) {
            elevation -= 4.8;
            if (elevation < 1) {
                new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
                RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, .56, 0);
                this.baseSpeed = 0;
                this.plane = 0;
                this.setZLayer(1);
                this.isSolid = true;
                scheduledDestructionAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 8);
                return;
            }
            this.team = -1;
            this.velocity.y = -1;
            this.velocity.x = .1;
            this.turret.rotate(4);
            return;
        }
        if(!isRubble) {
            super.tick();
            RTSUnit preferred = getPreferredTargetIfInRange();
            currentTarget = preferred != null ? preferred : nearestEnemyInRange();
            boolean offCooldown = (getHostGame().getGameTickNumber() - lastFireTick) > attackInterval;
            if (currentTarget != null && offCooldown) {
                if (Math.abs(turret.rotationNeededToFace(currentTarget.getPixelLocation())) < 2) {
                    lastFireTick = getHostGame().getGameTickNumber();
                    Sequence attackAnimation = team == 0 ? attackSequence : attackSequenceRed;
                    turret.setGraphic(attackAnimation.copyMaintainSource());
                    System.out.println("" + this.ID + " located at " + this.getLocationAsOfLastTick()+"/" + this.getLocation()+ "/" + this.getPixelLocation() + " firing on tick " + getHostGame().getGameTickNumber() + " at " + currentTarget.ID + " located at " + currentTarget.getLocationAsOfLastTick()+"/" + currentTarget.getLocation()+ "/" + currentTarget.getPixelLocation());
                    fireDelayed(currentTarget, 10);
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        int shadowOffsetX = 5;
        int shadowOffsetY = Math.max(elevation, 9);
        Coordinate renderLocation = getRenderLocation();
        renderLocation.x += shadowOffsetX;
        renderLocation.y += shadowOffsetY;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = renderLocation.x - toRender.getWidth() / 2;
        int renderY = renderLocation.y - toRender.getHeight() / 2;
        g.rotate(Math.toRadians(turret.getRotation()), renderLocation.x, renderLocation.y);
        g.drawImage(toRender, renderX, renderY, null);
        g.setTransform(old);

        if (isSelected() && !isRubble) {
            drawHealthBar(g);
        }
        
         if(ExternalCommunicator.outOfSyncUnitIds.indexOf(ID) > -1) {
            g.setColor(Color.ORANGE);
            g.fillOval(getRenderLocation().x-getWidth()/2, getPixelLocation().y-getHeight()/2, getWidth()/2, getHeight()/2);
        }
    }

    @Override
    public void die() {
        if (isRubble) {
            return;
        }
        this.turret.setGraphic(team == 0 ? destroyedSprite : destroyedSpriteRed);
        this.isRubble = true;
        this.isSolid = false;
        this.setSelected(false);
    }

    public class HellicopterTurret extends SubObject {

        private int bobOffset = -1;
        private final double bobAmount = 8;

        public HellicopterTurret(Coordinate offset) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            this.setZLayer(11);
        }

        @Override
        public void tick() {
            if(bobOffset == -1) {
                // set bobOffset for first time so they are not all bobing the exact same
                bobOffset = Main.generateRandomIntFromSeed(0, 200, (long)(getHost().getLocationAsOfLastTick().x + getHost().getLocationAsOfLastTick().y));
            }
            if (!isRubble) {
                updateLocationForBob();
                updateRotation();
            }
        }

        @Override
        public void render(Graphics2D g) {
            super.render(g);
            if (!isRubble) {
                double bladesAngle = (System.currentTimeMillis() * 2160.0 / 1000.0) % 360;
                Coordinate renderLoc = getRenderLocation();
                VolatileImage bladesImg = (team == 0 ? bladesSprite : bladesSpriteRed).getCurrentVolatileImage();
                AffineTransform old = g.getTransform();
                g.rotate(Math.toRadians(bladesAngle), renderLoc.x, renderLoc.y);
                g.drawImage(bladesImg, renderLoc.x - bladesImg.getWidth() / 2, renderLoc.y - bladesImg.getHeight() / 2, null);
                g.setTransform(old);
            }
        }

        @Override
        public void onAnimationCycle() {
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        }

        private void updateLocationForBob() {
            long tick = getHostGame().getGameTickNumber() + bobOffset;

            double speedPerTick = RTSGame.tickAdjust(2.0);
            double cycleLength = 200.0 / speedPerTick; // up (100) + down (100)

            double cyclePos = (tick % (long) cycleLength) * speedPerTick;

            double bobPercent = cyclePos <= 100
                    ? cyclePos
                    : 200 - cyclePos;

            double newY = bobAmount * (bobPercent / 100.0);

            Coordinate newOffset = new Coordinate(0, (int) newY);
            newOffset.rotateAboutPoint(new Coordinate(0, 0), -getRotation());
            this.setOffset(newOffset);
        }

        private void updateRotation() {
            Hellicopter host = (Hellicopter) getHost();
            double desiredRotationAmount = this.getHost().getRotation() - getRotation();
            double maxRotation = RTSGame.tickAdjust(5);

            if (host.currentTarget != null) {
                desiredRotationAmount = rotationNeededToFace(host.currentTarget.getPixelLocation());
            }

            double rotationAmount = 0;

            if (Math.abs(desiredRotationAmount) > maxRotation) {
                rotationAmount = desiredRotationAmount > 0 ? maxRotation : -maxRotation;
            } else {
                rotationAmount = desiredRotationAmount;
            }

            this.rotate(rotationAmount);
        }

    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + HellicopterBullet.staticDamage + " (x2)   Interval: " + 2 + "s    Range: " + range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        return out;
    }

}

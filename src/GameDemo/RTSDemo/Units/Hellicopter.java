package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.PathingLayer;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.Effects.SmokePoofEffect;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * the hellicopter itself is invisible, instead we render the subobject to
 * simply movement and rotation
 *
 * @author guydu
 */
public class Hellicopter extends RTSUnit {

    public boolean sightBlockerImmune = true;

    @Override
    public boolean ignoresSightBlockers() { return sightBlockerImmune; }

    public static final double VISUAL_SCALE = .34;

    // Team-neutral sprites
    public static volatile Sprite deadSprite = null;
    public static volatile Sprite rubbleSprite = null;
    public static volatile Sequence deathShadowFadeout = null;
    public static volatile Sprite shadowSprite = null;

    // Team-colored maps
    private static final Map<Integer, Sprite>   bodySpriteMap   = new HashMap<>();
    private static final Map<Integer, Sprite>   bladesSpriteMap = new HashMap<>();
    private static final Map<Integer, Sequence> attackSeqMap    = new HashMap<>();

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (!bodySpriteMap.isEmpty()) return;

        shadowSprite = Sprite.generateShadowSprite(RTSAssetManager.hellicopter, .7);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(4);
        deadSprite = new Sprite(RTSAssetManager.hellicopterDead);
        rubbleSprite = new Sprite(RTSAssetManager.hellicopterRubble);
        deathShadowFadeout = Sequence.createFadeout(RTSAssetManager.hellicopterDeathShadow, 40);
        deathShadowFadeout.setSignature("chopperDeathShadow");
        deadSprite.applyAlphaEdgeBlurSelf(2);
        rubbleSprite.applyAlphaEdgeBlurSelf(2);

        for (int team : RTSGame.activeTeams) {
            Sprite body = new Sprite(RTSAssetManager.getHellicopterBody(team));
            body.applyAlphaEdgeBlurSelf(2);
            bodySpriteMap.put(team, body);

            Sprite blades = new Sprite(RTSAssetManager.getHellicopterBlades(team));
            blades.scaleTo(VISUAL_SCALE);
            blades.applyAlphaEdgeBlurSelf(2);
            bladesSpriteMap.put(team, blades);

            Sequence attack = new Sequence(RTSAssetManager.getHellicopterAttack(team), "heliAttack");
            attack.setFrameDelay(40);
            attackSeqMap.put(team, attack);
        }
    }

    public Sprite getBodySprite()    { return bodySpriteMap.get(team); }
    public Sprite getBladesSprite()  { return bladesSpriteMap.get(team); }
    public Sequence getAttackSequence() { return attackSeqMap.get(team); }

    public HellicopterTurret turret;
    public long lastFireTick = 0;
    public int attackInterval = RTSGame.desiredTPS * 2;
    public int elevation = 149;
    private double hullRotationSpeed = 0.0;
    public long scheduledDestructionAtTick = 0;
    public long fadeoutScheduledAtTick = 0;
    private boolean shadowPhase = false;
    private double deathVelocityX = 0;
    private double deathVelocityY = 0;
    private boolean deathExplosionSpawned = false;
    public long pendingBulletSpawnAtTick = 0;
    public RTSUnit pendingBulletTarget = null;

    public Hellicopter(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(getBodySprite());
        this.setZLayer(11);
        this.plane = 2;
        this.isSolid = true;
        this.setBaseSpeed(RTSGame.tickAdjust(4.5));
        this.rotationSpeed = RTSGame.tickAdjust(3.2);
        turret = new HellicopterTurret(new Coordinate(0, 0));
        this.addSubObject(turret);
        this.bodyRectWidthFraction  = 0.28;
        this.bodyRectHeightFraction = 0.28;
        this.canAttackAir = true;
        this.pathingModifiers.put(PathingLayer.Type.water, 1.0);
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
    public void setRotation(double r, boolean includeTurret) {
        super.setRotation(r);
        if(includeTurret) this.turret.setRotation(r);
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
        if (isRubble && elevation <= 0) {
            this.setGraphic(rubbleSprite);
            if (turret != null) {
                turret.setGraphic(rubbleSprite);
            }
        } else {
            this.setGraphic(getBodySprite());
            if (turret != null) {
                turret.setGraphic(getBodySprite());
            }
        }
    }

    @Override
    public void addRenderHook(Framework.RenderHook hook) {
        turret.addRenderHook(hook);
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
        // Check for scheduled destruction (after shadow fadeout completes)
        if (scheduledDestructionAtTick > 0 && getHostGame().getGameTickNumber() >= scheduledDestructionAtTick) {
            this.destroy();
            return;
        }

        // Check for scheduled shadow fadeout (replaces rubble after it sits on ground)
        if (fadeoutScheduledAtTick > 0 && getHostGame().getGameTickNumber() >= fadeoutScheduledAtTick) {
            new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
            this.setGraphic(deathShadowFadeout.copyMaintainSource());
            turret.isInvisible = true;
            this.isSolid = false;
            this.setZLayer(-10);
            shadowPhase = true;
            scheduledDestructionAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 3);
            fadeoutScheduledAtTick = 0;
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

        if (isRubble && elevation > 0) {
            elevation -= 3.0;
            if (!deathExplosionSpawned && elevation < 8) {
                OnceThroughSticker deathBlast = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
                deathBlast.setRenderScale(1.6);
                getHostGame().addIndependentEffect(new SmokePoofEffect(getHostGame(), getPixelLocation(), 26, getZLayer() + 1));
                RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, .56, 0);
                deathExplosionSpawned = true;
            }
            if (elevation <= 0) {
                this.baseSpeed = 0;
                this.velocity.x = 0;
                this.velocity.y = 0;
                this.plane = 0;
                this.setZLayer(1);
                this.isSolid = true;
                this.turret.setGraphic(rubbleSprite);
                this.setRenderScale(1.0);
                this.turret.setRenderScale(1.0);
                fadeoutScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 8);
                return;
            }
            double fallScale = 0.95 + 0.05 * (Math.max(0, elevation) / 149.0);
            this.setRenderScale(fallScale);
            turret.setRenderScale(fallScale);
            this.team = -1;
            if (Math.abs(deathVelocityX) < 0.1 && Math.abs(deathVelocityY) < 0.1) {
                this.velocity.x = 0;
                this.velocity.y = -1.2;
            } else {
                this.velocity.x = deathVelocityX;
                this.velocity.y = deathVelocityY;
            }
            this.turret.rotate(1.5);
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
                    turret.setGraphic(getAttackSequence().copyMaintainSource());
                    System.out.println("" + this.ID + " located at " + this.getLocationAsOfLastTick()+"/" + this.getLocation()+ "/" + this.getPixelLocation() + " firing on tick " + getHostGame().getGameTickNumber() + " at " + currentTarget.ID + " located at " + currentTarget.getLocationAsOfLastTick()+"/" + currentTarget.getLocation()+ "/" + currentTarget.getPixelLocation());
                    fireDelayed(currentTarget, 10);
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;

        if (shadowPhase) {
            super.render(g);
            return;
        }

        if (isRubble && elevation <= 0) {
            return;
        }

        int shadowOffsetX = 5;
        int shadowOffsetY = Math.max(elevation, 9);
        DCoordinate renderLocation = getRenderLocation();
        double centerX = renderLocation.x + shadowOffsetX;
        double centerY = renderLocation.y + shadowOffsetY;
        double shadowScale = isRubble ? 0.95 + 0.05 * (Math.max(0, elevation) / 149.0) : 1.0;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        double drawWidth = toRender.getWidth() * shadowScale;
        double drawHeight = toRender.getHeight() * shadowScale;
        double renderX = centerX - drawWidth / 2.0;
        double renderY = centerY - drawHeight / 2.0;
        g.rotate(Math.toRadians(turret.getRotation()), centerX, centerY);
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(renderX, renderY);
        shadowTransform.scale(shadowScale, shadowScale);
        g.drawImage(toRender, shadowTransform, null);
        g.setTransform(old);

        if (isSelected() && !isRubble) {
            drawHealthBar(g);
        }

        if (ExternalCommunicator.outOfSyncUnitIds.indexOf(ID) > -1) {
            g.setColor(Color.ORANGE);
            g.fillOval(getRenderLocation().toCoordinate().x - getWidth() / 2, getPixelLocation().y - getHeight() / 2, getWidth() / 2, getHeight() / 2);
        }
    }

    @Override
    public void die() {
        if (isRubble) {
            return;
        }
        deathVelocityX = this.velocity.x;
        deathVelocityY = this.velocity.y;
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
            this.setGraphic(getBodySprite());
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
            if (!((RTSUnit) getHost()).shouldRender()) return;
            super.render(g);
            if (!isRubble) {
                double bladesAngle = (System.currentTimeMillis() * 2160.0 / 1000.0) % 360;
                DCoordinate renderLoc = getRenderLocation();
                VolatileImage bladesImg = getBladesSprite().getCurrentVolatileImage();
                AffineTransform old = g.getTransform();
                g.rotate(Math.toRadians(bladesAngle), renderLoc.x, renderLoc.y);
                g.drawImage(bladesImg, AffineTransform.getTranslateInstance(renderLoc.x - bladesImg.getWidth() / 2.0, renderLoc.y - bladesImg.getHeight() / 2.0), null);
                g.setTransform(old);
            }
        }

        @Override
        public void onAnimationCycle() {
            if (!isRubble) {
                this.setGraphic(getBodySprite());
            }
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
    public java.awt.image.BufferedImage getSelectionImage() {
        return RTSAssetManager.hellicopterSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + HellicopterBullet.staticDamage + " (x2)   Interval: " + 2 + "s    Range: " + getRange());
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        return out;
    }

}

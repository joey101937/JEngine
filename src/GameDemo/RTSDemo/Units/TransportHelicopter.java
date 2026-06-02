package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.PathingLayer;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.FlyButton;
import GameDemo.RTSDemo.Buttons.LandButton;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import GameDemo.RTSDemo.ReinforcementPoint;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.SpawnLocation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

public class TransportHelicopter extends RTSUnit implements ReinforcementPoint {

    public boolean sightBlockerImmune = true;

    @Override
    public boolean ignoresSightBlockers() { return sightBlockerImmune; }

    public static final double VISUAL_SCALE = .44;

    public static volatile Sprite baseSprite = null;
    public static volatile Sprite baseSpriteRed = null;
    public static volatile Sprite deadSprite = null;
    public static volatile Sprite rubbleSprite = null;
    public static volatile Sequence deathShadowFadeout = null;
    public static volatile Sprite shadowSprite = null;
    public static volatile Sprite bladesSprite = null;
    public static volatile Sprite bladesSpriteRed = null;

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (baseSprite != null) {
            return;
        }
        baseSprite = new Sprite(RTSAssetManager.transportHeli);
        baseSpriteRed = new Sprite(RTSAssetManager.transportHeliRed);
        shadowSprite = Sprite.generateShadowSprite(RTSAssetManager.transportHeli, .7);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(4);
        bladesSprite = new Sprite(RTSAssetManager.hellicopterBlades);
        bladesSpriteRed = new Sprite(RTSAssetManager.hellicopterBladesRed);
        bladesSprite.scaleTo(VISUAL_SCALE);
        bladesSpriteRed.scaleTo(VISUAL_SCALE);
        deadSprite = new Sprite(RTSAssetManager.chopperDead);
        rubbleSprite = new Sprite(RTSAssetManager.chopperRubble);
        deathShadowFadeout = Sequence.createFadeout(RTSAssetManager.chopperDeathShadow, 40);
        deathShadowFadeout.setSignature("chopperDeathShadow");
        deadSprite.applyAlphaEdgeBlurSelf(2);
        rubbleSprite.applyAlphaEdgeBlurSelf(2);
        baseSprite.applyAlphaEdgeBlurSelf(2);
        baseSpriteRed.applyAlphaEdgeBlurSelf(2);
        bladesSprite.applyAlphaEdgeBlurSelf(2);
        bladesSpriteRed.applyAlphaEdgeBlurSelf(2);
    }

    public TransportHeliTurret turret;
    public int elevation = 149;
    private double hullRotationSpeed = 0.0;
    public long scheduledDestructionAtTick = 0;
    public long fadeoutScheduledAtTick = 0;
    private boolean shadowPhase = false;
    private double deathVelocityX = 0;
    private double deathVelocityY = 0;
    private boolean deathExplosionSpawned = false;

    // land/fly state
    public boolean isLanded = false;
    public boolean isLanding = false;
    public boolean isTakingOff = false;
    private long landingStartTick = 0;
    private long takeoffStartTick = 0;

    public TransportHelicopter(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        this.setZLayer(11);
        this.plane = 2;
        this.isSolid = true;
        this.setBaseSpeed(RTSGame.tickAdjust(4.0));
        this.rotationSpeed = RTSGame.tickAdjust(3.2);
        turret = new TransportHeliTurret(new Coordinate(0, 0));
        this.addSubObject(turret);
        this.bodyRectWidthFraction  = 0.28;
        this.bodyRectHeightFraction = 0.28;
        this.pathingModifiers.put(PathingLayer.Type.water, 1.0);
        this.setRenderBrightness(1.25);
        addButton(new LandButton(this));
        addButton(new FlyButton(this));
    }

    // -- terrain and zone checks --

    public boolean isOnGroundTerrain() {
        if (getHostGame() == null) return false;
        PathingLayer pl = getHostGame().getPathingLayer();
        if (pl == null) return true;
        PathingLayer.Type type = pl.getTypeAt(getPixelLocation());
        return type != PathingLayer.Type.water && type != PathingLayer.Type.impass;
    }

    public boolean isLandingZoneClear() {
        if (getHostGame() == null) return true;
        Coordinate pos = getPixelLocation();
        double radius = Math.max(getWidth(), getHeight()) * 1.2;
        for (GameObject2 obj : getHostGame().getObjectsNearPoint(pos, radius)) {
            if (obj == this || !(obj instanceof RTSUnit)) continue;
            RTSUnit other = (RTSUnit) obj;
            if (other.plane >= 2 || !other.isSolid || other.isRubble) continue;
            if (pos.distanceFrom(other.getPixelLocation()) < radius) return false;
        }
        return true;
    }

    // -- landing / takeoff triggers --

    public void startLanding() {
        if (isLanding || isTakingOff || isLanded || isRubble) return;
        if (!isOnGroundTerrain()) return;
        if (!isLandingZoneClear()) return;
        setImmobilized(true);
        isLanding = true;
        landingStartTick = getHostGame().getGameTickNumber();
        sightBlockerImmune = false;
        plane = 0;
    }

    public void startTakeoff() {
        if (!isLanded || isLanding || isTakingOff || isRubble) return;
        isTakingOff = true;
        isLanded = false;
        takeoffStartTick = getHostGame().getGameTickNumber();
    }

    @Override
    public void triggerAbility(int index, Coordinate target, String targetUnitId) {
        switch (index) {
            case 0 -> startLanding();
            case 1 -> startTakeoff();
        }
    }

    // -- rotation / speed overrides --

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
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            if (turret != null) {
                turret.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            }
        }
    }

    @Override
    public void addRenderHook(Framework.RenderHook hook) {
        turret.addRenderHook(hook);
    }

    @Override
    public int getWidth() {
        return super.getWidth() / 2;
    }

    @Override
    public int getHeight() {
        return super.getHeight() / 2;
    }

    @Override
    public void tick() {
        // scheduled destruction (after shadow fadeout completes)
        if (scheduledDestructionAtTick > 0 && getHostGame().getGameTickNumber() >= scheduledDestructionAtTick) {
            this.destroy();
            return;
        }

        // shadow fadeout phase
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

        // death fall
        if (isRubble && elevation > 0) {
            elevation -= 3.0;
            if (!deathExplosionSpawned && elevation < 8) {
                OnceThroughSticker deathBlast = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
                deathBlast.setRenderScale(1.6);
                GameDemo.RTSDemo.RTSSoundManager.get().play(GameDemo.RTSDemo.RTSSoundManager.TANK_DEATH, .56, 0);
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

        // landing transition — no early return so super.tick() still ticks buttons
        if (isLanding) {
            int transitionTicks = RTSGame.desiredTPS * 2;
            long elapsed = getHostGame().getGameTickNumber() - landingStartTick;
            double progress = Math.min(1.0, (double) elapsed / transitionTicks);
            elevation = (int)(149 * (1.0 - progress));
            if (progress >= 1.0) {
                elevation = 0;
                isLanding = false;
                isLanded = true;
                velocity.x = 0;
                velocity.y = 0;
                this.setZLayer(3);
                turret.setZLayer(3);
            }
        }

        // takeoff transition — no early return so super.tick() still ticks buttons
        if (isTakingOff) {
            int transitionTicks = RTSGame.desiredTPS * 2;
            long elapsed = getHostGame().getGameTickNumber() - takeoffStartTick;
            double progress = Math.min(1.0, (double) elapsed / transitionTicks);
            elevation = (int)(149 * progress);
            if (progress >= 1.0) {
                elevation = 149;
                isTakingOff = false;
                plane = 2;
                sightBlockerImmune = true;
                setImmobilized(false);
                this.setZLayer(11);
                turret.setZLayer(11);
            }
        }

        // always call super.tick() for living units — this ticks buttons and proximity scans;
        // isImmobilized prevents movement when landed or transitioning
        if (!isRubble) {
            super.tick();
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;

        if (shadowPhase) {
            super.render(g);
            return;
        }

        // rubble on ground — turret renders the graphic
        if (isRubble && elevation <= 0) {
            return;
        }

        int shadowOffsetX = 5;
        int shadowOffsetY = isLanded ? 18 : Math.max(elevation, 9);
        Coordinate renderLocation = getRenderLocation();
        renderLocation.x += shadowOffsetX;
        renderLocation.y += shadowOffsetY;
        double shadowScale = isRubble ? 0.95 + 0.05 * (Math.max(0, elevation) / 149.0) : 1.0;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int drawWidth = (int)(toRender.getWidth() * shadowScale);
        int drawHeight = (int)(toRender.getHeight() * shadowScale);
        int renderX = renderLocation.x - drawWidth / 2;
        int renderY = renderLocation.y - drawHeight / 2;
        g.rotate(Math.toRadians(turret.getRotation()), renderLocation.x, renderLocation.y);
        g.drawImage(toRender, renderX, renderY, drawWidth, drawHeight, null);
        g.setTransform(old);

        if (isSelected() && !isRubble) {
            drawHealthBar(g);
        }

        if (ExternalCommunicator.outOfSyncUnitIds.indexOf(ID) > -1) {
            g.setColor(Color.ORANGE);
            g.fillOval(getRenderLocation().x - getWidth() / 2, getPixelLocation().y - getHeight() / 2, getWidth() / 2, getHeight() / 2);
        }
    }

    @Override
    public void die() {
        if (isRubble) {
            return;
        }
        // if dying while landed/transitioning, give it a small elevation so the fall plays
        if (isLanded || isLanding || isTakingOff) {
            elevation = 10;
            isLanded = false;
            isLanding = false;
            isTakingOff = false;
        }
        deathVelocityX = this.velocity.x;
        deathVelocityY = this.velocity.y;
        this.isRubble = true;
        this.isSolid = false;
        this.setSelected(false);
        this.setImmobilized(false);
    }

    public class TransportHeliTurret extends SubObject {

        private int bobOffset = -1;
        private final double bobAmount = 8;

        // render-only (non-deterministic) blade state
        private transient double bladesAngle = 0;
        private transient long lastBladesRenderMs = -1;

        public TransportHeliTurret(Coordinate offset) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            this.setZLayer(11);
        }

        @Override
        public void tick() {
            if (bobOffset == -1) {
                bobOffset = Main.generateRandomIntFromSeed(0, 200, (long)(getHost().getLocationAsOfLastTick().x + getHost().getLocationAsOfLastTick().y));
            }
            if (!isRubble && !isLanded) {
                updateLocationForBob();
                updateRotation();
            }
        }

        @Override
        public void render(Graphics2D g) {
            if (!((RTSUnit) getHost()).shouldRender()) return;
            super.render(g);

            // blades always render when not rubble (stopped when elevation = 0, spinning when airborne)
            if (!isRubble) {
                long now = System.currentTimeMillis();
                if (elevation > 0 && lastBladesRenderMs > 0) {
                    double elapsedMs = now - lastBladesRenderMs;
                    double speedFactor = Math.max(0.0, elevation / 149.0);
                    bladesAngle = (bladesAngle + elapsedMs * 2160.0 / 1000.0 * speedFactor) % 360;
                }
                // only advance the clock when spinning; reset when stopped so re-spin doesn't jump
                lastBladesRenderMs = (elevation > 0) ? now : -1;

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
            if (!isRubble) {
                this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            }
        }

        private void updateLocationForBob() {
            long tick = getHostGame().getGameTickNumber() + bobOffset;
            double speedPerTick = RTSGame.tickAdjust(2.0);
            double cycleLength = 200.0 / speedPerTick;
            double cyclePos = (tick % (long) cycleLength) * speedPerTick;
            double bobPercent = cyclePos <= 100 ? cyclePos : 200 - cyclePos;
            // bob amplitude fades out as the helicopter descends
            double bobScale = Math.max(0.0, elevation / 149.0);
            double newY = bobAmount * (bobPercent / 100.0) * bobScale;
            Coordinate newOffset = new Coordinate(0, (int) newY);
            newOffset.rotateAboutPoint(new Coordinate(0, 0), -getRotation());
            this.setOffset(newOffset);
        }

        private void updateRotation() {
            double desiredRotationAmount = this.getHost().getRotation() - getRotation();
            double maxRotation = RTSGame.tickAdjust(5);
            double rotationAmount;
            if (Math.abs(desiredRotationAmount) > maxRotation) {
                rotationAmount = desiredRotationAmount > 0 ? maxRotation : -maxRotation;
            } else {
                rotationAmount = desiredRotationAmount;
            }
            this.rotate(rotationAmount);
        }
    }

    @Override
    public BufferedImage getSelectionImage() {
        return RTSAssetManager.hellicopterSelectionImage;
    }

    // -- ReinforcementPoint --

    @Override
    public int getOwningTeam() { return team; }

    @Override
    public double getCaptureRadius() { return 200; }

    @Override
    public SpawnLocation getSpawnLocation() {
        return new SpawnLocation(getPixelLocation(), getRotation());
    }

    @Override
    public boolean isActive() { return isLanded && !isRubble; }

    @Override
    public boolean isCapturable() { return false; }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        String state = isLanded ? "Landed" : (isLanding ? "Landing..." : (isTakingOff ? "Taking off..." : "Flying"));
        out.add("Speed: " + baseSpeed + "    State: " + state);
        return out;
    }
}

package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.PathingLayer;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.LaunchMissileButton;
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
 * the apache itself is invisible, instead we render the subobject as the hull of the apache to
 * simplify movement and rotation
 *
 * @author guydu
 */
public class Apache extends RTSUnit {

    public boolean sightBlockerImmune = true;

    @Override
    public boolean ignoresSightBlockers() { return sightBlockerImmune; }

    public static final double VISUAL_SCALE = .34;

    // Docked missile positions in original image pixels (before VISUAL_SCALE).
    // Left outer, left inner, right inner, right outer.
    private static final int[][] DOCKED_MISSILE_OFFSETS = {
        {-85, -25},
        { -65, -25},
        {  60, -25},
        { 75, -25},
    };

    private static final int MISSILE_FIRE_INTERVAL = 12; // ticks between each of the 4 launches

    public static volatile Sprite baseSprite = null;
    public static volatile Sprite baseSpriteRed = null;
    public static volatile Sprite emptyPodsSprite = null;
    public static volatile Sprite emptyPodsSpriteRed = null;
    public static volatile Sprite dockedMissileSprite = null;
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
        baseSprite = new Sprite(RTSAssetManager.apache);
        baseSpriteRed = new Sprite(RTSAssetManager.apacheRed);
        emptyPodsSprite = new Sprite(RTSAssetManager.apacheEmptyPods);
        emptyPodsSpriteRed = new Sprite(RTSAssetManager.apacheEmptyPodsRed);
        destroyedSprite = new Sprite(RTSAssetManager.apacheDestroyed);
        destroyedSpriteRed = new Sprite(RTSAssetManager.apacheDestroyedRed);
        shadowSprite = Sprite.generateShadowSprite(RTSAssetManager.apacheEmptyPods, .7);
        shadowSprite.scaleTo(VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(4);
        attackSequence = new Sequence(RTSAssetManager.apacheAttack, "apacheAttack");
        attackSequenceRed = new Sequence(RTSAssetManager.apacheAttackRed, "apacheAttackRed");
        bladesSprite = new Sprite(RTSAssetManager.apacheBlades);
        bladesSpriteRed = new Sprite(RTSAssetManager.apacheBladesRed);
        bladesSprite.scaleTo(VISUAL_SCALE);
        bladesSpriteRed.scaleTo(VISUAL_SCALE);
        dockedMissileSprite = new Sprite(RTSAssetManager.apacheDockedMissile);
        dockedMissileSprite.scaleTo(VISUAL_SCALE);
        emptyPodsSprite.applyAlphaEdgeBlurSelf(2);
        emptyPodsSpriteRed.applyAlphaEdgeBlurSelf(2);
        bladesSprite.applyAlphaEdgeBlurSelf(2);
        bladesSpriteRed.applyAlphaEdgeBlurSelf(2);
        attackSequence.setFrameDelay(40);
        attackSequenceRed.setFrameDelay(40);
        ApacheMissile.initGraphics();
    }

    public ApacheTurret turret;
    public long lastFireTick = 0;
    public int attackInterval = RTSGame.desiredTPS * 2;
    public int elevation = 149;
    private double hullRotationSpeed = 0.0;
    public long scheduledDestructionAtTick = 0;
    public long pendingBulletSpawnAtTick = 0;
    public RTSUnit pendingBulletTarget = null;

    // Missile ability state
    public Coordinate missileAbilityTarget = null;
    public int missilesFiredCount = 0;
    public int missilesInFlight = 0;
    public long missileNextFireAtTick = -1;
    private boolean abilityNavSuppressCancel = false;

    public Apache(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(team == 0 ? emptyPodsSprite : emptyPodsSpriteRed);
        this.setZLayer(11);
        this.plane = 2;
        this.isSolid = true;
        this.setBaseSpeed(RTSGame.tickAdjust(4.5));
        this.rotationSpeed = RTSGame.tickAdjust(2.0);
        turret = new ApacheTurret(new Coordinate(0, 0));
        this.addSubObject(turret);
        this.canAttackAir = true;
        this.pathingModifiers.put(PathingLayer.Type.water, 1.0);
        this.setRenderBrightness(1.25);
        this.addButton(new LaunchMissileButton(this));
    }

    @Override
    public void triggerAbility(int index, Coordinate target) {
        if (index == 0) {
            missileAbilityTarget = target.copy();
            missilesFiredCount = 0;
            missileNextFireAtTick = -1;
            LaunchMissileButton btn = (LaunchMissileButton) getButtons().get(0);
            double dist = distanceFrom(target);
            if (dist <= btn.maxCastRange && dist >= btn.minCastRange) {
                missileNextFireAtTick = getHostGame().getGameTickNumber();
                btn.tickLastUsed = btn.tickNumber;
            } else if (dist > btn.maxCastRange) {
                Coordinate navTarget = calculateNavToward(target, btn.maxCastRange);
                abilityNavSuppressCancel = true;
                setDesiredLocation(navTarget);
                abilityNavSuppressCancel = false;
            } else {
                Coordinate navTarget = calculateNavAway(target, btn.minCastRange);
                abilityNavSuppressCancel = true;
                setDesiredLocation(navTarget);
                abilityNavSuppressCancel = false;
            }
        }
    }

    private Coordinate calculateNavToward(Coordinate target, double castRange) {
        Coordinate myLoc = getPixelLocation();
        double dx = target.x - myLoc.x;
        double dy = target.y - myLoc.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double t = (dist - castRange + 60) / dist;
        return new Coordinate((int) (myLoc.x + dx * t), (int) (myLoc.y + dy * t));
    }

    private Coordinate calculateNavAway(Coordinate target, double minRange) {
        Coordinate myLoc = getPixelLocation();
        double dx = myLoc.x - target.x;
        double dy = myLoc.y - target.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) { dx = 1; dist = 1; } // avoid divide-by-zero if exactly on top
        return new Coordinate(
                (int) (target.x + dx / dist * (minRange + 60)),
                (int) (target.y + dy / dist * (minRange + 60)));
    }

    @Override
    public void setDesiredLocation(Coordinate c) {
        if (!abilityNavSuppressCancel && missileAbilityTarget != null && missileNextFireAtTick == -1) {
            missileAbilityTarget = null;
        }
        super.setDesiredLocation(c);
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
        double angleFactor = Math.max(0.0, Math.min(1.0, (160 - angle) / 160));
        return super.getSpeed() * angleFactor;
    }

    @Override
    public void onPostDeserialization() {
        super.onPostDeserialization();
        if (isRubble) {
            this.setGraphic(team == 0 ? destroyedSprite : destroyedSpriteRed);
        } else {
            this.setGraphic(team == 0 ? emptyPodsSprite : emptyPodsSpriteRed);
        }
        if (turret != null) {
            if (isRubble) {
                turret.setGraphic(team == 0 ? destroyedSprite : destroyedSpriteRed);
            } else {
                turret.setGraphic(team == 0 ? emptyPodsSprite : emptyPodsSpriteRed);
            }
        }
    }

    @Override
    public int getWidth() {
        return super.getWidth() / 2;
    }

    @Override
    public int getHeight() {
        return super.getHeight() / 2;
    }

    public void fireDelayed(RTSUnit targetUnit, int delay) {
        pendingBulletSpawnAtTick = getHostGame().getGameTickNumber() + delay;
        pendingBulletTarget = targetUnit;
    }

    private void fireMissile(int missileIndex) {
        int[] offsets = DOCKED_MISSILE_OFFSETS[missileIndex];
        int ox = (int) (offsets[0] * VISUAL_SCALE);
        int oy = (int) (offsets[1] * VISUAL_SCALE);
        Coordinate spawnOffset = new Coordinate(ox, oy);
        spawnOffset.adjustForRotation(turret.getRotation());
        Coordinate spawnPos = turret.getPixelLocation().copy().add(spawnOffset);

        double angle = Math.toRadians(missileIndex * 90 + 45);
        int offsetX = (int) (Math.cos(angle) * ApacheMissile.AOE_RADIUS * 0.6);
        int offsetY = (int) (Math.sin(angle) * ApacheMissile.AOE_RADIUS * 0.6);
        Coordinate targetPos = new Coordinate(
                missileAbilityTarget.x + offsetX,
                missileAbilityTarget.y + offsetY);

        missilesInFlight++;
        getHostGame().addObject(new ApacheMissile(this, spawnPos, targetPos, missileIndex));

        if (isOnScreen()) {
            RTSSoundManager.get().play(RTSSoundManager.HELICOPTER_ATTACK,
                    Main.generateRandomDoubleLocally(0.7, 0.9),
                    Main.generateRandomIntLocally(0, 150));
        }
    }

    public void onMissileExploded() {
        missilesInFlight = Math.max(0, missilesInFlight - 1);
        if (missilesInFlight == 0 && missilesFiredCount >= 4) {
            missileAbilityTarget = null;
            missilesFiredCount = 0;
            missileNextFireAtTick = -1;
        }
    }

    @Override
    public void tick() {
        if (scheduledDestructionAtTick > 0 && getHostGame().getGameTickNumber() >= scheduledDestructionAtTick) {
            new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
            this.destroy();
            return;
        }

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

        if (!isRubble) {
            super.tick();

            // Missile ability handling
            if (missileAbilityTarget != null) {
                double castRange = getButtons().get(0).maxCastRange;
                double distToTarget = distanceFrom(missileAbilityTarget);

                // Navigation phase: waiting for a valid firing position (no shots fired yet)
                if (missileNextFireAtTick == -1 && missilesFiredCount == 0 && missilesInFlight == 0) {
                    LaunchMissileButton btn = (LaunchMissileButton) getButtons().get(0);
                    if (distToTarget <= castRange && distToTarget >= btn.minCastRange) {
                        missileNextFireAtTick = getHostGame().getGameTickNumber();
                        btn.tickLastUsed = btn.tickNumber;
                    }
                }

                // Firing + in-flight lock: hold position and face target
                if (missileNextFireAtTick != -1 || missilesInFlight > 0) {
                    this.velocity.y = 0;
                    applyHullRotation(rotationNeededToFace(missileAbilityTarget));

                    if (missilesFiredCount < 4 && getHostGame().getGameTickNumber() >= missileNextFireAtTick
                            && (missilesFiredCount > 0 || Math.abs(turret.rotationNeededToFace(missileAbilityTarget)) < 10)) {
                        fireMissile(missilesFiredCount);
                        missilesFiredCount++;
                        if (missilesFiredCount < 4) {
                            missileNextFireAtTick = getHostGame().getGameTickNumber() + MISSILE_FIRE_INTERVAL;
                        } else {
                            missileNextFireAtTick = -1; // all fired; onMissileExploded() will clear target
                        }
                    }
                }
            }

            // Regular gun combat (skipped while actively firing missiles)
            if (missileNextFireAtTick == -1) {
                RTSUnit preferred = getPreferredTargetIfInRange();
                currentTarget = preferred != null ? preferred : nearestEnemyInRange();
                boolean offCooldown = (getHostGame().getGameTickNumber() - lastFireTick) > attackInterval;
                if (currentTarget != null && offCooldown) {
                    if (Math.abs(turret.rotationNeededToFace(currentTarget.getPixelLocation())) < 2) {
                        lastFireTick = getHostGame().getGameTickNumber();
                        Sequence attackAnimation = team == 0 ? attackSequence : attackSequenceRed;
                        turret.setGraphic(attackAnimation.copyMaintainSource());
                        System.out.println("" + this.ID + " located at " + this.getLocationAsOfLastTick() + "/" + this.getLocation() + "/" + this.getPixelLocation() + " firing on tick " + getHostGame().getGameTickNumber() + " at " + currentTarget.ID + " located at " + currentTarget.getLocationAsOfLastTick() + "/" + currentTarget.getLocation() + "/" + currentTarget.getPixelLocation());
                        fireDelayed(currentTarget, 10);
                    }
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;
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
        missileAbilityTarget = null;
        missilesFiredCount = 0;
        missilesInFlight = 0;
        missileNextFireAtTick = -1;
        this.turret.setGraphic(team == 0 ? destroyedSprite : destroyedSpriteRed);
        this.isRubble = true;
        this.isSolid = false;
        this.setSelected(false);
    }

    public class ApacheTurret extends SubObject {

        private int bobOffset = -1;
        private final double bobAmount = 8;

        public ApacheTurret(Coordinate offset) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(team == 0 ? emptyPodsSprite : emptyPodsSpriteRed);
            this.setZLayer(11);
        }

        @Override
        public void tick() {
            if (bobOffset == -1) {
                bobOffset = Main.generateRandomIntFromSeed(0, 200, (long) (getHost().getLocationAsOfLastTick().x + getHost().getLocationAsOfLastTick().y));
            }
            if (!isRubble) {
                updateLocationForBob();
                updateRotation();
            }
        }

        @Override
        public void render(Graphics2D g) {
            if (!((RTSUnit) getHost()).shouldRender()) return;
            if (!isRubble) {
                // Docked missiles rendered before body so they appear underneath
                Coordinate renderLoc = getRenderLocation();
                LaunchMissileButton missileButton = (LaunchMissileButton) Apache.this.getButtons().get(0);
                int firstVisible;
                if (Apache.this.missileNextFireAtTick != -1) {
                    firstVisible = Apache.this.missilesFiredCount;
                } else if (!missileButton.isOnCooldown()) {
                    firstVisible = 0;
                } else {
                    firstVisible = 4;
                }
                if (dockedMissileSprite != null && firstVisible < 4) {
                    renderDockedMissiles(g, renderLoc, firstVisible);
                }
            }

            super.render(g); // renders emptyPodsSprite (or destroyed sprite) on top of docked missiles

            if (!isRubble) {
                // Rotor blades on top of body
                double bladesAngle = (System.currentTimeMillis() * 2160.0 / 1000.0) % 360;
                Coordinate renderLoc = getRenderLocation();
                VolatileImage bladesImg = (team == 0 ? bladesSprite : bladesSpriteRed).getCurrentVolatileImage();
                AffineTransform old = g.getTransform();
                g.rotate(Math.toRadians(bladesAngle), renderLoc.x, renderLoc.y);
                g.drawImage(bladesImg, renderLoc.x - bladesImg.getWidth() / 2, renderLoc.y - bladesImg.getHeight() / 2, null);
                g.setTransform(old);
            }
        }

        private void renderDockedMissiles(Graphics2D g, Coordinate renderLoc, int startIndex) {
            VolatileImage missileImg = dockedMissileSprite.getCurrentVolatileImage();
            if (missileImg == null) return;
            AffineTransform old = g.getTransform();
            g.rotate(Math.toRadians(getRotation()), renderLoc.x, renderLoc.y);
            for (int i = startIndex; i < DOCKED_MISSILE_OFFSETS.length; i++) {
                int[] offset = DOCKED_MISSILE_OFFSETS[i];
                int ox = (int) (offset[0] * VISUAL_SCALE);
                int oy = (int) (offset[1] * VISUAL_SCALE);
                g.drawImage(missileImg,
                        renderLoc.x + ox - missileImg.getWidth() / 2,
                        renderLoc.y + oy - missileImg.getHeight() / 2, null);
            }
            g.setTransform(old);
        }

        @Override
        public void onAnimationCycle() {
            this.setGraphic(team == 0 ? emptyPodsSprite : emptyPodsSpriteRed);
        }

        private void updateLocationForBob() {
            long tick = getHostGame().getGameTickNumber() + bobOffset;

            double speedPerTick = RTSGame.tickAdjust(2.0);
            double cycleLength = 200.0 / speedPerTick;

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
            Apache host = (Apache) getHost();
            double desiredRotationAmount = this.getHost().getRotation() - getRotation();
            double maxRotation = RTSGame.tickAdjust(2.0);

            // During missile ability (firing or missiles still in flight), face the ability target
            if (host.missileAbilityTarget != null && (host.missileNextFireAtTick != -1 || host.missilesInFlight > 0)) {
                desiredRotationAmount = rotationNeededToFace(host.missileAbilityTarget);
            } else if (host.currentTarget != null) {
                desiredRotationAmount = rotationNeededToFace(host.currentTarget.getPixelLocation());
            }

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
    public java.awt.image.BufferedImage getSelectionImage() {
        return RTSAssetManager.apacheSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + HellicopterBullet.staticDamage + " (x2)   Interval: " + 2 + "s    Range: " + range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        out.add("Missile: " + ApacheMissile.DAMAGE_AMOUNT + " AoE (x4)   Range: 600   Cooldown: 10s");
        return out;
    }

}

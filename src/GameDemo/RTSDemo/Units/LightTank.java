
package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.GraphicalAssets.Graphic;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Hitbox;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Buttons.LayMineButton;
import GameDemo.RTSDemo.CommandButton;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSSoundManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author guydu
 */
public class LightTank extends RTSUnit {

    public static final double LIGHT_TANK_SPEED = RTSGame.tickAdjust(2.8);
    public static final double LIGHT_TANK_ATTACK_INTERVAL = 1.6;

    public static final double VISUAL_SCALE = .48;
    
    // Team-neutral sprites
    public static volatile Sprite hullShadow = null;
    public static volatile Sprite turretShadow = null;
    public static volatile Sprite hullSpriteDestroyed = null;
    public static volatile Sprite turretSpriteDestroyed = null;
    public static volatile Sequence deathFadeout = null;

    // Team-colored maps
    private static final Map<Integer, Sprite>   hullSpriteMap         = new HashMap<>();
    private static final Map<Integer, Sprite>   turretSpriteMap       = new HashMap<>();
    private static final Map<Integer, Sprite>   hullDamagedSpriteMap  = new HashMap<>();
    private static final Map<Integer, Sprite>   turretDamagedSpriteMap = new HashMap<>();
    private static final Map<Integer, Sequence> fireSequenceMap       = new HashMap<>();
    private static final Map<Integer, Sequence> fireDamagedSequenceMap = new HashMap<>();

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (!hullSpriteMap.isEmpty()) return;

        hullShadow = Sprite.generateShadowSprite(RTSAssetManager.lightTankHull, .8);
        turretShadow = Sprite.generateShadowSprite(RTSAssetManager.lightTankTurret, .8);
        hullSpriteDestroyed = new Sprite(RTSAssetManager.lightTankHullDestroyed);
        turretSpriteDestroyed = new Sprite(RTSAssetManager.lightTankTurretDestroyed);
        deathFadeout = Sequence.createFadeout(RTSAssetManager.lightTankDeathShadow, 40);
        deathFadeout.setSignature("deathFadeoutLightTank");
        hullShadow.scaleTo(VISUAL_SCALE);
        turretShadow.scaleTo(VISUAL_SCALE);
        hullShadow.applyAlphaEdgeBlurSelf(8);
        turretShadow.applyAlphaEdgeBlurSelf(3);

        for (int team : RTSGame.activeTeams) {
            Sprite hull = new Sprite(RTSAssetManager.getLightTankHull(team));
            hull.applyAlphaEdgeBlurSelf(1);
            hullSpriteMap.put(team, hull);

            Sprite turret = new Sprite(RTSAssetManager.getLightTankTurret(team));
            turret.applyAlphaEdgeBlurSelf(1);
            turretSpriteMap.put(team, turret);

            hullDamagedSpriteMap.put(team, new Sprite(RTSAssetManager.getLightTankHullDamaged(team)));
            turretDamagedSpriteMap.put(team, new Sprite(RTSAssetManager.getLightTankTurretDamaged(team)));

            Sequence fire = new Sequence(RTSAssetManager.getLightTankFire(team), "lightTankFire");
            fire.setFrameDelay(35);
            fireSequenceMap.put(team, fire);

            Sequence fireDamaged = new Sequence(RTSAssetManager.getLightTankFireDamaged(team), "lightTankFireDamaged");
            fireDamaged.setFrameDelay(35);
            fireDamagedSequenceMap.put(team, fireDamaged);
        }
    }

    // instance fields
    public LightTankTurret turret;
    public long barrelCooldownExpiresAtTick = 0;
    private long fadeoutScheduledAtTick = 0;
    private long destructionScheduledAtTick = 0;
    private double hullRotationSpeed = 0.0;

    public LightTank(int x, int y, int team) {
        super(x, y, team);
        this.maxHealth = 140;
        this.currentHealth = maxHealth;
        this.setScale(VISUAL_SCALE);
        this.setGraphic(getHullSprite());
        turret = new LightTankTurret(new Coordinate(0, 0), this);
        this.addSubObject(turret);
        this.isSolid = true;
        this.cargoSize = 6;
        this.setHitbox(new Hitbox(this, getWidth() / 2));
        this.range = 500;
        this.baseSpeed = LIGHT_TANK_SPEED;
        this.mass = 800;
        this.rotationSpeed = RTSGame.tickAdjust(2.5);
        initializeButtons();
    }

    private void initializeButtons() {
        addButton(new LayMineButton(this));
    }

    @Override
    public void setHostGame(Framework.Game g) {
        super.setHostGame(g);
    }

    @Override
    public void onPostDeserialization() {
        // Restore graphics after deserialization
        this.setGraphic(isRubble ? hullSpriteDestroyed : getHullSprite());
        if (turret != null) {
            turret.setGraphic(isRubble ? turretSpriteDestroyed : turret.getTurretSprite());
        }
        // Restore button transient fields after deserialization
        for (CommandButton button : getButtons()) {
            button.restoreTransientFields();
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;
        if (isSolid) {
            drawShadow(g, hullShadow, 5, 9);
        }
        super.render(g);
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
    public int getBoardingRange(RTSUnit transport) {
        return super.getBoardingRange(transport) + 30;
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
    public void tick() {
        super.tick();

        // Check for scheduled destruction
        if (destructionScheduledAtTick > 0 && getHostGame().getGameTickNumber() >= destructionScheduledAtTick) {
            this.destroy();
            return;
        }

        // Check for scheduled fadeout
        if (fadeoutScheduledAtTick > 0 && getHostGame().getGameTickNumber() >= fadeoutScheduledAtTick) {
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "lightTankDespawnExplosion"), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.setZLayer(-10);
            this.turret.isInvisible = true;
            destructionScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 3);
            fadeoutScheduledAtTick = 0;
            return;
        }

        // Check barrel cooldown expiration
        if (barrelCooldownExpiresAtTick > 0 && getHostGame().getGameTickNumber() >= barrelCooldownExpiresAtTick) {
            barrelCooldownExpiresAtTick = 0;
        }

        if (isRubble) {
            return;
        }
        populateNearbyEnemies();
        RTSUnit preferred = getPreferredTargetIfInRange();
        currentTarget = preferred != null ? preferred : nearestEnemyGroundUnit;
        if (currentTarget != null && Math.abs(turret.rotationNeededToFace(currentTarget.getPixelLocation())) < 3 && barrelCooldownExpiresAtTick == 0) {
            System.out.println("rotation needed to face "  + turret.rotationNeededToFace(currentTarget.getPixelLocation()));
            fire(currentTarget);
        }
        setGraphic(getHullSprite());
    }
    
    public void playAttackSound() {
        int offset = Main.generateRandomIntLocally(0, 20);
        double volumeOnScreen = Main.generateRandomDoubleLocally(.70, .76);
        double volumeOffScreen = Main.generateRandomDoubleLocally(.64, .71);
        RTSSoundManager.get().play(
                RTSSoundManager.LIGHT_TANK_ATTACK,
                isOnScreen() ? volumeOnScreen : volumeOffScreen,
                offset);
    }

    public void fire(RTSUnit target) {
        barrelCooldownExpiresAtTick = getHostGame().getGameTickNumber() + (int) (RTSGame.desiredTPS * LIGHT_TANK_ATTACK_INTERVAL);
        playAttackSound();
        turret.setGraphic(getFireSequence());
        Coordinate muzzelLocation = new Coordinate(0, 0);
        muzzelLocation.y -= turretSpriteMap.get(0).getHeight() * 2 / 5;
        muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, turret.getRotationRealTime());
        muzzelLocation.add(turret.getPixelLocation());
        Coordinate randomOffset = new Coordinate(
                Main.generateRandomIntFromSeed(-target.getWidth() / 4, target.getWidth() / 4, getHostGame().getGameTickNumber()),
                Main.generateRandomIntFromSeed(-target.getWidth() / 4, target.getWidth() / 4, getHostGame().getGameTickNumber())
        );
        LightTankBullet bullet = new LightTankBullet(muzzelLocation.toDCoordinate(), target.getLocationAsOfLastTick().add(randomOffset), getPreferredTargetId());
        bullet.shooter = this;
        getHostGame().addObject(bullet);
    }

    public Sequence getFireSequence() {
        boolean isDamaged = currentHealth <= maxHealth * .33;
        return isDamaged ? fireDamagedSequenceMap.get(team).copyMaintainSource() : fireSequenceMap.get(team).copyMaintainSource();
    }

    public Sprite getHullSprite() {
        if (isRubble) return hullSpriteDestroyed;
        boolean isDamaged = currentHealth <= maxHealth * .33;
        return isDamaged ? hullDamagedSpriteMap.get(team) : hullSpriteMap.get(team);
    }

    @Override
    public int getWidth() {
        return (int)(hullSpriteMap.get(0).getWidth() * VISUAL_SCALE);
    }

    @Override
    public int getHeight() {
        return (int)(hullSpriteMap.get(0).getHeight() * VISUAL_SCALE);
    }

    @Override
    public void die() {
        if (this.isRubble) {
            return;
        }
        OnceThroughSticker deathExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "lightTankDeathExplosion"), getPixelLocation());
        this.isRubble = true;
        this.team = -1;
        this.setBaseSpeed(0);
        this.setDesiredLocation(this.getPixelLocation());
        this.setGraphic(hullSpriteDestroyed);
        turret.setGraphic(turretSpriteDestroyed);
        if(isOnScreen()) {
            RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, Main.generateRandomDoubleLocally(.62, .64), 0);
        }
        fadeoutScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 10);
    }

    public class LightTankTurret extends SubObject {

        public LightTank hull;
        public double desiredRotationAngle = 0;
        private double turretRotationSpeed = 0.0;
        private double previousHullRotation = Double.NaN;

        public LightTankTurret(Coordinate offset, LightTank h) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(getTurretSprite());
            this.hull = h;
        }

        public void updateDesiredRotation() {
            if (hull.isRubble) {
                desiredRotationAngle = getRotation();
                return;
            }
            if (currentTarget == null) {
                desiredRotationAngle = hull.getRotationRealTime();
            } else {
                desiredRotationAngle = angleFrom(hull.currentTarget.getPixelLocation());
            }
        }

        @Override
        public void tick() {
            if (!isAnimated() || isRubble) {
                setGraphic(getTurretSprite());
            }

            // Mirror hull rotation — turret is physically mounted on the hull
            double currentHullRotation = getHost().getRotationRealTime();
            if (!Double.isNaN(previousHullRotation)) {
                double hullDelta = currentHullRotation - previousHullRotation;
                if (hullDelta > 180) hullDelta -= 360;
                else if (hullDelta < -180) hullDelta += 360;
                rotate(hullDelta);
            }
            previousHullRotation = currentHullRotation;

            updateDesiredRotation();
            double maxSpeed = RTSGame.tickAdjust(3.5);
            double desiredRotation = desiredRotationAngle - getRotationRealTime();
            if (desiredRotation > 180) desiredRotation -= 360;
            else if (desiredRotation < -180) desiredRotation += 360;

            final double accel = RTSGame.tickAdjust(0.4);
            double targetSpeed = Math.abs(desiredRotation) < 0.01 ? 0.0 : Math.copySign(maxSpeed, desiredRotation);

            if (turretRotationSpeed < targetSpeed) {
                turretRotationSpeed = Math.min(turretRotationSpeed + accel, targetSpeed);
            } else if (turretRotationSpeed > targetSpeed) {
                turretRotationSpeed = Math.max(turretRotationSpeed - accel, targetSpeed);
            }

            if (Math.abs(desiredRotation) <= Math.abs(turretRotationSpeed)) {
                rotate(desiredRotation);
                turretRotationSpeed = 0;
            } else {
                rotate(turretRotationSpeed);
            }
        }

        @Override
        public void render(Graphics2D g) {
            if (!hull.shouldRender()) return;
            if (getHost().isSolid && !isRubble) {
                AffineTransform old = g.getTransform();
                VolatileImage toRender = turretShadow.getCurrentVolatileImage();
                Coordinate pixelLocation = getRenderLocation().add(new Coordinate(2, 3));
                int renderX = pixelLocation.x - toRender.getWidth() / 2;
                int renderY = pixelLocation.y - toRender.getHeight() / 2;
                g.rotate(Math.toRadians(getRenderRotation()), pixelLocation.x, pixelLocation.y);
                g.drawImage(toRender, renderX, renderY, null);
                g.setTransform(old);
            }
            super.render(g);
        }

        public Graphic getTurretSprite() {
            if (isRubble) return turretSpriteDestroyed;
            boolean isDamaged = hull != null && hull.currentHealth <= hull.maxHealth * .33;
            return isDamaged ? turretDamagedSpriteMap.get(team) : turretSpriteMap.get(team);
        }

        @Override
        public void onAnimationCycle() {
            this.setGraphic(getTurretSprite());
        }

    }

    @Override
    public java.awt.image.BufferedImage getSelectionImage() {
        return RTSAssetManager.lightTankSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + LightTankBullet.staticDamage + "    Interval: " + LIGHT_TANK_ATTACK_INTERVAL + "s    Range: " + range);
        out.add("Speed: " + LIGHT_TANK_SPEED + "    Targets: Ground");
        return out;
    }

    @Override
    public void triggerAbility(int index, Coordinate target, String targetUnitId) {
        if (index == 0) {
            Coordinate minePos = target;
            if (minePos == null) {
                minePos = getPixelLocation().copy();
                Coordinate offset = new Coordinate(0, getHeight() / 2);
                offset.adjustForRotation(getRotation());
                minePos = minePos.add(offset);
            }
            getHostGame().addObject(new Landmine(minePos.x, minePos.y, team));
        }
    }
}

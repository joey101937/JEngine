package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.Effects.MuzzleSmokeEffect;
import GameDemo.RTSDemo.Effects.SmokePoofEffect;
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
 * Stationary 88mm gun emplacement. The base is fixed in place; the gun on top
 * traverses to track targets and fires the same way a tank's main gun does.
 *
 * @author Joseph
 */
public class T2Turret extends RTSUnit {

    public static final double attackFrequency = 2.15;
    // Seconds the gun must hold a valid firing solution on a target before it can shoot.
    public static final double windupSeconds = 0.5;
    public static final double VISUAL_SCALE = .55;
    /** Distance from the traverse pivot to the muzzle, as a fraction of the gun sprite's height. */
    private static final double MUZZLE_OFFSET_FRACTION = .46;

    public Gun gun;
    public long weaponCooldownExpiresAtTick = 0;
    // Windup state: which target we are currently winding up on, and the tick the windup began.
    public String windupTargetId = null;
    public long windupStartTick = 0;
    private long fadeoutScheduledAtTick = 0;
    private long destructionScheduledAtTick = 0;

    // Team-neutral sprites
    public static volatile Sprite baseShadow = null;
    public static volatile Sprite gunShadow = null;
    // Per-frame shadows of the firing animation so the shadow recoils in step with the barrel.
    public static volatile Sequence gunFireShadow = null;
    public static volatile Sprite rubbleBaseSprite = null;
    public static volatile Sprite rubbleGunSprite = null;
    public static volatile Sequence deathFadeout = null;

    // Team-colored sprite/sequence maps
    private static final Map<Integer, Sprite>   baseSpriteMap       = new HashMap<>();
    private static final Map<Integer, Sprite>   gunSpriteMap        = new HashMap<>();
    private static final Map<Integer, Sprite>   gunDamagedSpriteMap = new HashMap<>();
    private static final Map<Integer, Sequence> fireAnimMap         = new HashMap<>();
    private static final Map<Integer, Sequence> fireAnimDamagedMap  = new HashMap<>();

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (!baseSpriteMap.isEmpty()) return;

        rubbleBaseSprite = new Sprite(RTSAssetManager.t2TurretBaseDestroyed);
        rubbleGunSprite = new Sprite(RTSAssetManager.t2TurretGunDestroyed);
        rubbleBaseSprite.applyAlphaEdgeBlurSelf(1);
        rubbleGunSprite.applyAlphaEdgeBlurSelf(1);
        deathFadeout = Sequence.createFadeout(RTSAssetManager.t2TurretBaseDestroyed, 40);
        deathFadeout.setSignature("fadeout");

        baseShadow = Sprite.generateShadowSprite(RTSAssetManager.t2TurretBase, .8);
        gunShadow = Sprite.generateShadowSprite(RTSAssetManager.t2TurretGun, .8);
        gunFireShadow = Sequence.generateShadowSequence(RTSAssetManager.getT2TurretFire(0), .8);
        baseShadow.applyAlphaEdgeBlurSelf(4);
        gunShadow.applyAlphaEdgeBlurSelf(3);
        gunFireShadow.applyAlphaEdgeBlurSelf(3);
        baseShadow.scaleTo(VISUAL_SCALE);
        gunShadow.scaleTo(VISUAL_SCALE);
        gunFireShadow.scaleTo(VISUAL_SCALE);

        for (int team : RTSGame.activeTeams) {
            Sprite emplacement = new Sprite(RTSAssetManager.getT2TurretBase(team));
            emplacement.applyAlphaEdgeBlurSelf(1);
            baseSpriteMap.put(team, emplacement);

            Sprite barrel = new Sprite(RTSAssetManager.getT2TurretGun(team));
            barrel.applyAlphaEdgeBlurSelf(1);
            gunSpriteMap.put(team, barrel);

            Sprite barrelDamaged = new Sprite(RTSAssetManager.getT2TurretGunDamaged(team));
            barrelDamaged.applyAlphaEdgeBlurSelf(1);
            barrelDamaged.setSignature("damagedGun");
            gunDamagedSpriteMap.put(team, barrelDamaged);

            Sequence fire = new Sequence(RTSAssetManager.getT2TurretFire(team), "t2TurretFire");
            fire.setSignature("fireAnimation");
            fire.setFrameDelay(35);
            fire.applyAlphaEdgeBlurSelf(1);
            fireAnimMap.put(team, fire);

            Sequence fireDamaged = new Sequence(RTSAssetManager.getT2TurretFireDamaged(team));
            fireDamaged.setSignature("fireAnimation");
            fireDamaged.setFrameDelay(35);
            fireDamaged.applyAlphaEdgeBlurSelf(1);
            fireAnimDamagedMap.put(team, fireDamaged);
        }
    }

    public T2Turret(Coordinate c, int team) {
        super(c, team);
        init();
    }

    public T2Turret(int x, int y, int team) {
        super(x, y, team);
        init();
    }

    private void init() {
        isSolid = true;
        preventOverlap = true;
        setScale(VISUAL_SCALE);
        this.setGraphic(getBaseSprite());
        gun = new Gun(new Coordinate(0, 0));
        this.addSubObject(gun);
        this.maxHealth = 80;
        this.currentHealth = maxHealth;
        this.baseSpeed = 0;
        this.mass = 5000;
        this.baseRange = 750;
        this.sightRadius = 650;
        this.cargoSize = -1;
    }

    public Sprite getBaseSprite() {
        return isRubble ? rubbleBaseSprite : baseSpriteMap.get(team);
    }

    public boolean isDamaged() {
        return currentHealth > 0 && currentHealth < maxHealth / 3;
    }

    @Override
    public int getWidth() {
        // consistent width so that width is not tied to animation frame
        return (int)(baseSpriteMap.get(0).getWidth() * VISUAL_SCALE);
    }

    @Override
    public int getHeight() {
        // consistent height so that height is not tied to animation frame
        return (int)(baseSpriteMap.get(0).getHeight() * VISUAL_SCALE);
    }

    /**
     * The emplacement is anchored, so move orders are ignored.
     */
    @Override
    public void setDesiredLocation(Coordinate c) {
    }

    @Override
    public void setRotation(double r, boolean includeGun) {
        super.setRotation(r);
        if (includeGun) this.gun.setRotation(r);
    }

    @Override
    public void onPostDeserialization() {
        super.onPostDeserialization();
        // Restore graphics after deserialization
        this.setGraphic(getBaseSprite());
        if (gun != null) {
            gun.setGraphic(gun.getGunSprite());
        }
    }

    @Override
    public void onAnimationCycle() {
        if ("fadeout".equals(getGraphic().getSignature())) {
            this.isInvisible = true;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!shouldRender()) return;
        if (isSolid) {
            drawShadow(g, baseShadow, 1, 1);
        }
        super.render(g);
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
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "transientExplosion"), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.setZLayer(-100);
            this.gun.isInvisible = true;
            destructionScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 3);
            fadeoutScheduledAtTick = 0;
            return;
        }

        // Check weapon cooldown expiration
        if (weaponCooldownExpiresAtTick > 0 && getHostGame().getGameTickNumber() >= weaponCooldownExpiresAtTick) {
            weaponCooldownExpiresAtTick = 0;
        }
    }

    /*
    when the emplacement tries to fire, it first checks if the gun is still firing.
    if not, tell the gun to fire at the target location
     */
    public void fire(Coordinate target) {
        if (weaponCooldownExpiresAtTick > 0 || target.distanceFrom(getLocation()) < getHeight() * 3 / 5 || Math.abs(gun.rotationNeededToFace(target)) > 1) {
            // Not able to fire this tick (on cooldown, too close, or not aimed) — abandon any windup in progress.
            windupTargetId = null;
            windupStartTick = 0;
            return;
        }
        // Aimed and able to fire. Require a held firing solution on this specific target for windupSeconds before shooting.
        String targetId = currentTarget != null ? currentTarget.ID : null;
        if (!java.util.Objects.equals(targetId, windupTargetId)) {
            windupTargetId = targetId;
            windupStartTick = getHostGame().getGameTickNumber();
            return;
        }
        long windupTicks = (long) (RTSGame.desiredTPS * windupSeconds);
        if (getHostGame().getGameTickNumber() - windupStartTick < windupTicks) {
            return; // still winding up
        }
        windupTargetId = null;
        windupStartTick = 0;
        weaponCooldownExpiresAtTick = getHostGame().getGameTickNumber() + (int) (RTSGame.desiredTPS * attackFrequency);
        gun.onFire(target);
    }

    @Override
    public void die() {
        if (this.isRubble) {
            return;
        }
        OnceThroughSticker deathExplosion = new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence, "transientExplosion"), getPixelLocation());
        getHostGame().addIndependentEffect(new SmokePoofEffect(getHostGame(), getPixelLocation(), 24, getZLayer() + 1));
        this.isRubble = true;
        this.team = -1;
        this.setGraphic(rubbleBaseSprite);
        gun.setGraphic(rubbleGunSprite);
        RTSSoundManager.get().play(RTSSoundManager.TANK_DEATH, getLocation(), Main.generateRandomDoubleLocally(4.6, 6.9));
        fadeoutScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 10);
    }

    @Override
    public java.awt.image.BufferedImage getSelectionImage() {
        return RTSAssetManager.t2TurretSelectionImage;
    }

    @Override
    public ArrayList<String> getInfoLines() {
        var out = new ArrayList<String>();
        out.add("Dmg: " + T2Bullet.staticDamage + "    Interval: " + attackFrequency + "s    Range: " + getRange());
        out.add("Cannot move    Targets: Ground");
        return out;
    }

    public class Gun extends SubObject {

        private double gunRotationSpeed = 0.0;

        public Gun(Coordinate offset) {
            super(offset);
            setScale(VISUAL_SCALE);
            this.setGraphic(getGunSprite());
        }

        public Sequence getFireSequence() {
            return isDamaged() ? fireAnimDamagedMap.get(team).copyMaintainSource() : fireAnimMap.get(team).copyMaintainSource();
        }

        public Sprite getGunSprite() {
            if (isRubble) return rubbleGunSprite;
            return isDamaged() ? gunDamagedSpriteMap.get(team) : gunSpriteMap.get(team);
        }

        /*
        fires the gun at the location.
        first, play the firing animation on the gun, then create the shell object
        and spawn it into the game world followed by the muzzle smoke
         */
        public void onFire(Coordinate target) {
            setGraphic(getFireSequence());
            try {
                RTSSoundManager.get().play(
                    RTSSoundManager.T2_TURRET_ATTACK,
                    getLocation(),
                    Main.generateRandomDoubleLocally(8.1, 13.2),
                    Main.generateRandomIntLocally(0, 40));
            } catch (Exception e) {
                e.printStackTrace();
            }
            DCoordinate muzzelLocation = new DCoordinate(0, 0);
            muzzelLocation.y -= getHeight() * MUZZLE_OFFSET_FRACTION;
            muzzelLocation = DCoordinate.adjustForRotation(muzzelLocation, getRotationRealTime());
            muzzelLocation.add(getPixelLocation());
            RTSUnit targetUnit = ((RTSUnit) this.getHost()).currentTarget;
            int longestSide = Math.max(targetUnit.getWidth(), targetUnit.getHeight());
            Coordinate offset = new Coordinate(
                    Main.generateRandomIntFromSeed(-longestSide / 3, longestSide / 3, getHostGame().getGameTickNumber() + (int) getLocationAsOfLastTick().x),
                    Main.generateRandomIntFromSeed(-longestSide / 3, longestSide / 3, getHostGame().getGameTickNumber() + (int) getLocationAsOfLastTick().y)
            );
            target.add(offset);
            T2Bullet bullet = new T2Bullet(muzzelLocation, target.toDCoordinate(), ((RTSUnit) this.getHost()).getPreferredTargetId());
            bullet.shooter = this.getHost();
            getHostGame().addObject(bullet);

            // Cosmetic puff of gun smoke jetting from the barrel tip along the shot direction.
            DCoordinate forward = DCoordinate.adjustForRotation(new DCoordinate(0, -1), getRotationRealTime());
            // Pull the puff back toward the gun so it reads as coming from the barrel rather than out in front of it.
            DCoordinate smokeLocation = muzzelLocation.copy();
            smokeLocation.x -= forward.x * 10;
            smokeLocation.y -= forward.y * 10;
            getHostGame().addIndependentEffect(new MuzzleSmokeEffect(
                    getHostGame(), smokeLocation.toCoordinate(), forward.x, forward.y, 8, getZLayer() + 1));
        }

        /*
        this runs whenever an animation cycle ends.
        here we use it to put the gun back on its regular sprite once the
        firing animation has played through
         */
        @Override
        public void onAnimationCycle() {
            if ("fireAnimation".equals(getGraphic().getSignature())) {
                setGraphic(getGunSprite());
            }
            if (isRubble) {
                setGraphic(rubbleGunSprite);
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (isRubble) {
                return;
            }
            if (currentHealth > 0 && !getGraphic().isAnimated()) {
                this.setGraphic(getGunSprite());
            }

            RTSUnit preferred = ((RTSUnit) getHost()).getPreferredTargetIfInRange();
            RTSUnit enemy = preferred != null ? preferred : nearestEnemyInRange();
            ((RTSUnit) getHost()).currentTarget = enemy;

            double desiredRotation;
            double maxSpeed = RTSGame.tickAdjust(0.7);
            if (enemy == null) {
                // Settle back to the facing the emplacement was placed at
                desiredRotation = getHost().getRotationRealTime() - getRotationRealTime();
                if (desiredRotation > 180) desiredRotation -= 360;
                else if (desiredRotation < -180) desiredRotation += 360;
            } else {
                desiredRotation = rotationNeededToFace(enemy.getPixelLocation());
            }

            final double accel = RTSGame.tickAdjust(0.15);
            double targetSpeed = Math.abs(desiredRotation) < 0.01 ? 0.0 : Math.copySign(maxSpeed, desiredRotation);

            if (gunRotationSpeed < targetSpeed) {
                gunRotationSpeed = Math.min(gunRotationSpeed + accel, targetSpeed);
            } else if (gunRotationSpeed > targetSpeed) {
                gunRotationSpeed = Math.max(gunRotationSpeed - accel, targetSpeed);
            }

            if (Math.abs(desiredRotation) <= Math.abs(gunRotationSpeed)) {
                rotate(desiredRotation);
                gunRotationSpeed = 0;
            } else {
                rotate(gunRotationSpeed);
            }

            if (enemy != null) {
                ((T2Turret) getHost()).fire(enemy.getPixelLocation());
            }
        }

        @Override
        public void render(Graphics2D g) {
            if (!((RTSUnit) getHost()).shouldRender()) return;
            if (getHost().isSolid) {
                AffineTransform old = g.getTransform();
                // While the gun is playing its fire animation, cast the matching recoiled
                // shadow frame so the shadow retracts with the barrel; otherwise the resting shadow.
                VolatileImage toRender;
                if (getGraphic() instanceof Sequence seq && "fireAnimation".equals(seq.getSignature())) {
                    int frame = Math.min(seq.getCurrentFrameIndex(), gunFireShadow.frames.length - 1);
                    toRender = gunFireShadow.frames[frame].getCurrentVolatileImage();
                } else {
                    toRender = gunShadow.getCurrentVolatileImage();
                }
                DCoordinate gunRenderLoc = getRenderLocation();
                int shadowOffsetY = 6;
                int shadowOffsetX = 6;
                double renderX = gunRenderLoc.x - toRender.getWidth() / 2.0;
                double renderY = gunRenderLoc.y - toRender.getHeight() / 2.0;
                g.rotate(Math.toRadians(getRotationRealTime()), gunRenderLoc.x + shadowOffsetX, gunRenderLoc.y + shadowOffsetY);
                g.drawImage(toRender, AffineTransform.getTranslateInstance(renderX, renderY + shadowOffsetY), null);
                g.setTransform(old);
            }
            super.render(g);
        }
    }
}

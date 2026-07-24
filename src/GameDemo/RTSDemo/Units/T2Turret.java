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
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Stationary 88mm gun emplacement. The base is fixed in place; the turret on top traverses to
 * track targets and fires the same way a tank's main gun does.
 *
 * The turret is composited in code from three stacked parts sharing one canvas (render order:
 * mount, gun, shield). Only the gun slides back on fire — the mount and ballistic shield stay
 * fixed — so the recoil is animated procedurally rather than baked into sprite frames. This lets
 * damaged variants of any single part drop in and stay compatible with the recoil automatically.
 *
 * @author Joseph
 */
public class T2Turret extends RTSUnit {

    public static final double attackFrequency = 2.15;
    // Seconds the gun must hold a valid firing solution on a target before it can shoot.
    public static final double windupSeconds = 0.5;
    public static final double VISUAL_SCALE = .55;
    /** Distance from the traverse pivot to the muzzle, as a fraction of the turret sprite's height. */
    private static final double MUZZLE_OFFSET_FRACTION = .46;

    // Recoil curve, measured from the old baked frames: the gun snaps back ~33px (at source
    // resolution), holds briefly, then eases forward to battery. Timings are in seconds.
    private static final double RECOIL_PEAK_SOURCE_PX = 33.0;
    private static final double RECOIL_KICK_SECONDS = 0.06;   // fast snap to full recoil
    private static final double RECOIL_HOLD_SECONDS = 0.14;   // held at peak
    private static final double RECOIL_RETURN_SECONDS = 0.60; // linear return to rest

    public Turret turret;
    public long weaponCooldownExpiresAtTick = 0;
    // Windup state: which target we are currently winding up on, and the tick the windup began.
    public String windupTargetId = null;
    public long windupStartTick = 0;
    private long fadeoutScheduledAtTick = 0;
    private long destructionScheduledAtTick = 0;
    /** Game tick the current recoil began; -1 when the gun is at rest. */
    public long recoilStartTick = -1;

    // Team-neutral sprites
    public static volatile Sprite baseShadow = null;
    public static volatile Sprite turretShadow = null;   // static parts (mount + shield)
    public static volatile Sprite gunShadow = null;      // recoils with the gun
    public static volatile Sprite gunSizingSprite = null;
    public static volatile Sprite rubbleBaseSprite = null;
    public static volatile Sprite rubbleTurretSprite = null;
    public static volatile Sequence deathFadeout = null;

    // Team-colored part sprites (each pre-scaled to VISUAL_SCALE for manual compositing)
    private static final Map<Integer, Sprite> baseSpriteMap          = new HashMap<>();
    private static final Map<Integer, Sprite> baseDamagedSpriteMap   = new HashMap<>();
    private static final Map<Integer, Sprite> mountSpriteMap         = new HashMap<>();
    private static final Map<Integer, Sprite> mountDamagedSpriteMap  = new HashMap<>();
    private static final Map<Integer, Sprite> gunSpriteMap           = new HashMap<>();
    private static final Map<Integer, Sprite> gunDamagedSpriteMap    = new HashMap<>();
    private static final Map<Integer, Sprite> shieldSpriteMap        = new HashMap<>();
    private static final Map<Integer, Sprite> shieldDamagedSpriteMap = new HashMap<>();

    static {
        initGraphics();
    }

    public static void initGraphics() {
        if (!baseSpriteMap.isEmpty()) return;

        rubbleBaseSprite = new Sprite(RTSAssetManager.t2TurretBaseDestroyed);
        rubbleBaseSprite.applyAlphaEdgeBlurSelf(1);
        rubbleTurretSprite = partSprite(RTSAssetManager.t2TurretGunDestroyed);
        deathFadeout = Sequence.createFadeout(RTSAssetManager.t2TurretBaseDestroyed, 40);
        deathFadeout.setSignature("fadeout");

        // Sizing-only graphic for the turret subobject (never drawn; render() composites the parts).
        gunSizingSprite = new Sprite(RTSAssetManager.t2Gun);

        baseShadow = Sprite.generateShadowSprite(RTSAssetManager.t2TurretBase, .8);
        baseShadow.applyAlphaEdgeBlurSelf(4);
        baseShadow.scaleTo(VISUAL_SCALE);

        // Shadow is split so the gun's shadow can recoil with it: the fixed parts (mount + shield)
        // share one static shadow; the gun gets its own that slides back on fire.
        turretShadow = Sprite.generateShadowSprite(compositeParts(
                RTSAssetManager.t2Mount, RTSAssetManager.t2Shield), .8);
        turretShadow.applyAlphaEdgeBlurSelf(3);
        gunShadow = Sprite.generateShadowSprite(RTSAssetManager.t2Gun, .8);
        gunShadow.applyAlphaEdgeBlurSelf(3);

        for (int team : RTSGame.activeTeams) {
            Sprite emplacement = new Sprite(RTSAssetManager.getT2TurretBase(team));
            emplacement.applyAlphaEdgeBlurSelf(1);
            baseSpriteMap.put(team, emplacement);

            Sprite emplacementDamaged = new Sprite(RTSAssetManager.getT2TurretBaseDamaged(team));
            emplacementDamaged.applyAlphaEdgeBlurSelf(1);
            baseDamagedSpriteMap.put(team, emplacementDamaged);

            mountSpriteMap.put(team,         partSprite(RTSAssetManager.getT2Mount(team)));
            mountDamagedSpriteMap.put(team,  partSprite(RTSAssetManager.getT2MountDamaged(team)));
            gunSpriteMap.put(team,           partSprite(RTSAssetManager.getT2Gun(team)));
            gunDamagedSpriteMap.put(team,    partSprite(RTSAssetManager.getT2GunDamaged(team)));
            shieldSpriteMap.put(team,        partSprite(RTSAssetManager.getT2Shield(team)));
            shieldDamagedSpriteMap.put(team, partSprite(RTSAssetManager.getT2ShieldDamaged(team)));
        }
    }

    /**
     * A part sprite kept at full resolution (scale 1). The VISUAL_SCALE is folded into the draw
     * transform at render time so each part is resampled only once, matching the engine's own
     * scale+rotate draw and avoiding the softness of a pre-scale followed by a rotate.
     */
    private static Sprite partSprite(BufferedImage raw) {
        Sprite s = new Sprite(raw);
        s.applyAlphaEdgeBlurSelf(1);
        return s;
    }

    /** Draws the given part images onto one canvas so a single shadow silhouette can be generated. */
    private static BufferedImage compositeParts(BufferedImage... parts) {
        BufferedImage out = new BufferedImage(parts[0].getWidth(), parts[0].getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        for (BufferedImage part : parts) g.drawImage(part, 0, 0, null);
        g.dispose();
        return out;
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
        turret = new Turret(new Coordinate(0, 0));
        this.addSubObject(turret);
        this.maxHealth = 80;
        this.currentHealth = maxHealth;
        this.baseSpeed = 0;
        this.mass = 5000;
        this.baseRange = 750;
        this.sightRadius = 650;
        this.cargoSize = -1;
    }

    public Sprite getBaseSprite() {
        if (isRubble) return rubbleBaseSprite;
        return isDamaged() ? baseDamagedSpriteMap.get(team) : baseSpriteMap.get(team);
    }

    public boolean isDamaged() {
        return currentHealth > 0 && currentHealth < maxHealth / 3;
    }

    public Sprite mountSprite()  { return isDamaged() ? mountDamagedSpriteMap.get(team)  : mountSpriteMap.get(team); }
    public Sprite gunSprite()    { return isDamaged() ? gunDamagedSpriteMap.get(team)    : gunSpriteMap.get(team); }
    public Sprite shieldSprite() { return isDamaged() ? shieldDamagedSpriteMap.get(team) : shieldSpriteMap.get(team); }

    /**
     * Current recoil displacement of the gun in source-image pixels along the barrel axis (0 at rest,
     * positive = retracted). The VISUAL_SCALE is applied by the render transform, so this is measured
     * in the same units as {@link #RECOIL_PEAK_SOURCE_PX}. Follows the measured curve: quick snap back,
     * brief hold, linear return. Purely cosmetic, so it is driven off the game tick.
     */
    public double currentRecoilSourcePixels() {
        if (recoilStartTick < 0 || getHostGame() == null) return 0;
        double elapsed = (getHostGame().getGameTickNumber() - recoilStartTick) / (double) RTSGame.desiredTPS;
        double frac;
        if (elapsed < RECOIL_KICK_SECONDS) {
            frac = elapsed / RECOIL_KICK_SECONDS;
        } else if (elapsed < RECOIL_KICK_SECONDS + RECOIL_HOLD_SECONDS) {
            frac = 1.0;
        } else if (elapsed < RECOIL_KICK_SECONDS + RECOIL_HOLD_SECONDS + RECOIL_RETURN_SECONDS) {
            frac = 1.0 - (elapsed - RECOIL_KICK_SECONDS - RECOIL_HOLD_SECONDS) / RECOIL_RETURN_SECONDS;
        } else {
            return 0;
        }
        return frac * RECOIL_PEAK_SOURCE_PX;
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
        if (includeGun) this.turret.setRotation(r);
    }

    @Override
    public void onPostDeserialization() {
        super.onPostDeserialization();
        // Restore graphics after deserialization
        this.setGraphic(getBaseSprite());
        if (turret != null) {
            turret.setGraphic(gunSizingSprite);
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
            this.turret.isInvisible = true;
            destructionScheduledAtTick = getHostGame().getGameTickNumber() + (RTSGame.desiredTPS * 3);
            fadeoutScheduledAtTick = 0;
            return;
        }

        // Check weapon cooldown expiration
        if (weaponCooldownExpiresAtTick > 0 && getHostGame().getGameTickNumber() >= weaponCooldownExpiresAtTick) {
            weaponCooldownExpiresAtTick = 0;
        }

        // Swap the base to its damaged sprite below 1/3 HP (the turret parts swap in their own render).
        if (!isRubble) {
            this.setGraphic(getBaseSprite());
        }
    }

    /*
    when the emplacement tries to fire, it first checks if it is off cooldown and aimed.
    if so, tell the turret to fire at the target location
     */
    public void fire(Coordinate target) {
        if (weaponCooldownExpiresAtTick > 0 || target.distanceFrom(getLocation()) < getHeight() * 3 / 5 || Math.abs(turret.rotationNeededToFace(target)) > 1) {
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
        turret.onFire(target);
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
        this.recoilStartTick = -1;
        this.setGraphic(rubbleBaseSprite);
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

    public class Turret extends SubObject {

        private double turretRotationSpeed = 0.0;

        public Turret(Coordinate offset) {
            super(offset);
            setScale(VISUAL_SCALE);
            // Sizing graphic only; render() composites the individual parts and does not draw this.
            this.setGraphic(gunSizingSprite);
        }

        /*
        fires the gun: kick off the recoil, play the report, spawn the shell, and puff muzzle smoke.
         */
        public void onFire(Coordinate target) {
            ((T2Turret) getHost()).recoilStartTick = getHostGame().getGameTickNumber();
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

        @Override
        public void tick() {
            super.tick();
            if (isRubble) {
                return;
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

            if (enemy != null) {
                ((T2Turret) getHost()).fire(enemy.getPixelLocation());
            }
        }

        /*
        Composites the turret from its parts each frame: mount, then the gun (slid back by the
        current recoil along the barrel axis), then the shield on top. All rotate about the pivot.
         */
        @Override
        public void render(Graphics2D g) {
            T2Turret host = (T2Turret) getHost();
            if (!host.shouldRender()) return;
            DCoordinate loc = getRenderLocation();
            double rotation = getRotationRealTime();
            double recoil = host.currentRecoilSourcePixels();

            if (host.isRubble) {
                if (host.isSolid) drawLayer(g, turretShadow.getCurrentVolatileImage(), loc, rotation, 3, 3, 0);
                drawLayer(g, rubbleTurretSprite.getCurrentVolatileImage(), loc, rotation, 0, 0, 0);
                return;
            }

            if (host.isSolid) {
                drawLayer(g, turretShadow.getCurrentVolatileImage(), loc, rotation, 3, 3, 0);      // mount + shield, static
                drawLayer(g, gunShadow.getCurrentVolatileImage(),    loc, rotation, 3, 3, recoil); // gun shadow recoils
            }
            drawLayer(g, host.mountSprite().getCurrentVolatileImage(),  loc, rotation, 0, 0, 0);
            drawLayer(g, host.gunSprite().getCurrentVolatileImage(),    loc, rotation, 0, 0, recoil);
            drawLayer(g, host.shieldSprite().getCurrentVolatileImage(), loc, rotation, 0, 0, 0);
        }

        /**
         * Draws a full-resolution part image centred on {@code loc} with VISUAL_SCALE and rotation
         * folded into a single transform (one resample, matching the engine's own draw). {@code offX}
         * /{@code offY} is a screen-space offset for the drop shadow; {@code recoilSrcPx} slides the
         * part down its local +Y axis in source pixels (before scaling) for the gun's recoil.
         */
        private void drawLayer(Graphics2D g, VolatileImage img, DCoordinate loc, double rotation,
                               int offX, int offY, double recoilSrcPx) {
            AffineTransform t = new AffineTransform();
            t.translate(loc.x + offX, loc.y + offY);
            t.rotate(Math.toRadians(rotation));
            t.scale(VISUAL_SCALE, VISUAL_SCALE);
            t.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0 + recoilSrcPx);
            g.drawImage(img, t, null);
        }
    }
}

package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.Effects.BurnMarkEffect;
import GameDemo.RTSDemo.Effects.ExhaustTrailEffect;
import GameDemo.RTSDemo.Effects.ExplosionEffect;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 * Missile fired by the Apache's Launch Missile ability. Flies to a ground coordinate,
 * then explodes dealing AoE damage to all non-flying enemy units within AOE_RADIUS.
 * Shadow converges toward the missile as it nears the target, and size decreases 0-5%.
 */
public class ApacheMissile extends Projectile {

    public static final int AOE_RADIUS = 80;
    public static final int DAMAGE_AMOUNT = 40;
    private static final int INITIAL_SHADOW_OFFSET = 149;
    private static final double LAUNCH_SPEED = RTSGame.tickAdjust(9.0);
    private static final double TERMINAL_SPEED = RTSGame.tickAdjust(4.0);

    public static volatile Sprite missileSprite = null;
    public static volatile Sprite shadowSprite = null;

    private final Apache shooter;
    private final Coordinate targetCoord;
    private final double initialDistance;
    private final double scaleLoss;
    private boolean hasExploded = false;

    // Cosmetic exhaust trail; transient so it re-attaches after a save/load.
    private transient boolean exhaustSpawned = false;

    public static void initGraphics() {
        if (missileSprite != null) return;
        missileSprite = new Sprite(RTSAssetManager.apacheMissileProjectile);
        missileSprite.applyAlphaEdgeBlurSelf(2);
        // shadow is drawn manually so needs its scale baked in via scaleTo
        shadowSprite = Sprite.generateShadowSprite(RTSAssetManager.apacheMissileProjectile, 0.7);
        shadowSprite.scaleTo(Apache.VISUAL_SCALE);
        shadowSprite.applyAlphaEdgeBlurSelf(3);
    }

    public ApacheMissile(Apache shooter, Coordinate spawnPos, Coordinate targetCoord, int missileIndex) {
        super(spawnPos);
        this.shooter = shooter;
        this.targetCoord = targetCoord.copy();
        this.setScale(Apache.VISUAL_SCALE);
        this.setGraphic(missileSprite);
        this.setZLayer(10);
        this.plane = 2;
        this.isSolid = false;
        this.setBaseSpeed(LAUNCH_SPEED);
        this.initialDistance = spawnPos.distanceFrom(targetCoord);
        this.maxRange = initialDistance + 80;

        
        this.scaleLoss = .12;

        this.launch(new DCoordinate(targetCoord.x, targetCoord.y));
    }

    @Override
    public void onPostDeserialization() {
        this.setGraphic(missileSprite);
    }

    @Override
    public void tick() {
        super.tick();

        // Lazily attach a small exhaust trail once we have a host game (not available at construction).
        if (!exhaustSpawned && getHostGame() != null) {
            getHostGame().addIndependentEffect(new ExhaustTrailEffect(
                    getHostGame(), this, () -> !hasExploded,
                    getHeight() * 0.5, 0, 4.5, Math.max(1, RTSGame.desiredTPS / 30), getZLayer() - 1));
            exhaustSpawned = true;
        }

        if (!hasExploded) {
            double dist = Coordinate.distanceBetween(getPixelLocation(), targetCoord);
            double progress = (initialDistance > 0) ? Math.max(0.0, Math.min(1.0, 1.0 - dist / initialDistance)) : 1.0;
            // Decelerate hard as it nears the target so it flies almost overhead, then tanks down onto it
            double speedFall = progress * progress;
            setBaseSpeed(LAUNCH_SPEED + (TERMINAL_SPEED - LAUNCH_SPEED) * speedFall);
            setRenderScale(1.0 - scaleLoss * progress);
            setRenderBrightness(1.0 - scaleLoss * progress);

            double dx = targetCoord.x - getLocation().x;
            double dy = targetCoord.y - getLocation().y;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 5) {
                double perpX = -dy / len;
                double perpY = dx / len;
                double snakeAmp = 20.0 * (1.0 - progress * progress);
                double snakeVal = Math.sin(getHostGame().getGameTickNumber() * 0.15) * snakeAmp;
                launch(new DCoordinate(
                        targetCoord.x + perpX * snakeVal,
                        targetCoord.y + perpY * snakeVal));
            }

            if (dist <= RTSGame.tickAdjust(16) + 5) {
                hasExploded = true;
                destroy();
            }
        }
    }

    @Override
    public void onDestroy() {
        getHostGame().addIndependentEffect(new ExplosionEffect(getHostGame(), getPixelLocation(),
                (int) (AOE_RADIUS * 0.4), (int) (RTSGame.desiredTPS * 0.45), 500));
        getHostGame().addIndependentEffect(new BurnMarkEffect(getHostGame(), getPixelLocation(),
                RTSAssetManager.apacheBurnMark, AOE_RADIUS, RTSGame.desiredTPS * 5, -10));
        hasExploded = true;
        performAoeExplosion();
        shooter.onMissileExploded();
    }

    private void performAoeExplosion() {
        ArrayList<RTSUnit> targets = new ArrayList<>();
        for (GameObject2 obj : getHostGame().getObjectsNearPoint(getPixelLocation(), AOE_RADIUS)) {
            if (obj instanceof RTSUnit unit
                    && unit.team != shooter.team
                    && !unit.isRubble
                    && unit.plane < 2
                    && !unit.isCloaked) {
                targets.add(unit);
            }
        }
        // sort by ID for deterministic ordering in multiplayer
        targets.sort((a, b) -> a.ID.compareTo(b.ID));
        int lookahead = 0;
        for (RTSUnit unit : targets) {
            int roll = Main.generateDeterministicRandomInt(0, 99, lookahead++);
            if (roll >= unit.getDodgeChance()) {
                Damage dmg = new Damage(DAMAGE_AMOUNT);
                dmg.source = shooter;
                dmg.launchLocation = shooter.getPixelLocation();
                dmg.impactLoaction = getPixelLocation();
                unit.takeDamage(dmg);
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (shadowSprite == null) return;
        Coordinate pixLoc = getPixelLocation();
        double dist = Coordinate.distanceBetween(pixLoc, targetCoord);
        double progress = (initialDistance > 0) ? Math.max(0.0, Math.min(1.0, 1.0 - dist / initialDistance)) : 1.0;
        double scale = 1.0 - scaleLoss * progress;

        AffineTransform saved = g.getTransform();

        // Shadow converges toward missile as it nears the ground target
        int shadowOffset = (int) (INITIAL_SHADOW_OFFSET * (1.0 - progress));
        VolatileImage shadowImg = shadowSprite.getCurrentVolatileImage();
        if (shadowImg != null) {
            AffineTransform at = new AffineTransform(saved);
            at.translate(pixLoc.x, pixLoc.y + shadowOffset);
            at.rotate(Math.toRadians(getRotation()));
            at.scale(scale, scale);
            at.translate(-shadowImg.getWidth() / 2.0, -shadowImg.getHeight() / 2.0);
            g.setTransform(at);
            g.drawImage(shadowImg, 0, 0, null);
            g.setTransform(saved);
        }

        drawProceduralMissile(g, pixLoc, getRotation(), Apache.VISUAL_SCALE * scale, progress);
    }

    private static double smoothstep(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3 - 2 * t);
    }

    private static double smoothRange(double x, double lo, double hi) {
        return smoothstep((x - lo) / (hi - lo));
    }

    /** Teardrop flame shape rooted at the tail (nose-up local space), pointing +Y behind the missile. */
    private static Path2D.Double flameShape(double tail, double len, double w) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(-w, tail - 1);
        p.quadTo(-w, tail + len * 0.55, 0, tail + len);
        p.quadTo(w, tail + len * 0.55, w, tail - 1);
        p.quadTo(0, tail + 2, -w, tail - 1);
        p.closePath();
        return p;
    }

    /**
     * Draws the missile in Java2D rather than as a flat sprite so it can visually "nose over"
     * as it descends. Local space has the missile nose pointing up (-Y); the caller's rotation
     * aims it along its travel direction. As {@code progress} climbs the missile foreshortens
     * along its own length (pivoting about the tail), so from the top-down camera we increasingly
     * look down its tail: the side blades give way to an X of fins around a revealed exhaust nozzle.
     */
    private void drawProceduralMissile(Graphics2D g, Coordinate pos, double rotationDeg, double totalScale, double progress) {
        double dive = smoothstep(progress);
        double fore = 1.0 - 0.70 * dive; // length projection: 1.0 flat -> ~0.30 nose-down

        final double halfW = 5.2;
        final double noseTip = -34;
        final double noseBase = -17;
        final double bodyTop = -17;
        final double tail = 32;

        double sideFinAlpha = 1.0 - smoothRange(progress, 0.15, 0.55);
        double xFinAlpha = smoothRange(progress, 0.30, 0.85);

        AffineTransform savedTx = g.getTransform();
        Paint savedPaint = g.getPaint();
        Composite savedComposite = g.getComposite();
        Object savedAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // base: local units, nose up, no foreshortening (used for the tail face which stays end-on)
        AffineTransform base = new AffineTransform(savedTx);
        base.translate(pos.x, pos.y);
        base.rotate(Math.toRadians(rotationDeg));
        base.scale(totalScale, totalScale);

        // foreshortened frame, squashed along the length about the invariant tail point
        AffineTransform atFore = new AffineTransform(base);
        atFore.translate(0, tail);
        atFore.scale(1.0, fore);
        atFore.translate(0, -tail);

        final Color finColor = new Color(0x22, 0x24, 0x28);

        // --- Motor exhaust flame trailing off the tail (flickers; foreshortens with the body) ---
        g.setTransform(atFore);
        double flicker = 0.85 + 0.15 * Math.sin(getHostGame().getGameTickNumber() * 0.9);
        double burn = 1.0 - 0.7 * dive; // motor eases off as the missile decelerates and noses down
        double flameLen = 16 * burn * flicker;
        double flameW = halfW * (0.85 - 0.15 * dive);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.85 * burn)));
        g.setPaint(new Color(255, 140, 40));
        g.fill(flameShape(tail, flameLen, flameW));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.9 * burn)));
        g.setPaint(new Color(255, 236, 170));
        g.fill(flameShape(tail, flameLen * 0.6, flameW * 0.5));

        // --- Side-profile fins: swept blades on the flanks near the tail ---
        if (sideFinAlpha > 0.02) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) sideFinAlpha));
            g.setPaint(finColor);
            for (int side = -1; side <= 1; side += 2) {
                Path2D.Double fin = new Path2D.Double();
                fin.moveTo(side * halfW, tail - 16);
                fin.lineTo(side * (halfW + 6.5), tail + 4);
                fin.lineTo(side * (halfW - 0.5), tail + 1);
                fin.closePath();
                g.fill(fin);
            }
        }

        // --- End-on fins: an X of blades around the tail, drawn un-squashed so the face stays round ---
        g.setTransform(base);
        if (xFinAlpha > 0.02) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) xFinAlpha));
            g.setPaint(finColor);
            double r = 7 + 5 * xFinAlpha;
            for (int k = 0; k < 4; k++) {
                double a = Math.toRadians(45 + k * 90);
                double pa = a + Math.PI / 2;
                double bx = Math.cos(pa) * 2.6;
                double by = Math.sin(pa) * 2.6;
                Path2D.Double blade = new Path2D.Double();
                blade.moveTo(bx, tail + by);
                blade.lineTo(Math.cos(a) * r, tail + Math.sin(a) * r);
                blade.lineTo(-bx, tail - by);
                blade.closePath();
                g.fill(blade);
            }
        }

        // --- Body (cylindrical tube shading), foreshortened ---
        g.setTransform(atFore);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(-halfW, 0), new Point2D.Double(halfW, 0),
                new float[]{0f, 0.42f, 1f},
                new Color[]{new Color(0x8f, 0x98, 0x9f), new Color(0x3e, 0x43, 0x49), new Color(0x1b, 0x1d, 0x21)}));
        g.fill(new RoundRectangle2D.Double(-halfW, bodyTop, 2 * halfW, tail - bodyTop, halfW * 1.2, halfW * 1.2));

        // --- Nose cone (lighter ogive) ---
        g.setPaint(new LinearGradientPaint(
                new Point2D.Double(-halfW, 0), new Point2D.Double(halfW, 0),
                new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(0xc6, 0xd0, 0xd8), new Color(0x94, 0x9f, 0xa8), new Color(0x5e, 0x66, 0x6e)}));
        Path2D.Double nose = new Path2D.Double();
        nose.moveTo(-halfW, noseBase);
        nose.quadTo(-halfW, noseTip + 4, 0, noseTip);
        nose.quadTo(halfW, noseTip + 4, halfW, noseBase);
        nose.closePath();
        g.fill(nose);

        // seeker tip
        g.setPaint(new Color(0xdc, 0xc6, 0x7e));
        g.fill(new Ellipse2D.Double(-1.7, noseTip + 1, 3.4, 3.4));

        // --- Exhaust nozzle + glow: comes into view as we look down the tail (end-on frame) ---
        g.setTransform(base);
        if (xFinAlpha > 0.02) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) xFinAlpha));
            g.setPaint(new Color(0x0d, 0x0e, 0x11));
            double nr = halfW * 0.95;
            g.fill(new Ellipse2D.Double(-nr, tail - nr, 2 * nr, 2 * nr));
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(0, tail), (float) nr,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 200, 120, 220), new Color(255, 120, 40, 0)}));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (xFinAlpha * 0.9)));
            g.fill(new Ellipse2D.Double(-nr * 0.7, tail - nr * 0.7, 2 * nr * 0.7, 2 * nr * 0.7));
        }

        g.setTransform(savedTx);
        g.setPaint(savedPaint);
        g.setComposite(savedComposite);
        if (savedAA != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedAA);
        }
    }
}

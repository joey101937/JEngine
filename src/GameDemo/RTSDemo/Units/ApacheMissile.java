package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
    private static final double INITIAL_SPEED = RTSGame.tickAdjust(1.5);
    private static final double MAX_SPEED = RTSGame.tickAdjust(9);
    private static final double SPEED_ACCEL = RTSGame.tickAdjust(0.35);

    public static volatile Sprite missileSprite = null;
    public static volatile Sprite shadowSprite = null;

    private final Apache shooter;
    private final Coordinate targetCoord;
    private final double initialDistance;
    private final double scaleLoss;
    private boolean hasExploded = false;

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
        this.setBaseSpeed(INITIAL_SPEED);
        this.initialDistance = spawnPos.distanceFrom(targetCoord);
        this.maxRange = initialDistance + 80;

        
        this.scaleLoss = .2;

        this.launch(new DCoordinate(targetCoord.x, targetCoord.y));
    }

    @Override
    public void onPostDeserialization() {
        this.setGraphic(missileSprite);
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasExploded) {
            setBaseSpeed(Math.min(MAX_SPEED, getBaseSpeed() + SPEED_ACCEL));
            double dist = Coordinate.distanceBetween(getPixelLocation(), targetCoord);
            double progress = (initialDistance > 0) ? Math.max(0.0, Math.min(1.0, 1.0 - dist / initialDistance)) : 1.0;
            setRenderScale(1.0 - scaleLoss * progress);
            setRenderBrightness(1.0 - scaleLoss * progress);
            if (dist <= RTSGame.tickAdjust(16) + 5) {
                hasExploded = true;
                destroy();
            }
        }
    }

    @Override
    public void onDestroy() {
        new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
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

        super.render(g);
    }
}

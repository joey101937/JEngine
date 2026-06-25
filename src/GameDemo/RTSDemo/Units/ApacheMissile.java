package GameDemo.RTSDemo.Units;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;
import GameDemo.RTSDemo.BurnMarkEffect;
import GameDemo.RTSDemo.Damage;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
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
    private static final double INITIAL_SPEED = RTSGame.tickAdjust(7.0);
    private static final double MIN_SPEED = RTSGame.tickAdjust(6.5);

    public static volatile Sprite missileSprite = null;
    public static volatile Sprite shadowSprite = null;

    private static final int TRAIL_LENGTH = 10;
    private static final Color TRAIL_COLOR = new Color(210, 210, 210);

    private final Apache shooter;
    private final Coordinate targetCoord;
    private final double initialDistance;
    private final double scaleLoss;
    private boolean hasExploded = false;
    private final Coordinate[] trail = new Coordinate[TRAIL_LENGTH];
    private int trailHead = 0;

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

        
        this.scaleLoss = .15;

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
            double dist = Coordinate.distanceBetween(getPixelLocation(), targetCoord);
            double progress = (initialDistance > 0) ? Math.max(0.0, Math.min(1.0, 1.0 - dist / initialDistance)) : 1.0;
            setBaseSpeed(INITIAL_SPEED - (INITIAL_SPEED - MIN_SPEED) * progress);
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
        new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
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

        // Record position for smoke trail
        trail[trailHead] = new Coordinate(pixLoc.x, pixLoc.y);
        trailHead = (trailHead + 1) % TRAIL_LENGTH;

        // Draw smoke trail behind missile, condensing toward missile as it descends
        Coordinate missileRenderPos = trail[(trailHead - 1 + TRAIL_LENGTH) % TRAIL_LENGTH];
        Composite oldComposite = g.getComposite();
        for (int i = 0; i < TRAIL_LENGTH; i++) {
            int idx = (trailHead - 1 - i + TRAIL_LENGTH) % TRAIL_LENGTH;
            Coordinate tp = trail[idx];
            if (tp == null || missileRenderPos == null) break;
            double spread = Math.max(0.05, 1.0 - progress * 0.95);
            int cx = missileRenderPos.x + (int) ((tp.x - missileRenderPos.x) * spread);
            int cy = missileRenderPos.y + (int) ((tp.y - missileRenderPos.y) * spread);
            float alpha = (1.0f - (float) i / TRAIL_LENGTH) * 0.45f;
            int radius = Math.max(1, (int) (4 * scale * (1.0 - (double) i / TRAIL_LENGTH * 0.6)));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(TRAIL_COLOR);
            g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }
        g.setComposite(oldComposite);

        super.render(g);
    }
}

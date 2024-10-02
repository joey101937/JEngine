package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.ArrayList;

/**
 * the hellicopter itself is invisible, instead we render the subobject to
 * simply movement and rotation
 *
 * @author guydu
 */
public class Hellicopter extends RTSUnit {

    public static final double VISUAL_SCALE = 1.05;

    public static final Sprite baseSprite = new Sprite(RTSAssetManager.hellicopter);
    public static final Sprite destroyedSprite = new Sprite(RTSAssetManager.hellicopterDestroyed);
    public static final Sprite destroyedSpriteRed = new Sprite(RTSAssetManager.hellicopterDestroyedRed);
    public static final Sprite shadowSprite = new Sprite(RTSAssetManager.hellicopterShadow);
    public static final Sequence attackSequence = new Sequence(RTSAssetManager.hellicopterAttack, "heliAttack");
    public static final SoundEffect attackSound = new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au"));

    public static final Sprite baseSpriteRed = new Sprite(RTSAssetManager.hellicopterRed);
    public static final Sequence attackSequenceRed = new Sequence(RTSAssetManager.hellicopterAttackRed, "helliAttackRed");

    public HellicopterTurret turret;
    public long lastFireTick = 0;
    public int attackInterval = Main.ticksPerSecond * 2;
    public int elevation = 99;

    static {
        baseSprite.scale(VISUAL_SCALE);
        shadowSprite.scaleTo(VISUAL_SCALE);
        attackSequence.scale(VISUAL_SCALE);
        attackSequenceRed.scale(VISUAL_SCALE);
        baseSpriteRed.scaleTo(VISUAL_SCALE);
        baseSpriteRed.scaleTo(VISUAL_SCALE);
        destroyedSprite.scaleTo(VISUAL_SCALE);
        destroyedSpriteRed.scaleTo(VISUAL_SCALE);
    }

    public Hellicopter(int x, int y, int team) {
        super(x, y, team);
        this.setScale(VISUAL_SCALE);
        this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        this.setZLayer(11);
        this.plane = 2;
        this.isSolid = true;
        this.setBaseSpeed(4.5);
        turret = new HellicopterTurret(new Coordinate(0, 0));
        this.addSubObject(turret);
        this.canAttackAir = true;
    }

    @Override
    public int getWidth() {
        if (isRubble) {
            return super.getWidth() / 2;
        } else {
            return super.getWidth();
        }
    }

    @Override
    public int getHeight() {
        if (isRubble) {
            return super.getHeight() / 2;
        } else {
            return super.getHeight();
        }
    }

    public void fireDelayed(RTSUnit targetUnit, int delay) {
        addTickDelayedEffect(delay, game -> {
            if (!this.isAlive() || !targetUnit.isAlive()) {
                return;
            }
            Coordinate center = getPixelLocation();
            Coordinate leftOffset = new Coordinate(-30, -30);
            Coordinate rightOffset = new Coordinate(30, -30);

            leftOffset.adjustForRotation(turret.getRotation());
            rightOffset.adjustForRotation(turret.getRotation());

            getHostGame().addObject(new HellicopterBullet(this, center.copy().add(leftOffset), targetUnit));
            getHostGame().addObject(new HellicopterBullet(this, center.copy().add(rightOffset), targetUnit));
            if (attackSound.getNumCopiesPlaying() < 5) {
                if (isOnScreen()) {
                    attackSound.playCopy(Main.generateRandomDoubleLocally(.65, .75), Main.generateRandomIntLocally(0, 200));
                    addTickDelayedEffect(Main.ticksPerSecond / 2, c -> attackSound.changeNumCopiesPlaying(-1));
                } else {
                    attackSound.playCopy(Main.generateRandomDoubleLocally(.55, .6), Main.generateRandomIntLocally(0, 200));
                    addTickDelayedEffect(Main.ticksPerSecond / 2, c -> attackSound.changeNumCopiesPlaying(-1));
                }
            }
        });
    }

    @Override
    public void tick() {
        if (isRubble && elevation > 1) {
            elevation -= 4.8;
            if (elevation < 1) {
                new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
                this.baseSpeed = 0;
                this.plane = 0;
                this.setZLayer(1);
                this.isSolid = true;
                addTickDelayedEffect(Main.ticksPerSecond * 8, c -> {
                    new OnceThroughSticker(getHostGame(), new Sequence(RTSAssetManager.explosionSequence), getPixelLocation());
                    this.destroy();
                });
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
            currentTarget = nearestEnemyInRange();
            boolean offCooldown = (tickNumber - lastFireTick) > attackInterval;
            if (currentTarget != null && offCooldown) {
                if (Math.abs(turret.rotationNeededToFace(currentTarget.getPixelLocation())) < 2) {
                    lastFireTick = tickNumber;
                    Sequence attackAnimation = team == 0 ? attackSequence : attackSequenceRed;
                    turret.setGraphic(attackAnimation.copyMaintainSource());
                    fireDelayed(currentTarget, 10);
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        int shadowOffsetX = 5;
        int shadowOffsetY = Math.max(elevation, 9);
        Coordinate pixelLocation = getPixelLocation();
        pixelLocation.x += shadowOffsetX;
        pixelLocation.y += shadowOffsetY;
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = pixelLocation.x - toRender.getWidth() / 2;
        int renderY = pixelLocation.y - toRender.getHeight() / 2;
        g.rotate(Math.toRadians(turret.getRotation()), pixelLocation.x, pixelLocation.y);
        g.drawImage(toRender, renderX, renderY, null);
        g.setTransform(old);

        if (isSelected() && !isRubble) {
            drawHealthBar(g);
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

        private double bobAmount = 8;
        private double bobPercent = 0;
        private boolean bobbingDown = true;

        public HellicopterTurret(Coordinate offset) {
            super(offset);
            this.setScale(VISUAL_SCALE);
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
            this.setZLayer(11);
        }

        @Override
        public void tick() {
            if (!isRubble) {
                updateLocationForBob();
                updateRotation();
            }
        }

        @Override
        public void onAnimationCycle() {
            this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        }

        private void updateLocationForBob() {
            if (bobPercent >= 100) {
                bobbingDown = false;
            }
            if (bobPercent <= 0) {
                bobbingDown = true;
            }
            if (bobbingDown) {
                bobPercent += 2;
            } else {
                bobPercent -= 2;
            }
            double newY = bobAmount * (bobPercent / 100);
            Coordinate newOffset = new Coordinate(0, (int) newY);
            newOffset.rotateAboutPoint(new Coordinate(0, 0), -getRotation());
            this.setOffset(newOffset);
        }

        private void updateRotation() {
            Hellicopter host = (Hellicopter) getHost();
            double desiredRotationAmount = this.getHost().getRotation() - getRotation();
            double maxRotation = 5;

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
        out.add("Dmg: " + HellicopterBullet.damage + " (x2)   Interval: " + 2 + "s    Range: " + range);
        out.add("Speed: " + baseSpeed + "    Targets: Ground+Air");
        return out;
    }

}

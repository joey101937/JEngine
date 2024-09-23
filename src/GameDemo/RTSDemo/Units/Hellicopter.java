/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.Graphics2D;
import java.io.File;

/**
 * the hellicopter itself is invisible, instead we render the subobject to
 * simply movement and rotation
 *
 * @author guydu
 */
public class Hellicopter extends RTSUnit {
    public static double VISUAL_SCALE = 1.05;

    public static Sprite baseSprite = new Sprite(SpriteManager.hellicopter);
    public static Sprite shadowSprite = new Sprite(SpriteManager.hellicopterShadow);
    public static Sequence attackSequence = new Sequence(SpriteManager.hellicopterAttack);
    public static SoundEffect attackSound = new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au"));

    public static Sprite baseSpriteRed = new Sprite(blueToRed(SpriteManager.hellicopter));
    public static Sequence attackSequenceRed = new Sequence(blueToRed(SpriteManager.hellicopterAttack));

    public HellicopterTurret turret;
    public long lastFireTick = 0;
    public int attackInterval = Main.ticksPerSecond * 2;
    
    static {
        shadowSprite.scaleTo(VISUAL_SCALE);
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
    public void setRotation(double d) {
        super.setRotation(d);
    }

    public void fireDelayed(RTSUnit targetUnit, int delay) {
        addTickDelayedEffect(delay, game -> {
            if(!this.isAlive() || !targetUnit.isAlive()) return;
            Coordinate center = getPixelLocation();
            Coordinate leftOffset = new Coordinate(-30, -30);
            Coordinate rightOffset = new Coordinate(30, -30);

            leftOffset.adjustForRotation(turret.getRotation());
            rightOffset.adjustForRotation(turret.getRotation());

            getHostGame().addObject(new HellicopterBullet(this, center.copy().add(leftOffset), targetUnit));
            getHostGame().addObject(new HellicopterBullet(this, center.copy().add(rightOffset), targetUnit));
            if(attackSound.getNumCopiesPlaying() < 10) {
                if (isOnScreen()) {
                    attackSound.playCopy(Main.generateRandomDoubleLocally(.65, .75));
                    addTickDelayedEffect(Main.ticksPerSecond/2, c -> attackSound.changeNumCopiesPlaying(-1));
                } else {
                    attackSound.playCopy(Main.generateRandomDoubleLocally(.55, .6));
                    addTickDelayedEffect(Main.ticksPerSecond/2, c -> attackSound.changeNumCopiesPlaying(-1));
                }
            }
        });
    }

    @Override
    public void tick() {
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

    @Override
    public void render(Graphics2D g) {
        drawShadow(g, shadowSprite, 5, 99);
       
        if(isSelected()) {
            drawHealthBar(g);
        }
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
            updateLocationForBob();
            updateRotation();
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

}

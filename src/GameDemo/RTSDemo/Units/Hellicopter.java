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
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.io.File;

/**
 * the hellicopter itself is invisible, instead we render the subobject to
 * simply movement and rotation
 *
 * @author guydu
 */
public class Hellicopter extends RTSUnit {

    public static Sprite baseSprite = new Sprite(SpriteManager.hellicopter);
    public static Sprite shadowSprite = new Sprite(SpriteManager.hellicopterShadow);
    public static Sequence attackSequence = new Sequence(SpriteManager.hellicopterAttack);
    public static SoundEffect attackSound = new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au"));
    
    public Sprite baseSpriteRed = new Sprite(blueToRed(SpriteManager.hellicopter));
    public Sequence attackSequenceRed = new Sequence(blueToRed(SpriteManager.hellicopterAttack));

    public HellicopterTurret turret;
    public long lastFireTick = 0;
    public RTSUnit firedOnUnit = null;
    public int attackInterval = Main.ticksPerSecond * 2;
    public long tickToFireOn = -1;

    public Hellicopter(int x, int y, int team) {
        super(x, y, team);
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
        turret.setRotation(d);
    }

    public void fire(RTSUnit targetUnit) {
        Coordinate center = getPixelLocation();
        Coordinate leftOffset = new Coordinate(-30, -30);
        Coordinate rightOffset = new Coordinate(30, -30);

        leftOffset.adjustForRotation(turret.getRotation());
        rightOffset.adjustForRotation(turret.getRotation());
        
        getHostGame().addObject(new HellicopterBullet(this, center.copy().add(leftOffset), targetUnit));
        getHostGame().addObject(new HellicopterBullet(this, center.copy().add(rightOffset), targetUnit));
        if(isOnScreen()) {
            attackSound.playCopy(Main.generateRandomDouble(.65, .75));
        }
    }

    @Override
    public void tick() {
        super.tick();
        currentTarget = nearestEnemyInRange();
        boolean offCooldown = (tickNumber - lastFireTick) > attackInterval;
        if (currentTarget != null && offCooldown) {
            if(Math.abs(turret.angleFrom(currentTarget.getPixelLocation())) < 2) {
                lastFireTick = tickNumber;
                tickToFireOn = tickNumber + 10;
                Sequence attackAnimation = team == 0 ? attackSequence : attackSequenceRed;
                turret.setGraphic(attackAnimation.copyMaintainSource());
                firedOnUnit = currentTarget;
            }
        }
        
        if(tickNumber == tickToFireOn && firedOnUnit.isAlive()) {
            fire(firedOnUnit);
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform old = g.getTransform();
        VolatileImage toRender = shadowSprite.getCurrentVolatileImage();
        int renderX = getPixelLocation().x - toRender.getWidth() / 2;
        int renderY = getPixelLocation().y - toRender.getHeight() / 2;
        int shadowOffset = 90;
        g.rotate(Math.toRadians(turret.getRotation()), getPixelLocation().x, getPixelLocation().y + shadowOffset);
        g.drawImage(toRender, renderX, renderY + shadowOffset, null);
        g.setTransform(old);
    }

    public class HellicopterTurret extends SubObject {

        private double bobAmount = 8;
        private double bobPercent = 0;
        private boolean bobbingDown = true;

        public HellicopterTurret(Coordinate offset) {
            super(offset);
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
                desiredRotationAmount = angleFrom(host.currentTarget.getPixelLocation());
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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Units;

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

    public Sprite baseSpriteRed = new Sprite(blueToRed(SpriteManager.hellicopter));

    public RTSUnit closestEnemy;
    public HellicopterTurret turret;
    public long lastFireTick = 0;
    public int attackInterval = Main.ticksPerSecond * 2;

    public Hellicopter(int x, int y, int team) {
        super(x, y, team);
        this.setGraphic(team == 0 ? baseSprite : baseSpriteRed);
        this.setZLayer(11);
        this.plane = 2;
        this.isSolid = true;
        this.setBaseSpeed(4.5);
        turret = new HellicopterTurret(new Coordinate(0, 0));
        this.addSubObject(turret);
    }

    public void fire(RTSUnit targetUnit) {
        lastFireTick = tickNumber;
        turret.setGraphic(attackSequence.copyMaintainSource());
    }

    @Override
    public void tick() {
        super.tick();
        closestEnemy = nearestEnemyInRange();
        boolean offCooldown = (tickNumber - lastFireTick) > attackInterval;
        if (closestEnemy != null && offCooldown) {
            if(Math.abs(turret.angleFrom(closestEnemy.getPixelLocation())) < 2) {
                fire(currentTarget);
            }
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

            if (host.closestEnemy != null) {
                desiredRotationAmount = angleFrom(host.closestEnemy.getPixelLocation());
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

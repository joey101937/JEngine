/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.RTSDemo.Units;

import Framework.Audio.SoundEffect;
import Framework.Coordinate;
import Framework.GameObject2;
import Framework.GameObject2.MovementType;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Main;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.SubObject;
import GameDemo.RTSDemo.RTSUnit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * This is a gank gameobject. Tank class is the chasis
 * @author Joseph
 */
public class TankUnit extends RTSUnit{
    public static SoundEffect launchSoundSource = new SoundEffect(new File(Main.assets + "Sounds/gunshot.wav"));
    public static SoundEffect deathSound = new SoundEffect(new File(Main.assets + "Sounds/blast2.wav"));
    public Turret turret;
    public final static double VISUAL_SCALE = 1;
    public boolean weaponOnCooldown = false;
    
    // Modified buffered images for team color
    public static BufferedImage enemyTankChasisImage = greenToRed(SpriteManager.tankChasis2);
    public static BufferedImage enemyTankTurretImage =  greenToRed(SpriteManager.tankTurret2);
    public static BufferedImage[] enemyTankFireAnimation = greenToRed(SpriteManager.tankFireAnimation2);
    
    // sprites for reuse
    public static volatile Sprite chasisSpriteGreen = null; // new Sprite(SpriteManager.tankChasis2);
    public static volatile Sprite chasisSpriteRed = null; // new Sprite(enemyTankChasisImage);
    public static volatile Sprite turretSpriteGreen = null; // new Sprite(SpriteManager.tankTurret);
    public static volatile Sprite turretSpriteRed = null; // new Sprite(enemyTankTurretImage);
    public static volatile Sprite rubbleHullSprite = null;
    public static volatile Sprite rubbleTurretSprite = null;
    public static volatile Sequence deathAnimationHull = null;
    public static volatile Sequence deathAnimationTurret = null;
    public static volatile Sprite deathShadow = null;
    public static volatile Sequence deathFadeout;
    
    public static volatile Sequence turretFireAnimationGreen = null; // new Sequence(SpriteManager.tankFireAnimation);
    public static volatile Sequence turretFireAnimationRed = null; // new Sequence(enemyTankFireAnimation);
    
    public static void initGraphics() {
        if(chasisSpriteGreen != null) return;
        chasisSpriteGreen = new Sprite(SpriteManager.tankChasis2);
        chasisSpriteRed = new Sprite(enemyTankChasisImage);
        turretSpriteGreen = new Sprite(SpriteManager.tankTurret2);
        turretSpriteRed =  new Sprite(enemyTankTurretImage);
        turretFireAnimationGreen = new Sequence(SpriteManager.tankFireAnimation2);
        turretFireAnimationRed = new Sequence(enemyTankFireAnimation);
        rubbleHullSprite = new Sprite(SpriteManager.tankDeadHull);
        rubbleTurretSprite = new Sprite(SpriteManager.tankDeadTurret);
        deathAnimationHull = new Sequence(SpriteManager.tankHullDeathAni);
        deathAnimationHull.setSignature("deathHull");
        deathAnimationTurret = new Sequence(SpriteManager.tankTurretDeathAni);
        deathShadow = new Sprite(SpriteManager.tankDeadHullShadow);
        deathFadeout = Sequence.createFadeout(SpriteManager.tankDeadHullShadow, 40);
        deathFadeout.setSignature("fadeout");
        
        deathAnimationHull.setFrameDelay(60);
        deathAnimationTurret.setFrameDelay(60);
        
        List.of(chasisSpriteGreen,
                chasisSpriteRed,
                turretSpriteGreen,
                turretSpriteRed,
                turretFireAnimationGreen,      
                turretFireAnimationRed,
                rubbleHullSprite,
                rubbleTurretSprite,
                deathShadow,
                deathAnimationHull
                ).forEach(x -> x.scaleTo(VISUAL_SCALE));
    }
    
    @Override
    public void onAnimationCycle() {
        if("deathHull".equals(getGraphic().getSignature())) {
            this.setGraphic(rubbleHullSprite);
        }
        if("fadeout".equals(getGraphic().getSignature())) {
            this.isInvisible = true;
        }
    }
    
    
    /*
    sets up the tank values
     */
    public TankUnit(Coordinate c) {
        super(c, 0);
        init();
    }

    public TankUnit(int x, int y) {
        super(x, y, 0);
        init();
    }
    
    public TankUnit(int x, int y, int team) {
        super(x, y, team);
        init();
    }

    private void init() {
        initGraphics();
        Sprite chassSprite = team == 0 ? chasisSpriteGreen : chasisSpriteRed;
        this.setGraphic(chassSprite);
        this.movementType = MovementType.RotationBased;
        turret = new Turret(new Coordinate(0, 0));
        this.addSubObject(turret);
        setScale(VISUAL_SCALE);
        isSolid = true;
        preventOverlap = true;
        this.maxHealth = 200;//tanks can take 4 shots
        this.currentHealth = maxHealth;
    }

    //when a tank tries to fire, it first checks if its turret is still firing. 
    //if not, tell the turret to fire at target location
    public void fire(Coordinate target) {
        if (weaponOnCooldown || target.distanceFrom(location) < getHeight() * 3 / 5 || Math.abs(turret.angleFrom(target))>1) { //limited to one shot per 60 ticks
            return;
        }
        weaponOnCooldown = true;
        turret.onFire(target);
        this.addTickDelayedEffect(Main.ticksPerSecond, x -> {weaponOnCooldown = false;});
    }

    public class Turret extends SubObject {
        Sequence fireAnimation = team == 0 ? turretFireAnimationGreen : turretFireAnimationRed;    //simple recoil animation
        Sprite turretSprite = team == 0 ? turretSpriteGreen : turretSpriteRed; 
        
        /*
        this firing boolean is linked to the animation  with the onAnimationCycle
        method below. This means the tank will not fire until the fire animation is
        done playing. 
        */
        public boolean firing = false;
        
        public Turret(Coordinate offset) {
            super(offset);
            this.setGraphic(turretSprite);
            setScale(VISUAL_SCALE);
            fireAnimation.setSignature("fireAnimation");
        }
        /*
        fires the gun at the location.
        first, play the firing animation on the gun, then create a small explosion
        effect for the muzzleflash, then create the bullet object and spawn it
        into the game world
         */
        public void onFire(Coordinate target) {
            setGraphic(fireAnimation.copyMaintainSource());
            try {
                if(isOnScreen()) {
                   launchSoundSource.playCopy((Math.random() * .2) + .4f);
                }                
            } catch (Exception e) {
                e.printStackTrace();
            }
            firing = true;
            Coordinate muzzelLocation = new Coordinate(0, 0);
            muzzelLocation.y -= fireAnimation.frames[0].getHeight()*2/5;
            muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, getRotation());
            muzzelLocation.add(getPixelLocation());
            RTSUnit targetUnit = ((RTSUnit)this.getHost()).currentTarget;
            int longestSide = Math.max(targetUnit.getWidth(), targetUnit.getHeight());
            Coordinate offset = new Coordinate(Main.generateRandomInt(-longestSide / 3, longestSide / 3), Main.generateRandomInt(-longestSide / 3, longestSide / 3));
            target.add(offset);
            TankBullet bullet = new TankBullet(muzzelLocation.toDCoordinate(),target.toDCoordinate());
            bullet.shooter=this.getHost();
            getHostGame().addObject(bullet);
        }
        
        /*
        this runs whenever an animation cycle ends.
        here we use it to tell the gank when its ready to fire again and
        also to reset the object back to using the regular turret sprite
        */
        @Override
        public void onAnimationCycle(){
            if(getGraphic().getSignature().equals("fireAnimation")){
                firing = false;
                setGraphic(turretSprite);
            }
            if(isRubble) {
                setGraphic(rubbleTurretSprite);
            }
            
        }

        //tank turret tick
        @Override
        public void tick() {
            // System.out.println(this + " " + this.ID);
            super.tick();
            if(isRubble) return;
            RTSUnit enemy = nearestEnemyInRange();
            ((RTSUnit)getHost()).currentTarget = enemy;
            if (enemy == null) {
                double desiredRotation = getHost().getRotation()-getRotation();
                if(desiredRotation > 180) {
                    desiredRotation -= 360;
                } else if (desiredRotation < -180) {
                    desiredRotation += 360;
                }
                double maxRotation = 5;
                if (Math.abs(desiredRotation) < maxRotation) {
                    rotate(desiredRotation);
                } else {
                    if (desiredRotation > 0) {
                        rotate(maxRotation);
                    } else {
                        rotate(-maxRotation);
                    }
                }
            } else {
                double desiredRotation = angleFrom(enemy.getPixelLocation());
                double maxRotation = 5;
                if (Math.abs(desiredRotation) < maxRotation) {
                    rotate(desiredRotation);
                } else {
                    if (desiredRotation > 0) {
                        rotate(maxRotation);
                    } else {
                        rotate(-maxRotation);
                    }
                }
                Coordinate targetPoint = enemy.getPixelLocation();
                ((TankUnit)getHost()).fire(targetPoint);
            }
        }

    }
    
    @Override
    public void die() {
        if(this.isRubble) return;
        OnceThroughSticker deathExplosion = new OnceThroughSticker(getHostGame(), new Sequence(SpriteManager.explosionSequence), getPixelLocation());
        this.isRubble = true;
        this.setBaseSpeed(0);
        this.setDesiredLocation(this.getPixelLocation());
        this.setGraphic(deathAnimationHull.copyMaintainSource());
        turret.setGraphic(deathAnimationTurret.copyMaintainSource());
        addTickDelayedEffect(Main.ticksPerSecond*10, c -> {
            OnceThroughSticker despawnExplosion = new OnceThroughSticker(getHostGame(), new Sequence(SpriteManager.explosionSequence), getPixelLocation());
            this.setGraphic(deathFadeout.copyMaintainSource());
            this.isSolid = false;
            this.turret.isInvisible = true;
            addTickDelayedEffect(Main.ticksPerSecond * 3, c2 -> {
                this.destroy();
            });
            // this.destroy();
        });
    }

}


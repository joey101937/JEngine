///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package GameDemo.RTSDemo.Units;
//
//import Framework.Audio.SoundEffect;
//import Framework.Coordinate;
//import Framework.GameObject2;
//import Framework.GameObject2.MovementType;
//import Framework.GraphicalAssets.Sequence;
//import Framework.GraphicalAssets.Sprite;
//import Framework.Main;
//import Framework.SpriteManager;
//import Framework.Stickers.OnceThroughSticker;
//import Framework.SubObject;
//import GameDemo.RTSDemo.RTSUnit;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * This is a gank gameobject. Tank class is the chasis
// * @author Joseph
// */
//public class TankUnit extends RTSUnit{
//    public static SoundEffect launchSoundSource = new SoundEffect(new File(Main.assets + "Sounds/gunshot.wav"));
//    public static SoundEffect deathSound = new SoundEffect(new File(Main.assets + "Sounds/blast2.wav"));
//    public Turret turret;
//    public final static double VISUAL_SCALE = .15;
//    private Long lastFiredTime = 0L;
//    public static final int RANGE = 500; 
//    
//    // Modified buffered images for team color
//    public static BufferedImage enemyTankChasisImage = greenToRed(SpriteManager.tankChasis);
//    public static BufferedImage enemyTankTurretImage =  greenToRed(SpriteManager.tankTurret);
//    public static BufferedImage[] enemyTankFireAnimation = greenToRed(SpriteManager.tankFireAnimation);
//    
//    // sprites for reuse
//    public static volatile Sprite chasisSpriteGreen = null; // new Sprite(SpriteManager.tankChasis2);
//    public static volatile Sprite chasisSpriteRed = null; // new Sprite(enemyTankChasisImage);
//    public static volatile Sprite turretSpriteGreen = null; // new Sprite(SpriteManager.tankTurret);
//    public static volatile Sprite turretSpriteRed = null; // new Sprite(enemyTankTurretImage);
//    
//    public static volatile Sequence turretFireAnimationGreen = null; // new Sequence(SpriteManager.tankFireAnimation);
//    public static volatile Sequence turretFireAnimationRed = null; // new Sequence(enemyTankFireAnimation);
//    
//    public static void initGraphics() {
//        if(chasisSpriteGreen != null) return;
//        chasisSpriteGreen = new Sprite(SpriteManager.tankChasis);
//        chasisSpriteRed = new Sprite(enemyTankChasisImage);
//        turretSpriteGreen = new Sprite(SpriteManager.tankTurret);
//        turretSpriteRed =  new Sprite(enemyTankTurretImage);
//        turretFireAnimationGreen = new Sequence(SpriteManager.tankFireAnimation);
//        turretFireAnimationRed = new Sequence(enemyTankFireAnimation);
//        List.of(
//                chasisSpriteGreen,
//                chasisSpriteRed,
//                turretSpriteGreen,
//                turretSpriteRed,
//                turretFireAnimationGreen,      
//                turretFireAnimationRed
//                ).forEach(x -> x.scaleTo(VISUAL_SCALE));
//    }
//    
//    
//    
//    /*
//    sets up the tank values
//     */
//    public TankUnit(Coordinate c) {
//        super(c, 0);
//        init();
//    }
//
//    public TankUnit(int x, int y) {
//        super(x, y, 0);
//        init();
//    }
//    
//    public TankUnit(int x, int y, int team) {
//        super(x, y, team);
//        init();
//    }
//
//    private void init() {
//        initGraphics();
//        Sprite chassSprite = team == 0 ? chasisSpriteGreen : chasisSpriteRed;
//        this.setGraphic(chassSprite);
//        this.movementType = MovementType.RotationBased;
//        turret = new Turret(new Coordinate(0, 0));
//        this.addSubObject(turret);
//        setScale(VISUAL_SCALE);
//        isSolid = true;
//        preventOverlap = true;
//        this.maxHealth = 200;//tanks can take 4 shots
//        this.currentHealth = maxHealth;
//    }
//
//    //when a tank tries to fire, it first checks if its turret is still firing. 
//    //if not, tell the turret to fire at target location
//    public void fire(Coordinate target) {
//        if (turret.firing || target.distanceFrom(location) < getHeight() * 3 / 5 || tickNumber-lastFiredTime < 60L || Math.abs(turret.angleFrom(target))>1) { //limited to one shot per 60 ticks
//            return;
//        }
//        lastFiredTime = this.tickNumber;
//        turret.onFire(target);
//    }
//
//    public class Turret extends SubObject{
//        Sequence fireAnimation = team == 0 ? turretFireAnimationGreen : turretFireAnimationRed;    //simple recoil animation
//        Sprite turretSprite = team == 0 ? turretSpriteGreen : turretSpriteRed; 
//        
//        /*
//        this firing boolean is linked to the animation  with the onAnimationCycle
//        method below. This means the tank will not fire until the fire animation is
//        done playing. 
//        */
//        public boolean firing = false;
//        
//        public Turret(Coordinate offset) {
//            super(offset);
//            this.setGraphic(turretSprite);
//            setScale(VISUAL_SCALE);
//            fireAnimation.setSignature("fireAnimation");
//        }
//        /*
//        fires the gun at the location.
//        first, play the firing animation on the gun, then create a small explosion
//        effect for the muzzleflash, then create the bullet object and spawn it
//        into the game world
//         */
//        public void onFire(Coordinate target) {
//            setGraphic(fireAnimation.copyMaintainSource());
//            try {
//                if(isOnScreen()) {
//                   launchSoundSource.playCopy((Math.random() * .2) + .4f);
//                }                
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            firing = true;
//            Coordinate muzzelLocation = new Coordinate(0, 0);
//            muzzelLocation.y -= fireAnimation.frames[0].getHeight()*2/5;
//            muzzelLocation = Coordinate.adjustForRotation(muzzelLocation, getRotation());
//            muzzelLocation.add(getPixelLocation());
//            TankBullet bullet = new TankBullet(muzzelLocation.toDCoordinate(),target.toDCoordinate());
//            bullet.shooter=this.getHost();
//            getHostGame().addObject(bullet);
//        }
//        
//        /*
//        this runs whenever an animation cycle ends.
//        here we use it to tell the gank when its ready to fire again and
//        also to reset the object back to using the regular turret sprite
//        */
//        @Override
//        public void onAnimationCycle(){
//            if(getGraphic().getSignature().equals("fireAnimation")){
//                firing = false;
//                setGraphic(turretSprite);
//            }
//        }
//
//        public RTSUnit nearestInRange() {
//            if(getHostGame()==null){
//                System.out.println("null host game");
//                return null;
//            }
//            ArrayList<GameObject2> nearby = getHostGame().getObjectsNearPoint(getPixelLocation(), RANGE);
//            double closestDistance = RANGE + 1;
//            GameObject2 closest = null;
//            if (!nearby.isEmpty()) {
//                for (GameObject2 go : nearby) {
//                    if (!(go instanceof TankUnit) || go==this.getHost()) {
//                        continue;
//                    }
//                    if(((TankUnit)go).team == team) continue;
//                    if (location.distanceFrom(go.getLocationAsOfLastTick()) < closestDistance) {
//                        closestDistance = location.distanceFrom(go.getLocationAsOfLastTick());
//                        closest = go;
//                    }
//                }
//            }
//            return (RTSUnit) closest;
//        }
//        //tank turret tick
//        @Override
//        public void tick() {
//            // System.out.println(this + " " + this.ID);
//            super.tick();
//            RTSUnit enemy = nearestInRange();
//            if (enemy == null) {
//                double desiredRotation = getHost().getRotation()-getRotation();
//                if(desiredRotation > 180) {
//                    desiredRotation -= 360;
//                } else if (desiredRotation < -180) {
//                    desiredRotation += 360;
//                }
//                double maxRotation = 5;
//                if (Math.abs(desiredRotation) < maxRotation) {
//                    rotate(desiredRotation);
//                } else {
//                    if (desiredRotation > 0) {
//                        rotate(maxRotation);
//                    } else {
//                        rotate(-maxRotation);
//                    }
//                }
//            } else {
//                double desiredRotation = angleFrom(enemy.getPixelLocation());
//                double maxRotation = 5;
//                if (Math.abs(desiredRotation) < maxRotation) {
//                    rotate(desiredRotation);
//                } else {
//                    if (desiredRotation > 0) {
//                        rotate(maxRotation);
//                    } else {
//                        rotate(-maxRotation);
//                    }
//                }
//                Coordinate offset = new Coordinate(Main.generateRandom(-enemy.getWidth() / 4, enemy.getWidth() / 4), Main.generateRandom(-enemy.getHeight() / 4, enemy.getHeight() / 4));
//                Coordinate targetPoint = enemy.getPixelLocation();
//                targetPoint.add(offset);
//                ((TankUnit)getHost()).fire(targetPoint);
//            }
//        }
//
//    }
//
//    @Override
//    public void onCollide(GameObject2 other, boolean fromMyTick){
//        
//    }
//
//    @Override
//    public void onDestroy() {
//        OnceThroughSticker deathAni = new OnceThroughSticker(getHostGame(), new Sequence(SpriteManager.explosionSequence), getPixelLocation());
//    }
//}
//

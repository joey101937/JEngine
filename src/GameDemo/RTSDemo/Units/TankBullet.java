///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package GameDemo.RTSDemo.Units;
//
//import Framework.DCoordinate;
//import Framework.GameObject2;
//import Framework.Hitbox;
//import Framework.UtilityObjects.Projectile;
//import Framework.GraphicalAssets.Sequence;
//import Framework.SpriteManager;
//import Framework.Stickers.OnceThroughSticker;
//import GameDemo.RTSDemo.RTSUnit;
//import java.awt.image.BufferedImage;
//
///**
// *
// * @author Joseph
// */
//public class TankBullet extends Projectile {
//
//    public GameObject2 shooter; //the object that launched this projectile
//    
//    public static Sequence bulletGraphic = new Sequence(new BufferedImage[]{SpriteManager.bullet});
//    public static Sequence explosionSmall = new Sequence(SpriteManager.explosionSequenceSmall);
//
//    public TankBullet(DCoordinate start, DCoordinate end) {
//        super(start, end);
//        bulletGraphic.setSignature("bullet graphic");
//        bulletGraphic.scaleTo(.25); // scales parent to the same size as how the sequence will be used so we dont have to scale on the fly
//        setScale(.25);
//        this.setGraphic(bulletGraphic.copyMaintainSource());
//        baseSpeed = 20;
//        this.setHitbox(new Hitbox(this, 0)); //sets this to se a circular hitbox. updateHitbox() method manages radius for us so we set it to 0 by default
//        maxRange = 750;
//    }
//    
//    
//
//
//    //when this runs into a creature, deal damage to it then destroy this projectile
//    @Override
//    public void onCollide(GameObject2 other, boolean fromMyTick){
//        if(other==shooter)return; //dont collde with the gameobject that launched this projectile
//        if(other instanceof RTSUnit) {
//            RTSUnit otherUnit = (RTSUnit) other;
//            if (shooter instanceof RTSUnit) {
//                if(((RTSUnit)shooter).team == otherUnit.team) return; // no friendly fire
//            }
//            otherUnit.takeDamage(20);
//            OnceThroughSticker impactExplosion = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), getPixelLocation());
//            destroy();
//        }
//    }
//
//    @Override
//    public void onTimeOut() {
//        OnceThroughSticker s = new OnceThroughSticker(getHostGame(), explosionSmall.copyMaintainSource(), this.getPixelLocation());
//    }
//
//    /**
//     * bullets just destroy when they go out of bounds
//     */
//    @Override
//    public void constrainToWorld() {
//        DCoordinate loc = location;
//        if (loc.x < getHostGame().worldBorder) {
//            onTimeOut();
//            destroy();
//        }
//        if (loc.y < getHostGame().worldBorder) {
//            onTimeOut();
//            destroy();
//        }
//        if (loc.x > getHostGame().getWorldWidth() - getHostGame().worldBorder) {
//            onTimeOut();
//            destroy();
//        }
//        if (loc.y > getHostGame().getWorldHeight() - getHostGame().worldBorder) {
//            onTimeOut();
//            destroy();
//        }
//    }
//}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga.Enemies;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import GameDemo.Galiga.Bolt;
import GameDemo.Galiga.GaligaGame;
import GameDemo.Galiga.PlayerShip;

/**
 *
 * @author Joseph
 */
public class BossShip extends EnemyShip {
    
    private int hitPoints = 5;
    private static Sprite bossSprite = new Sprite(SpriteManager.evilShip);
    
    public BossShip(Coordinate c) {
        super(c);
        bossInit();
    }

    public BossShip(DCoordinate dc) {
        super(dc);
        bossInit();
    }

    public BossShip(int x, int y) {
        super(x, y);
        bossInit();
    }
    
    private void bossInit() {
        // Boss is 2x the size of normal enemies
        scale(2); // Normal enemies are 0.2, so 0.4 is 2x
        setName("Boss " + ID);
        // Boss moves slower than normal enemies
        baseSpeed = 1.5;
    }
    
    @Override
    public void shoot() {
        // Boss shoots three projectiles side by side
        Coordinate centerPos = this.getPixelLocation();
        DCoordinate target = GaligaGame.player.getLocation();
        
        // Center bolt
        Bolt centerBolt = new Bolt(centerPos);
        centerBolt.setYCoordinate(centerBolt.getLocation().y + this.getHeight()/2);
        centerBolt.isFriendly = false;
        centerBolt.launch(target);
        getHostGame().addObject(centerBolt);
        
        // Left bolt
        Bolt leftBolt = new Bolt(new DCoordinate(centerPos.x - 20, centerPos.y));
        leftBolt.setYCoordinate(leftBolt.getLocation().y + this.getHeight()/2);
        leftBolt.isFriendly = false;
        leftBolt.launch(new DCoordinate(target.x - 50, target.y));
        getHostGame().addObject(leftBolt);
        
        // Right bolt
        Bolt rightBolt = new Bolt(new DCoordinate(centerPos.x + 20, centerPos.y));
        rightBolt.setYCoordinate(rightBolt.getLocation().y + this.getHeight()/2);
        rightBolt.isFriendly = false;
        rightBolt.launch(new DCoordinate(target.x + 50, target.y));
        getHostGame().addObject(rightBolt);
        
        GaligaGame.pewSound.playCopy(.7f);
    }
    
    @Override
    public void tick() {
        super.tick();
        // Boss shoots more frequently than normal enemies
        if(((int)(Math.random()*200 * (1/difficultyMultiplier))) == 1){
            // 1 out of 200 chance every tick (2x more frequent than normal)
            shoot();
        }
    }
    
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if(other instanceof PlayerShip) {
            ((PlayerShip)other).onHit();
            destroy();
        }
    }
    
    public void takeDamage() {
        hitPoints--;
        if(hitPoints <= 0) {
            destroy();
        }
    }
    
    public int getHitPoints() {
        return hitPoints;
    }
    
    @Override
    public void onDestroy() {
        new OnceThroughSticker(GaligaGame.mainGame, new Sequence(SpriteManager.explosionSequence), getPixelLocation());
        GaligaGame.deathSound.playCopy(.7f);
        // Boss gives more points than normal enemies
        GaligaGame.UI.increaseScore(2500);
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga.Enemies;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;
import GameDemo.Galiga.Bolt;
import GameDemo.Galiga.GaligaGame;
import GameDemo.Galiga.PlayerShip;

/**
 *
 * @author Joseph
 */
public class EnemyShip extends GameObject2{
    
    public static double difficultyMultiplier = 1.0; //increase this to increase difficulty
    public static Sprite enemySprite = new Sprite(SpriteManager.evilShip);
    
    public EnemyShip(Coordinate c) {
        super(c);
        init();
    }

    public EnemyShip(DCoordinate dc) {
        super(dc);
        init();
    }

    public EnemyShip(int x, int y) {
        super(x, y);
        init();
    }
 
    protected void init() {
        Sprite s = enemySprite;
        scale(.2);
        setGraphic(s);
        isSolid = true;
        preventOverlap=false;
        setName("Enemy " + ID);
        movementType = MovementType.SpeedRatio;
        baseSpeed = 2.66;
        this.velocity.x = 1;
    }
    
    @Override
    public void onCollideWorldBorder(DCoordinate l) {
        if (l.x <= getHostGame().worldBorder) {
            setXCoordinate(getHostGame().getWorldWidth() - getHostGame().worldBorder);
        }
        if (l.y <= getHostGame().worldBorder) {
            setYCoordinate(getHostGame().getWorldHeight() - getHostGame().worldBorder - 200); // dont let ships teleport behind the player
        }
        if (l.x >= getHostGame().getWorldWidth() - getHostGame().worldBorder) {
            setXCoordinate(getHostGame().worldBorder);
        }
        if (l.y >= getHostGame().getWorldHeight() - getHostGame().worldBorder) {
            setYCoordinate(getHostGame().worldBorder);
        }
    }

    @Override
    public void onDestroy(){
        GaligaGame.deathSound.playCopy(.7f);
        GaligaGame.UI.increaseScore(500);
    }
    
    public void shoot(){
        Bolt b = new Bolt(this.getPixelLocation());
        b.setYCoordinate(b.getLocation().y + this.getHeight()/2);
        b.isFriendly=false;
        DCoordinate target = GaligaGame.player.getLocation();
        b.launch(target);
        getHostGame().addObject(b);
        GaligaGame.pewSound.playCopy(.7f);
    }
    
    @Override
    public void tick(){
        super.tick();
        if(((int)(Math.random()*400 * (1/difficultyMultiplier))) == 1){
            //1 out of 400 chance every tick
            shoot();
        }
    }
    
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick){
        if(other instanceof PlayerShip){
            ((PlayerShip)other).onHit();
            destroy();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga;

import GameDemo.Galiga.Enemies.EnemyShip;
import GameDemo.Galiga.Enemies.BossShip;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;
import Framework.Stickers.OnceThroughSticker;
import Framework.UtilityObjects.Projectile;

/**
 *
 * @author Joseph
 */
public class Bolt extends Projectile{
    
    public boolean isFriendly = false; //friendly = player bullet, nonfriendly = enemy
    
    public Bolt(Coordinate start) {
        super(start);
        init();
    }

    public Bolt(DCoordinate start) {
        super(start);
        init();
    }

    public Bolt(Coordinate start, Coordinate destination) {
        super(start, destination);
        init();
    }

    public Bolt(DCoordinate start, DCoordinate destination) {
        super(start, destination);
        init();
    }
    
    private void init(){
        this.setGraphic(new Sprite(SpriteManager.bolt));
        this.isSolid=true;
        this.setScale(3);
        baseSpeed = 7;
    }
    
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick) {
        if (other instanceof PlayerShip) {
            if (isFriendly) {
                //do nothing
                return;
            } else {
                ((PlayerShip)other).onHit();
                this.destroy();
                return;
            }
        } else if (other instanceof BossShip) {
            if (isFriendly) {
                ((BossShip)other).takeDamage();
                // play explosion animation
                new OnceThroughSticker(GaligaGame.mainGame, new Sequence(SpriteManager.explosionSequence) ,getPixelLocation());
                this.destroy();
                return;
            } else {
                //do nothing
                return;
            }
        } else if (other instanceof EnemyShip) {
            if (isFriendly) {
                other.destroy();
                // play explosion animation
                new OnceThroughSticker(GaligaGame.mainGame, new Sequence(SpriteManager.explosionSequence) ,getPixelLocation());
                this.destroy();
                return;
            } else {
                //do nothing
                return;
            }
        }
    }
    
}

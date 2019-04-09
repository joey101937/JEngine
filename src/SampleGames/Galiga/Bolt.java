/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga;

import SampleGames.Galiga.Enemies.EnemyShip;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;
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
    }
    
    @Override
    public void onCollide(GameObject2 other) {
        System.out.println("on collide");
        if (other instanceof PlayerShip) {
            if (isFriendly) {
                //do nothing
                return;
            } else {
                ((PlayerShip)other).onHit();
                this.destroy();
                return;
            }
        } else if (other instanceof EnemyShip) {
            if (isFriendly) {
                other.destroy();
                this.destroy();
                return;
            } else {
                //do nothing
                return;
            }
        }
    }
    
}

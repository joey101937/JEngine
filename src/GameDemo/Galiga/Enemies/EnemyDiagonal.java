/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Galiga.Enemies;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sprite;
import Framework.DemoSpriteManager;

/**
 *
 * @author Joseph
 */
public class EnemyDiagonal extends EnemyShip{
    /** Pre-scaled once at class load; see {@link EnemyShip#enemySprite}. */
    public static Sprite sprite = new Sprite(DemoSpriteManager.evilShip);
    static { sprite.scale(0.2); }

    public EnemyDiagonal(Coordinate c) {
        super(c);
    }

    public EnemyDiagonal(DCoordinate dc) {
        super(dc);
    }

    public EnemyDiagonal(int x, int y) {
        super(x, y);
    }
    
    @Override
    protected void init() {
        setGraphic(sprite);
        isSolid = true;
        preventOverlap=false;
        setName("Enemy " + ID);
        movementType = MovementType.RotationBased;
        baseSpeed = 2.66;
        velocity.y = 1;
        this.rotate(35 + (Math.random()*60));
        if(Math.random() > .5){
            this.rotate(180);
        }
    }
}

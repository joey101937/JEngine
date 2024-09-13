/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.Galiga.Enemies;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GraphicalAssets.Sprite;
import Framework.SpriteManager;

/**
 *
 * @author Joseph
 */
public class EnemyDiagonal extends EnemyShip{
    public static Sprite sprite = new Sprite(SpriteManager.evilShip);

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
        Sprite s = sprite;
        scale(.2);
        setGraphic(s);
        isSolid = true;
        preventOverlap=false;
        setName("Enemy " + ID);
        movementType = MovementType.RotationBased;
        baseSpeed = 4;
        velocity.y = 1;
        this.rotate(35 + (Math.random()*60));
        if(Math.random() > .5){
            this.rotate(180);
        }
    }
}

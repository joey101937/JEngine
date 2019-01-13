/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Sprite;
import Framework.SpriteManager;

/**
 *
 * @author Joseph
 */
public class Spaceship extends GameObject2{
    
    public Spaceship(Coordinate c) {
        super(c);
        setAnimationFalse(new Sprite(SpriteManager.spaceship));
        name = "Spaceship";
        scale= .3;
        movementType = GameObject2.MovementType.RotationBased;
        baseSpeed = 6;
    }
    
}

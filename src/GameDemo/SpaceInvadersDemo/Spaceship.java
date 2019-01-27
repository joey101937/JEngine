/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SpaceInvadersDemo;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.GameObject2;
import Framework.Sprite;
import Framework.SpriteManager;
import static GameDemo.SpaceInvadersDemo.SpaceInputHandler.waypoint;

/**
 * This is the shapship game-object that you see on screen. This object
 * follows the mouse around the world by constantly rotating to face, then moving
 * towards the set waypoint.
 * @author Joseph
 */
public class Spaceship extends GameObject2{
    /*
    The constructor takes a coordiante as the initial location.
    first, set the visual representation to be a non-animated sprite using
    setAnimationFalse method and providing a sprite created with the desired
    image of a spaceship. Then, scale the ship down because the image we used
    is larger than we would like. Setting movementType means that velocity will
    be relative to this object's orientation rather than global coordinates.
    Finally we set the base speed to 6 so that it moves mildly fast.
    */
    public Spaceship(Coordinate c) {
        super(c);
        setAnimationFalse(new Sprite(SpriteManager.spaceship));
        name = "Spaceship";
        scale= .3;
        movementType = GameObject2.MovementType.RotationBased;
        baseSpeed = 6;
    }
    
    /*
    this runs every game tick. Rememever to call super to make the movement work
    waypoint is the location set by the mouse. every tick we rotate to face it
    then set our velocity forwards. velocity is (0,-1) because we are using
    rotation based movement. -1 in the Y axis means we go upwards in the Y 
    direction. (0,0) is topleft corner. rotation based movement means that going
    up in the Y axis actually makes the object move upwards relative to its own
    direction, causing the ship to go forwards in whatever direction its facing.
    
    stops moving if it is close to its destination.
    */
    @Override
    public void tick() {
        super.tick();
        System.out.println(waypoint);
        if (Coordinate.distanceBetween(waypoint, getPixelLocation()) < getWidth()) {
            velocity.y = 0;
            velocity.x = 0;
        } else {
            lookAt(waypoint);
            velocity = new DCoordinate(0, -1);   //negative on Y axis because 0 is at the top of the screen
        }
    }
}

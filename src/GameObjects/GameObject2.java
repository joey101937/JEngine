/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects;

import Framework.Camera;
import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.Main;
import Framework.PathingLayer;
import Framework.Sequence;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent class for all objects that appear in the gameworld
 * @author Joseph
 */
public class GameObject2 {
    public String name= "Unnamed " + this.getClass().getName();
    public int tickNumber = 0;
    public int renderNumber = 0;
    public DCoordinate location = new DCoordinate(0,0); //location relative to the world
    public DCoordinate velocity = new DCoordinate(0,0); //added to location as a ratio of speed each tick
    public int innateRotation = 0; //0 = top of sprite is forwards, 90 is right of sprite is forwards, 180 is bottom of sprite is forwards etc
    public double baseSpeed = 2; //total distance the object can move per tick
    private boolean isAnimated = false;
    protected Sequence sequence = null; //animation sequence to run if animated
    public BufferedImage sprite = null; //static sprite if not animated
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public double rotation = 0;
    public boolean isSolid = false; //weather or not this object collides with other objects
    protected boolean isAlive = true; //weather or not the object has been destroyed
    protected boolean horizontalFlip = false;
    public MovementType movementType = MovementType.SpeedRatio;
    protected Rectangle hitbox = new Rectangle(0,0,0,0);
    public final int ID;
    private static int IDLog = 0; //used to assign IDs
    public HashMap<PathingLayer.Type,Double> pathingModifiers = new HashMap<>(); //stores default speed modifiers for different terrain types
    
    
    public static enum MovementType{
    RawVelocity, SpeedRatio;
    }
    
    /**
     * used to get integer location of object, used when rendering to screen
     * @return integer location
     */
    public Coordinate getPixelLocation() {
        return new Coordinate(location);
    }
    /**
     * @return The Rectangle object used as hitbox
     */
    public Rectangle getHitbox() {
        return hitbox;
    }
    /**
     * sets hitbox based on current size and location
     */
    private void updateHitbox() {
        int width = getWidth();
        int height = getHeight();
        hitbox.width = width;
        hitbox.height = height;
        hitbox.x = (int) (location.x - width / 2.0);
        hitbox.y = (int) (location.y - height / 2.0);
    }


    /**
     * returns the width of the visual gameobject. If no visual, return 0.
     * @return width of visual
     */
    public int getWidth() {
        try{
        if (isAnimated) {
            return sequence.getCurrentFrame().getWidth();
        } else {
            return sprite.getWidth();
        }
        }catch(NullPointerException npe){
            return 0;
        }
    }
    /**
     * returns the height of the visual gameobject. If no visual, return 0
     * @return height of visual
     */
    public int getHeight() {
        try {
            if (isAnimated) {
                return sequence.getCurrentFrame().getHeight();
            } else {
                return sprite.getHeight();
            }
        } catch (NullPointerException npe) {
            return 0;
        }
    }
    /**
     * @return gets the current animation sequence this object is rendering
     */
    public Sequence getCurrentSequence(){
        return sequence;
    }
    
    /**
     * changes the current animation sequence to the given sequence
     * @param s sequence to use
     */
    public void setSequence(Sequence s){
        if(sequence == s) return;
        else sequence = s;
    }

    public void setRotation(double degrees) {
        rotation = degrees;
    }

    public void rotate(double degrees) {
        rotation += degrees;
    }
    
    /**
     * Rotates this object so that its front (determined by innate rotation) is
     * angled towards other's location
     * @param other object whos location we will look at
     */
    public void lookAt(GameObject2 other) {
        setRotation(DCoordinate.angleFrom(location, other.location) - innateRotation);
    }
    /**
     * Rotates this object so that its front (determined by innate rotation) is
     * angled towards given location
     * @param destination location to look at
     */
    public void lookAt(DCoordinate destination){
         setRotation(DCoordinate.angleFrom(location, destination) - innateRotation);
    }
    /**
     * Draws the object on screen in the game world
     * @param g Graphics2D object to draw with
     */
    public void render(Graphics2D g){
        renderNumber++;
        if(!isOnScreen() && !Main.overviewMode)return;
        Coordinate pixelLocation = getPixelLocation();
        AffineTransform old = g.getTransform();
        while(rotation > 360){rotation-=360;}  //constrain rotation size
        while(rotation < -360){rotation+=360;}
        g.rotate(Math.toRadians(rotation),getPixelLocation().x,getPixelLocation().y);
        if(isAnimated){
            if(sequence == null){
                System.out.println("Warning trying to render null sequence object " +name);
                return;
            }
            if(sequence.getCurrentFrame()!=null){
                sequence.startAnimating();
                BufferedImage toRender = sequence.getCurrentFrame();
                g.drawImage(toRender, pixelLocation.x-toRender.getWidth()/2 , pixelLocation.y-toRender.getHeight()/2,null); //draws frmae centered on pixelLocation
            }else{
                System.out.println("Warning: null frame in sequence of " + name);
            }
        }else{
            if(sprite!=null){
                g.drawImage(sprite, pixelLocation.x-sprite.getWidth()/2, pixelLocation.y-sprite.getHeight()/2, null); //draws sprite centered on pixelLocation
            }else{
                System.out.println("Warning: unanimated game object sprite is null " + name);
            }
        }
        if (Main.debugMode) {
            g.setColor(Color.red);
            g.draw(this.getHitbox());
            g.drawRect((int) location.x - 15, (int) location.y - 15, 30, 30);
            g.drawString(name, hitbox.x, hitbox.y);
             //TODO DRAW LINE FACING ROTATION DIRECTION
             g.rotate(Math.toRadians(innateRotation));
             g.drawLine((int)location.x,(int)location.y, (int)location.x, (int)location.y-80);
             g.rotate(-Math.toRadians(innateRotation));
        }
        g.setTransform(old); //reset rotation for next item to render
    }
    
    
    /**
     * sets an object to not animate and only render one image as the animation
     * @param image static image to be rendered instead 
     */
    public void setAnimationFalse(BufferedImage image){
        isAnimated = false;
        sprite = image;
    }
    /**
     * sets the object to animate through a sequence
     * @param s sequence to begin rendering. may be changed later
     */
    public void setAnimationTrue(Sequence s){
        sequence = s;
        isAnimated = true;
    }
    
    public void tick(){
        updateLocation();
        tickNumber++;
    }
    
    public void updateLocation() {
      DCoordinate newLocation = location.copy();
        switch (movementType) {
            case SpeedRatio:
                double delta = 0.0;
                double totalVelocity = Math.abs(velocity.x) + Math.abs(velocity.y);
                if (totalVelocity != 0) {
                    delta = (baseSpeed) / totalVelocity;
                }
                newLocation.x += velocity.x * delta;
                newLocation.y += velocity.y * delta;
                break;
            case RawVelocity:
                newLocation.add(velocity);
                break;
        }
        //COLLISION
        if (isSolid) {
            for (GameObject2 other : Game.handler.getAllObjects()) {
                if (!other.isSolid || other==this) {
                    continue;
                }
                if (hitbox.intersects(other.getHitbox())) {
                    Rectangle intersection = hitbox.intersection(other.getHitbox());
                    boolean horizontalCollision = true;
                    boolean verticalCollision = true;
                    if(intersection.width < baseSpeed*1.5) {
                        horizontalCollision = false;
                    }
                    if(intersection.height < baseSpeed*1.5){
                        verticalCollision = false;
                    }
                    //verticalCollision and horizontalCollision determine if we just halt velocity or actively move object back
                    if (velocity.x > 0 && other.location.x > newLocation.x) {
                        newLocation.x = location.x;
                        if(!horizontalCollision) newLocation.x = other.hitbox.x - getWidth()/2 - 1;
                       //collision right
                    }
                    if (velocity.x < 0 && other.location.x < newLocation.x) {
                         newLocation.x = location.x;
                        if(!horizontalCollision) newLocation.x = other.hitbox.x + other.hitbox.width + getWidth()/2 +1;
                        //collision left
                    }
                    if (velocity.y > 0 && other.location.y > newLocation.y) {
                         newLocation.y = location.y;
                          if(!verticalCollision) newLocation.y = other.hitbox.y - getHeight()/2 - 1;
                        //collision down
                    }
                    if (velocity.y < 0 && other.location.y < newLocation.y) {
                        newLocation.y = location.y;
                        if(!verticalCollision) newLocation.y = other.hitbox.y + other.hitbox.height + getHeight()/2 + 1;
                       //collision up
                    }
                    collide(other);
                }
            }
        }
        location = newLocation;
        constrainToWorld();
        updateHitbox();
        
    }
    
    
    /**
     * prevents the object from moving outside the world by resetting the location
     * to a legal, in-bounds location
     */
    public void constrainToWorld(){
        if(location.x < 0) location.x=0;
        if(location.y < 0) location.y=0;
        if(location.x > Game.worldWidth - Game.worldBorder) location.x = Game.worldWidth- Game.worldBorder;
        if(location.y > Game.worldHeight - Game.worldBorder) location.y = Game.worldHeight- Game.worldBorder;
    }
    
    public GameObject2(Coordinate c){
      init(new DCoordinate(c));
      ID = IDLog++;
      
    }
    public GameObject2(DCoordinate dc){
        init(dc);
        ID = IDLog++;
    }
    /**
     * sets initial values common for all gameObjects
     * @param dc spawn point
     */
    private final void init(DCoordinate dc){
        location = dc;
        //set up default pathing modifiers. Move normal on ground, half speed in water, not at all in impass
        pathingModifiers.put(PathingLayer.Type.ground, 1.0);
        pathingModifiers.put(PathingLayer.Type.water, .5);
        pathingModifiers.put(PathingLayer.Type.impass, 0.0);
    }
    /**
     * removes object from game, functionally
     */
    protected void destroy() {
        isAlive = false;
        onDestroy();
        Game.mainGame.removeObject(this);
    }
    /**
     * method that runs when this object is destroyed, to be used for gameplay
     */
    public void onDestroy(){
        
    }
    
    /**
     * Runs each tick this object's hitbox is touching another object's hitbox
     * @param other the object whose hitbox we are touching
     */
    public void collide(GameObject2 other){
       
    }
    
    /**
     * If this onbject's hitbox is intersected by the camera's field of view
     * @return 
     */
    public boolean isOnScreen(){
        return this.getHitbox().intersects(Camera.getFieldOfView());
    }
}

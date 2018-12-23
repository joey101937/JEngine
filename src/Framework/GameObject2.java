/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.Stickers.Sticker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent class for all objects that appear in the gameworld
 * @author Joseph
 */
public class GameObject2 {
    public Game hostGame;
    public String name= "Unnamed " + this.getClass().getName(); 
    public long tickNumber = 0; //used for debugging, counts number of times this has ticked
    public long renderNumber = 0; //used for debugging, counts number of times this has rendered
    public DCoordinate location = new DCoordinate(0,0); //location relative to the world
    public DCoordinate velocity = new DCoordinate(0,0); //added to location as a ratio of speed each tick
    public int innateRotation = 0; //0 = top of sprite is forwards, 90 is right of sprite is right, 180 is bottom of sprite is forwards etc
    protected double baseSpeed = 2; //total distance the object can move per tick
    private boolean isAnimated = false;//weather or not this object uses sprite or sequence
    protected Sequence sequence = null; //animation sequence to run if animated
    public BufferedImage sprite = null; //static sprite if not animated
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public double rotation = 0;
    /**non-solid object will phase through other objects without triggering either object's onCollide method*/
    public boolean isSolid = false; //weather or not this object collides with other objects
    public boolean isInvisible = false; //invisible gameobjects are not rendered
    public double scale = 1; //size multiplier
    protected boolean isAlive = true; //weather or not the object has been destroyed
    protected boolean horizontalFlip = false;
    public MovementType movementType = MovementType.SpeedRatio;
    public int plane = 0; //which 'layer' a unit is on. Units only collide with others in the same plane
    protected Hitbox hitbox ;
    protected ArrayList<Sticker> attachedStickers = new ArrayList<>();
    public final int ID;
    private static int IDLog = 0; //used to assign IDs
    public HashMap<PathingLayer.Type,Double> pathingModifiers = new HashMap<>(); //stores default speed modifiers for different terrain types
    public ArrayList<SubObject> subObjects = new ArrayList<>(); //stores all subobjects on this object
    /**this determines weather or not a gameobject will be able to move through other solid units, however this still triggers onCollide*/
    public boolean preventOverlap = true; 
    
    /**
     * @return speed this unit should be able to move at with the current terrain
     */
    public double getSpeed() {
        if (hostGame.pathingLayer == null) {
            return baseSpeed;
        }
        return baseSpeed * pathingModifiers.get(currentTerrain());
    }

   public double getBaseSpeed(){
       return baseSpeed;
   }
   public void setBaseSpeed(double bs){
       baseSpeed = bs;
   }

   /**
    * Gets the current terrain this object is on; Terrain is determined by 
    * terrain type of pixel that this object is centered on
    */
   public PathingLayer.Type currentTerrain(){
       if(hostGame.pathingLayer==null){
           System.out.println("trying to get terrain type of null pathing layer -"+name);
           return null;
       }
       return hostGame.pathingLayer.getTypeAt(this.getPixelLocation());
   }
   /**
    * runs when the game containing this gameobject is issued a pause or unpause order
    * @param input true = pausing. false = unpausing
    */
    public void onGamePause(boolean input) {   
    }
    
    
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
    public Hitbox getHitbox() {
        return hitbox;
    }

    
    public void setHitbox(Hitbox replacement){
        this.hitbox = replacement;
    }
    


    /**
     * returns the width of the visual. If no visual, return 0.
     * @return width of visual
     */
    public int getWidth() {
        try{
        if (isAnimated) {
            return (int)(sequence.getCurrentFrame().getWidth());
        } else {
            return (int)(sprite.getWidth());
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
                return (int)(sequence.getCurrentFrame().getHeight());
            } else {
                return (int)(sprite.getHeight());
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
        if (isSolid && preventOverlap && getHitbox()!=null){
            //if solid first check collisions
            for (GameObject2 other : hostGame.getAllObjects()) {
                if (plane==other.plane && other.isSolid && other.getHitbox()!=null && getHitbox().intersectsIfRotated(other.getHitbox(), degrees-rotation)){
                    return;
                }
               
            }
        }
        rotation = degrees - innateRotation;
        if (getHitbox() != null) {
            getHitbox().rotateTo(degrees);
        }

    }

    public void rotate(double degrees) {
        if(isSolid && preventOverlap && getHitbox()!=null){
            //if solid first check collisions
            for(GameObject2 other : hostGame.getAllObjects()){
                if(plane==other.plane && other.isSolid && other.getHitbox()!=null && getHitbox().intersectsIfRotated(other.getHitbox(), degrees)){
                     return; 
                }
            }
        }
        rotation += degrees;
        if(getHitbox()!=null){
            getHitbox().rotate(degrees);
        }
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
     * Rotates this object so that its front (determined by innate rotation) is
     * angled towards given location
     * @param destination location to look at
     */
    public void lookAt(Coordinate destination){
         setRotation(DCoordinate.angleFrom(getPixelLocation(), destination) - innateRotation);
    }
    /**
     * Draws the object on screen in the game world
     * @param g Graphics2D object to draw with
     */
    public void render(Graphics2D g){
        renderNumber++;
        if((!isOnScreen() && !Main.overviewMode)||isInvisible)return;
        Coordinate pixelLocation = getPixelLocation();
        AffineTransform old = g.getTransform();
        if(sequence!=null && sequence.getScale()!=scale){
            sequence.scaleTo(scale);
        }
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
            g.drawRect((int) location.x - 15, (int) location.y - 15, 30, 30);
            g.drawString(name, (int)location.x-getWidth()/2, (int)location.y-getHeight()/2);
             //TODO DRAW LINE FACING ROTATION DIRECTION
             g.rotate(Math.toRadians(innateRotation));
             g.drawLine((int)location.x,(int)location.y, (int)location.x, (int)location.y-80);
             g.rotate(-Math.toRadians(innateRotation));
        }
        g.setTransform(old); //reset rotation for next item to render
        if(Main.debugMode && getHitbox()!=null)getHitbox().render(g); //render hitbox without graphics rotation
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
    
    /**
     * maintains hitboxes, runs after default render.
     * Override to use custom hitboxes.
     * 
     * by default, this sets up a rectangular hitbox and maintains it based on current sprite
     * if the hitbox is set to be circular, maintains circle radius to be equal to width/2
     */
    public void updateHitbox() {
        if (getHitbox() == null && getWidth()>0 && renderNumber>0) {
            int width = getWidth();
            int height = getHeight();
            Coordinate[] verts = new Coordinate[4];
            verts[0] = new Coordinate(-width / 2, -height / 2);
            verts[1] = new Coordinate(width / 2, -height / 2);
            verts[2] = new Coordinate(-width / 2, height / 2);
            verts[3] = new Coordinate(width / 2, height / 2);
            setHitbox(new Hitbox(this, verts));
            return;
        }
        if (getHitbox() != null && getHitbox().type == Hitbox.Type.box) {
            int width = getWidth();
            int height = getHeight();
            Coordinate[] verts = new Coordinate[4];
            verts[0] = new Coordinate(-width / 2, -height / 2);
            verts[1] = new Coordinate(width / 2, -height / 2);
            verts[2] = new Coordinate(-width / 2, height / 2);
            verts[3] = new Coordinate(width / 2, height / 2);
            getHitbox().setVertices(verts);
        }else if(getHitbox() != null && getHitbox().type == Hitbox.Type.circle){
            getHitbox().radius = getWidth()/2;
        }
        
    }

    /**
     * this method runs every "tick" similar to update() in unity; Reccomended you
     * start your overridden tick method with super() so that updateLocation
     * method runs and tickNumber continues counting
     */
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
                    delta = (getSpeed()) / totalVelocity;
                }
                newLocation.x += velocity.x * delta;
                newLocation.y += velocity.y * delta;
                break;
            case RawVelocity:
                newLocation.add(velocity);
                break;
        }
        //COLLISION
        if (isSolid && getHitbox()!=null) {
            Coordinate toMove = new Coordinate((int)(newLocation.x-location.x),(int)(newLocation.y-location.y));
            for (GameObject2 other : hostGame.handler.getAllObjects()) {
                if (!other.isSolid || other==this || other.plane!=plane) {
                    continue;
                }
                if (getHitbox().intersects(other.getHitbox())) {
                    //if we are already on top of another unit, just keep going to not get stuck
                    onCollide(other);
                    continue;
                }
                if (preventOverlap && other.preventOverlap && getHitbox().intersectsIfMoved(other.getHitbox(), new Coordinate((int)Math.ceil(velocity.x),(int)Math.ceil(velocity.y)))) {
                    //if we would collide with a unit, stop moving and run onCollide
                    //prevents units from stacking on top of eachother
                    newLocation = location.copy();
                    onCollide(other);
                    continue;
                }
                for (SubObject sub : other.subObjects) {
                    if (getHitbox().intersects(sub.getHitbox())) {
                        //if we are already on top of another unit, just keep going to not get stuck
                        onCollide(sub);
                        continue;
                    }
                    if (preventOverlap && other.preventOverlap && getHitbox().intersectsIfMoved(sub.getHitbox(), new Coordinate((int) Math.ceil(velocity.x), (int) Math.ceil(velocity.y)))) {
                        //if we would collide with a unit, stop moving and run onCollide
                        //prevents units from stacking on top of eachother controlled with preventOverlap condtion
                        newLocation = location.copy();
                        onCollide(sub);
                        continue;
                    }
                }
            }

        }
        if(hostGame.pathingLayer==null || this.pathingModifiers.get(hostGame.pathingLayer.getTypeAt(new Coordinate(newLocation))) > .05){
            //Only change location if the terrain there is pathable with a speed multiplier of at least .05
            location = newLocation;
        }
        updateHitbox();
        constrainToWorld();
    }
    
    
    /**
     * prevents the object from moving outside the world by resetting the location
     * to a legal, in-bounds location
     */
    public void constrainToWorld(){
        if(location.x < hostGame.worldBorder) location.x=hostGame.worldBorder;
        if(location.y < hostGame.worldBorder) location.y=hostGame.worldBorder;
        if(location.x > hostGame.worldWidth - hostGame.worldBorder) location.x = hostGame.worldWidth- hostGame.worldBorder;
        if(location.y > hostGame.worldHeight - hostGame.worldBorder) location.y = hostGame.worldHeight- hostGame.worldBorder;
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
        //set up default pathing modifiers. Move normal on ground, reduced speed in water, not at all in impass
        pathingModifiers.put(PathingLayer.Type.ground, 1.0);
        pathingModifiers.put(PathingLayer.Type.water, .33);
        pathingModifiers.put(PathingLayer.Type.impass, 0.0);
    }
    /**
     * removes object from game, functionally
     */
    public final void destroy() {
        isAlive = false;
        onDestroy();
        if (!(this instanceof SubObject)) {
            hostGame.removeObject(this);

        }else{
            SubObject me = (SubObject)this;
            me.setHost(null);
        }
    }

    /**
     * method that runs when this object is destroyed, to be used for gameplay
     * Note this does not handle technical engine removal of object from game
     */
    public void onDestroy(){
        
    }
    
    /**
     * @return weather or not this object is considered alive 
     */
    public boolean isAlive(){
        return isAlive;
    }
    
    /**
     * Runs each tick this object's hitbox is touching another object's hitbox
     * @param other the object whose hitbox we are touching
     */
    public void onCollide(GameObject2 other){
   //     if(!Main.debugMode)return;
   //   System.out.println(this.toString() + " colliding with " + other);
    }
    
    /**
     * If this onbject's hitbox is intersected by the camera's field of view
     * @return 
     */
    public boolean isOnScreen(){
        Rectangle sightBox = new Rectangle((int)this.location.x-getWidth()/2,(int)this.location.y-getHeight()/2,getWidth(),getHeight());
        return sightBox.intersects(hostGame.camera.getFieldOfView());
    }
    
    @Override
    public String toString(){
        return this.name + " in game " + hostGame;
    }
 
    
    public ArrayList<Sticker> getAttachedStickers(){
        return attachedStickers;
    }
    
    public void detatchSticker(Sticker s){
        if(attachedStickers.contains(s)){
            attachedStickers.remove(s);
            s.attachTo(null);
        }
    }
    //detaches all stickers which have been attached to this object
    public void detatchAllStickers() {
        for (Sticker s : attachedStickers) {
            s.attachTo(null);
        }
        attachedStickers.clear();
    }


}

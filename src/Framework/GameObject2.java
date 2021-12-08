/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Stickers.Sticker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import Framework.GraphicalAssets.Graphic;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Parent class for all objects that appear in the gameworld
 * @author Joseph
 */
public class GameObject2 {    
    private Game hostGame;
    private String name= "Unnamed " + this.getClass().getSimpleName(); 
    public long tickNumber = 0; //used for debugging, counts number of times this has ticked
    public long renderNumber = 0; //used for debugging, counts number of times this has rendered
    public DCoordinate location = new DCoordinate(0,0); //location relative to the world
    protected DCoordinate locationAsOfLastTick = new DCoordinate(0,0);
    public DCoordinate velocity = new DCoordinate(0,0); //added to location as a ratio of speed each tick
    protected double baseSpeed = 2; //total distance the object can move per tick
    private Graphic graphic; //visual representation of the object
    private double rotation = 0; //rotatoin in degrees (not radians)
    private double rotationAsOfLastTick = 0; // rotation in degrees as of last tick
    /**non-solid object will phase through other objects without triggering either object's onCollide method*/
    public boolean isSolid = false; //weather or not this object collides with other objects
    public boolean isInvisible = false; //invisible gameobjects are not rendered
    private volatile double scale = 1; //size multiplier
    public MovementType movementType = MovementType.SpeedRatio;
    private int zLayer = 1;
    public boolean shouldFlushGraphicOnDestroy = false;
    private HashMap<String, Object> syncedState = new HashMap<>();
    private HashMap<String, Object> futerSyncedState = new HashMap<>();
    
     /**
     * how the object behaves when traveling in two directions (up/down and
     * side/side) and it collides with something in one but not both of the
     * directions Disabled = stops moving Enabled = moves relative to portion of
     * velocity in unblocked direction
     */
    public boolean collisionSliding = true;
    public int plane = 0; //which 'layer' a unit is on. Units only collide with others in the same plane
    protected Hitbox hitbox ;
    protected ArrayList<Sticker> attachedStickers = new ArrayList<>();
    public final int ID;
    private static int IDLog = 0; //used to assign IDs
    public HashMap<PathingLayer.Type,Double> pathingModifiers = new HashMap<>(); //stores default speed modifiers for different terrain types
    private CopyOnWriteArrayList<SubObject> subObjects = new CopyOnWriteArrayList<>(); //stores all subobjects on this object
    /**this determines weather or not a gameobject will be able to move through other solid units, however this still triggers onCollide*/
    public boolean preventOverlap = true; 

    
    /**
     * returns list of subobjects immediately linked to this. Does not get the
     * subobject of the subobjects however
     * @return 
     */
    public CopyOnWriteArrayList<SubObject> getImmediateSubObjects() {
        return subObjects;
    }

    
    public void setImmediateSubObjects(CopyOnWriteArrayList<SubObject> subObjects) {
        this.subObjects = subObjects;
    }

    /**
     * recursively gets all subobjects and all subobjects of subobjects
     * @return 
     */
    public List<SubObject> getAllSubObjects() {
        List<SubObject> out = new ArrayList<>();
        for (SubObject so : getImmediateSubObjects()) {
            out.add(so);
            for (SubObject so2 : so.getAllSubObjects()) {
                out.add(so2);
            }
        }
        return out;
    }

    
    public void addSubObject(SubObject sub){
        sub.setHost(this);
    }
    
    
    public Game getHostGame(){
    return hostGame;
    }
    
    /**
     * @return speed this unit should be able to move at with the current terrain
     */
    public double getSpeed() {
        if (hostGame.pathingLayer == null) {
            return baseSpeed;
        }
        return baseSpeed * pathingModifiers.get(getCurrentTerrain());
    }

   public double getBaseSpeed(){
       return baseSpeed;
   }
   public void setBaseSpeed(double bs){
       baseSpeed = bs;
   }
   
   /**
    * runs when the gameobject2 runs or attempts to run into a new pathing layer
    * @param newLayer 
    */
   public void onPathingLayerCollision(PathingLayer.Type newLayer) {
   }

   /**
    * Gets the current terrain this object is on; Terrain is determined by 
    * terrain type of pixel that this object is centered on
    * @return terrain type at the center of this object
    */
   public PathingLayer.Type getCurrentTerrain(){
       if(hostGame.pathingLayer==null){
           System.out.println("trying to get terrain type of null pathing layer -"+getName());
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
    RawVelocity, SpeedRatio, RotationBased;
    }

    
    /**
     * used to get integer location of object, used when rendering to screen
     * AS OF LAST TICK- PREFER WHEN MULTITHREADING
     * @return integer location
     */
    public Coordinate getPixelLocation() {
        return new Coordinate(locationAsOfLastTick);
    }
    
     /**
     * used to get integer location of object, used when rendering to screen
     * @param realtime is this realtime location or location as of last tick. 
     * @return integer location
     */
    public Coordinate getPixelLocation(boolean realtime) {
        return new Coordinate( realtime ? location : getLocationAsOfLastTick());
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
        return graphic.getCurrentImage().getWidth();
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
            return graphic.getCurrentImage().getHeight();
        } catch (NullPointerException npe) {
            return 0;
        }
    }
    
    /**
     * gets current visual representation of this object
     * could be Sprite class if not animated or Sequence class if animated
     * @return current visual representation object
     */
    public Graphic getGraphic(){
        return graphic;
    }
    
     /**
     * Sets current visual representation of this object
     * could be Sprite class if not animated or Sequence class if animated
     * @param g new graphic object
     */
    public void setGraphic(Graphic g){
        g.scaleTo(getScale());
        graphic = g;
    }

    /**
     * Directly sets the rotation of this object to a given degree.
     * Does not check collision.
     * @param degree sets direction to this exact degree
     */
    public void setRotation(double degree){
        rotation = degree;
    }
    
    /**
     * @return Rotation of this object in degrees.
     */
    public double getRotation(){
        return rotationAsOfLastTick;
    }
    
    public double getRotationRealTime() {
        return rotation;
    }
    
    /**
     * rotates the object to a given degree, checks for collision at destination
     * @param degrees new degree of location.
     */
    public void rotateTo(double degrees) {
        double degreeToRotate = degrees-rotation;
        rotate(degreeToRotate);
    }

    /**
     * rotates the object from the current rotation to the current location +
     * given degrees. Checks collision at destination
     * @param degrees amount to rotate 
     */
    public void rotate(double degrees) {
        if(isSolid && preventOverlap && getHitbox()!=null){
            //if solid first check collisions
            for(GameObject2 other : getHostGame().getAllObjects()){
                if(canCollideWith(other) && getHitbox().intersectsIfRotated(other.getHitbox(), degrees) && !getHitbox().intersects(other.getHitbox())){
                     // getHostGame().handler.registerCollision(this, other);
                     this.onCollide(other, true);
                     other.onCollide(this, false);
                     return; 
                }
            }
        }
        rotation += degrees;
        for(SubObject sub : subObjects){
            sub.onHostRotate(degrees);
        }
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
        rotateTo(DCoordinate.angleFrom(location, other.getLocationAsOfLastTick()));
    }
    /**
     * Rotates this object so that its front (determined by innate rotation) is
     * angled towards given location
     * @param destination location to look at
     */
    public void lookAt(DCoordinate destination){
         rotateTo(DCoordinate.angleFrom(location, destination));
    }
        /**
     * Rotates this object so that its front (determined by innate rotation) is
     * angled towards given location
     * @param destination location to look at
     */
    public void lookAt(Coordinate destination){
         rotateTo(DCoordinate.angleFrom(getPixelLocation(), destination));
    }
    
    /**
     * returns degree of rotation required to face given point from current orientation
     * @param point point to look at
     * @return degree of rotation required to face given point from current orientation
     */
    public double angleFrom(Coordinate point){
        double result = DCoordinate.angleFrom(getPixelLocation(), point);
        if(result-getRotation()>180)result-=360;
        return result - getRotation();
    }
    
    
     /**
     * Draws the object on screen in the game world
     * @param g Graphics2D object to draw with
     */
    public void render(Graphics2D g) {
        render(g, false);
    }
    
    /**
     * Draws the object on screen in the game world
     * @param g Graphics2D object to draw with
     */
    public void render(Graphics2D g, boolean ignoreRestrictions){
        Graphics2D graphics = (Graphics2D)g.create();
        renderNumber++;
        if (getGraphic() != null && getGraphic().getScale() != scale) {
            getGraphic().scaleTo(scale);
        }
        Coordinate pixelLocation = getPixelLocation();
        AffineTransform old = graphics.getTransform();
        if (!isOnScreen() && !Main.overviewMode() && !ignoreRestrictions) {
            //offscreen without overview mode? dont bother rendering anything.
            if (isAnimated()) {
                Sequence sequence = (Sequence) getGraphic();
                if (sequence.getCurrentFrame() != null && sequence.currentFrameIndex == sequence.frames.length - 1) {
                    this.onAnimationCycle();
                }
            }
            return;
        }
        if (isInvisible) { //if invisible, you can still see debug mode visuals
            if (Main.debugMode) {
                renderDebugVisuals(graphics);
                if (getHitbox() != null) {
                    graphics.setTransform(old);
                    getHitbox().render(graphics);
                }
            }
            return;
        }
        while(rotation > 360){rotation-=360;}  //constrain rotation size
        while(rotation < -360){rotation+=360;}
        graphics.rotate(Math.toRadians(rotation), getPixelLocation().x, getPixelLocation().y);
        if (getGraphic() == null) {
            //System.out.println("Warning null graphic for " + name);
        } else if (isAnimated()) {
            Sequence sequence = (Sequence)getGraphic();
            if(sequence == null){
                if(renderNumber>10 && tickNumber>2)System.out.println("Warning trying to render null sequence object " +getName());
                if(Main.debugMode){
                    renderDebugVisuals(graphics);
                }
                return;
            }
            if(sequence.getCurrentFrame()!=null){
                sequence.startAnimating();
                BufferedImage toRender = sequence.getCurrentFrame();
                graphics.drawImage(toRender, pixelLocation.x-toRender.getWidth()/2 , pixelLocation.y-toRender.getHeight()/2,null); //draws frmae centered on pixelLocation
                if(sequence.currentFrameIndex == sequence.frames.length-1) this.onAnimationCycle();
            }else{
                if(renderNumber>10 && tickNumber>2)System.out.println("Warning: null frame in sequence of " + getName());
            }
        }else{
            Sprite sprite = (Sprite)getGraphic();
            if(sprite!=null){                
                graphics.drawImage(sprite.getImage(), pixelLocation.x - sprite.getImage().getWidth() / 2, pixelLocation.y - sprite.getImage().getHeight() / 2, null); //draws sprite centered on pixelLocation
            } else {
                if (renderNumber > 10 && tickNumber > 2) {
                    System.out.println("Warning: unanimated game object sprite is null " + getName());
                }
            }
        }
        if (Main.debugMode) {
            renderDebugVisuals(graphics);
        }
        graphics.setTransform(old); //reset rotation for next item to render
        if (getHitbox() != null && Main.debugMode) {
            getHitbox().render(graphics);
        }
    }

    /**
     * renders the little rectangle and line showing where the object is and
     * orientation in debug mode, also renders a string showing object name
     *
     * @param g graphics to use
     */
    public void renderDebugVisuals(Graphics2D g) {
        Color originalColor = g.getColor();
        g.setColor(Color.red);
        g.drawRect((int) location.x - 15, (int) location.y - 15, 30, 30);
        g.drawString(getName(), (int) location.x - getWidth() / 2, (int) location.y - getHeight() / 2);
        g.drawLine((int) location.x, (int) location.y, (int) location.x, (int) location.y - 80);
    }

    /**
     * maintains hitboxes, runs after default render.
     * Override to use custom hitboxes.
     * by default, this sets up a rectangular hitbox and maintains it based on current sprite
     * if the hitbox is set to be circular, maintains circle radius to be equal to width/2
     */
    public synchronized void updateHitbox() {
        //if no hitbox, create the default box hitbox
        if (getHitbox() == null && getWidth()>0 && renderNumber>0) {
            int width = getWidth();
            int height = getHeight();
            Coordinate[] verts = new Coordinate[4];
            verts[0] = new Coordinate(-width / 2, -height / 2);
            verts[1] = new Coordinate(width / 2, -height / 2);
            verts[2] = new Coordinate(-width / 2, height / 2);
            verts[3] = new Coordinate(width / 2, height / 2);
            Hitbox hb = new Hitbox(this,verts);
            hb.rotateTo(getRotation());
            setHitbox(hb);
            return;
        }
        //maintain the default hitbox
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
            //maintain default circle hitbox
            getHitbox().radius = getWidth()/2;
        }
        
    }
    
    
      /**
     * this method runs every "tick". All ticking objects will execute this method before any objects can tick.
     * so every tick all preticks fire, then all ticks fire.
     */
    public void preTick() {
        updateLocation();
        tickNumber++;
    }

    /**
     * this method runs every "tick" similar to update() in unity; Reccomended you
     * start your overridden tick method with super() so that updateLocation
     * method runs and tickNumber continues counting
     */
    public void tick(){
    }
    
    private boolean canCollideWith(GameObject2 other) {
        return isSolid && other.isSolid
                && other != this
                && this.getHitbox() != null && other.getHitbox() != null
                && this.plane == other.plane;
    }
    
    /**
     * Returns new movement based on collisions given a proposed movement
     * triggers on collide
     * @param proposedMovement
     * @return new movement, adjusted for collissions
     */
    private DCoordinate updateMovementBasedOnCollision(DCoordinate proposedMovement) {
        Coordinate roundedProposedMovement = new Coordinate(
                proposedMovement.x >= 0 ? (int) Math.ceil(proposedMovement.x) : (int)Math.floor(proposedMovement.x),
                proposedMovement.y >= 0 ? (int) Math.ceil(proposedMovement.y) : (int)Math.floor(proposedMovement.y)
        );
        DCoordinate newLocation;
        ArrayList<GameObject2> otherObjects = hostGame.handler.getAllObjects();
        otherObjects.remove(this);
        ArrayList<GameObject2> otherObjsAndOtherSubObjects = new ArrayList<>();
        for (GameObject2 other : otherObjects) {
            otherObjsAndOtherSubObjects.add(other);
            for (GameObject2 sub : other.getAllSubObjects()) {
                otherObjsAndOtherSubObjects.add(sub);
            }
        }
        ArrayList<GameObject2> thisAndSubs = new ArrayList<>();
        thisAndSubs.add(this);
        for (GameObject2 sub : this.getAllSubObjects()) {
            thisAndSubs.add(sub);
        }
        boolean xClear = true;
        boolean yClear = true;
        for (GameObject2 current : thisAndSubs) {
            if (!current.isSolid || current.getHitbox() == null) {
                continue;
            }
            newLocation = current.location.copy();
            newLocation.add(proposedMovement);
            for (GameObject2 other : otherObjsAndOtherSubObjects) {
                if (!xClear && !yClear) {
                    return new DCoordinate(0, 0);
                }
                if (!canCollideWith(other)) {
                    continue;
                }
                double[] movementLine = {current.location.x, current.location.y, newLocation.x, newLocation.y};
                boolean needsMovementLine = proposedMovement.x + proposedMovement.y > getWidth();
                if (current.getHitbox().intersectsIfMoved(other.getHitbox(), roundedProposedMovement)
                        || (needsMovementLine && other.getHitbox().intersectsWithLine(movementLine))) {
                    // getHostGame().handler.registerCollision(this, other);
                    current.onCollide(other, true);
                    other.onCollide(current, false);
                    if (!current.preventOverlap || !other.preventOverlap) {
                        continue;
                    }
                    // already overlapping
                    if (current.getHitbox().intersects(other.getHitbox())) {
                        if (newLocation.distanceFrom(other.getLocationAsOfLastTick()) > current.location.distanceFrom(other.getLocationAsOfLastTick())) {
                            continue; //if we are moving away from it, allow the movement
                        } else {
                            xClear = false;
                            yClear = false;
                        }
                    }
                    if (current.collisionSliding) {
                        // check x only then y only
                        // use to determine what directions are clear
                        if (current.getHitbox().intersectsIfMoved(other.getHitbox(), new Coordinate((int) roundedProposedMovement.x, 0))) {
                            xClear = false;
                        }
                        if (current.getHitbox().intersectsIfMoved(other.getHitbox(), new Coordinate(0, (int) roundedProposedMovement.y))) {
                            yClear = false;
                        }
                    } else {
                        xClear = false;
                        yClear = false;
                    }
                }
            }
        }
        DCoordinate newMovement = new DCoordinate(
                xClear ? proposedMovement.x : 0,
                yClear ? proposedMovement.y : 0
        );
        return newMovement;
    }
    /**
     * This method runs every tick and controls object positioning regarding:
     * note: called as part of default tick; if you want to override tick then
     * you will need to call super.tick() or this once directly. (not both)
     * -Adjusts object position based on velocity
     * -Constrains object to world when necessary
     * -Detects collisions
     * -updates hitbox
     */
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
            case RotationBased:
                double deltaR = 0.0;
                DCoordinate vel = velocity.copy();
                vel = DCoordinate.adjustForRotation(vel, rotation);
                double totalVelocityR = Math.abs(vel.x) + Math.abs(vel.y);
                if (totalVelocityR != 0) {
                    deltaR = (getSpeed()) / totalVelocityR;
                }
                newLocation.x += vel.x * deltaR;
                newLocation.y += vel.y * deltaR;
                break;
            case RawVelocity:
                newLocation.add(velocity);
                break;
            default :
                throw new RuntimeException("Movement Type undefined for object: " + this);
        }
         DCoordinate proposedMovement = new DCoordinate((newLocation.x-location.x),(newLocation.y-location.y));
           //COLLISION
          newLocation = location.copy();
          newLocation.add(updateMovementBasedOnCollision(proposedMovement));
           

        
        //pathing layer now
        
        if (hostGame.pathingLayer != null) {
            if(!hostGame.pathingLayer.getTypeAt(getPixelLocation()).name.equals(hostGame.pathingLayer.getTypeAt(newLocation.toCoordinate()).name)) {
                this.onPathingLayerCollision(hostGame.pathingLayer.getTypeAt(newLocation.toCoordinate()));
            }
            if (pathingModifiers.get(hostGame.pathingLayer.getTypeAt(new Coordinate(newLocation))) < .05 && collisionSliding) {
                //pathing at new location is blocked. (speed multiplier < .05)
                //check directions to see which are blocked so we can possibly slide
                boolean xClear = pathingModifiers.get(hostGame.pathingLayer.getTypeAt(new Coordinate((int) Math.ceil(newLocation.x), (int) Math.ceil(location.y)))) > .05;
                boolean yClear = pathingModifiers.get(hostGame.pathingLayer.getTypeAt(new Coordinate((int) Math.ceil(location.x), (int) Math.ceil(newLocation.y)))) > .05;
                if (!xClear) {
                    newLocation.x = location.x;
                }
                if (!yClear) {
                    newLocation.y = location.y;
                }
            }
        }
        if(hostGame.pathingLayer==null || pathingModifiers.get(hostGame.pathingLayer.getTypeAt(new Coordinate(newLocation))) > .05){
             location = newLocation;
        }
        updateHitbox();
        constrainToWorld();
    }

    
    /**
     * runs whevenever an object would run out of bounds
     * by default, prevents the object from moving outside the world by 
     * resetting the location to a legal, in-bounds location
     */
    public void constrainToWorld(){
        if(location.x < hostGame.worldBorder) location.x=hostGame.worldBorder;
        if(location.y < hostGame.worldBorder) location.y=hostGame.worldBorder;
        if(location.x > hostGame.getWorldWidth() - hostGame.worldBorder) location.x = hostGame.getWorldWidth()- hostGame.worldBorder;
        if(location.y > hostGame.getWorldHeight() - hostGame.worldBorder) location.y = hostGame.getWorldHeight()- hostGame.worldBorder;
    }
    /**
     * Creates new GameObject2 at location
     * @param c location
     */
    public GameObject2(Coordinate c){
      init(new DCoordinate(c));
      ID = IDLog++;
    }
     /**
     * Creates new GameObject2 at exact location
     * @param dc location
     */
    public GameObject2(DCoordinate dc){
        init(dc);
        ID = IDLog++;
    }
     /**
     * Creates new GameObject2 at location
     * @param x location x-coordinate
     * @param y location y-coordinate
     */
    public GameObject2(int x, int y){
      init(new DCoordinate(x,y));
      ID = IDLog++; 
    }
    /**
     * sets initial values common for all gameObjects
     * @param dc spawn point
     */
    private final void init(DCoordinate dc){
        location = dc;
        locationAsOfLastTick = dc;
        //set up default pathing modifiers. Move normal on ground, reduced speed in water, not at all in impass
        pathingModifiers.put(PathingLayer.Type.ground, 1.0);
        pathingModifiers.put(PathingLayer.Type.hostile, 1.0);
        pathingModifiers.put(PathingLayer.Type.water, .33);
        pathingModifiers.put(PathingLayer.Type.impass, 0.0);
    }
    /**
     * removes object from game, functionally
     */
    public final void destroy() {
        onDestroy();
        for(SubObject so : getAllSubObjects()) {
            so.onDestroy();
        }
        if (!(this instanceof SubObject)) {
            hostGame.removeObject(this);
            
        }else{
//            SubObject me = (SubObject)this;
//            me.setHost(null);
        }
        // this.detatchAllStickers();
        // if(hitbox!=null)hitbox.host=null;
        // if(graphic!=null && this.shouldFlushGraphicOnDestroy)graphic.destroy();
        // graphic = null;
    }

    /**
     * method that runs when this object is destroyed, to be used for gameplay
     * Note this does not handle technical engine removal of object from game
     */
    public void onDestroy(){
        
    }
    /**
     * runs whenvever the current animation sequence renders the last frame in animation
     */
    public void onAnimationCycle(){}
    
    /**
     * @return weather or not this object is considered alive 
     */
    public boolean isAlive(){
        return hostGame.getAllObjects().contains(this);
    }
    /**
     * Weather or not this object is using an animated sequence or static sprite
     * if graphic is null, return false;
     * @return Weather or not this object is using an animated sequence or static sprite
     */
    public boolean isAnimated(){
        if(graphic==null)return false;
        return graphic.isAnimated();
    }
    
    /**
     * Runs each tick this object's hitbox is touching another object's hitbox
     * @param other the object whose hitbox we are touching
     * @param fromMyTick if this gameobject's tick initiated the collision
     */
    public void onCollide(GameObject2 other, boolean fromMyTick){
    }
    
    /**
     * If this onbject's hitbox is intersected by the camera's field of view
     * @return if the object is visable by the camera
     */
    public boolean isOnScreen(){
        Rectangle sightBox = new Rectangle((int)(this.location.x-getWidth()/2),(int)this.location.y-(getHeight()/2),getWidth()+1,getHeight()+1);
        return sightBox.intersects(hostGame.getCamera().getFieldOfView());
    }
    
    @Override
    public String toString(){
        return this.getName() + " in game " + getHostGame();
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

    /**
     * zLayer is the how high or low the object is on the Z axis, and determines which objects render
     * on top of which. Higher zLayers will render on top of lower.
     * @return zLayer value for this object.
     */
    public int getZLayer() {
        return zLayer;
    }
    
    /**
     * zLayer is the how high or low the object is on the Z axis, and determines which objects render
     * on top of which. Higher zLayers will render on top of lower.
     * @param zLayer new zLayer value for this object.
     */
    public void setZLayer(int zLayer) {
        this.zLayer = zLayer;
    }
    
    /**
     * sets the size scale to given multiple of original size
     * @param d size multiple
     */
    public void setScale(double d){
        scale=d;
    }
    
    /**
     * multiplies size scale of object by given amount from current
     * @param d amount to scale by
     */
    public void scale(double d){
        scale*=d;
    }
    
    /**
     * gets current size multiplier of object
     * @return current size multiplier of object
     */
    public double getScale(){
        return scale;
    }

    protected void setHostGame(Game g){
        hostGame = g;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DCoordinate getLocationAsOfLastTick() {
        return locationAsOfLastTick;
    }

    protected void setLocationAsOfLastTick(DCoordinate locationAsOfLastTIck) {
        this.locationAsOfLastTick = locationAsOfLastTIck;
    }
    
    protected void setRotationAsOfLastTick(double r) {
        this.rotationAsOfLastTick = r;
    }
    
    /**
     * moves future synced state into synced state, then clears it
     */
    protected void updateSyncedState() {
        for(String key : futerSyncedState.keySet()) {
            syncedState.put(key, futerSyncedState.get(key));
        }
        futerSyncedState.clear();
    }
    
    /**
     * sets a property to be tick-synced. this will not be accesable until next tick. or pretick
     * @param key name of property
     * @param value value to store
     */
    public void setSycnedProperty(String key, Object value) {
        futerSyncedState.put(key, value);
    }
    
    public Object getSycnedProperty(String key) {
        return syncedState.get(key);
    }
     

}

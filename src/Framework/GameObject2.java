/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.CoreLoop.Renderable;
import Framework.GraphicalAssets.Sequence;
import Framework.GraphicalAssets.Sprite;
import Framework.Stickers.Sticker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import Framework.GraphicalAssets.Graphic;
import java.awt.AlphaComposite;
import java.awt.image.VolatileImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Parent class for all objects that appear in the game world
 * @author Joseph
 */
public class GameObject2 implements Comparable<GameObject2>, Renderable{
    private Game hostGame;
    private String name= this.getClass().getSimpleName(); 
    /** counts number of times this has ticked. incremented in default preTick method */
    public long tickNumber = 0;
    /** counts number of times this has rendered. incremented in default render */
    public long renderNumber = 0;
    /** location relative to the world. Changing this move the object! */
    public DCoordinate location = new DCoordinate(0,0);
    protected DCoordinate locationAsOfLastTick = new DCoordinate(0,0);
    /** Used to move the object. Applied to location in default preTick method via the updateLocation method */
    public DCoordinate velocity = new DCoordinate(0,0);
    /** total distance the object can move per tick before pathing modifiers. RawVelocity movement type ignores this*/
    protected double baseSpeed = 2;
    private Graphic graphic; //visual representation of the object
    private double rotation = 0; //rotatoin in degrees (not radians)
    private double rotationAsOfLastTick = 0; // rotation in degrees as of last tick
    /**non-solid object will phase through other objects without triggering either object's onCollide method*/
    public boolean isSolid = false;
    /** invisible objects are not rendered by default however will render debug visuals and continue incrementing renderCount */
    public boolean isInvisible = false;
    private float renderOpacity = 1f;
    private double scale = 1;
    protected double scaleAsOfLastTick = 1;
    /** Determines how velocity is applied to this objects location each preTick */
    public MovementType movementType = MovementType.SpeedRatio;
    private int zLayer = 1;
    private final HashMap<String, Object> syncedState = new HashMap<>();
    private final HashMap<String, Object> futureSyncedState = new HashMap<>();
    private int widthAsOfLastTick = 0, heightAsOfLastTick = 0;
    public Coordinate lastRenderLocation = null;
    private boolean cachedHasCycled = false;
    
    private boolean movedLastTick = false; // if the object moved last tick. set with pretick
    private DCoordinate lastMovement = new DCoordinate(0,0); // difference between location now and last tick. used for lerp
    
    /**
     * this list is a list of point offsets from the center of the game object. When determining if a location is valid to move to
     * via velocity when using a pathing map, it will also check these points to make sure that the points do not overlap with impassable terrain
     */
    public ArrayList<Coordinate> additionalPathingChecks = new ArrayList<>();
    private boolean waitingToGenerateDefaultPathingChecks = false;
    
     /**
     * how the object behaves when traveling in two directions (up/down and
     * side/side) and it collides with something in one but not both of the
     * directions Disabled = stops moving Enabled = moves relative to portion of
     * velocity in unblocked direction
     */
    public boolean collisionSliding = true;
    /**
     * Which 'layer' a unit is on. Units only collide with others in the same plane
     */
    public int plane = 0; 
    protected Hitbox hitbox ;
    protected ArrayList<Sticker> attachedStickers = new ArrayList<>();
    /**
     * Identifier for this gameObject. This is by default a unique stringified integer.
     * If you update the logic for generating the ID, make sure that it is unique to avoid intermittent bugs
     */
    public String ID;
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
        if(hostGame == null) {
            if(Main.debugMode && Window.currentGame != null) System.out.println("Null hostgame for " + this.name + " returning " + Window.currentGame.getName());
            this.hostGame = Window.currentGame;
            return hostGame;
        }
        return hostGame;
    }
    
    /**
     * triggers when this object is added to a game
     */
    public void onGameEnter(){}
    
    /**
     * @return speed this unit should be able to move at with the current terrain
     */
    public double getSpeed() {
        if (hostGame.pathingLayer == null) {
            return baseSpeed;
        }
        return baseSpeed * pathingModifiers.getOrDefault(getCurrentTerrain(), 1.0);
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

    @Override
    public int compareTo(GameObject2 o) {
        double myNum = getLocationAsOfLastTick().x + getLocationAsOfLastTick().y;
        double theirNum = o.getLocationAsOfLastTick().x + o.getLocationAsOfLastTick().y;
        if (myNum != theirNum) return myNum < theirNum ? -1 : 1;
        else return ID.compareTo(o.ID);
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
        return (realtime ? location : getLocationAsOfLastTick()).toPixelCoordinate();
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
        return (int)(graphic.getCurrentImage().getWidth() * scale);
        }catch(NullPointerException npe){
            System.out.println("Returnning 0 for width of GameObject2 with no graphic " + this);
            return 0;
        }
    }
    /**
     * returns the height of the visual gameobject. If no visual, return 0
     * @return height of visual
     */
    public int getHeight() {
        try {
            return (int)(graphic.getCurrentImage().getHeight() * scale);
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
        cachedHasCycled = false;
        return graphic;
    }
    
    public void onSetGraphic(Graphic newGraphic){};
    
     /**
     * Sets current visual representation of this object
     * could be Sprite class if not animated or Sequence class if animated
     * @param g new graphic object
     */
    public void setGraphic(Graphic g){
        if(graphic == g) return;
        graphic = g;
        onSetGraphic(g);
    }

    /**
     * Directly sets the rotation of this object to a given degree.
     * Does not check collision.
     * @param degree sets direction to this exact degree
     */
    public void setRotation(double degree){
        rotation = degree;
        for(SubObject sub : this.getAllSubObjects()) {
            sub.onHostRotate(degree - rotation);
        }
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
            double padding = hostGame.handler.currentSnapshot.largestSideLength * 1.5;
            if(!Main.ignoreCollisionsOnRotation) {
                for(GameObject2 other : getHostGame().getObjectsNearPoint(getPixelLocation(true), longestSideLength() + padding)){
                    if(canCollideWith(other) && getHitbox().intersectsIfRotated(other.getHitbox(), degrees) && !getHitbox().intersects(other.getHitbox())){
                         getHostGame().handler.registerCollision(this, other);
                         System.out.println("preventing rotation due to collision");
                         return; 
                    }
                }   
            }
        }
        rotation += degrees;
        for (SubObject sub : subObjects){
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
    public double rotationNeededToFace(Coordinate point){
        double result = DCoordinate.angleFrom(getPixelLocation(), point);
        if(result-getRotation()>180)result-=360;
        if(result-getRotation()<-180)result+=360;
        return result - getRotation();
    }
    
    /**
     * This is like rotationNeededToFace as if current rotation is always 0
     * returns degree of angle between this location and given location
     * @param point point to look at
     * @return 
     */
    public double angleFrom(Coordinate point){
        double result = DCoordinate.angleFrom(getPixelLocation(), point);
        if(result>180)result-=360;
        if(result < -181) result +=360;
        return result;
    }
    
    
    /**
     * distance between my exact location and the other object's exact point location
     * usually this means center-to-center
     * @param other other gameobject
     * @return  distance
     */
    public double distanceFrom(GameObject2 other) {
        if(this.getHostGame() != other.getHostGame()) return -1;
        return this.getLocationAsOfLastTick().distanceFrom(other.getLocationAsOfLastTick());
    }
    
    /**
     * distance between my exact location and the other exact point location
     * usually this means center-to-center
     * @param other other coord
     * @return  distance
     */
    public double distanceFrom(DCoordinate other) {
        return this.getLocationAsOfLastTick().distanceFrom(other);
    }
    
     /**
     * distance between my exact location and the other exact point location
     * usually this means center-to-center
     * @param other other coord
     * @return  distance
     */
    public double distanceFrom(Coordinate other) {
        return this.getLocationAsOfLastTick().distanceFrom(other);
    }
    
    
     /**
     * Draws the object on screen in the game world
     * @param g Graphics2D object to draw with
     */
    public void render(Graphics2D g) {
        render(g, false);
    }
    
    private boolean shouldTriggerOnAnimationCycle () {
        if(getGraphic() instanceof Sequence mySequ) {
            boolean cacheCheck = !cachedHasCycled && mySequ.hasCycled;
            boolean onLastFrame = mySequ.getCurrentFrame() != null && mySequ.currentFrameIndex == mySequ.frames.length - 1;
            cachedHasCycled = mySequ.hasCycled;
            return cacheCheck || onLastFrame;
        } else {
            cachedHasCycled = false;
            return false;
        }
    }
    
    private void scaleGraphicObj(Graphics2D graphics, double scale, DCoordinate locationOverride) {
        AffineTransform scaleTransform = graphics.getTransform();
        scaleTransform.translate(locationOverride.x, locationOverride.y);
        scaleTransform.scale(scale, scale);
        scaleTransform.translate(-locationOverride.x, -locationOverride.y);
        graphics.setTransform(scaleTransform);
    }
    
    private void scaleGraphicObj(Graphics2D graphics, double scale) {
        scaleGraphicObj(graphics, scale, location);
    }
    
    /**
     * This is the actual pixel location that the object should be rendered at. This will be equal to its pexelLocation unless
     * lerping is enabled. If it is, this will be lerp adjusted.
     * 
     * This should only be used for render logic. Game logic should use pixelLocation instead.
     * @return the lerp-adjusted coordinate.
     */
    public Coordinate getRenderLocation() {
         if(Main.enableLerping && movedLastTick) {
           Coordinate pixelLocation = getPixelLocation();
           float deltaTime = 1- getHostGame().getPercentThroughTick();

           DCoordinate movement = lastMovement.copy(); // getMovementNextTick();
           if(Main.lerpType != null && Main.lerpType.equals("predictive")) {
                movement = getMovementNextTick();
           }
           
           Coordinate renderOffset = movement.scale(deltaTime).toCoordinate();

           return constrainToWorld(pixelLocation.add(renderOffset).toDCoordinate()).toCoordinate();
        } else {
             return getPixelLocation();
         }
    }
    
    /**
     * Draws the object on screen in the game world
     * @param g Graphics2D object to draw with
     * @param ignoreRestrictions if this is true, it will render even if it usually wouldnt due to being off screen
     */
    public void render(Graphics2D g, boolean ignoreRestrictions){
        renderNumber++;
        Coordinate pixelLocation = getRenderLocation();
        lastRenderLocation = pixelLocation;
        
        boolean triggerAnimationCycle = shouldTriggerOnAnimationCycle();
        
        if (!isOnScreen() && !Main.overviewMode() && !ignoreRestrictions) {
            //offscreen without overview mode? dont bother rendering anything.
            if (triggerAnimationCycle) {
                this.onAnimationCycle();
            }
            return;
        }
        
        Graphics2D graphics = (Graphics2D)g.create();
        AffineTransform old = graphics.getTransform();
        
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
        graphics.rotate(Math.toRadians(rotation), pixelLocation.x, pixelLocation.y);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, renderOpacity));
        
        // Apply scaling
        scaleGraphicObj(graphics, scale, pixelLocation.toDCoordinate());
        
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
            if(sequence.getCurrentVolatileFrame()!=null){
                sequence.startAnimating();
                VolatileImage toRender = sequence.getCurrentVolatileFrame();
                graphics.drawImage(toRender, pixelLocation.x-toRender.getWidth()/2 , pixelLocation.y-toRender.getHeight()/2,null); //draws frame centered on pixelLocation
                if(triggerAnimationCycle) this.onAnimationCycle();
            }else{
                if(renderNumber>10 && tickNumber>2)System.out.println("Warning: null frame in sequence of " + getName());
            }
        }else{
            Sprite sprite = (Sprite)getGraphic();
            if(sprite!=null){                
                graphics.drawImage(sprite.getCurrentVolatileImage(), pixelLocation.x - sprite.getImage().getWidth() / 2, pixelLocation.y - sprite.getImage().getHeight() / 2, null); //draws sprite centered on pixelLocation
            } else {
                if (renderNumber > 10 && tickNumber > 2) {
                    System.out.println("Warning: unanimated game object sprite is null " + getName());
                }
            }
        }
        
        
        // Undo scaling
        scaleGraphicObj(graphics, 1/scale, pixelLocation.toDCoordinate());
        
         if (Main.debugMode) {
            renderDebugVisuals(graphics);
        }
      
        // reset graphics object in case the render method is overridden and then super.render() is called.
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // reset opacity
        graphics.setTransform(old); // reset transform
        
         
        
        
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
        for (Coordinate c: additionalPathingChecks) {
            Coordinate centerOfItem = location.copy().add(c).toCoordinate();
            int widthOfVisal = 8;
            g.drawRect(centerOfItem.x - widthOfVisal/2, centerOfItem.y - widthOfVisal/2, widthOfVisal, widthOfVisal);
        }
    }
    
    /**
     * populates the additionalPathingChecks with a basic set of offset points based on the object's
     * height, width and hit box type. USed to make the object collide with impassable terrain at its edges
     */
    public void generateDefaultPathingOffsets() {
        if(renderNumber <= 1) {
            // this method needs hitbox to be instantiated (happens on render 1, based on graphic)
            waitingToGenerateDefaultPathingChecks = true;
            return;
        }
        additionalPathingChecks.clear();
        
        // corner points based on hitbox type
        if(getHitbox().type == Hitbox.Type.box) {
            additionalPathingChecks.add(new Coordinate(getWidth()/2, 0)); // right middle
            additionalPathingChecks.add(new Coordinate(-getWidth()/2, 0)); // left middle
            additionalPathingChecks.add(new Coordinate(0, -getHeight()/2)); // top middle
            additionalPathingChecks.add(new Coordinate(0, getHeight()/2)); // bottom middle
        
            additionalPathingChecks.add(new Coordinate(getWidth()/2, getHeight()/2)); // bot right
            additionalPathingChecks.add(new Coordinate(getWidth()/2, -getHeight()/2)); // top right
            additionalPathingChecks.add(new Coordinate(-getWidth()/2, getHeight()/2)); // bot left
            additionalPathingChecks.add(new Coordinate(-getWidth()/2, -getHeight()/2)); // top left
        } else {
            // circle
            additionalPathingChecks.add(new Coordinate(getWidth()/2, 0)); // right middle
            additionalPathingChecks.add(new Coordinate(-getWidth()/2, 0)); // left middle
            additionalPathingChecks.add(new Coordinate(0, -getWidth()/2)); // top middle
            additionalPathingChecks.add(new Coordinate(0, getWidth()/2)); // bottom middle
            // find points on a circle https://math.stackexchange.com/questions/260096/find-the-coordinates-of-a-point-on-a-circle.
            // need to use *Math.PI/180 to turn raidans into degrees
            additionalPathingChecks.add(new DCoordinate(getWidth()/2 * Math.sin(45*Math.PI/180), getWidth()/2 * Math.cos(45*Math.PI/180)).toCoordinate()); // bot right
            additionalPathingChecks.add(new DCoordinate(getWidth()/2 * Math.sin(135*Math.PI/180), getWidth()/2 * Math.cos(135*Math.PI/180)).toCoordinate()); // bot left
            additionalPathingChecks.add(new DCoordinate(getWidth()/2 * Math.sin(225*Math.PI/180), getWidth()/2 * Math.cos(225*Math.PI/180)).toCoordinate()); // top left
            additionalPathingChecks.add(new DCoordinate(getWidth()/2 * Math.sin(315*Math.PI/180), getWidth()/2 * Math.cos(315*Math.PI/180)).toCoordinate()); // top right
        }
    }

    /**
     * maintains hitboxes, runs after default render.
     * Override to use custom hitboxes.
     * by default, this sets up a rectangular hitbox and maintains it based on current sprite
     * if the hitbox is set to be circular, maintains circle radius to be equal to width/2
     */
    public void updateHitbox() {
        //if no hitbox, create the default box hitbox
        if (getHitbox() == null && getWidthAsOfLastTick()>0 && renderNumber>0) {
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
            getHitbox().radius = Math.max(getWidthAsOfLastTick(), getHeightAsOfLastTick())/2;
            getHitbox().updateFarthestAndShortest();
        }
        
    }
    
    
      /**
     * this method runs every "tick". All ticking objects will execute this method before any objects can tick.
     * so every tick all preticks fire, then all ticks fire.
     */
    public void preTick() {
        while(rotation > 360){rotation-=360;}  //constrain rotation size
        while(rotation < -360){rotation+=360;}
        if(waitingToGenerateDefaultPathingChecks && renderNumber > 1) {
            System.out.println("queue done");
            waitingToGenerateDefaultPathingChecks = false;
            generateDefaultPathingOffsets();
        }
        if(this.getHitbox() != null) {
            getHitbox().updateFarthestAndShortest();
        }
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
     /**
     * this method runs every "tick" AFTER all objects have ticked
     */
    public void postTick(){
        updateHitbox();
        movedLastTick = !location.equals(locationAsOfLastTick);
        lastMovement = location.copy().subtract(locationAsOfLastTick);
    }
    
    private boolean canCollideWith(GameObject2 other) {
        return isSolid && other.isSolid
                && other != this
                && this.getHitbox() != null && other.getHitbox() != null
                && this.plane == other.plane
                && distanceFrom(other.locationAsOfLastTick) <= (getHitbox().getFarthestRange() + other.getHitbox().getFarthestRange() + Math.abs(velocity.x * getSpeed()) + Math.abs(velocity.y * getSpeed()));
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
        double padding = Main.collisionCheckRadius > 0 ? Main.collisionCheckRadius : hostGame.handler.currentSnapshot.largestSideLength * 1.5;
        ArrayList<GameObject2> otherObjects = hostGame.getObjectsNearPoint(getPixelLocation(), longestSideLength() + padding);
        otherObjects.remove(this);
        ArrayList<GameObject2> otherObjsAndOtherSubObjects = new ArrayList<>();
        if(!Main.ignoreSubobjectCollision) {
            for (GameObject2 other : otherObjects) {
                otherObjsAndOtherSubObjects.add(other);
                for (GameObject2 sub : other.getAllSubObjects()) {
                    otherObjsAndOtherSubObjects.add(sub);
                }
                
            }
        } else {
            otherObjsAndOtherSubObjects = otherObjects;
        }
        ArrayList<GameObject2> thisAndSubs = new ArrayList<>();
        thisAndSubs.add(this);
        if(!Main.ignoreSubobjectCollision) {
            for (GameObject2 sub : this.getAllSubObjects()) {
                thisAndSubs.add(sub);
            }
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
                if (current.getHitbox().intersectsIfMoved(other.getHitbox(), roundedProposedMovement)) {
                    getHostGame().handler.registerCollision(this, other);
                    if (!current.preventOverlap || !other.preventOverlap) {
                        continue;
                    }
                    // already overlapping
                    if (current.getHitbox().intersects(other.getHitbox())) {
                        if (newLocation.distanceFrom(other.getCenterForCollisionSliding()) > current.location.distanceFrom(other.getCenterForCollisionSliding())) {
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
     * Returns new movement based on impassable pathing zones
     * triggers onPathingLayerCollision
     * @param proposedMovement proposed movement
     * @return new movement, adjusted for pathing
     */
    public DCoordinate updateMovementBasedOnPathing (DCoordinate proposedMovement) {
        DCoordinate newLocation = proposedMovement.add(location.copy());
        if (getHostGame().pathingLayer != null) {
            if (!getHostGame().pathingLayer.getTypeAt(getPixelLocation()).name.equals(getHostGame().pathingLayer.getTypeAt(newLocation.toCoordinate()).name)) {
                this.onPathingLayerCollision(getHostGame().pathingLayer.getTypeAt(newLocation.toCoordinate()));
            }
            if (!isNewLocationClearForPathing(newLocation.toCoordinate()) && collisionSliding) {
                //pathing at new location is blocked. (speed multiplier < .01)
                //check directions to see which are blocked so we can possibly slide
                boolean xClear = isXClearForPathingAtNewLocation(newLocation.x - location.x);
                boolean yClear = isYClearForPathingAtNewLocation(newLocation.y - location.y);
                if (!xClear) {
                    newLocation.x = location.x;
                }
                if (!yClear) {
                    newLocation.y = location.y;
                }
            }
        }
        
        if (!isNewLocationClearForPathing(newLocation.toCoordinate())) {
            // cancel movement if pathing layer says destination is not pathable
            newLocation = location.copy();
        }
        
        return newLocation.subtract(location);
    }
    
    /**
     * checks if overlapping impassable terrain if moved on x axis
     * @param xModifier amount to add to location
     */
    private boolean isXClearForPathingAtNewLocation(double xModifier) {
       Coordinate newLocation = location.copy().add(xModifier, 0.0).toCoordinate();
       
       ArrayList<Coordinate> pointsToCheck = new ArrayList<>();
       pointsToCheck.add(newLocation);
       for(Coordinate offset : this.additionalPathingChecks) {
           pointsToCheck.add(newLocation.copy().add(offset));
       }
       for(Coordinate c : pointsToCheck) {
           if(pathingModifiers.get(hostGame.pathingLayer.getTypeAt(Math.max(c.x, 0), Math.max(c.y, 0))) < .01) {
               return false;
           }
       }
       return true;
    }
    
    /**
     * checks if overlapping impassable terrain if moved on y axis
     * @param yModifier amount to add to location
     */
    private boolean isYClearForPathingAtNewLocation(double yModifier) {
       Coordinate newLocation = location.copy().add(0.0, yModifier).toCoordinate();
       
       ArrayList<Coordinate> pointsToCheck = new ArrayList<>();
       pointsToCheck.add(newLocation);
       for(Coordinate offset : this.additionalPathingChecks) {
           pointsToCheck.add(newLocation.copy().add(offset));
       }
       for(Coordinate c : pointsToCheck) {
           if(pathingModifiers.get(hostGame.pathingLayer.getTypeAt(Math.max(c.x, 0), Math.max(c.y, 0))) < .01) {
               return false;
           }
       }
       return true;
    }
    
    /**
     * checks if overlapping impassable terrain if moved to new location
     * @param newLocation new Location
     * @return whether or not its clear
     */
    public boolean isNewLocationClearForPathing(Coordinate newLocation) {
        if(hostGame.getPathingLayer() == null) return true;
       
       ArrayList<Coordinate> pointsToCheck = new ArrayList<>();
       pointsToCheck.add(newLocation);
       for(Coordinate offset : this.additionalPathingChecks) {
           pointsToCheck.add(newLocation.copy().add(offset));
       }
       for(Coordinate c : pointsToCheck) {
           if(pathingModifiers.getOrDefault(hostGame.pathingLayer.getTypeAt(Math.max(c.x, 0), Math.max(c.y, 0)), 1.0) < .01) {
               onPathingLayerCollision(getHostGame().getPathingLayer().getTypeAt(c));
               return false;
           }
       }
       return true;
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
        if(Main.ignoreCollisionsForStillObjects && velocity.x == 0 && velocity.y == 0) return;
        
        //COLLISION
        DCoordinate oldLocation = location.copy();
        DCoordinate newLocation = location.copy();
        
        DCoordinate proposedMovement = this.getMovementNextTick();
        proposedMovement = updateMovementBasedOnCollision(proposedMovement);
        proposedMovement = updateMovementBasedOnPathing(proposedMovement);
        
        newLocation.add(proposedMovement);

        // pathing layer now
        
        location = newLocation;
        
        if(!location.equals(constrainToWorld(location))) {
            // triggering on collide after location has been updated in case the handler wants to change location based on collision
            onCollideWorldBorder(location.copy());
        }
        
        lastMovement = newLocation.copy().subtract(oldLocation);

        location = constrainToWorld(location);
    }

    
    /**
     * returns the movement the object undergo next tick assuming that 
     * it is moved only with its own velocity and it does not collide with anything
     * @return DCoordiante representation of the movement 
     */
    public DCoordinate getMovementNextTick() {
        DCoordinate newLocation = location.copy();
        switch (movementType) {
            case SpeedRatio:
                    // https://math.stackexchange.com/questions/175896/finding-a-point-along-a-line-a-certain-distance-away-from-another-point
                  DCoordinate start = location.copy();
                  DCoordinate rawEnd = location.copy().add(velocity);
                  double rawDistance = start.distanceFrom(rawEnd);
                  if(getSpeed() == 0 || rawDistance == 0) break;
                  double ratioOfDistance = getSpeed() / rawDistance;
                  newLocation.x = ((1.0 - ratioOfDistance) * start.x) + (ratioOfDistance * rawEnd.x);
                  newLocation.y = ((1.0 - ratioOfDistance) * start.y) + (ratioOfDistance * rawEnd.y);
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
            default:
                throw new RuntimeException("Movement Type undefined for object: " + this);
        }
        DCoordinate movement = new DCoordinate((newLocation.x - location.x), (newLocation.y - location.y));
        return movement;
    }
    /**
     * this method is triggered from the default constrainToWorld function when
     * it detects that it's x or y coordinates are outside playable bounds and needs to be brought back in
     * @param loc the out of bounds location that the object tried to collide with
     */
    public void onCollideWorldBorder(DCoordinate loc) {};
    
    /**
     * returns a copy of the input that if the input is out of bounds, will be updated to be in bounds
     * @param input input location
     * @return copy of input conditionally changed
     */
    public DCoordinate constrainToWorld(DCoordinate input){
        DCoordinate value = input.copy();
        if(value.x < hostGame.worldBorder) {
            value.x=hostGame.worldBorder;
        }
        if(value.y < hostGame.worldBorder) {
            value.y=hostGame.worldBorder;
        }
        if(value.x > hostGame.getWorldWidth() - hostGame.worldBorder) {
            value.x = hostGame.getWorldWidth()- hostGame.worldBorder;
        }
        if(value.y > hostGame.getWorldHeight() - hostGame.worldBorder){
            value.y = hostGame.getWorldHeight()- hostGame.worldBorder;
        }
        
        return value;
    }
    /**
     * Creates new GameObject2 at location
     * @param c location
     */
    public GameObject2(Coordinate c){
      init(new DCoordinate(c));
      ID = String.valueOf(IDLog++);
    }
     /**
     * Creates new GameObject2 at exact location
     * @param dc location
     */
    public GameObject2(DCoordinate dc){
        init(dc);
        ID = String.valueOf(IDLog++);
    }
     /**
     * Creates new GameObject2 at location
     * @param x location x-coordinate
     * @param y location y-coordinate
     */
    public GameObject2(int x, int y){
      init(new DCoordinate(x,y));
      ID = String.valueOf(IDLog++); 
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
            so.onHostDestroy(this);
            so.onDestroy();
        }
        if (!(this instanceof SubObject)) {
            getHostGame().removeObject(this);
        }else{
            SubObject me = (SubObject)this;
            if(me.getHost() != null) me.getHost().subObjects.remove(me);
            me.setHost(null);
            
        }
        this.detatchAllStickers();
    }

    /**
     * method that runs when this object is destroyed, to be used for gameplay
     * Note this does not handle technical engine removal of object from game
     */
    public void onDestroy(){
        
    }
    /**
     * runs whenever the current animation sequence renders the last frame in animation
     * if the animation is looping particularly fast, this may not trigger right away as the last frame could be too fast.
     */
    public void onAnimationCycle(){}
    
    /**
     * This is considered alive if it has a hostGame and that hostGame contians this object
     * @return weather or not this object is considered alive. 
     */
    public boolean isAlive(){
        return hostGame != null ? hostGame.getAllObjects().contains(this) : false;
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
        int longestSide = Math.max(getWidth(), getHeight());
        Rectangle sightBox = new Rectangle((int)(this.location.x-(longestSide/2)),(int)this.location.y-(longestSide/2),longestSide+1,longestSide+1);
        return sightBox.intersects(getHostGame().getCamera().getFieldOfView());
    }
    
    @Override
    public String toString(){
        return this.getName() + ID + " at " + location;
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

    public void setHostGame(Game g){
        hostGame = g;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DCoordinate getLocationAsOfLastTick() {
        return locationAsOfLastTick.copy();
    }
    
    /**
     * this method is used to determine the centerpoint of the object for collision when collision sliding is enabled.
     * ie if other objects want to move to a point intersecting with this object, it is allowed as long as their new position places them farther than this point.
     * @return DCoordinate the point
     */
    public DCoordinate getCenterForCollisionSliding() {
        return getLocationAsOfLastTick();
    }

    public void setLocationAsOfLastTick(DCoordinate locationAsOfLastTIck) {
        this.locationAsOfLastTick = locationAsOfLastTIck.copy();
    }
    
    public void setRotationAsOfLastTick(double r) {
        this.rotationAsOfLastTick = r;
    }
    
    public void setScaleAsOfLastTick(double d) {
        scaleAsOfLastTick = d;
    }
    
    /**
     * moves future synced state into synced state, then clears it
     */
    public void updateSyncedState() {
        for(String key : futureSyncedState.keySet()) {
            syncedState.put(key, futureSyncedState.get(key));
        }
        futureSyncedState.clear();
    }
    
    /**
     * sets a property to be tick-synced. this will not be accesable until next tick. or pretick
     * @param key name of property
     * @param value value to store
     */
    public void setSycnedProperty(String key, Object value) {
        futureSyncedState.put(key, value);
    }
    
    public Object getSycnedProperty(String key) {
        return syncedState.get(key);
    }

    public int getWidthAsOfLastTick() {
        return widthAsOfLastTick;
    }

    public void setWidthAsOfLastTick(int widthAsOfLastTick) {
        this.widthAsOfLastTick = widthAsOfLastTick;
    }

    public int getHeightAsOfLastTick() {
        return heightAsOfLastTick;
    }

    public void setHeightAsOfLastTick(int heightAsOfLastTick) {
        this.heightAsOfLastTick = heightAsOfLastTick;
    }
     
    /**
     * Executes the given function after a set number of ticks have passed.
     * This function will happen before the pretick on the given tick number
     * executes in the order submitted
     * @param tickDelay number of ticks to wait before executing the function
     * @param c function to execute. Game will be passed as the only parameter
     */
    public void addTickDelayedEffect(int tickDelay, Consumer c) {
        if(hostGame == null) {
            System.out.println("error trying to add tickDelayedEffect with null game");
            Window.currentGame.addTickDelayedEffect(new TickDelayedEffect(Window.currentGame.handler.globalTickNumber + tickDelay, c));
            return;
        }
        hostGame.addTickDelayedEffect(new TickDelayedEffect(hostGame.handler.globalTickNumber + tickDelay, c));
    }
    
    /**
     * Adds time triggered effect. Given function will run at the start of the first tick after given time in ms
     * get current time in ms using System.currentTimeMillis();
     * 
     * (System.currentTimeMillis() + 1000)  means trigger after 1 second delay
     * @param timeMs milli to trigger on
     * @param c function to execute
     */
    public synchronized void addTimeTriggeredEffect(long timeMs, Consumer c) {
        if(hostGame == null) {
            System.out.println("error trying to add timeTriggeredEffect with null game");
            Window.currentGame.addTimeTriggeredEffect(new TimeTriggeredEffect(timeMs, c));
            return;
        }
         getHostGame().addTimeTriggeredEffect(new TimeTriggeredEffect(timeMs, c));
    }

    public float getRenderOpacity() {
        return renderOpacity;
    }

    /**
     * 0-1 value for percent opaque to render the graphic
     * @param renderOpacity 0f-1f
     */
    public void setRenderOpacity(float renderOpacity) { 
        if(renderOpacity < 0) {
            this.renderOpacity = 0;
            return;
        }
        if(renderOpacity > 1) {
            this.renderOpacity = 1;
            return;
        }
        this.renderOpacity = renderOpacity;
    }
    
    public boolean hasVelocity() {
        return velocity.x < 0 || velocity.x > 0 || velocity.y < 0 || velocity.y > 0;
    }
    
    /**
     * returns getWidth or getHeight depending on which is larger
     * @return 
     */
    public int longestSideLength () {
        return Math.max(getWidth(), getHeight());
    }
    
    /**
     * whether or not the object is in the same location as last tick. Logic calculated during posttick.
     * @return value
     */
    public boolean movedLastTick() {
        return this.movedLastTick;
    }
}

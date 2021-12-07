/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

/**
 * Gameobjects that are attached to a regular gameobject at a given point offset.
 * Not stored in handler, but in host objects
 * TODO: work on subobject collision
 * @author joey
 */
public class SubObject extends GameObject2{
    private GameObject2 host = null;
    private  Coordinate offset = new Coordinate(0,0);
    private  Coordinate adjustedOffset = new Coordinate(0,0);
    
    
    
    /**
     * This is this offset adjsuted for host location. Relative to world. 
     * host-location plus adjustedOffset equals the location of this subobject
     * from any host rotation
     * @return adjusted offset
     */
    public Coordinate getAdjustedOffset(){
        return adjustedOffset;
    }
    
    /**
     * gets the GameObject2 host. If this subobject is a subobject of another 
     * subobject, go up the tree until you find the base object
     * @return 
     */
    public GameObject2 getRootHost(){
        if(getHost() instanceof SubObject){
            return ((SubObject)host).getRootHost();
        }else{
            return host;
        }
    }
    
    
    /**
     * gets the raw offset relative to the host object. this does not reflect
     * rotation adjustments and therefore host-location + offset does not equal
     * the location of this object in the world
     * @return offset relative to host, no rotation adjustment
     */
    public Coordinate getOffset(){
        return offset;
    }
    
    /**
     * sets the raw offset relative to the host object. this does not reflect
     * rotation adjustments and therefore host-location + offset does not equal
     * the location of this object in the world
     * @param c new offset relative to host, no rotation adjustment
     */
    public void setOffset(Coordinate c) {
            offset=c;
            updateAdjustedOffset();
    }
    
    /**
     * Creates a subobject with a given offset. Use setHost(GameObject2) method
     * to attach this to a gameobject
     * @param offset location modifier. subobject location will be host location + offset
     */
    public SubObject(Coordinate offset){
        super(new Coordinate(0,0));
        this.offset.add(offset);
        isSolid = false; //subobjects do not have collision by default
    }
    
    
    /**
     * sets the host object to carry this subobject.
     * note: removes this subobject from previous host if applicable
     * @param host GameObject attach to
     */
    public void setHost(GameObject2 host) {
        if(this.host!=null)this.host.getImmediateSubObjects().remove(this);
        this.host = host;
        if (host != null) {
            this.setHostGame(host.getHostGame());
            this.host.getImmediateSubObjects().add(this);
        }
        updateAdjustedOffset();
    }

    
    @Override
    public boolean isOnScreen(){
        return host.isOnScreen();
    }
    
    public Game getHostGame(){
        if(host==null){
            return null;
        }else{
            return host.getHostGame();
        }
    }
    
    public GameObject2 getHost(){
        return host;
    }
    
    /**
     * updating the location sets the location to host location + offset
     * subobjects do not use velocity by default
     */
    @Override
    public void updateLocation(){
        DCoordinate newLocation = host.location.copy();
        newLocation.add(adjustedOffset);
        updateHitbox();
        this.location = newLocation;
    }
    /**
     * Tick method on subobjects should alaways begin with super.tick();
     */
    @Override
    public void tick(){
        super.tick();
        if(!host.isAlive()){
            destroy();
        }
    }
    
    @Override
    public void onCollide(GameObject2 other, boolean fromMyTick){
        host.onCollide(other, fromMyTick);
    }
    
    /**
     * runs whenever the host rotates.
     * default: adjusts location to rotate with host
     * @param degree degree of rotation
     */
    public void onHostRotate(double degree){
        updateAdjustedOffset();
    }
    
    private void updateAdjustedOffset() {
        if(host==null)return;
        adjustedOffset = offset.copy();
        adjustedOffset.adjustForRotation(host.getRotation());
    }
    
}

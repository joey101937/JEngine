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
    public  Coordinate offset = new Coordinate(0,0);
    
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
        if(this.host!=null)this.host.subObjects.remove(this);
        this.host = host;
        this.hostGame=host.hostGame;
        if(host!=null)this.host.subObjects.add(this);
    }

    
    @Override
    public boolean isOnScreen(){
        return host.isOnScreen();
    }
    
    public Game getHostGame(){
        if(host==null){
            return null;
        }else{
            return host.hostGame;
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
        newLocation.add(offset);
        updateHitbox();
        this.location = newLocation;
    }
    /**
     * Tick method on subobjects should alaways begin with super.tick();
     */
    @Override
    public void tick(){
        super.tick();
        isAlive = host.isAlive; 
    }
    
    @Override
    public void onCollide(GameObject2 other){
        host.onCollide(other);
    }
}

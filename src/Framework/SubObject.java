/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

/**
 * Gameobjects that are attached to a regular gameobject.
 * Not stored in handler
 * TODO: work on subobject collision
 * @author joey
 */
public class SubObject extends GameObject2{
    private GameObject2 host = null;
    public  Coordinate offset = new Coordinate(0,0);
    
    /**
     * Creates a subobject with a given offset. Use setHost(GameObject2) method
     * to attach this to a gameobject
     * @param offset 
     */
    public SubObject(Coordinate offset){
        super(new Coordinate(0,0));
        this.offset.add(offset);
        isSolid = false; //subobjects do not have collision by default
    }
    
    /**
     * sets the host object to carry this subobject.
     * @param host GameObject attach to
     */
    public void setHost(GameObject2 host) {
        if(host!=null)host.subObjects.remove(this);
        this.host = host;
        if(host!=null)this.host.subObjects.add(this);
    }
    
    /**
     * updating the location sets the location to host location + offset
     * subobjects do not use velocity by default
     */
    @Override
    public void updateLocation(){
        DCoordinate newLocation = host.location.copy();
        newLocation.add(offset);
        this.location = newLocation;
        this.updateHitbox();
    }
}

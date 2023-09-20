/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.UtilityObjects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Window;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * This is a helper object used to move GameObject2s from one game to another.
 * Uses the shouldMakeDestinationGameActive method to determine if it should make the destination active.
 * extend this class and override that method to change the logic.
 * @author joey101937 <g.uydude@yahoo.com>
 */
public class Portal extends BlockObject {
    Thread helperThread; // use this to execute the transport so the host game can safely pause
    
    public Game destination;
    public Coordinate destinationPoint;
    
    private GameObject2 currentInteractingObject;
    
    public Portal(Coordinate c, Dimension dimension, Game destinationGame, Coordinate destinationPoint) {
        super(c, dimension.width, dimension.height);
        init(destinationGame, destinationPoint);
    }
    
    public Portal(DCoordinate c, Dimension dimension, Game destinationGame, Coordinate destinationPoint) {
        super(c, dimension.width, dimension.height);
        init(destinationGame, destinationPoint);
    }
    
    /**
     * this method determines if the portal should make the destination game active upon the object being teleported.
     * by default this returns whether or not the gameobject2 is the target of its host game's camera
     * @param go the game object to be teleported
     * @return whether or not the game should be made active
     */
    public boolean shouldMakeDestinationGameActive (GameObject2 go) {
        return go.getHostGame().getCamera().getTarget() == go;
    }
    
    private void init(Game g, Coordinate c) {
        this.destination = g;
        this.destinationPoint = c;
        this.setColor(Color.orange);
        this.preventOverlap = false;
        this.setName("Portal to " + g);
        this.isInvisible = true;
    }
    
    @Override
    public void renderDebugVisuals(Graphics2D g){
        Color originalColor = g.getColor();
        g.setColor(this.getColor());
        g.fillRect(getPixelLocation().x - getWidth()/2, getPixelLocation().y - getHeight()/2, getWidth(), getHeight());
        g.setColor(Color.red);
        g.drawString(getName(), (int) location.x - getWidth() / 2, (int) location.y - getHeight() / 2);      
        g.setColor(originalColor);
    }
    
    @Override
    public void onCollide(GameObject2 go2, boolean fromMyTick) {
        trigger(go2);
    }
    
    /**
     * actually conducts the transfer from current game to new game
     * @param go2 target of portal teleport
     */
    public synchronized void trigger(GameObject2 go2) {
        if(helperThread != null && helperThread.isAlive()) return;
        if(currentInteractingObject != null) return;
        helperThread = new Thread(new PortalHelper(go2));
        helperThread.start();
    }
    
    private class PortalHelper implements Runnable {
        public GameObject2 go;
        public PortalHelper(GameObject2 go) {
            this.go = go;
        }

        @Override
        public synchronized void run() {
            System.out.println("portal triggering");
            boolean shouldMakeDestinationActive = shouldMakeDestinationGameActive(go);
            Game prevGame = go.getHostGame();
            boolean originalVisibility = go.isInvisible;
            boolean originalIsTrackingTarget = prevGame.getCamera().isTrackingTarget();
            boolean isOriginalTarget = prevGame.getCamera().getTarget() == go;
            double originalSpeed = go.getBaseSpeed();
            go.setBaseSpeed(0);
            prevGame.removeObject(go);
            go.isInvisible = true;
            go.location = destinationPoint.toDCoordinate();
            destination.addObject(go);
            if(shouldMakeDestinationActive) {
                prevGame.getCamera().setIsTackingTarget(false);
                Window.setCurrentGame(destination);
                prevGame.getCamera().setIsTackingTarget(originalIsTrackingTarget);
                if(isOriginalTarget) destination.getCamera().setTarget(go); // if go was the camera target, set as target of destination's camera also
            }
            go.isInvisible = originalVisibility;
            go.setBaseSpeed(originalSpeed);
            currentInteractingObject = null;
            System.out.println("portal done");
        }
    }
    
}

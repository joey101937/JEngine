/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Manages aggregate lists of GameObjects
 * @author Joseph
 */
public class Handler {

    private volatile LinkedList<GameObject2> storage = new LinkedList<>();
    public Game hostGame;
    
    private List<GameObject2> toRender = storage;
    
    public Handler(Game g){
        hostGame=g;
    }
    /**
     * number of gameobjects this handler is resoincible for
     * @return number of gameobjects this handler oversees
     */
    public int size(){
        return storage.size();
    }
    
    public synchronized void addObject(GameObject2 o) {
        if (!storage.contains(o)) {
            storage.add(o);
            if(o.getHostGame()!=null && o.getHostGame()!=this.hostGame){
                o.getHostGame().removeObject(o);
            }
            o.setHostGame(hostGame);
        }
    }
    
    /**
     * safely removes object using iterator to prevent concurrent modification
     * @param toRemove  object to remove
     */
    public synchronized void removeObject(GameObject2 toRemove){
         ListIterator iterator = storage.listIterator();
          while(iterator.hasNext()){
            Object obj = iterator.next();
            if(toRemove==obj){
                iterator.remove();
                return;
            }
        }
    }
    
    /**
     * @return a list of all objects in the game, note changing this list does
     * NOT change the game state, however modifying items within it may. This 
     * should be used primarily when accessing items in game to minimize access to
     * the raw storage list and in turn reduce the liklihood of concurrent
     * modification exceptions
     */
    public synchronized ArrayList<GameObject2> getAllObjects(){
        ArrayList<GameObject2> output = new ArrayList<>();
         ListIterator iter = storage.listIterator();
         while(iter.hasNext()){
             GameObject2 obj = (GameObject2)iter.next();
             output.add(obj);
         }
        return output;
    }
    
    /**
     * renders all objects in the game, along with their subobjects
     * @param g should be the game's graphics
     */
    public synchronized void render(Graphics2D g) {
        for (GameObject2 go : toRender) {
            try{
             go.render(g);
                 for(SubObject so : go.getAllSubObjects()){
                     so.render(g);
                 }
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }
    }

    /**
     * ticks all objects in the game along with their subobjects
     */
    public synchronized void tick() {
        toRender = getAllObjects();
        toRender.sort(new renderSorter());
        for (GameObject2 go : getAllObjects()) {
            go.setHostGame(hostGame);
            go.tick();
            for (SubObject so : go.getAllSubObjects()) {
                so.tick();
            }

        }
    }

    /**
     * used to sort rendering based on zLayer
     */
    private static class renderSorter implements Comparator<GameObject2> {

        @Override
        public int compare(GameObject2 o1, GameObject2 o2) {
            if (o1.getZLayer() > o2.getZLayer()) {
                return 1;
            } else if (o1.getZLayer() < o2.getZLayer()) {
                return -1;
            } else {
                //must be equal
                return 0;
            }
        }

    }

}

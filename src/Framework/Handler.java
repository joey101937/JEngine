/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameObjects.GameObject2;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Manages aggregate lists of GameObjects
 * @author Joseph
 */
public class Handler {

    private volatile LinkedList<GameObject2> storage = new LinkedList<>();
   
    /**
     * number of gameobjects this handler is resoincible for
     * @return 
     */
    public int size(){
        return storage.size();
    }
    
    public synchronized void addObject(GameObject2 o){
      storage.add(o);
    }
    
    /**
     * safely removes object using iterator to prevent concurrent modification
     * @param toRemove 
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
     * renders all objects in the game
     * @param g 
     */
    public synchronized void render(Graphics2D g) {
        for (GameObject2 go : getAllObjects()) {
            try{
             go.render(g);   
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }
    }

    /**
     * ticks all objects in the game
     */
    public synchronized void tick() {
      for(GameObject2 go : getAllObjects()){
          go.tick();
      }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manages aggregate lists of GameObjects
 * @author Joseph
 */
public class Handler {
    private long globalTickNumber = 0;
    
    public ExecutorService tickService = Executors.newFixedThreadPool(Main.tickThreadCount);
    public ExecutorService collisionService = Executors.newFixedThreadPool(4);

    // private volatile LinkedList<GameObject2> storage = new LinkedList<>();
    private ConcurrentHashMap<Integer, GameObject2> storage = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, GameObject2> storageAsOfLastTick = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<CollisionEvent> collisionLedger = new CopyOnWriteArrayList<>();
    public Game hostGame;
    
    // private Map<Integer, GameObject2> toRenger = storage;
    private List<GameObject2> toRender = new LinkedList<GameObject2>();
    
    public Handler(Game g){
        hostGame=g;
    }
    /**
     * number of gameobjects this handler is resoincible for
     * @return number of gameobjects this handler oversees
     */
    public int size(){
        return storageAsOfLastTick.size();
    }
    
    /**
     * number of gameobjects this handler is resoincible for
     * @return number of gameobjects this handler oversees
     */
    public int sizeRealTime(){
        return storage.size();
    }
    
    public void registerCollision(GameObject2 a, GameObject2 b) {
        if(Main.tickType == TickType.unified) {
            a.onCollide(b, true);
            b.onCollide(a, false);
        } else {
          collisionLedger.add(new CollisionEvent(a, b));
        }
    }
    
    private void executeCollisions() {
        collisionLedger.sort(null);
        Collection<Future<?>> tasks = new LinkedList<Future<?>>();
        for(CollisionEvent event : collisionLedger) {
            if(event.a == null || event.b == null) return;
            tasks.add(collisionService.submit(event));
        }
        for(Future<?> f : tasks) {
            try {
                f.get();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        collisionLedger.clear();
    }
    
    /**
     * updates sycned values for determinism
     */
    private void populateStorageAsOfLastTick() {
        storageAsOfLastTick.clear();
        for(GameObject2 go : storage.values()) {
            go.setLocationAsOfLastTick(go.location);
            go.setRotationAsOfLastTick(go.getRotationRealTime());
            go.updateSyncedState();
            storageAsOfLastTick.put(go.ID, go);
            for(SubObject sub : go.getAllSubObjects()) {
                sub.setLocationAsOfLastTick(sub.location);
                sub.setRotationAsOfLastTick(sub.getRotationRealTime());
            }
        }
    }
    
    public void addObject(GameObject2 o) {
        if (!storage.containsKey(o.ID)) {
            storage.put(o.ID, o);
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
    public void removeObject(GameObject2 toRemove){
         storage.remove(toRemove.ID);
    }
    
    /**
     * @return a list of all objects in the game, note changing this list does
     * NOT change the game state, however modifying items within it may. This 
     * should be used primarily when accessing items in game to minimize access to
     * the raw storage list and in turn reduce the liklihood of concurrent
     * modification exceptions
     * 
     *  USE THIS TO MAKE GAME DETERMINSTIC
     */
    public ArrayList<GameObject2> getAllObjects(){
        ArrayList<GameObject2> output = new ArrayList<>();
        for(GameObject2 go : storageAsOfLastTick.values()) output.add(go);
        return output;
    }
    
     /**
     * @return a list of all objects in the game, note changing this list does
     * NOT change the game state, however modifying items within it may. This 
     * should be used primarily when accessing items in game to minimize access to
     * the raw storage list and in turn reduce the liklihood of concurrent
     * modification exceptions
     * 
     * MAY CAUSE RANDOMNESS IN MULTITHREADED GAMES
     */
    public ArrayList<GameObject2> getAllObjectsRealTime(){
        ArrayList<GameObject2> output = new ArrayList<>();
        for(GameObject2 go : storage.values()) output.add(go);
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
    public void tick() {
        globalTickNumber++;
        if(Main.tickType == TickType.unified) {
            tickUnified();
        } else if (Main.tickType == TickType.modular) {
            tickModular();
        } else if (Main.tickType == TickType.semiModular) {
            tickSemiModular();
        }
    }
    
    private void tickUnified() {
        populateStorageAsOfLastTick();
        toRender = getAllObjects();
        toRender.sort(new renderSorter());
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : getAllObjects()) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "unifiedTick")));
            } else {
              (new TickTask(go, "unifiedTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        tickTasks.clear();
    }
    
    private void tickModular() {
        toRender = getAllObjects();
        toRender.sort(new renderSorter());
        populateStorageAsOfLastTick();
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : getAllObjects()) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "preTick")));     
            } else {
                (new TickTask(go, "preTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        tickTasks.clear();
        // populateStorageAsOfLastTick();
//        System.out.println("TICK " + globalTickNumber + " LOCATIONS");
//        for(GameObject2 go : toRender) {
//            System.out.println(go.ID + " - " + go.getLocationAsOfLastTick() + " - r" + go.getRotation());
//        }
        for (GameObject2 go : toRender) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "tick")));
            } else {
              (new TickTask(go, "tick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        executeCollisions();
        tickTasks.clear();
        populateStorageAsOfLastTick();
        for (GameObject2 go : toRender) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "postTick")));
            } else {
               (new TickTask(go, "postTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
    }
    
    private void tickSemiModular() {
        toRender = getAllObjects();
        toRender.sort(new renderSorter());
        populateStorageAsOfLastTick();
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : getAllObjects()) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "semiModularTick")));     
            } else {
                (new TickTask(go, "semiModularTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        tickTasks.clear();
        executeCollisions();
        populateStorageAsOfLastTick();
        for (GameObject2 go : toRender) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "postTick")));
            } else {
               (new TickTask(go, "postTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
    }

    private void waitForAllJobs(Collection<Future<?>> a) {
        for (Future<?> currTask : a) {
            try {
                currTask.get();
            } catch (Exception e) {
                e.printStackTrace();
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
    
    private class TickTask implements Runnable {
        GameObject2 go;
        String type = "tick";
        
        public TickTask (GameObject2 go2, String type) {
            go = go2;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                go.setHostGame(hostGame);
                if (type.equals("preTick")) {
                    go.preTick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.preTick();
                    }
                } else if (type.equals("tick")) {
                    go.tick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.tick();
                    }
                } else if (type.equals("postTick")) {
                    go.postTick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.postTick();
                    }
                } else if (type.equals("unifiedTick")) {
                    go.preTick();
                    go.tick();
                    go.postTick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.preTick();
                        so.tick();
                        so.postTick();
                    }
                } else if (type.equals("semiModularTick")) {
                    go.preTick();
                    go.tick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.preTick();
                        so.tick();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    
    private static class CollisionEvent implements Comparable<CollisionEvent>, Runnable{
        public GameObject2 a;
        public GameObject2 b;
        public String name;

        public CollisionEvent(GameObject2 p1, GameObject2 p2){
                a = p1;
                b = p2;
                name = a.ID + " " + b.ID;
        }        

        @Override
        public int compareTo(CollisionEvent o) {
            return name.compareTo(o.name);
        }

        @Override
        public void run() {
            a.onCollide(b, true);
            b.onCollide(b, false);
        }
        
    }

    public static enum TickType{
        unified, // tick threads execute preTick - tick - postTick as a single instance. onCollide is triggered immediately. most performant but has thread randomness
        modular, // tick threads execute preTick - tick - postTick as separate events that happen separately in order. least performant but deterministic with high level of control
        semiModular // tick threads execute preTick and tick together and postTick indidually. Captures performance of unified with determinism of modular
    }
}

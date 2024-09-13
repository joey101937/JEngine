/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Manages aggregate lists of GameObjects
 * @author Joseph
 */
public class Handler {
    protected long globalTickNumber = 0;
    
    public ExecutorService tickService = Executors.newFixedThreadPool(Main.tickThreadCount);
    public ExecutorService renderService = Main.renderThreadCount > 0
                ? Executors.newFixedThreadPool(Main.renderThreadCount)
                :  Executors.newFixedThreadPool(1);
    public ExecutorService renderServiceCached = Executors.newCachedThreadPool();
    public ExecutorService syncService = Executors.newVirtualThreadPerTaskExecutor();
    
    Map<Integer, GameObject2> storage = Collections.synchronizedMap(new HashMap());
    Map<Integer, GameObject2> storageAsOfLastTick = Collections.synchronizedMap(new HashMap());
    public volatile Game hostGame;
    
    
    private List<GameObject2> toRender = new LinkedList<GameObject2>();
    private List<GameObject2> toTick = new LinkedList<GameObject2>();
    private List<GameObject2> toRemove = new LinkedList<GameObject2>();
    private List<GameObject2> toAdd = new LinkedList<GameObject2>();
    
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
            a.onCollide(b, true);
            b.onCollide(a, false);
    }
    
    /**
     * updates sycned values for determinism
     */
    private void populateStorageAsOfLastTick() {
        storageAsOfLastTick.clear();
        LinkedList<Future<?>> tasks = new LinkedList<>();
        for(GameObject2 go : storage.values()) {
              tasks.add(syncService.submit(new SyncTask(go)));
        }
        waitForAllJobs(tasks);
    }
    
    /**
     * adds object at the end of the tick
     * @param o 
     */
    public void addObject(GameObject2 o) {
        this.toAdd.add(o);
    }
    
    /**
     * Adds the gameobject to the world directly, and in the middle of the tick
     * this is bad for determinism
     */
    public void addObjectMidTick(GameObject2 go) {
        if(go.getHostGame()!=null && go.getHostGame()!=this.hostGame){
                go.getHostGame().removeObject(go);
            }
            storage.put(go.ID, go);
            go.setHostGame(hostGame);
            go.onGameEnter();
    }
    
    /**
     * safely removes object using iterator to prevent concurrent modification
     * @param toRemove  object to remove
     */
    public void removeObject(GameObject2 toRemove){
        this.toRemove.add(toRemove);
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
        if(Main.tickType != TickType.unified) output.sort(null);
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
        if(Main.tickType != TickType.unified) output.sort(null);
        return output;
    }
    
    
    /**
     * renders all objects in the game, along with their subobjects
     * @param g should be the game's graphics
     */
    public void render(Graphics2D g) {
        HashMap<Integer, LinkedList<GameObject2>> renderMap = new HashMap<>();
        for(GameObject2 go : toRender) {
            if(renderMap.get(go.getZLayer()) == null) {
                LinkedList<GameObject2> list = new LinkedList<>();
                list.add(go);
                renderMap.put(go.getZLayer(), list);
            } else {
                renderMap.get(go.getZLayer()).add(go);
            }
        }
        
        List<Integer> zLayers = renderMap.keySet().stream().sorted().collect(Collectors.toList());
        
        // all objects in a z layer can render together
        for(Integer zLayer: zLayers) {
            Collection<Future<?>> renderTasks = new LinkedList<>();
            for(GameObject2 go : renderMap.get(zLayer)) {
                renderTasks.add((Main.renderThreadCount > 0 ? renderService : renderServiceCached).submit(new RenderTask(go, g)));
            }
            waitForAllJobs(renderTasks);
        }
    }

    /**
     * ticks all objects in the game along with their subobjects
     */
    public void tick() {
        globalTickNumber++;
        LinkedList<TickDelayedEffect> effectsRun = new LinkedList<>();
        for(TickDelayedEffect tde : hostGame.tickDelayedEffects) {
            if(tde.targetTick == globalTickNumber) {
                tde.consumer.accept(hostGame);
                effectsRun.add(tde);
            }
        }
        hostGame.tickDelayedEffects.removeAll(effectsRun);
        if(Main.tickType == TickType.unified) {
            tickUnified();
        } else if (Main.tickType == TickType.modular) {
            tickModular();
        }
        conductRemoval();
        condunctAdditions();
    }
    
    private void tickUnified() {
        populateStorageAsOfLastTick();
        toRender = getAllObjects();
        toTick = getAllObjects();
        toTick.sort(null);
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : toTick) {
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
        toRender = getAllObjectsRealTime();
        toTick = getAllObjectsRealTime();
        toTick.sort(null);
        populateStorageAsOfLastTick();
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : toTick) {
            (new TickTask(go, "preTick")).run();
        }
        toTick = getAllObjectsRealTime();
        toTick.sort(null);
        populateStorageAsOfLastTick(); // remove this prob
        for (GameObject2 go : toTick) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "tick")));
            } else {
              (new TickTask(go, "tick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        tickTasks.clear();
        
        for (GameObject2 go : toTick) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(go, "postTick")));
            } else {
               (new TickTask(go, "postTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
    }

    public static void waitForAllJobs(Collection<Future<?>> a) {
        for (Future<?> currTask : a) {
            try {
                currTask.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private class RenderTask implements Runnable {
        public GameObject2 gameObejct;
        public Graphics2D graphics;
        
        public RenderTask (GameObject2 obj, Graphics2D g) {
            this.gameObejct = obj;
            this.graphics = g;
        }

        @Override
        public void run() {
          try{
            gameObejct.render((Graphics2D)graphics.create());
            for(SubObject so : gameObejct.getAllSubObjects()){
                so.render((Graphics2D)graphics.create());
            }
            }catch(Exception e){
                e.printStackTrace();
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
//                    System.out.println("pretick " + go);
                    go.preTick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.preTick();
                    }
                } else if (type.equals("tick")) {
                    System.out.println("tick " + go);
                    go.tick();
                    for (SubObject so : go.getAllSubObjects()) {
                        so.tick();
                    }
                } else if (type.equals("postTick")) {
//                    System.out.println("posttick " + go);
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
    
    private class SyncTask implements Runnable {
        public GameObject2 go;
        
        SyncTask (GameObject2 g) {
            go = g;
        }

        @Override
        public void run() {
            go.setLocationAsOfLastTick(go.location);
            go.setRotationAsOfLastTick(go.getRotationRealTime());
            go.setScaleAsOfLastTick(go.getScale());
            go.setWidthAsOfLastTick(go.getWidth());
            go.setHeightAsOfLastTick(go.getHeight());
            go.updateSyncedState();
            storageAsOfLastTick.put(go.ID, go);
            for (SubObject sub : go.getAllSubObjects()) {
                sub.setLocationAsOfLastTick(sub.location);
                sub.setRotationAsOfLastTick(sub.getRotationRealTime());
                sub.setScaleAsOfLastTick(sub.getScale());
                sub.setWidthAsOfLastTick(sub.getWidth());
                sub.setHeightAsOfLastTick(sub.getHeight());
                sub.updateSyncedState();
            }
        }
    }
    
    private void conductRemoval() {
        for(GameObject2 go : toRemove) {
            storage.remove(go.ID);
            go.setHostGame(null);
        }
        toRemove.clear();
    }
    
    private void condunctAdditions() {
        for(GameObject2 go : toAdd) {
            if(go.getHostGame()!=null && go.getHostGame()!=this.hostGame){
                go.getHostGame().removeObject(go);
            }
            storage.put(go.ID, go);
            go.setHostGame(hostGame);
            go.onGameEnter();
        }
        toAdd.clear();
    }

    public static enum TickType{
        unified, // tick threads in paralell execute in preTick - tick - postTick as a single instance. onCollide is triggered immediately. more performant but has thread randomness
        modular, // tick threads execute preTick synchronously- tick (async) - postTick(async) as separate events that happen separately in order. less performant but deterministic with high level of control
    }
}

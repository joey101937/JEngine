package Framework.CoreLoop;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.TickDelayedEffect;
import Framework.TimeTriggeredEffect;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author guydu
 */
public class Handler implements Serializable{

    public static ExecutorService newMinSizeCachedThreadPool(int minSize) {
        return new ThreadPoolExecutor(minSize, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    protected ExecutorService tickService = Executors.newFixedThreadPool(Main.tickThreadCount);
    protected ExecutorService renderService = Main.renderThreadCount > 0
            ? Executors.newFixedThreadPool(Main.renderThreadCount)
            : Executors.newFixedThreadPool(1);
    protected ExecutorService renderServiceCached = this.newMinSizeCachedThreadPool(6);
    protected ExecutorService syncService = Executors.newVirtualThreadPerTaskExecutor();

    public Game hostGame;
    private LinkedList<GameObject2> toAdd = new LinkedList<>();
    private LinkedList<GameObject2> toRemove = new LinkedList<>();
    private ArrayList<GameObject2> activeObjects = new ArrayList<>(); // LIVE REMEMBER TO SYNCHRONIZE
    private QuadTree quadTree;
    private ArrayList<TickDelayedEffect> tickDelayedEffects = new ArrayList<>(); // LIVE REMEMBER TO SYNCHRONIZE
    private ArrayList<TimeTriggeredEffect> timeTriggeredEffects = new ArrayList<>(); // LIVE REMEMBER TO SYNCHRONIZE

    public long globalTickNumber = 0L;

    public Snapshot currentSnapshot = new Snapshot(new ArrayList<GameObject2>(), quadTree, 0);

    public Handler(Game g) {
        hostGame = g;
        quadTree = new QuadTree(0, new Rectangle(0,0, 20000, 20000));
    }

    public int size() {
        return currentSnapshot.gameObjects.size();
    }

    public ArrayList<GameObject2> getAllObjects() {
        return new ArrayList<>(currentSnapshot.gameObjects);
    }

    /**
     * Gets all objects within a specified radius of a point
     * @param point The center point to search around
     * @param radius The radius to search within
     * @return List of objects within the specified radius
     */
    public ArrayList<GameObject2> getObjectsNearPoint(Coordinate point, int radius) {
        return currentSnapshot.quadTree.retrieve(point, radius);
    }

    /**
     * Gets all objects within a rectangular area
     * @param area The rectangular area to search within
     * @return List of objects within the specified area
     */
    public ArrayList<GameObject2> getObjectsInArea(Rectangle area) {
        return currentSnapshot.quadTree.retrieve(area);
    }

    public void render(Graphics2D g) {
        HashMap<Integer, LinkedList<Renderable>> renderMap = new HashMap<>();
        for (GameObject2 go : hostGame.getObjectsOnScreen(false)) {
            if (renderMap.get(go.getZLayer()) == null) {
                LinkedList<Renderable> list = new LinkedList<>();
                list.add(go);
                renderMap.put(go.getZLayer(), list);
            } else {
                renderMap.get(go.getZLayer()).add(go);
            }
        }
        for (IndependentEffect ie : hostGame.getIndependentEffects()) {
            if (renderMap.get(ie.getZLayer()) == null) {
                LinkedList<Renderable> list = new LinkedList<>();
                list.add(ie);
                renderMap.put(ie.getZLayer(), list);
            } else {
                renderMap.get(ie.getZLayer()).add(ie);
            }
        }
        // stickers here. todo make stickers have their own zlayer
        if (renderMap.get(Main.stickerZLayer) == null) {
            LinkedList<Renderable> list = new LinkedList<>();
            list.add(hostGame.visHandler);
            renderMap.put(Main.stickerZLayer, list);
        } else {
            renderMap.get(Main.stickerZLayer).add(hostGame.visHandler);
        }

        List<Integer> zLayers = renderMap.keySet().stream().sorted().collect(Collectors.toList());

        // all objects in a z layer can render together
        for (Integer zLayer : zLayers) {
            Collection<Future<?>> renderTasks = new LinkedList<>();
            for (Renderable go : renderMap.get(zLayer)) {
                renderTasks.add((Main.renderThreadCount > 0 ? renderService : renderServiceCached).submit(new RenderTask(go, g)));
            }
            waitForAllJobs(renderTasks);
        }
    }

    public synchronized void tick() {
        globalTickNumber++;
        long currentMillisecond = System.currentTimeMillis();
        LinkedList<TickDelayedEffect> tickDelatyedEffectsRun = new LinkedList<>();
        LinkedList<TimeTriggeredEffect> timeTriggeredEffectsRun = new LinkedList<>();
        // create a copy to iterate on
        LinkedList<TickDelayedEffect> currentDelayedEffects = new LinkedList(tickDelayedEffects);
        for (TickDelayedEffect tde : currentDelayedEffects) {
            if (tde.targetTick <= globalTickNumber) {
                tde.consumer.accept(hostGame);
                tickDelatyedEffectsRun.add(tde);
            }
        }
        
        LinkedList<TimeTriggeredEffect> currentTimeEvents = new LinkedList(timeTriggeredEffects);
        for (TimeTriggeredEffect tte : currentTimeEvents) {
            if (tte.targetMillisecond <= currentMillisecond) {
                tte.consumer.accept(hostGame);
                timeTriggeredEffectsRun.add(tte);
            }
        }
        
        updateTickDelayedEffects(tickDelatyedEffectsRun, true);
        updateTimeTriggredEffects(timeTriggeredEffectsRun, true);
        if (Main.tickType == Handler.TickType.unified) {
            tickUnified();
        } else if (Main.tickType == Handler.TickType.modular) {
            tickModular();
        }
        conductAdditionsAndRemovals();
        createSnapshot();
    }

    private void tickUnified() {
        ArrayList<GameObject2> toTick = new ArrayList<GameObject2>(activeObjects);
        toTick.sort(null);
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : toTick) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(hostGame, go, "unifiedTick")));
            } else {
                (new TickTask(hostGame, go, "unifiedTick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        tickTasks.clear();
    }

    private void tickModular() {
        ArrayList<GameObject2> toTick = new ArrayList<GameObject2>(activeObjects);
        toTick.sort(null);
        Collection<Future<?>> tickTasks = new LinkedList<>();
        for (GameObject2 go : toTick) {
            (new TickTask(hostGame, go, "preTick")).run();
        }

        for (GameObject2 go : toTick) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(hostGame, go, "tick")));
            } else {
                (new TickTask(hostGame, go, "tick")).run();
            }
        }
        waitForAllJobs(tickTasks);
        tickTasks.clear();

        for (GameObject2 go : toTick) {
            if (Main.tickThreadCount > 1) {
                tickTasks.add(tickService.submit(new TickTask(hostGame, go, "postTick")));
            } else {
                (new TickTask(hostGame, go, "postTick")).run();
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

    private void createSnapshot() {
        ArrayList<GameObject2> snapshotList = new ArrayList<GameObject2>(activeObjects);
        quadTree = new QuadTree(0, new Rectangle(0,0, hostGame.getWorldWidth(), hostGame.getWorldHeight()));
        int longestSideLength = 0;
        for(GameObject2 o : activeObjects) {
            quadTree.insert(o);
            if(o.longestSideLength() > longestSideLength) longestSideLength = o.longestSideLength();
        }
        currentSnapshot = new Snapshot(snapshotList, quadTree.copy(), globalTickNumber);
        currentSnapshot.largestSideLength = longestSideLength;

        ArrayList<Future<?>> tasks = new ArrayList<>();
        for (GameObject2 go : activeObjects) {
            tasks.add(syncService.submit(new SyncTask(go)));
        }
        waitForAllJobs(tasks);
    }

    public synchronized void addObject(GameObject2 go) {
        if (go.getHostGame() != null && go.getHostGame() != this.hostGame) {
            go.getHostGame().removeObject(go);
        }
        go.setHostGame(hostGame);
        go.onGameEnter();
        this.toAdd.add(go);
    }

    public synchronized void removeObject(GameObject2 toRemove) {
        this.toRemove.add(toRemove);
    }

    private synchronized void conductAdditionsAndRemovals() {
        conductRemovals();
        condunctAdditions();
    }

    private synchronized void conductRemovals() {
        for (GameObject2 go : toRemove) {
            activeObjects.remove(go);
            go.setHostGame(null);
        }
        toRemove.clear();
    }

    private synchronized void condunctAdditions() {
        for (GameObject2 go : toAdd) {
            activeObjects.add(go);
        }
        toAdd.clear();
    }

    public void registerCollision(GameObject2 a, GameObject2 b) {
        a.onCollide(b, true);
        b.onCollide(a, false);
    }

    public static enum TickType {
        unified, // tick threads in paralell execute in preTick - tick - postTick as a single instance. onCollide is triggered immediately. more performant but has thread randomness
        modular, // tick threads execute preTick synchronously- tick (async) - postTick(async) as separate events that happen separately in order. less performant but deterministic with high level of control
    }

    private synchronized void updateTickDelayedEffects(Collection<TickDelayedEffect> tdes, boolean forRemoval) {
        if (!forRemoval) {
            this.tickDelayedEffects.addAll(tdes);
        } else {
            this.tickDelayedEffects.removeAll(tdes);
        }
    }
    
    private synchronized void updateTimeTriggredEffects(Collection<TimeTriggeredEffect> ttes, boolean forRemoval) {
        if (!forRemoval) {
            this.timeTriggeredEffects.addAll(ttes);
        } else {
            this.timeTriggeredEffects.removeAll(ttes);
        }
    }

    public synchronized void addTickDelayedEffect(TickDelayedEffect tde) {
        var list = new LinkedList<TickDelayedEffect>();
        list.add(tde);
        updateTickDelayedEffects(list, false);
    }
    
    public synchronized void addTimeTriggeredEffect(TimeTriggeredEffect tte) {
        var list = new LinkedList<TimeTriggeredEffect>();
        list.add(tte);
        updateTimeTriggredEffects(list, false);
        System.out.println("adding time triggere event at " + tte.targetMillisecond);
    }
    
    public void setQuadTreeBounds (int width, int height) {
        //update quad tree root to have these bounds
    }
}

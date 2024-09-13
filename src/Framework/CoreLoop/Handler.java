/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework.CoreLoop;

import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.TickDelayedEffect;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 *
 * @author guydu
 */
public class Handler {

    protected ExecutorService tickService = Executors.newFixedThreadPool(Main.tickThreadCount);
    protected ExecutorService renderService = Main.renderThreadCount > 0
            ? Executors.newFixedThreadPool(Main.renderThreadCount)
            : Executors.newFixedThreadPool(1);
    protected ExecutorService renderServiceCached = Executors.newCachedThreadPool();
    protected ExecutorService syncService = Executors.newVirtualThreadPerTaskExecutor();
    
    public Game hostGame;
    private LinkedList<GameObject2> toAdd = new LinkedList<>();
    private LinkedList<GameObject2> toRemove = new LinkedList<>();
    private ArrayList<GameObject2> activeObjects = new ArrayList<>();

    public long globalTickNumber = 0L;

    public Snapshot currentSnapshot = new Snapshot(new ArrayList<GameObject2>(), 0);

    public Handler(Game g) {
        hostGame = g;
    }
    
    public int size() {
        return currentSnapshot.gameObjects.size();
    }

    public ArrayList<GameObject2> getAllObjects() {
        return new ArrayList<>(currentSnapshot.gameObjects);
    }

    public void render(Graphics2D g) {
        HashMap<Integer, LinkedList<GameObject2>> renderMap = new HashMap<>();
        for (GameObject2 go : activeObjects) {
            if (renderMap.get(go.getZLayer()) == null) {
                LinkedList<GameObject2> list = new LinkedList<>();
                list.add(go);
                renderMap.put(go.getZLayer(), list);
            } else {
                renderMap.get(go.getZLayer()).add(go);
            }
        }

        List<Integer> zLayers = renderMap.keySet().stream().sorted().collect(Collectors.toList());

        // all objects in a z layer can render together
        for (Integer zLayer : zLayers) {
            Collection<Future<?>> renderTasks = new LinkedList<>();
            for (GameObject2 go : renderMap.get(zLayer)) {
                renderTasks.add((Main.renderThreadCount > 0 ? renderService : renderServiceCached).submit(new RenderTask(go, g)));
            }
            waitForAllJobs(renderTasks);
        }
    }

    public void tick() {
        globalTickNumber++;
        LinkedList<TickDelayedEffect> effectsRun = new LinkedList<>();
        for (TickDelayedEffect tde : hostGame.tickDelayedEffects) {
            if (tde.targetTick == globalTickNumber) {
                tde.consumer.accept(hostGame);
                effectsRun.add(tde);
            }
        }
        hostGame.tickDelayedEffects.removeAll(effectsRun);
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
        currentSnapshot = new Snapshot(snapshotList, globalTickNumber);

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

}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework.CoreLoop;

import Framework.GameObject2;
import Framework.SubObject;

/**
 *
 * @author guydu
 */
public class SyncTask implements Runnable {

    public GameObject2 go;

    SyncTask(GameObject2 g) {
        go = g;
    }

    @Override
    public void run() {
        go.setLocationAsOfLastTick(go.getLocation());
        go.setRotationAsOfLastTick(go.getRotationRealTime());
        go.setScaleAsOfLastTick(go.getScale());
        go.setWidthAsOfLastTick(go.getWidth());
        go.setHeightAsOfLastTick(go.getHeight());
        go.updateSyncedState();
        for (SubObject sub : go.getAllSubObjects()) {
            sub.setLocationAsOfLastTick(sub.getLocation());
            sub.setRotationAsOfLastTick(sub.getRotationRealTime());
            sub.setScaleAsOfLastTick(sub.getScale());
            sub.setWidthAsOfLastTick(sub.getWidth());
            sub.setHeightAsOfLastTick(sub.getHeight());
            sub.updateSyncedState();
        }
    }
}

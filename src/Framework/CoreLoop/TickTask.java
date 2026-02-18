/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework.CoreLoop;

import Framework.Game;
import Framework.GameObject2;
import Framework.SubObject;

/**
 *
 * @author guydu
 */
public class TickTask implements Runnable {

    GameObject2 go;
    String type = "tick";
    Game hostGame;
    
    public TickTask(Game host, GameObject2 go2, String type) {
        go = go2;
        this.type = type;
        hostGame = host;
    }

    @Override
    public void run() {
        try {
            go.setHostGame(hostGame);
            if(type.equals("preTick")) {
//                    System.out.println("pretick " + go);
                go.preTick();
                for (SubObject so : go.getAllSubObjects()) {
                    so.preTick();
                }
            } else if (type.equals("tick")) {
//                    System.out.println("tick " + go);
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

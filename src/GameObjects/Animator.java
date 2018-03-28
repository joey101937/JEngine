/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects;

import Template.Game;

/**
 * this class when started, makes all the gameobjects play their animations
 * @author Joseph
 */
public class Animator implements Runnable{
    
    public static void start(){
    Thread t = new Thread(new Animator());    
    t.start();
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }
            for (GameObject go : Game.handler.storage) {
                go.toRender++;
            }
        }
    }

}

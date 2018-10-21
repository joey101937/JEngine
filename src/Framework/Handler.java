/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import GameObjects.GameObject2;
import java.awt.Graphics2D;
import java.util.LinkedList;

/**
 *
 * @author Joseph
 */
public class Handler {

    public volatile LinkedList<GameObject2> storage = new LinkedList<>();

    public void render(Graphics2D g) {
        for (GameObject2 go : storage) {
            try{
             go.render(g);   
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }
    }

    public void tick() {
        //below is just a fancy for each loop where 'go' is a GameObject2
        try {
            storage.stream().filter((go) -> !(go == null)).forEachOrdered((go) -> {
                try {
                    go.tick();
                } catch (Exception e) {
                    //catch for object ticks going wrong
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            //catch for tick process in general going wrong
            e.printStackTrace();
        }
    }

}

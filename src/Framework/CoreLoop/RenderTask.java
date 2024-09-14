/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Framework.CoreLoop;

import Framework.GameObject2;
import Framework.SubObject;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class RenderTask implements Runnable {

    public Renderable renderable;
    public Graphics2D graphics;

    public RenderTask(Renderable obj, Graphics2D g) {
        this.renderable = obj;
        this.graphics = g;
    }

    @Override
    public void run() {
        try {
            renderable.render((Graphics2D) graphics.create());
            if(renderable instanceof GameObject2 go) {
                for (SubObject so : go.getAllSubObjects()) {
                    so.render((Graphics2D) graphics.create());
                }   
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

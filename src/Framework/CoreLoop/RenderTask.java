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

    public GameObject2 gameObejct;
    public Graphics2D graphics;

    public RenderTask(GameObject2 obj, Graphics2D g) {
        this.gameObejct = obj;
        this.graphics = g;
    }

    @Override
    public void run() {
        try {
            gameObejct.render((Graphics2D) graphics.create());
            for (SubObject so : gameObejct.getAllSubObjects()) {
                so.render((Graphics2D) graphics.create());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Template;

import GameObjects.GameObject;
import java.awt.Graphics;
import static java.lang.Math.E;
import java.util.LinkedList;

/**
 *
 * @author Joseph
 */
public class Handler {

    public LinkedList<GameObject> storage = new LinkedList<>();

    public void render(Graphics g) {
        for (GameObject go : storage) {
            go.render(g);
        }
    }

    public void tick() {
        for (GameObject go : storage) {
            go.tick();
        }
    }

}

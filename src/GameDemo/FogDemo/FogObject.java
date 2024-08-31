/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.FogDemo;

import Framework.GameObject2;
import Framework.GraphicalAssets.Sequence;
import Framework.Main;
import Framework.SpriteManager;

/**
 *
 * @author guydu
 */
public class FogObject extends GameObject2 {
    public final static Sequence fogSequence;
    
    static {
        fogSequence = new Sequence(SpriteManager.fogSequence);
        fogSequence.setFrameDelay(160);
    }
    
    public FogObject(int x, int y) {
        super(x, y);
        init();
    }
    
    private void init() {
        Sequence s = fogSequence.copy();
        s.setFrameDelay(60 + (int)(Main.getRandomSource().nextDouble() * 80));
        s.advanceMs( Main.getRandomSource().nextInt(0, 2000));
        this.setGraphic(fogSequence);
        this.setRotation(Main.getRandomSource().nextDouble() * 360);
        this.setZLayer(2);
    }
    
}

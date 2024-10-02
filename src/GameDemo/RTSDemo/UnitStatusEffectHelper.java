/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo;

import Framework.GameObject2;
import Framework.IndependentEffect;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class UnitStatusEffectHelper extends IndependentEffect{
    private static final int statusIconWidth = 20;
    private static final int statusIconHeight = 20;
    
    @Override
    public int getZLayer() {
        return 2;
    }

    @Override
    public void render(Graphics2D g) {
        for(GameObject2 go : RTSGame.game.getAllObjects()) {
            if(go.isOnScreen() && go instanceof RTSUnit u) {
                if(u.isImmobilized) {  
                    g.drawImage(
                            RTSAssetManager.immobilizationIcon,
                            u.getPixelLocation().x - (u.getWidth() / 2),
                            u.getPixelLocation().y - (u.getHeight() / 2) - 20,
                            statusIconWidth,
                            statusIconHeight,
                            null
                    );
                }
            }
        }
    }

    @Override
    public void tick() {
        
    }
    
}

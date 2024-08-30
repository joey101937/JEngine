/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleGames.SideScroller;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author joey101937
 */
public class MinotaurHealthBarEffect extends IndependentEffect{

    public Game hostGame;
    
    public int baseWidth = 300;
    public int height = 50;
    
    public MinotaurHealthBarEffect(Game g) {
        hostGame = g;
    }
    

    @Override
    public void render(Graphics2D g) {
        Coordinate topCenterOfScreen = hostGame.getCamera().getCenterPoint();
        topCenterOfScreen.y = 0;
        g.setColor(Color.green);
        g.fillRect(topCenterOfScreen.x - baseWidth/2, topCenterOfScreen.y, baseWidth, height);
        g.setColor(Color.red);
        g.fillRect(
                topCenterOfScreen.x - baseWidth/2,
                topCenterOfScreen.y,
                (int)(baseWidth * MinotaurGame.playerMinotaur.getPercentHealthMissing()),
                height
        );
    }

    @Override
    public void tick() {
      
    }
    
}

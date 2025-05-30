/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.IndependentEffect;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 *
 * @author guydu
 */
public class TooltipHelper extends IndependentEffect{
    public Font headerFont = new Font("timesRoman", Font.BOLD, 16);
    public Font bodyFont = new Font("timesRoman", Font.PLAIN, 14);
    
    public Color transparentGray = new Color(.5f,.5f,.5f, .8f);

    public int width = 450;
    public int height = 100;
    public InfoPanelEffect infoPanelEffect = RTSGame.infoPanelEffect;

    public Coordinate location = new Coordinate(infoPanelEffect.baseX + infoPanelEffect.width - width, infoPanelEffect.baseY - height - 10);
    
    public TooltipHelper () {
        super();
        
    }
    
    @Override
    public void render(Graphics2D g) {
        double scaleAmount = 1/RTSGame.game.getZoom();
        g.scale(scaleAmount, scaleAmount);
        CommandButton cb = infoPanelEffect.hoveredButton;
        if(cb != null) {
            Coordinate toRender = new Coordinate(location).add(RTSGame.game.getCamera().getWorldRenderLocation().scale(1/scaleAmount));
            g.setColor(transparentGray);
            g.fillRect(toRender.x, toRender.y, width, height);
            g.setColor(Color.WHITE);
            g.setFont(headerFont);
            g.drawString(cb.name, toRender.x + 8, toRender.y + 24);
            g.setFont(bodyFont);
            int runningY = toRender.y + 16 + 32;
            for(String line : cb.tooltipLines) {
                g.drawString(line, toRender.x + 8, runningY);
                runningY += 16;
            }
        }
        g.scale(1/scaleAmount, 1/scaleAmount);
    }

    @Override
    public void tick() {
        
    }
    
    @Override
    public int getZLayer(){
        return 999;
    }
}

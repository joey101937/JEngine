/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo;

import Framework.DCoordinate;
import Framework.Game;
import Framework.UI_Elements.Examples.Minimap;
import Framework.UI_Elements.Examples.Minimap.MinimapMouseListener;
import java.awt.event.MouseEvent;

/**
 *
 * @author guydu
 */
public class MinimapListener extends MinimapMouseListener {

    public MinimapListener(Game hostGame, Minimap m) {
        super(hostGame, m);
    }

    private boolean dragging = false;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == 3) {
            System.out.println("test");
            SelectionBoxEffect.selectedUnits.forEach(unit -> {
                 DCoordinate relativePoint = new DCoordinate(0, 0);
                relativePoint.x = (double) e.getX() / (double) map.getWidth();
                relativePoint.x *= hostGame.getWorldWidth();
                relativePoint.y = (double) e.getY() / (double) map.getHeight();
                relativePoint.y *= hostGame.getWorldHeight();
                unit.setDesiredLocation(relativePoint.toCoordinate());
            });
        } else if (e.getButton() == 1) {
            panTo(e);
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == 1) {
            dragging = false;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            panTo(e);
        }
    }

}

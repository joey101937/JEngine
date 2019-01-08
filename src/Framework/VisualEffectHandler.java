/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.Stickers.Sticker;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handler to control all stickers and non-GameObject visual effects in a game
 * @author Joseph
 */
public class VisualEffectHandler {
    public CopyOnWriteArrayList<Sticker> stickers = new CopyOnWriteArrayList<>();
    public LinkedList<Coordinate[]> lines = new LinkedList<>();
    
    /**
     * renders all visual effects to canvas
     * @param g Graphics2D object to use
     */
    public synchronized void render(Graphics2D g){
        renderStickers(g);
        renderLines(g);
    }
    /**
     * renders all stickers to canvas
     * @param g Graphics2D object to use
     */
    private void renderStickers(Graphics2D g){
         try {
            if (stickers == null) {
                resetStickers();
            }
            for (Sticker s : stickers) {
                if (s == null) {
                    stickers.remove(s);
                    break;
                }
                if (s.disabled) {
                    continue;
                }
                s.render(g);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            resetStickers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * renders all lines to canvas
     * @param g Graphics2D object to use
     */
    private void renderLines(Graphics2D g){
        for(Coordinate[] line: lines){
            try{
            if(line != null && line.length==2){
                 g.drawLine(line[0].x, line[0].y, line[1].x, line[1].y);
            }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    /**
     * add line to world (for debug, lines are perminent)
     * @param start beginning location of line
     * @param end  ending location of line
     */
    public void addLine(Coordinate start, Coordinate end){
        lines.add(new Coordinate[]{start,end});
    }
    /**
     * Resets the linked list that holds all the stickers, run if things go wrong
     * Note all current stickers are removed
     */
    public void resetStickers(){
        System.out.println("reset sticker");
        stickers = new CopyOnWriteArrayList<>();
    }
}
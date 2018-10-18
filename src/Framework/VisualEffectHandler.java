/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.Stickers.Sticker;
import java.awt.Graphics2D;
import java.util.LinkedList;

/**
 * @author Joseph
 */
public class VisualEffectHandler {
    public LinkedList<Sticker> stickers = new LinkedList<>();
    public LinkedList<Coordinate[]> lines = new LinkedList<>();
    
    public synchronized void render(Graphics2D g){
        renderStickers(g);
        renderLines(g);
    }
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
    public void addLine(Coordinate start, Coordinate end){
        lines.add(new Coordinate[]{start,end});
    }
    
    public void resetStickers(){
        System.out.println("reset sticker");
        stickers = new LinkedList<>();
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import Framework.Stickers.Sticker;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.scene.canvas.GraphicsContext;

/**
 * Handler to control all stickers and non-GameObject visual effects in a game
 * @author Joseph
 */
public class VisualEffectHandler {
    public CopyOnWriteArrayList<Sticker> stickers = new CopyOnWriteArrayList<>();
    public LinkedList<Coordinate[]> lines = new LinkedList<>();
    
    public ExecutorService stickerService = Executors.newCachedThreadPool();
    /**
     * renders all visual effects to canvas
     * @param g GraphicsContext object to use
     */
    public void render(GraphicsContext g){
        renderStickers(g);
        renderLines(g);
    }
    /**
     * renders all stickers to canvas
     * @param g GraphicsContext object to use
     */
    private void renderStickers(GraphicsContext g){
         try {
            if (stickers == null) {
                resetStickers();
            }
            Collection<Future<?>> stickerTasks = new LinkedList<>();
            for (Sticker s : stickers) {
                 stickerTasks.add(stickerService.submit(new StickerTask(s, g, stickers)));
             }
             for (Future<?> currTask : stickerTasks) {
                 try {
                     currTask.get();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
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
     * @param g GraphicsContext object to use
     */
    private void renderLines(GraphicsContext g){
        for(Coordinate[] line: lines){
            try{
            if(line != null && line.length==2){
                 g.strokeLine(line[0].x, line[0].y, line[1].x, line[1].y);
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
    
    private static class StickerTask implements Runnable{
        public Sticker s;
        public GraphicsContext g;
        public CopyOnWriteArrayList<Sticker> stkrs;
        
        public StickerTask (Sticker st, GraphicsContext g2d, CopyOnWriteArrayList<Sticker> stickers) {
            s = st;
            g = g2d;
        }

        @Override
        public void run() {
            if (s == null) {
                stkrs.remove(s);
                return;
            }
            if (s.disabled) {
                stkrs.remove(s);
            }
            if (System.currentTimeMillis() > s.creationTime + s.timeToRender) {
                s.disable();
            }
            s.render(g);
        }

    }
}
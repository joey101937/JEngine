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
    
    public synchronized void render(Graphics2D g){
        try{
            if(stickers==null){
                resetStickers();
            }
        for(Sticker s : stickers){
            if(s==null){
                stickers.remove(s);
                break;
            }
            if(s.disabled)continue;
            s.render(g);
        }
        }catch(NullPointerException npe){
        npe.printStackTrace();
        resetStickers();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void resetStickers(){
        System.out.println("reset sticker");
        stickers = new LinkedList<>();
    }
}
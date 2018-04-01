/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects.Blocks;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 *
 * @author Joseph
 */
public class BlockCore extends Block{
    
    
    public ArrayList<Block> components = new ArrayList<>();
    
    public BlockCore(int x, int y) {
        super(x, y);
        core = this;
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(x-width/2, y-height/2, width, height);
        if(attachments.get(Direction.Top)!=null)attachments.get(Direction.Top).render(g);
        if(attachments.get(Direction.Bottom)!=null)attachments.get(Direction.Bottom).render(g);
        if(attachments.get(Direction.Left)!=null)attachments.get(Direction.Left).render(g);
       if(attachments.get(Direction.Right)!=null) attachments.get(Direction.Right).render(g);
        
        //for (Block b : components) {
         ///   b.render(g);
       // }
        
    }
    
    @Override
    public void tick(){
    super.tick();
        for (Block b : components) {
            b.tick();
        }
    }

}

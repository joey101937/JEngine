/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects.Blocks;

import GameObjects.GameObject;
import Template.Game;
import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Joseph
 */
public class Block extends GameObject{
    /*    FIELDS      */
    public int health;
    public Map<Direction,Block> attachments = new HashMap<>();
    public BlockCore core = null;
    
    public void setCore(BlockCore c){
        if(core!=null)core.components.remove(this);
        this.core = c;
        if(core!=null)c.components.add(this);
    }
    
    public Block(int x, int y) {
        super(x, y);
        this.width = 50;
        this.height = 50;
        if(core == null)Game.handler.storage.add(this);

    }

    @Override
    public void render(Graphics g){
        g.setColor(Color.gray);
        g.fillRect(x-width/2, y-height/2, width, height);
    }
    @Override
    public void collide(GameObject go) {
        //todo
    }
    
    public void destroy(){
        //todo
        core.components.remove(this);
    }
    
    /**
     * creates a block atached to a given block on the given side
     * @param parent
     * @param direction 
     */
    public Block(Block parent, Direction direction){
        super(parent.x,parent.y);
        this.height = 50;
        this.width = 50;
        core = parent.core;
        switch(direction){
            case Top:
                y-=parent.height;
                break;
            case Bottom:
                y+=parent.height;
                break;
            case Left:
                x-=parent.width;
                break;
            case Right:
                x+=parent.width;
                break;
        }
        parent.attachments.put(direction, this);
        if(core!=null)this.speed = core.speed;
        if(core == null)Game.handler.storage.add(this);
        else core.components.add(this);
    }
    /**
     * connects given block to this structure on our given side
     * @param b 
     */
    public void connect(Block b, Direction direction){
        b.x = x;
        b.y = y;
        switch (direction) {
            case Top:
                b.y-=height;
                break;
            case Bottom:
               b.y+=height;
                break;
            case Left:
               b.x-=width;
                break;
            case Right:
                 b.x+=width;
                break;
        }
        this.attachments.put(direction, b);
        b.core = core;
        if(core!=null)b.speed = core.speed;
    }
     

}

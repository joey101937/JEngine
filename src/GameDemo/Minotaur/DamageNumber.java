/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.Minotaur;

import Framework.Coordinate;
import Framework.UtilityObjects.TextObject;
import java.awt.Color;

/**
 *
 * @author Joseph
 */
public class DamageNumber extends TextObject{
    public int lifeTime = 60 * 4; // 1 sec at 240fps
    
    public DamageNumber(int amount, Coordinate loc){
        super(loc,String.valueOf(amount));
        this.velocity.y= -1;
        this.baseSpeed= baseSpeed/4;
        this.setColor(Color.red);
        this.isSolid=false;
        this.setZLayer(2); //render on top of other objects
    }
    
    @Override
    public void tick(){
        super.tick();
        lifeTime--;
        if(lifeTime<0){
            destroy();
        }
    }
}

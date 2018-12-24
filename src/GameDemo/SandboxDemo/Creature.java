/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.SandboxDemo;

import Framework.GameObject2;
import Framework.Coordinate;
import Framework.DCoordinate;

/**
 * example setup of using GameObject2s to make damagable units with health
 * @author joey
 */
public abstract class Creature extends GameObject2 {
    public int currentHealth = 100;
    public int maxHealth = 100;
    public boolean invuln = false;
    
    public void takeDamage(int amount){
        if(invuln)return;
        currentHealth-=amount;
        if(currentHealth<=0){
            destroy();
        }
    }
    
    public void heal(int amount){
        currentHealth+=amount;
        if(currentHealth>maxHealth){
            currentHealth=maxHealth;
        }
    }
    
    public Creature(Coordinate c) {
        super(c);
    }
    public Creature(DCoordinate c){
        super(c);
    }
    
}

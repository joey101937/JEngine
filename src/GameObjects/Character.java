/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects;

import Framework.Coordinate;
import Framework.DCoordinate;

/**
 * Example of a game character using GO2 setup
 * @author Joseph
 */
public class Character extends GameObject2{
    
    public Character(DCoordinate c) {
        super(c);
    }
    
    public Character(Coordinate c){
        super(c);
    }
    
    /**
     * set up basics of character
     */
    private void characterSetup(){
        
    }
    
}

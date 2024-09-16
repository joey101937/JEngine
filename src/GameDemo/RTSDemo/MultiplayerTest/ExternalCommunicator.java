/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Coordinate;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.RTSUnit;

/**
 *
 * @author guydu
 */
public class ExternalCommunicator implements Runnable {

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public static void main(String[] args) {
        
    }
    
    public static void interperateMessage(String s) {
        if(s == null) return;
        if(s.startsWith("randomSeed:")) {
            Main.setRandomSeed(Long.parseLong(s.substring("randomSeed:".length())));
            System.out.println("setting random seed to " + Long.parseLong(s.substring("randomSeed:".length())));
            return;
        }
        // unit movement
        // example: m:1,100,200
        // moves unit with id 1 to coordinate 100,200
        if(s.startsWith("m:")) {
            var components = s.substring(2).split(",");
            int id = Integer.parseInt(components[0]);
            int x = Integer.parseInt(components[1]);
            int y = Integer.parseInt(components[2]);
            Coordinate coord = new Coordinate(x,y);
            System.out.println("issuing order to unit " + id + " to move to " + coord);
            GameObject2 go = Window.currentGame.getObjectById(id);
            if(go instanceof RTSUnit unit) {
                unit.setDesiredLocation(coord);
                System.out.println("done");
            }
        }
    }

    
}

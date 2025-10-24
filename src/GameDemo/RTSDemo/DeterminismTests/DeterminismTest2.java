/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.DeterminismTests;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import GameDemo.RTSDemo.RTSAssetManager;
import GameDemo.RTSDemo.Units.TankUnit;

/**
 * these determinism tests are one off battles intended to be run across multiple sessions so that the results can be compared
 * @author guydu
 */
public class DeterminismTest2 {
    public static void main(String[] args) {
        RTSAssetManager.initialize();
        Game game = new Game(RTSAssetManager.grassBG);
        Main.setRandomSeed(10);
        TankUnit green = new TankUnit(100, 100, 0);
        TankUnit red = new TankUnit(500, 100, 1);
        Coordinate meetingPoint = new Coordinate(100, 250);
        green.setDesiredLocation(meetingPoint);
        red.setDesiredLocation(meetingPoint);
        game.addObject(green);
        game.addObject(red);
        System.out.println("starting");
        for(int i = 0; i < 100; i++){
            game.tick();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameDemo.TownDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.SpriteManager;
import Framework.UtilityObjects.Portal;
import Framework.UtilityObjects.TextObject;
import Framework.Window;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 *
 * @author guydu
 */
public class TownDemo {
    
    public static Game outside, inside1, inside2;
    
    public static Portal portalToInside2;
    
    public static TownCharacter playerCharacter;
    
    public static TownBird townBird;
    
    public static Font gameFont = new Font("TimesRoman", Font.BOLD, 12);
    
    public static void main(String[] args) {
        Main.ticksPerSecond = 2;
        Main.enableLerping = true;
        Main.lerpType = "predictive";
        SpriteManager.initialize();
        playerCharacter = new TownCharacter(200, 200);
        townBird = new TownBird(450, 500);
        outside = new Game(SpriteManager.townOutside);
        outside.setPathingLayer(SpriteManager.townOutsidePathing);
        outside.setZoom(2);
        outside.setName("outside");
        
        inside1 = new Game(SpriteManager.buildingInterior);
        inside1.setPathingLayer(SpriteManager.buildingInteriorPathing);
        inside1.setName("inside 1");
        inside1.setZoom(2);
        
        inside2 = new Game(SpriteManager.buildingInterior);
        inside2.setPathingLayer(SpriteManager.buildingInteriorPathing);
        inside2.setName("inside 2");
        inside2.setZoom(2);
        
        setupPortals();
        
        outside.addObject(playerCharacter);
        outside.addObject(townBird);
        
        outside.addObject(new TextObject(new Coordinate(450, 550), "Press B to make the bird fly up")
                .setColor(Color.white)
                .setFont(gameFont));
        
        outside.getCamera().setTarget(playerCharacter);
        Window.initialize(outside);
        outside.setInputHandler(new TownInput());
        inside1.setInputHandler(new TownInput());
        inside2.setInputHandler(new TownInput());
    }
    
    
    private static void setupPortals() {      
        outside.addObject(new TextObject(new Coordinate(350, 240), "Walk into the door to trigger this one")
               .setColor(Color.white)
               .setFont(gameFont));
        
        outside.addObject(new Portal(
           new Coordinate(450, 320), // portal location
           new Dimension(30, 20), // portal size
           inside1, // destination game
           new Coordinate(270, 335) //  destination point
        ));
        
        portalToInside2 = new Portal(
           new Coordinate(633, 290), // portal location
           new Dimension(20, 20), // portal size
           inside2, // destination game
           new Coordinate(270, 325) //  destination point
        );
        outside.addObject(portalToInside2);
        
        outside.addObject(new TextObject(new Coordinate(580, 240), "Press E nearby to trigger this one")
                .setColor(Color.white)
                .setFont(gameFont));
        
        inside1.addObject(new Portal(
           new Coordinate(270, 372), // portal location
           new Dimension(50, 30), // portal size
           outside, // destination game
           new Coordinate(440,355) //  destination point
        ));
        
        inside2.addObject(new Portal(
           new Coordinate(270, 372), // portal location
           new Dimension(50, 30), // portal size
           outside, // destination game
           new Coordinate(633,355) //  destination point
        ));
    }
        
    
}

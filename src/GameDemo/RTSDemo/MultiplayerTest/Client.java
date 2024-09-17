/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.Hellicopter;
import GameDemo.RTSDemo.Units.TankUnit;
import java.io.PrintStream;

/**
 *
 * @author guydu
 */
public class Client {
    
    public static PrintStream printStream;

    public static void main(String[] args) {
        Client c = new Client();
        Game g = new Game(SpriteManager.grassBG);
        ExternalCommunicator.initialize(false);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        g.addObject(new Hellicopter(200, 200, 0));
        g.addObject(new Hellicopter(300, 200, 0));
        g.addObject(new Hellicopter(400, 200, 0));
        g.addObject(new Hellicopter(200, 1000, 1));
        g.addObject(new Hellicopter(300, 1000, 1));
        g.addObject(new Hellicopter(400, 1000, 1));
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }

   
}

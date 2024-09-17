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
        Game g = RTSGame.game;
        ExternalCommunicator.initialize(false);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        int spacer = 160;
        for (int i = 0; i < 20; i++) {
            g.addObject(new Hellicopter(200 + (i * spacer), 200, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(350 + (i * spacer), 200, 0));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new Hellicopter(200 + (i * spacer), 1000, 1));
        }
        for (int i = 0; i < 20; i++) {
            g.addObject(new TankUnit(200 + (i * spacer), 850, 1));
        }
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }

}

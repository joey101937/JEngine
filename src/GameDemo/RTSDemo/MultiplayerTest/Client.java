/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;
import static GameDemo.RTSDemo.MultiplayerTest.Server.createStartingUnits;
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
        createStartingUnits(g);
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
    }

}

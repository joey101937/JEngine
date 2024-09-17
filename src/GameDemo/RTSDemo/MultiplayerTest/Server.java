/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Game;
import Framework.SpriteManager;
import Framework.Window;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.Units.TankUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author guydu
 */
public class Server implements Runnable {

    public static void main(String[] args) {
        Server server = new Server();
        Game g = new Game(SpriteManager.grassBG);
        System.out.println("adding");
        g.addObject(new TankUnit(200, 200, 0));
        ExternalCommunicator.initialize(true);
        g.setHandleSyncTick(ExternalCommunicator.handleSyncTick);
        Window.initialize(g);
        RTSGame.setup(g);
        RTSGame.game = g;
        Thread.ofVirtual().start(server);
        // server.run();
    }

    @Override
    public void run() {
        try {
            ServerSocket servSocket = new ServerSocket(444);
            Socket socket = servSocket.accept();
            InputStreamReader ir = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            while (true) {
                String fromClient = br.readLine();
                if (fromClient != null) {
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    System.out.println("From client: " + fromClient);
                    ps.println("message received");
                    ps.flush();
                    ExternalCommunicator.interperateMessage(fromClient);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}

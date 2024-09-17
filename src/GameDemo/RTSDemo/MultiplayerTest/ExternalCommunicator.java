/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.MultiplayerTest;

import Framework.Coordinate;
import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import static GameDemo.RTSDemo.MultiplayerTest.Client.printStream;
import GameDemo.RTSDemo.RTSUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

/**
 *
 * @author guydu
 */
public class ExternalCommunicator implements Runnable {

    public static boolean isMultiplayer = true;

    public static ServerSocket servSocket;
    public static Socket socket;
    public static InputStreamReader inputReader;
    public static BufferedReader bufferdReader;
    public static long partnerTick = 0;
    public static Thread listenerThread;

    public static void initialize(boolean isServer) {

        try {
            if (isServer) {
                servSocket = new ServerSocket(444);
                // blocks until connection
                socket = servSocket.accept();
                printStream = new PrintStream(socket.getOutputStream()); //output stream is what we are sending
                inputReader = new InputStreamReader(socket.getInputStream());
                bufferdReader = new BufferedReader(inputReader);
                long seed = (long) (Math.random() * 999999999);
                Main.setRandomSeed(seed);
                Client.sendMessage("randomSeed:" + seed);
                System.out.println("setting random seed" + seed);
            } else {
                socket = new Socket("localhost", 444);
                printStream = new PrintStream(socket.getOutputStream()); //output stream is what we are sending
                InputStreamReader ir = new InputStreamReader(socket.getInputStream());
                bufferdReader = new BufferedReader(ir);
            }
            listenerThread = new Thread(new ExternalCommunicator());
            listenerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Consumer handleSyncTick = c -> {
        Game game = (Game) c;
        long currentTick = game.handler.globalTickNumber;
        if (isMultiplayer && currentTick % 1 == 0) {
//            Thread.ofVirtual().start(new Sender("finished:" + currentTick));
            Client.sendMessage("finished:" + currentTick);
        }
        while (partnerTick < currentTick && currentTick % 1 == 0) {
            // Main.wait(1);
            try {
                listenerThread.join(1);
            } catch (Exception e) {
                System.out.println("exception joining");
                Main.wait(1);
            }
//            Client.sendMessage("finished:" + currentTick);
        }
    };

    @Override
    public void run() {
        while (true) {
            try {
                // wait for partner to finish tick
                String nextMessage = bufferdReader.readLine();
                System.out.println("next message is " + nextMessage);
                interperateMessage(nextMessage);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    public static void interperateMessage(String s) {
        if (s == null) {
            return;
        }
        if (s.startsWith("randomSeed:")) {
            Main.setRandomSeed(Long.parseLong(s.substring("randomSeed:".length())));
            System.out.println("setting random seed to " + Long.parseLong(s.substring("randomSeed:".length())));
            return;
        }
        if (s.startsWith("finished:")) {
            partnerTick = Long.parseLong(s.substring(9));
        }
        // unit movement
        // example: m:1,100,200
        // moves unit with id 1 to coordinate 100,200
        if (s.startsWith("m:")) {
            var components = s.substring(2).split(",");
            int id = Integer.parseInt(components[0]);
            int x = Integer.parseInt(components[1]);
            int y = Integer.parseInt(components[2]);
            long intendedTick = Long.parseLong(components[3]); // this is when the input was actually done
            Coordinate coord = new Coordinate(x, y);
            long currentTick = Window.currentGame.handler.globalTickNumber;
            long tickToIssueOn = intendedTick + 1; // the input handler adds one tick delay for this purpose
            if(tickToIssueOn <= currentTick) {
                System.out.println("issuing order to unit " + id + " to move to " + coord + " on tick " + Window.currentGame.handler.globalTickNumber);
                GameObject2 go = Window.currentGame.getObjectById(id);
                if (go instanceof RTSUnit unit) {
                    unit.setDesiredLocation(coord);
                    System.out.println("done");
                }
            } else {
                Window.currentGame.addTickDelayedEffect((int)(tickToIssueOn - Window.currentGame.handler.globalTickNumber), c->{
                    System.out.println("issuing order to unit " + id + " to move to " + coord + " on tick " + Window.currentGame.handler.globalTickNumber);
                    GameObject2 go = Window.currentGame.getObjectById(id);
                    if (go instanceof RTSUnit unit) {
                        unit.setDesiredLocation(coord);
                        System.out.println("done");
                    }
                });
            }
        }
    }

    private static class Sender implements Runnable {

        String m;

        public Sender(String message) {
            m = message;
        }

        @Override
        public void run() {
            Client.sendMessage(m);
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Multiplayer;

import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.Window;
import GameDemo.RTSDemo.Commands.MoveCommand;
import GameDemo.RTSDemo.Commands.StopCommand;
import static GameDemo.RTSDemo.Multiplayer.Client.printStream;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

/**
 *
 * @author guydu
 */
public class ExternalCommunicator implements Runnable {

    public static int port = 444;

    public static boolean isMultiplayer = false;

    public static ServerSocket servSocket;
    public static Socket socket;
    public static InputStreamReader inputReader;
    public static BufferedReader bufferdReader;
    public static PrintStream printstream;
    public static long partnerTick = 0;
    public static Thread listenerThread;
    public static boolean isServer = false;
    public static int localTeam = 0;
    public static boolean isConnected = false;
    public static  boolean isResyncing = false;

    public static ExecutorService asyncService = Executors.newVirtualThreadPerTaskExecutor();
    
    public static volatile boolean isReadyForMultiplayerThisMachine = false;
    public static volatile boolean isReadyForMultiplayerOtherMachine = false;
    public static volatile long mpStartTime = -1;
    public static volatile boolean isMpStarted = false;
    
    public static void setAndCommunicateMultiplayerReady () {
        isReadyForMultiplayerThisMachine = true;
        sendMessage("readyPhase1");
    }
    
    public static void setAndCommunicateMultiplayerStartTime () {
        mpStartTime = System.currentTimeMillis() + 1000;
        sendMessage("mpStartTime:"+mpStartTime);
    }
    
    public static boolean isWaitingForMpStart() {
        return mpStartTime > 0 && mpStartTime > System.currentTimeMillis();
    };

    public static void initialize(boolean server) {

        try {
            isMultiplayer = true;
            if (server) {
                localTeam = 0;
                isServer = server;
                String publicIp = getPublicIP();
                System.out.println(publicIp);
                JOptionPane.showMessageDialog(null, "Server starting from your public ip: " + publicIp + ":" + port + "\n If no connection is made, will timeout in 30s");
                servSocket = new ServerSocket(port);
                System.out.println("server Inet Address: " + servSocket.getInetAddress());
                // blocks until connection
                asyncService.submit(() -> {
                    Main.wait(30000);
                    if (!isConnected) {
                        JOptionPane.showMessageDialog(null, "Timeout");
                        System.exit(0);
                    }
                    return null;
                });
                socket = servSocket.accept();
                isConnected = true;
                printStream = new PrintStream(socket.getOutputStream()); //output stream is what we are sending
                inputReader = new InputStreamReader(socket.getInputStream());
                bufferdReader = new BufferedReader(inputReader);
                long seed = (long) (Math.random() * 999999999);
                Main.setRandomSeed(seed);
                sendMessage("randomSeed:" + seed);
                System.out.println("setting random seed" + seed);

            } else {
                localTeam = 1;
                String peerAddress = JOptionPane.showInputDialog("Enter Connection Address");
                socket = new Socket(peerAddress, port);
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
        if(isMultiplayer && !isMpStarted && isWaitingForMpStart()) {
            System.out.println("waiting for mp");
            while(isWaitingForMpStart()) {
                Main.wait(10);
            }
            System.out.println("starting mp");
            game.handler.globalTickNumber = 0;
            game.setPaused(false);
        }
    };

    public static void communicateState() {
        StringBuilder builder = new StringBuilder();
        builder.append("unitstate:");
        for (GameObject2 go : Window.currentGame.getAllObjects()) {
            if (go instanceof RTSUnit unit && unit.team == localTeam) {
                builder.append(unit.toTransportString());
                builder.append(';');
            }
        }
        sendMessage(builder.toString());
    }

    public static void communicateState(RTSUnit unit) {
         sendMessage("unitstate:" + unit.toTransportString());
    }

    @Override
    public void run() {
        while (true) {
            try {
                String nextMessage = bufferdReader.readLine();
                interperateMessage(nextMessage);
            } catch (Exception e) {
                // System.out.println("Exception receiving message from peer");
            }
        }
    }

    public static void interperateMessage(String s) {
        if (s == null) {
            return;
        }
        if (s.startsWith("randomSeed:")) {
            Main.setRandomSeed(Long.parseLong(s.substring("randomSeed:".length())));
            return;
        }
        if (s.startsWith("finished:")) {
            partnerTick = Long.parseLong(s.substring(9));
        }
        if(s.equals("beginResync")) {
            beginResync(false);
            return;
        }
        if(s.equals("beginResyncPt2")) {
            beginResyncPt2();
            return;
        }
        if (s.startsWith("m:")) {
            System.out.println("message " + s);
            RTSGame.commandHandler.addCommand(MoveCommand.generateFromMpString(s), false);
        }
        
        if (s.startsWith("s:")) {
            System.out.println("message " + s);
            RTSGame.commandHandler.addCommand(StopCommand.generateFromMpString(s), false);
        }

        if (s.startsWith("unitstate:")) {
            String s2 = s.substring(10);
            var lineItems = s2.split(";");
            for (String line : lineItems) {
                if(line.equals("")) continue;
                var components = line.split(",");
                try {
                    GameObject2 go = Window.currentGame.getObjectById(components[0]);
                    if (go != null && go instanceof RTSUnit unit) {
                        unit.setFieldsPerString(line);
                    } else if (go == null) {
                        System.out.println("null for id" + components[0]);
                    }
                } catch (Exception e) {
                    System.out.println("unit state interpretation error " + line);
                    e.printStackTrace();
                }
            }
        }

        if (s.startsWith("unitRemoval:")) {
            String id = s.split(":")[1];
            GameObject2 go = Window.currentGame.getObjectById(id);
            if (go != null) {
                Window.currentGame.removeObject(go);
            }
        }
        
        if(s.startsWith("readyPhase1")) {
            isReadyForMultiplayerOtherMachine = true;
            if(isReadyForMultiplayerThisMachine) setAndCommunicateMultiplayerStartTime();
        }
        
        if(s.startsWith("mpStartTime")) {
            mpStartTime = Long.parseLong(s.split(":")[1]);
            if(isResyncing) {
                System.out.println("waiting for mp");
                while(isWaitingForMpStart()) {
                    Main.wait(10);
                }
                System.out.println("starting mp from mpStartTime handler");
                RTSGame.game.handler.globalTickNumber = 0;
                RTSGame.commandHandler.purge();
                RTSGame.game.setPaused(false);
                isResyncing = false;
            }
        }
    }

    public static void sendMessage(String message) {
        if (!isMultiplayer) {
            return;
        }
        if (printStream != null) {
            ExternalCommunicator.asyncService.submit(() -> {
                // Main.wait(60); // simulate lag
                printStream.println(message);
            });
        } else {
            System.out.println("ERROR NULL PRINTSTREAM");
        }
    }

    public static String getPublicIP() {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine(); //you get the IP as a String
            return ip;
        } catch (Exception e) {
            return "<public IP unknown>";
        }
    }
    
    
    public static synchronized void beginResync(boolean initiator) {
        if(isResyncing) return;
        isResyncing = true;
        System.out.println("beginResync " + initiator);
        if(initiator) sendMessage("beginResync");
        RTSGame.game.setPaused(true);
        for(GameObject2 go : RTSGame.game.getAllObjects()) {
//            if(go instanceof Projectile) {
//                RTSGame.game.removeObject(go);
//            }
        }
        mpStartTime = -1;
        communicateState();
        RTSGame.commandHandler.purge();
        if(isServer) {
            long seed = (long) (Math.random() * 999999999);
            Main.setRandomSeed(seed);
            sendMessage("randomSeed:" + seed);
         }
         
        if(!initiator) sendMessage("beginResyncPt2");
    }
    
    public static void beginResyncPt2() {
       setAndCommunicateMultiplayerStartTime();
       System.out.println("waiting for mp");
        while(isWaitingForMpStart()) {
            Main.wait(10);
        }
        System.out.println("starting mp from resyncpt2");
        RTSGame.game.handler.globalTickNumber = 0;
        RTSGame.commandHandler.purge();
        RTSGame.game.setPaused(false);
        isResyncing = false;
    }
}

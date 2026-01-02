/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Multiplayer;

import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.SerializationManager;
import Framework.Window;
import GameDemo.RTSDemo.Commands.MoveCommand;
import GameDemo.RTSDemo.Commands.StopCommand;
import static GameDemo.RTSDemo.Multiplayer.Client.printStream;
import GameDemo.RTSDemo.RTSGame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
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

    // Resync save file tracking
    private static final String RESYNC_SAVE_PATH = "saves/mp_resync_" + System.currentTimeMillis() + ".dat";
    private static volatile boolean waitingForSaveFile = false;
    private static volatile boolean saveFileReceived = false;
    private static volatile StringBuilder saveFileDataBuilder = null;
    private static volatile int expectedSaveFileSize = 0;
    private static volatile int expectedChunks = 0;
    private static volatile int receivedChunks = 0;
    private static volatile boolean clientLoadComplete = false;
    
    public static void setAndCommunicateMultiplayerReady () {
        isReadyForMultiplayerThisMachine = true;
        sendMessage("readyPhase1");
    }
    
    public static void setAndCommunicateMultiplayerStartTime () {
        mpStartTime = System.currentTimeMillis() + 2000;
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
                // Client receives this after loading is complete and being paused
                asyncService.submit(() -> {
                    System.out.println("Client received restart time, waiting for synchronized restart...");
                    while(isWaitingForMpStart()) {
                        Main.wait(10);
                    }
                    System.out.println("Client unpausing multiplayer game after resync");
                    RTSGame.game.setPaused(false);
                    isResyncing = false;
                    return null;
                });
            }
        }

        // Save file transfer messages
        if(s.startsWith("saveFileStart:")) {
            String[] parts = s.split(":");
            expectedSaveFileSize = Integer.parseInt(parts[1]);
            expectedChunks = Integer.parseInt(parts[2]);
            receivedChunks = 0;
            saveFileDataBuilder = new StringBuilder();
            System.out.println("Receiving save file: " + expectedSaveFileSize + " bytes in " + expectedChunks + " chunks");
        }

        if(s.startsWith("saveFileChunk:")) {
            String[] parts = s.split(":", 3);
            int chunkIndex = Integer.parseInt(parts[1]);
            String chunkData = parts[2];
            saveFileDataBuilder.append(chunkData);
            receivedChunks++;
            if(receivedChunks % 10 == 0) {
                System.out.println("Received chunk " + receivedChunks + "/" + expectedChunks);
            }
        }

        if(s.equals("saveFileEnd")) {
            try {
                // Decode Base64 and write to file
                String encodedData = saveFileDataBuilder.toString();
                byte[] fileData = Base64.getDecoder().decode(encodedData);

                // Create saves directory if needed
                File savesDir = new File("saves");
                if (!savesDir.exists()) {
                    savesDir.mkdir();
                }

                // Write to file
                try (FileOutputStream fos = new FileOutputStream(RESYNC_SAVE_PATH)) {
                    fos.write(fileData);
                }

                System.out.println("Save file received and written: " + fileData.length + " bytes");
                saveFileReceived = true;
                waitingForSaveFile = false;

                // Clean up
                saveFileDataBuilder = null;
            } catch (Exception e) {
                System.err.println("Error processing received save file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if(s.equals("loadComplete")) {
            clientLoadComplete = true;
            System.out.println("Client has completed loading");
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

        // Clear any pending operations
        mpStartTime = -1;
        RTSGame.commandHandler.purge();

        if(isServer) {
            // Server creates save file via tick-delayed effect (NOT paused yet)
            System.out.println("Server scheduling resync save file creation...");

            RTSGame.game.addTickDelayedEffect(1, g -> {
                System.out.println("Server creating resync save file...");
                createResyncSaveFile();

                // After save created, send it
                sendSaveFile();

                // Reset random seed
                long seed = (long) (Math.random() * 999999999);
                Main.setRandomSeed(seed);
                sendMessage("randomSeed:" + seed);

                // Don't pause yet - schedule loading first (still not paused)
                // The load will pause after it completes
                RTSGame.game.addTickDelayedEffect(1, g2 -> {
                    System.out.println("Server starting to load resync save file...");
                    loadResyncSaveFile();
                });
            });
        } else {
            // Client waits for save file (NOT paused yet)
            System.out.println("Client waiting for resync save file...");
            waitingForSaveFile = true;
            saveFileReceived = false;

            // Start async wait for file reception and loading
            asyncService.submit(() -> {
                while(!saveFileReceived) {
                    Main.wait(100);
                }
                // Once received, load it (will use tick-delayed effects)
                loadResyncSaveFile();
                return null;
            });
        }

        if(!initiator) sendMessage("beginResyncPt2");
    }

    /**
     * Creates a save file for resync purposes (synchronous)
     */
    private static void createResyncSaveFile() {
        try {
            // Create saves directory if it doesn't exist
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                savesDir.mkdir();
            }

            // Use SerializationManager to create the save
            // We need to do this synchronously, so we'll create the snapshot directly
            SerializationManager.GameStateSnapshot snapshot =
                new SerializationManager.GameStateSnapshot(RTSGame.game.handler, RTSGame.game);

            try (FileOutputStream fileOut = new FileOutputStream(RESYNC_SAVE_PATH);
                 java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(fileOut)) {
                out.writeObject(snapshot);
                System.out.println("Resync save file created with " + snapshot.gameObjects.size() + " objects");
            }
        } catch (Exception e) {
            System.err.println("Error creating resync save file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends the save file to the other machine via Base64 encoding
     */
    private static void sendSaveFile() {
        try {
            File saveFile = new File(RESYNC_SAVE_PATH);
            if (!saveFile.exists()) {
                System.err.println("Resync save file not found!");
                return;
            }

            // Read file into byte array
            byte[] fileData = new byte[(int) saveFile.length()];
            try (FileInputStream fis = new FileInputStream(saveFile)) {
                fis.read(fileData);
            }

            // Encode to Base64 for text transmission
            String encodedData = Base64.getEncoder().encodeToString(fileData);

            // Send in chunks to avoid overwhelming the buffer (64KB chunks)
            int chunkSize = 65536;
            int chunks = (int) Math.ceil((double) encodedData.length() / chunkSize);

            System.out.println("Sending save file: " + fileData.length + " bytes in " + chunks + " chunks");
            sendMessage("saveFileStart:" + fileData.length + ":" + chunks);

            for (int i = 0; i < chunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, encodedData.length());
                String chunk = encodedData.substring(start, end);
                sendMessage("saveFileChunk:" + i + ":" + chunk);
                Main.wait(10); // Small delay between chunks
            }

            sendMessage("saveFileEnd");
            System.out.println("Save file sent successfully");
        } catch (Exception e) {
            System.err.println("Error sending save file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the resync save file using SerializationManager
     */
    private static void loadResyncSaveFile() {
        try {
            File saveFile = new File(RESYNC_SAVE_PATH);
            if (!saveFile.exists()) {
                System.err.println("Resync save file not found for loading!");
                return;
            }

            System.out.println("Loading resync save file...");
            SerializationManager.loadGameState(RTSGame.game, RESYNC_SAVE_PATH);

            // Loading happens via tick delays (takes ~3 ticks)
            // Schedule pause and coordination after loading finishes
            RTSGame.game.addTickDelayedEffect(1, g -> {
                System.out.println("Resync load complete, now pausing for coordination");

                // NOW pause after loading is complete
                RTSGame.game.setPaused(true);

                if(!isServer) {
                    // Client signals completion
                    sendMessage("loadComplete");
                } else {
                    // Server waits for client to finish, then coordinates restart
                    asyncService.submit(() -> {
                        System.out.println("Server waiting for client load completion...");
                        while(!clientLoadComplete) {
                            Main.wait(100);
                        }
                        System.out.println("Both sides loaded and paused, coordinating restart...");
                        setAndCommunicateMultiplayerStartTime();

                        // Wait for the synchronized restart time
                        while(isWaitingForMpStart()) {
                            Main.wait(10);
                        }
                        System.out.println("Restarting multiplayer game after resync");
                        RTSGame.game.setPaused(false);
                        isResyncing = false;
                        clientLoadComplete = false; // Reset for next resync
                        return null;
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading resync save file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void beginResyncPt2() {
       // Coordination now happens via save file load completion in loadResyncSaveFile()
       // This acknowledgment message is received but no action needed
       System.out.println("beginResyncPt2 acknowledged (using save file coordination)");
    }
}

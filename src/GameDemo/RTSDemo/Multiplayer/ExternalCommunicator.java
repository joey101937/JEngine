/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameDemo.RTSDemo.Multiplayer;

import Framework.Game;
import Framework.GameObject2;
import Framework.Main;
import Framework.SerializationManager;
import GameDemo.RTSDemo.Commands.MoveCommand;
import GameDemo.RTSDemo.Commands.StopCommand;
import static GameDemo.RTSDemo.Multiplayer.Client.printStream;
import GameDemo.RTSDemo.RTSGame;
import GameDemo.RTSDemo.RTSInput;
import GameDemo.RTSDemo.RTSUnit;
import GameDemo.RTSDemo.TextChatEffect;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
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
    private static volatile boolean waitingForSaveFile = false;
    private static volatile boolean saveFileReceived = false;
    private static volatile StringBuilder saveFileDataBuilder = null;
    private static volatile int expectedSaveFileSize = 0;
    private static volatile int expectedChunks = 0;
    private static volatile int receivedChunks = 0;
    private static volatile String expectedChecksum = null;
    private static volatile boolean clientLoadComplete = false;

    // Adaptive tick synchronization
    private static volatile int currentInputDelay = 24; // Start high after resync, target is 12
    public static volatile double tickTimingOffset = 0; // How many ticks we're ahead/behind partner (use double for precision)
    private static volatile boolean readyToDecreaseDelay = false;
    private static volatile boolean partnerReadyToDecreaseDelay = false;
    private static volatile long lastTickHeartbeatTime = 0;
    private static final int TARGET_INPUT_DELAY = 12;
    private static final int INITIAL_INPUT_DELAY = 24;
    private static final int TICK_HEARTBEAT_INTERVAL_MS = 200; // Send tick heartbeat every 100ms

    // Ping tracking
    private static volatile long lastPingSentTime = 0;
    private static volatile long pendingPingSentAt = -1;
    public static volatile int currentPingMs = -1;
    private static final int PING_INTERVAL_MS = 2000;

    // Speed-up control parameters (for machine that's behind)
    private static final int NORMAL_TPS = 90;
    private static volatile int baseTicksPerSecond = NORMAL_TPS; // Save original TPS

    // Determinism check - compare game states every 5 seconds
    private static final int DETERMINISM_CHECK_INTERVAL = 450; // Every 5 seconds at 90 TPS
    private static final int DETERMINISM_GRACE_PERIOD = 450; // Skip checks for 10 seconds (900 ticks) after resync
    private static volatile long lastDeterminismCheckTick = 0;
    private static volatile long lastResyncCompletedTick = -10000; // Track when last resync completed
    private static final java.util.concurrent.ConcurrentHashMap<Long, String> partnerStateStrings = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<Long, String> ourStateStrings = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static ArrayList<String> outOfSyncUnitIds = new ArrayList<>();
    
    public static void setAndCommunicateMultiplayerReady () {
        isReadyForMultiplayerThisMachine = true;
        sendMessage("readyPhase1");
    }
    
    public static void setAndCommunicateMultiplayerStartTime () {
        mpStartTime = System.currentTimeMillis() + 4000;
        sendMessage("mpStartTime:"+mpStartTime);
        // Reset random seed
        long seed = (long) (Math.random() * 999999999);
        Main.setRandomSeed(seed);
        sendMessage("randomSeed:" + seed);
    }
    
    public static boolean isWaitingForMpStart() {
        return mpStartTime > 0 && mpStartTime > System.currentTimeMillis();
    };
    
    public static boolean isMPReadyForCommands() {
        if(!isMultiplayer) return true;
        System.out.println(tickTimingOffset + " offset compared to " + (RTSInput.getInputDelay() - 5));
        return Math.abs(tickTimingOffset) < RTSInput.getInputDelay() - 5;
    }

    private static String getResyncPath() {return "saves/mp_resync_" + (isServer ? "server" : "client") + ".dat";}
    
    public static void initialize(boolean server) {

        try {
            isMultiplayer = true;

            // Save the current TPS setting
            baseTicksPerSecond = Main.ticksPerSecond;

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

    public static Consumer<Game> handleSyncTick = game -> {       
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

        // Adaptive tick slowdown to maintain synchronization
        if(isMultiplayer && !isResyncing && currentTick > 0) {
            long now = System.currentTimeMillis();

            // Send periodic tick heartbeat to partner so they can measure offset
            if(now - lastTickHeartbeatTime >= TICK_HEARTBEAT_INTERVAL_MS) {
                sendMessage("tickHeartbeat:" + currentTick);
                lastTickHeartbeatTime = now;
            }

            // Send periodic ping to measure round-trip latency
            if(now - lastPingSentTime >= PING_INTERVAL_MS && pendingPingSentAt == -1) {
                pendingPingSentAt = now;
                lastPingSentTime = now;
                sendMessage("ping:" + now);
            }

            // Speed-up control when BEHIND (negative offset means partner is ahead)
            // Instead of slowing down the ahead machine, speed up the behind machine!
            if(tickTimingOffset < -2) {
                // We're behind - temporarily increase our TPS to catch up
                // Proportional boost: 0.25 TPS per tick of offset, capped at +5 TPS
                int maxBoost = (int) Math.min(baseTicksPerSecond * 0.8, 160); // 80% boost, capped at +160 TPS
                int tpsBoost = (int) Math.min(Math.abs(tickTimingOffset) * 0.25, maxBoost);
                int targetTPS = baseTicksPerSecond + tpsBoost;

                if(Main.ticksPerSecond != targetTPS) {
                    // System.out.println("[SPEEDUP] Boosting TPS from " + Main.ticksPerSecond + " to " + targetTPS + " (offset: " + String.format("%.1f", tickTimingOffset) + " ticks) - Team " + localTeam);
                    Main.ticksPerSecond = targetTPS;
                }
            } else if(tickTimingOffset > -1 && Main.ticksPerSecond != baseTicksPerSecond) {
                // Close to sync or ahead - restore normal TPS
                // System.out.println("[SPEEDUP] Restoring TPS to " + baseTicksPerSecond + " (offset: " + String.format("%.1f", tickTimingOffset) + " ticks) - Team " + localTeam);
                Main.ticksPerSecond = baseTicksPerSecond;
            }

            // Periodic determinism check every 5 seconds
            if(currentTick > 0 && currentTick % DETERMINISM_CHECK_INTERVAL == 0 && currentTick != lastDeterminismCheckTick) {
                // Skip checks during grace period after resync
                if(currentTick - lastResyncCompletedTick < DETERMINISM_GRACE_PERIOD) {
                    System.out.println("[DETERMINISM] Skipping check at tick " + currentTick + " (grace period: " + (DETERMINISM_GRACE_PERIOD - (currentTick - lastResyncCompletedTick)) + " ticks remaining)");
                    lastDeterminismCheckTick = currentTick; // Still mark as checked to avoid re-checking
                    return;
                }

                lastDeterminismCheckTick = currentTick;

                // Generate state string from all units
                String ourStateString = generateGameStateString();

                // Store our state string for this tick (for later comparison when partner's state arrives)
                ourStateStrings.put(currentTick, ourStateString);

                // Send to partner with tick number
                 sendMessage("stateCheck:" + currentTick + ":" + Base64.getEncoder().encodeToString(ourStateString.getBytes()));
                // System.out.println("[DETERMINISM] Sent state check for tick " + currentTick);

                // Check if we have partner's state for this exact tick
                String partnerStateString = partnerStateStrings.get(currentTick);
                if(partnerStateString != null) {
                    // Compare the strings
                    if(!ourStateString.equals(partnerStateString)) {
                        System.out.println("\n[DETERMINISM] ===== DESYNC DETECTED AT TICK " + currentTick + " =====");

                        // Analyze and report the differences
                        analyzeAndReportStateDifferences(ourStateString, partnerStateString, currentTick);

                        // Trigger resync
                        if(!isResyncing) {
                            System.out.println("being resync via partenr state check");
                            beginResync(true);
                        }
                    } else {
                        System.out.println("[DETERMINISM] Check PASSED for tick " + currentTick + " - games in sync");
                    }
                    // Clean up both state strings after comparison
                    partnerStateStrings.remove(currentTick);
                    ourStateStrings.remove(currentTick);
                }

                // Clean up old state strings (keep only last 3)
                if(partnerStateStrings.size() > 3) {
                    long oldestToKeep = currentTick - (DETERMINISM_CHECK_INTERVAL * 2);
                    partnerStateStrings.entrySet().removeIf(entry -> entry.getKey() < oldestToKeep);
                }
                if(ourStateStrings.size() > 3) {
                    long oldestToKeep = currentTick - (DETERMINISM_CHECK_INTERVAL * 2);
                    ourStateStrings.entrySet().removeIf(entry -> entry.getKey() < oldestToKeep);
                }
            }

            // Check if we're synchronized enough to decrease input delay
            if(currentInputDelay > TARGET_INPUT_DELAY) {
                // If tick offset is small (within 6 ticks) and stable
                if(Math.abs(tickTimingOffset) <= 6.0) {
                    if(!readyToDecreaseDelay) {
                        readyToDecreaseDelay = true;
                        sendMessage("readyToDecrease");
                        System.out.println("[SYNC] Ready to decrease input delay (current: " + currentInputDelay + ", offset: " + String.format("%.1f", tickTimingOffset) + ")");
                    }

                    // If both sides ready, decrease together
                    if(partnerReadyToDecreaseDelay) {
                        currentInputDelay = Math.max(currentInputDelay - 1, TARGET_INPUT_DELAY);
                        readyToDecreaseDelay = false;
                        partnerReadyToDecreaseDelay = false;
                        sendMessage("decreaseDelay:" + currentInputDelay);
                        System.out.println("[SYNC] Decreased input delay to " + currentInputDelay);
                    }
                } else {
                    // Reset ready flag if we drift out of sync
                    if(readyToDecreaseDelay) {
                        System.out.println("[SYNC] Drift detected (" + String.format("%.1f", tickTimingOffset) + " ticks), resetting ready flag");
                        readyToDecreaseDelay = false;
                    }
                }
            }
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
        System.out.println("received: " + s);
        if (s.startsWith("randomSeed:")) {
            Main.setRandomSeed(Long.parseLong(s.substring("randomSeed:".length())));
            return;
        }
        if (s.startsWith("finished:")) {
            partnerTick = Long.parseLong(s.substring(9));
        }
        if(s.equals("beginResync")) {
            if(isResyncing) return;
            System.out.println("starting rsync");
            beginResync(false);
            return;
        }
        if(s.equals("beginResyncPt2")) {
            beginResyncPt2();
            return;
        }
        if(s.startsWith("chat:")) {
            // note these messaegs are sent on the tick they were sent on. may be in the past (or future) for the receiver
            RTSGame.textChatEffect.addChatMessageToHistory(new TextChatEffect.ChatMessage(s));
        }
        if (s.startsWith("m:")) {
            // Drop commands during resync to prevent state corruption
            if(isResyncing) {
                System.out.println("Dropping move command during resync");
                return;
            }
            System.out.println("message " + s);
            MoveCommand cmd = MoveCommand.generateFromMpString(s);
            RTSGame.commandHandler.addCommand(cmd, false);
            updateTickTimingOffset(cmd.getExecuteTick());
        }

        if (s.startsWith("s:")) {
            // Drop commands during resync to prevent state corruption
            if(isResyncing) {
                System.out.println("Dropping stop command during resync");
                return;
            }
            System.out.println("message " + s);
            StopCommand cmd = StopCommand.generateFromMpString(s);
            RTSGame.commandHandler.addCommand(cmd, false);
            updateTickTimingOffset(cmd.getExecuteTick());
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
                    lastResyncCompletedTick = RTSGame.game.handler.globalTickNumber;
                    System.out.println("[DETERMINISM] Resync completed at tick " + lastResyncCompletedTick + ", grace period of " + DETERMINISM_GRACE_PERIOD + " ticks active");
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
            expectedChecksum = parts[3]; // SHA-256 checksum
            receivedChunks = 0;
            saveFileDataBuilder = new StringBuilder();
            System.out.println("Receiving save file: " + expectedSaveFileSize + " bytes in " + expectedChunks + " chunks");
            System.out.println("Expected checksum: " + expectedChecksum);
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

                // Verify checksum BEFORE writing to disk
                String actualChecksum = computeChecksum(fileData);
                System.out.println("Computed checksum: " + actualChecksum);

                if (!actualChecksum.equals(expectedChecksum)) {
                    String errorMsg = "CHECKSUM MISMATCH! Expected: " + expectedChecksum + ", Got: " + actualChecksum;
                    System.err.println(errorMsg);
                    System.err.println("Save file is corrupted! Size: " + fileData.length + " bytes, Expected: " + expectedSaveFileSize + " bytes");

                    // Notify server of corruption
                    sendMessage("loadFailed:Checksum verification failed - file corrupted during transmission");
                    isResyncing = false;
                    saveFileDataBuilder = null;
                    return;
                }

                System.out.println("Checksum verified successfully!");

                // Create saves directory if needed
                File savesDir = new File("saves");
                if (!savesDir.exists()) {
                    savesDir.mkdir();
                }

                // Write to file
                try (FileOutputStream fos = new FileOutputStream(getResyncPath())) {
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
                // Notify server of failure
                sendMessage("loadFailed:" + e.getMessage());
                isResyncing = false;
            }
        }

        if(s.equals("loadComplete")) {
            clientLoadComplete = true;
            System.out.println("Client has completed loading");
        }

        if(s.startsWith("loadFailed:")) {
            String errorMessage = s.substring(11);
            System.err.println("Client reported load failure: " + errorMessage);
            System.err.println("Aborting resync");
            isResyncing = false;
            clientLoadComplete = true; // Set to true to break server's wait loop
        }

        // Tick heartbeat for continuous synchronization
        if(s.startsWith("tickHeartbeat:")) {
            long partnerTickAtSend = Long.parseLong(s.substring(14));
            long ourTick = RTSGame.game.handler.globalTickNumber;

            // Adjust for one-way transit time using our own RTT measurement.
            // Without this, a behind machine can see a falsely positive (ahead) offset when
            // one-way latency in ticks exceeds the real gap.
            double adjustedPartnerTick = partnerTickAtSend;
            if(currentPingMs > 0) {
                double oneWayMs = currentPingMs / 2.0;
                adjustedPartnerTick += oneWayMs * RTSGame.desiredTPS / 1000.0;
            }

            // Calculate offset: positive means WE are ahead, negative means PARTNER is ahead
            double rawOffset = ourTick - adjustedPartnerTick;

            double oldOffset = tickTimingOffset;
            // Smooth the offset
            tickTimingOffset = tickTimingOffset * 0.5 + rawOffset * 0.5;

            System.out.println("[HEARTBEAT] Partner at tick " + partnerTickAtSend + " (ping-adjusted: " + String.format("%.1f", adjustedPartnerTick) + "), we're at " + ourTick +
                             " | Raw offset: " + String.format("%.1f", rawOffset) + " | Smoothed: " + String.format("%.1f", oldOffset) +
                             " -> " + String.format("%.1f", tickTimingOffset));
        }

        // Ping / pong for latency measurement
        if(s.startsWith("ping:")) {
            // Reflect back immediately so the sender can measure RTT
            sendMessage("pong:" + s.substring(5));
            return;
        }

        if(s.startsWith("pong:")) {
            long sentAt = Long.parseLong(s.substring(5));
            int rtt = (int)(System.currentTimeMillis() - sentAt);
            // EWMA smoothing (α=0.25): heavily weights existing estimate to filter spikes
            currentPingMs = currentPingMs < 0 ? rtt : (int)(currentPingMs * 0.75 + rtt * 0.25);
            pendingPingSentAt = -1;
            System.out.println("[PING] Round-trip latency: " + rtt + " ms (smoothed: " + currentPingMs + " ms)");
            return;
        }

        // State check messages for determinism verification
        if(s.startsWith("stateCheck:")) {
            String[] parts = s.substring(11).split(":", 2);
            long tick = Long.parseLong(parts[0]);
            String stateString = new String(Base64.getDecoder().decode(parts[1]));

            System.out.println("[DETERMINISM] Received partner state for tick " + tick);

            // Check if we have our state stored for this tick
            String ourStateString = ourStateStrings.get(tick);
            if(ourStateString != null) {
                // We have our state for this tick, compare it
                if(!ourStateString.equals(stateString)) {
                    System.out.println("\n[DETERMINISM] ===== DESYNC DETECTED AT TICK " + tick + " =====");

                    // Analyze and report the differences
                    analyzeAndReportStateDifferences(ourStateString, stateString, tick);

                    // Trigger resync
                    if(!isResyncing) {
                        System.out.println("beginning resync from statecheck");
                        beginResync(true);
                    }
                } else {
                    System.out.println("[DETERMINISM] Check PASSED for tick " + tick + " - games in sync");
                }
                // Clean up both state strings after comparison
                ourStateStrings.remove(tick);
                partnerStateStrings.remove(tick);
            } else {
                // We don't have our state for this tick yet, store partner's for later comparison
                partnerStateStrings.put(tick, stateString);
                System.out.println("[DETERMINISM] Stored partner state for tick " + tick + " (waiting for our state)");
            }
        }

        // Adaptive synchronization messages
        if(s.equals("readyToDecrease")) {
            partnerReadyToDecreaseDelay = true;
            System.out.println("Partner ready to decrease input delay");
        }

        if(s.startsWith("decreaseDelay:")) {
            int newDelay = Integer.parseInt(s.substring(14));
            currentInputDelay = newDelay;
            readyToDecreaseDelay = false;
            partnerReadyToDecreaseDelay = false;
            System.out.println("Synchronized decrease to input delay: " + currentInputDelay);
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
        ExternalCommunicator.ourStateStrings.clear();
        ExternalCommunicator.partnerStateStrings.clear();
        RTSGame.commandHandler.printCommandHistory();
        isResyncing = true;
        System.out.println("beginResync " + initiator);
        if(initiator) sendMessage("beginResync");

        // Reset adaptive synchronization to initial state
        resetAdaptiveSync();

        // Clear any pending operations
        mpStartTime = -1;
        // NOTE: Don't purge commands - they are part of the save state and should be preserved!

        if(isServer) {
            // Server creates save file on next tick, then immediately loads it
            System.out.println("Server scheduling resync save file creation...");

            RTSGame.game.addTickDelayedEffect(1, g -> {
                System.out.println("Server creating resync save file...");
                createResyncSaveFile();

                // Send file to client in background (don't wait for it)
                asyncService.submit(() -> {
                    // Small delay to let file handles close
                    Main.wait(50);
                    sendSaveFile();

                    // Wait for client to confirm file received, then both load simultaneously
                    int waitCount = 0;
                    int maxWaitSeconds = 10; // 10 second timeout
                    while(!clientLoadComplete && waitCount < maxWaitSeconds * 10) {
                        Main.wait(100);
                        waitCount++;
                    }

                    if(!clientLoadComplete) {
                        System.err.println("Server timeout waiting for client load confirmation after " + maxWaitSeconds + " seconds!");
                        System.err.println("Aborting resync - client may have failed to load save file");
                        isResyncing = false;
                        return null;
                    }

                    System.out.println("Client confirmed save received, both machines loading now...");
                    loadResyncSaveFile();
                    clientLoadComplete = false; // Reset for next resync

                    return null;
                });
            });
        } else {
            // Client waits for save file (NOT paused yet)
            System.out.println("Client waiting for resync save file...");
            waitingForSaveFile = true;
            saveFileReceived = false;

            // Start async wait for file reception
            asyncService.submit(() -> {
                try {
                    while(!saveFileReceived) {
                        Main.wait(50);
                    }
                    // Signal server that we received the file
                    System.out.println("Client received save file, signaling server...");
                    sendMessage("loadComplete");

                    // Now wait a moment for the server to start loading
                    Main.wait(200);

                    // Load simultaneously with server
                    System.out.println("Client loading save file...");
                    loadResyncSaveFile();
                } catch (Exception e) {
                    System.err.println("Client error during save file loading: " + e.getMessage());
                    e.printStackTrace();
                    // Notify server of failure
                    sendMessage("loadFailed:" + e.getMessage());
                    isResyncing = false;
                }
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
            SerializationManager.GameStateSnapshot snapshot = // SerializationManager.generateStateSnapshot(RTSGame.game);
                new SerializationManager.GameStateSnapshot(RTSGame.game);
            // alert! this snapshot is taken mid-tick so it may not work because some units will be one tick ahead of others.

            try (FileOutputStream fileOut = new FileOutputStream(getResyncPath());
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
            File saveFile = new File(getResyncPath());
            if (!saveFile.exists()) {
                System.err.println("Resync save file not found!");
                return;
            }

            // Read file into byte array
            byte[] fileData = new byte[(int) saveFile.length()];
            try (FileInputStream fis = new FileInputStream(saveFile)) {
                fis.read(fileData);
            }

            // Compute checksum of the original file data
            String checksum = computeChecksum(fileData);
            System.out.println("Computed file checksum: " + checksum + " (" + fileData.length + " bytes)");

            // Encode to Base64 for text transmission
            String encodedData = Base64.getEncoder().encodeToString(fileData);

            // Send in chunks to avoid overwhelming the buffer (64KB chunks)
            int chunkSize = 65536;
            int chunks = (int) Math.ceil((double) encodedData.length() / chunkSize);

            System.out.println("Sending save file: " + fileData.length + " bytes in " + chunks + " chunks");
            sendMessage("saveFileStart:" + fileData.length + ":" + chunks + ":" + checksum);

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
     * Computes SHA-256 checksum of byte array
     */
    private static String computeChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("Error computing checksum: " + e.getMessage());
            e.printStackTrace();
            return "ERROR";
        }
    }

    /**
     * Loads the resync save file using SerializationManager
     */
    private static void loadResyncSaveFile() {
        try {
            File saveFile = new File(getResyncPath());
            if (!saveFile.exists()) {
                System.err.println("Resync save file not found for loading!");
                return;
            }

            System.out.println("Loading resync save file...");
            SerializationManager.loadGameState(RTSGame.game, getResyncPath());

            // Loading happens via tick delays (takes ~2 ticks)
            // Schedule pause and coordination after loading finishes
            RTSGame.game.addTickDelayedEffect(3, g -> {
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
                        lastResyncCompletedTick = RTSGame.game.handler.globalTickNumber;
                        System.out.println("[DETERMINISM] Resync completed at tick " + lastResyncCompletedTick + ", grace period of " + DETERMINISM_GRACE_PERIOD + " ticks active");
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

    /**
     * Generates a state string from all units using their toTransportString() method
     */
    private static String generateGameStateString() {
        StringBuilder stateBuilder = new StringBuilder();

        // Get all units and sort by ID for deterministic ordering
        java.util.List<RTSUnit> units = new java.util.ArrayList<>();
        for (GameObject2 obj : RTSGame.game.getAllObjects()) {
            if (obj instanceof RTSUnit unit) {
                units.add(unit);
            }
        }

        // Sort by ID to ensure same order on both machines
        units.sort((a, b) -> a.ID.compareTo(b.ID));

        // Build state string from all units using toTransportString()
        for (RTSUnit unit : units) {
            stateBuilder.append(unit.toTransportString());
            stateBuilder.append("\n");
        }

        return stateBuilder.toString();
    }

    /**
     * Updates the tick timing offset based on incoming command execute ticks
     * This tells us if we're ahead or behind the partner
     */
    private static void updateTickTimingOffset(long incomingExecuteTick) {
        long currentTick = RTSGame.game.handler.globalTickNumber;
        long expectedExecuteTick = currentTick + currentInputDelay;

        // Calculate offset from command execute tick
        // If partner scheduled command sooner than expected, they're behind (we're ahead = positive)
        // If partner scheduled command later than expected, they're ahead (we're behind = negative)
        double offset = expectedExecuteTick - incomingExecuteTick;

        double oldSmoothedOffset = tickTimingOffset;

        // Smooth the offset with exponential moving average
        tickTimingOffset = tickTimingOffset * 0.5 + offset * 0.5;

        System.out.println("[CMD-UPDATE] Raw offset: " + String.format("%.1f", offset) +
                          " | Smoothed: " + String.format("%.1f", oldSmoothedOffset) + " -> " + String.format("%.1f", tickTimingOffset) +
                          " | currentTick:" + currentTick + " | inputDelay:" + currentInputDelay +
                          " | expectedExec:" + expectedExecuteTick + " | actualExec:" + incomingExecuteTick);
    }

    /**
     * Gets the current adaptive input delay
     */
    public static int getCurrentInputDelay() {
        return currentInputDelay;
    }

    /**
     * Resets adaptive synchronization state (called after resync)
     */
    private static void resetAdaptiveSync() {
        currentInputDelay = INITIAL_INPUT_DELAY;
        tickTimingOffset = 0.0;
        readyToDecreaseDelay = false;
        partnerReadyToDecreaseDelay = false;
        lastTickHeartbeatTime = 0;
        lastPingSentTime = 0;
        pendingPingSentAt = -1;

        // Reset determinism check state
        lastDeterminismCheckTick = 0;
        partnerStateStrings.clear();
        ourStateStrings.clear();

        // Restore base TPS (in case it was boosted for catch-up)
        if(Main.ticksPerSecond != baseTicksPerSecond) {
            System.out.println("[SYNC] Restoring TPS to " + baseTicksPerSecond + " (was " + Main.ticksPerSecond + ")");
            Main.ticksPerSecond = baseTicksPerSecond;
        }

        System.out.println("[SYNC] Reset adaptive sync - input delay: " + currentInputDelay);
    }

    public static void beginResyncPt2() {
       // Coordination now happens via save file load completion in loadResyncSaveFile()
       // This acknowledgment message is received but no action needed
       System.out.println("beginResyncPt2 acknowledged (using save file coordination)");
    }

    /**
     * Helper method to analyze and report differences between two game state snapshots.
     * Prints the number of units mismatched and which fields are different for each mismatched unit.
     *
     * @param ourStateString Our game state string
     * @param partnerStateString Partner's game state string
     * @param tick The tick number at which the desync was detected
     */
    private static void analyzeAndReportStateDifferences(String ourStateString, String partnerStateString, long tick) {
        System.out.println("\n[DETERMINISM] ===== ANALYZING DESYNC AT TICK " + tick + " =====");

        // Parse both state strings into maps (ID -> state line)
        java.util.Map<String, String> ourUnits = new java.util.HashMap<>();
        java.util.Map<String, String> partnerUnits = new java.util.HashMap<>();

        // Parse our state
        String[] ourLines = ourStateString.split("\n");
        for (String line : ourLines) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(",", -1);
            if (fields.length > 0) {
                String unitId = fields[0];
                ourUnits.put(unitId, line);
            }
        }

        // Parse partner state
        String[] partnerLines = partnerStateString.split("\n");
        for (String line : partnerLines) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split(",", -1);
            if (fields.length > 0) {
                String unitId = fields[0];
                partnerUnits.put(unitId, line);
            }
        }

        // Find mismatched units
        java.util.Set<String> allUnitIds = new java.util.HashSet<>();
        allUnitIds.addAll(ourUnits.keySet());
        allUnitIds.addAll(partnerUnits.keySet());

        int mismatchedUnits = 0;
        java.util.List<String> mismatchDetails = new java.util.ArrayList<>();

        // Field names for better reporting
        String[] fieldNames = {
            "ID", "location.x", "location.y", "currentHealth", "rotation",
            "desiredLocation.x", "desiredLocation.y", "isRubble", "commandGroup",
            "velocity.x", "velocity.y", "comingFromLocation", "baseSpeed", "originalSpeed",
            "isImmobilized", "isCloaked", "waypoints", "pathCacheUses",
            "pathCacheSignatureLastChangedTick", "pathStartCache", "pathEndCache",
            "pathCacheSignature", "pathCache"
        };

        for (String unitId : allUnitIds) {
            String ourUnit = ourUnits.get(unitId);
            String partnerUnit = partnerUnits.get(unitId);

            // Check if unit exists in both states
            if (ourUnit == null) {
                mismatchedUnits++;
                mismatchDetails.add("  Unit " + unitId + ": EXISTS ONLY IN PARTNER STATE (missing from our state)");
                continue;
            }

            if (partnerUnit == null) {
                mismatchedUnits++;
                mismatchDetails.add("  Unit " + unitId + ": EXISTS ONLY IN OUR STATE (missing from partner state)");
                continue;
            }

            // Compare fields
            if (!ourUnit.equals(partnerUnit)) {
                mismatchedUnits++;
                String[] ourFields = ourUnit.split(",", -1);
                String[] partnerFields = partnerUnit.split(",", -1);

                java.util.List<String> differentFields = new java.util.ArrayList<>();
                int maxFields = Math.max(ourFields.length, partnerFields.length);

                for (int i = 0; i < maxFields; i++) {
                    String ourValue = i < ourFields.length ? ourFields[i] : "<missing>";
                    String partnerValue = i < partnerFields.length ? partnerFields[i] : "<missing>";

                    if (!ourValue.equals(partnerValue)) {
                        String fieldName = i < fieldNames.length ? fieldNames[i] : "field[" + i + "]";
                        differentFields.add(fieldName + " (ours: " + ourValue + ", partner: " + partnerValue + ")");
                    }
                }

                StringBuilder detail = new StringBuilder("  Unit " + unitId + ": " + differentFields.size() + " field(s) differ\n");
                for (String diff : differentFields) {
                    detail.append("    - ").append(diff).append("\n");
                }
                mismatchDetails.add(detail.toString().trim());
                System.out.println("adding unit id to outOfSyncUnitIds" + unitId);
                outOfSyncUnitIds.add(unitId);
            }
        }

        // Print summary
        System.out.println("[DETERMINISM] Total units in our state: " + ourUnits.size());
        System.out.println("[DETERMINISM] Total units in partner state: " + partnerUnits.size());
        System.out.println("[DETERMINISM] Number of mismatched units: " + mismatchedUnits);

        if (mismatchedUnits > 0) {
            System.out.println("\n[DETERMINISM] MISMATCH DETAILS:");
            for (String detail : mismatchDetails) {
                System.out.println(detail);
            }
        }

        System.out.println("\n[DETERMINISM] ===== END DESYNC ANALYSIS =====\n");
    }
}

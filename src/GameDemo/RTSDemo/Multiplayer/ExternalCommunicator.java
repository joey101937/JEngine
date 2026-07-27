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
import GameDemo.RTSDemo.Commands.SetPreferredTargetCommand;
import GameDemo.RTSDemo.Commands.StopCommand;
import GameDemo.RTSDemo.Commands.TriggerAbilityCommand;
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
    public static volatile boolean isResyncing = false;
    /** Gate so only one resync hand-off is in flight at a time. See requestResync. */
    private static final java.util.concurrent.atomic.AtomicBoolean resyncRequested = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static ExecutorService asyncService = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Outbound messages go through a single thread so they reach the peer in the order they were
     * queued. A thread-per-task executor lets sends race, which reorders commands relative to each
     * other and lets saveFileEnd overtake the final save chunk.
     */
    private static final ExecutorService senderService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mp-message-sender");
        t.setDaemon(true);
        return t;
    });

    public static volatile boolean isReadyForMultiplayerThisMachine = false;
    public static volatile boolean isReadyForMultiplayerOtherMachine = false;
    /**
     * Deadline for the synchronized start/restart, measured against THIS machine's clock. The peer
     * sends a relative delay rather than an absolute timestamp, so a clock difference between the
     * two machines cannot shift the start.
     */
    public static volatile long mpStartDeadline = -1;
    /** True once the initial synchronized start has run. Gates the one-time tick counter reset. */
    public static volatile boolean isMpStarted = false;
    /** How long after both sides are ready the match begins. */
    private static final int START_COUNTDOWN_MS = 4000;

    // Resync save file tracking
    private static volatile boolean waitingForSaveFile = false;
    private static volatile boolean saveFileReceived = false;
    private static volatile String[] saveFileChunks = null;
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
    /** Wall time the last heartbeat arrived, used as the peer's liveness signal. */
    private static volatile long lastHeartbeatReceivedTime = 0;
    private static final int MIN_INPUT_DELAY = 12;
    private static final int INITIAL_INPUT_DELAY = 24;
    private static final int MAX_INPUT_DELAY = 40;
    /**
     * Floor that raises settle into. A resync resets the working delay, but the latency that forced
     * the raise is still there afterwards - dropping straight back to the initial value would walk
     * into the same late command again.
     */
    private static volatile int sustainedInputDelayFloor = INITIAL_INPUT_DELAY;
    // Drives the tick barrier, so it has to be fine-grained relative to the lead we allow.
    private static final int TICK_HEARTBEAT_INTERVAL_MS = 50;

    // Tick barrier. Rate control alone only nudges the tick rate, which lets a fast machine build an
    // arbitrary lead between corrections; once that lead exceeds the input delay, peer commands
    // arrive after their execute tick and can no longer be run in sync. The barrier makes the lead a
    // hard bound instead, so a late command becomes structurally impossible rather than merely rare.
    private static volatile long partnerTickAtLastHeartbeat = -1;
    private static final int MIN_TICK_LEAD = 3;   // never stall tighter than this, or we thrash
    private static final int LEAD_SAFETY_MARGIN = 2;
    private static final int MAX_STALL_MS = 2000; // upper bound on a single tick's stall
    /**
     * How long the peer may go without a heartbeat before the barrier stops waiting on them.
     * Heartbeats are sent off the tick thread, so silence this long means the peer is stalled
     * outright rather than merely running behind - and waiting on a stalled peer just spreads
     * their stall to us. Running on instead costs late commands, which raiseInputDelayFor and,
     * failing that, a resync already handle.
     */
    private static final int PARTNER_SILENCE_TIMEOUT_MS = 750;
    /** Ceiling on the resync file handshake, so a peer that never answers cannot wedge us. */
    private static final int RESYNC_HANDSHAKE_TIMEOUT_MS = 30000;
    /**
     * Lead the input delay should be sized to fund. A heartbeat every TICK_HEARTBEAT_INTERVAL_MS
     * leaves the partner's reported tick up to that many ticks stale, so a budget below it would put
     * the barrier in a stall on essentially every tick.
     */
    private static final int DESIRED_LEAD_BUDGET = 6;

    // Ping tracking
    private static volatile long pendingPingSentAt = -1;
    public static volatile int currentPingMs = -1;
    private static final int PING_INTERVAL_MS = 2000;

    // Symmetric rate control. Main.ticksPerSecond only paces the game loop - RTS logic scales off
    // RTSGame.desiredTPS - so nudging it changes how fast a machine consumes ticks without changing
    // what those ticks compute. Slowing the machine that is ahead is the reliable half: a machine
    // that is behind because it cannot hold its target rate will not go faster just because it is asked to.
    private static final int NORMAL_TPS = 90;
    private static volatile int baseTicksPerSecond = NORMAL_TPS; // Save original TPS
    private static final double RATE_CONTROL_DEADBAND = 2.0; // ticks of offset tolerated before adjusting
    private static final double TPS_PER_TICK_OF_OFFSET = 0.5; // proportional gain
    private static final double MAX_SPEEDUP_RATIO = 1.5;  // fastest a behind machine is asked to run
    private static final double MAX_SLOWDOWN_RATIO = 0.6; // slowest an ahead machine is asked to run
    /**
     * Past this offset, rate control would take minutes to converge (and cannot converge at all if
     * the gap came from a tick counter jump rather than from drift). Resync instead - loading the
     * snapshot restores both tick counters directly.
     */
    private static final int LARGE_OFFSET_RESYNC_THRESHOLD = 300;

    // Late command tracking - reported by CommandHandler when a peer command arrives after its execute tick
    private static volatile long lastLateCommandTick = -10000;
    private static volatile long worstRecentCommandLateness = 0;

    // Determinism check - compare game states every 5 seconds
    private static final int DETERMINISM_CHECK_INTERVAL = 450; // Every 5 seconds at 90 TPS
    private static final int DETERMINISM_GRACE_PERIOD = 450; // Skip checks for 10 seconds (900 ticks) after resync
    private static volatile long lastDeterminismCheckTick = 0;
    private static volatile long lastResyncCompletedTick = -10000; // Track when last resync completed
    private static final java.util.concurrent.ConcurrentHashMap<Long, String> partnerStateStrings = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<Long, String> ourStateStrings = new java.util.concurrent.ConcurrentHashMap<>();
    
    public static ArrayList<String> outOfSyncUnitIds = new ArrayList<>();
    
    /**
     * Called once this machine has finished loading the map and stabilized. Freezes the game so it
     * cannot accumulate ticks (or diverge) while the peer is still loading, then announces readiness.
     */
    public static void setAndCommunicateMultiplayerReady () {
        isReadyForMultiplayerThisMachine = true;
        if(RTSGame.game != null) RTSGame.game.setPaused(true);
        System.out.println("[SYNC] Loaded and paused, waiting for peer");
        sendMessage("readyPhase1");
        if(isServer && isReadyForMultiplayerOtherMachine) beginSynchronizedStart();
    }

    /**
     * Server-only. Both sides are loaded and paused, so pick the start moment and tell the client.
     * Only the server initiates so that crossing readyPhase1 messages cannot produce two competing
     * start times.
     */
    public static void beginSynchronizedStart () {
        // Reset random seed first so the client applies it before the countdown elapses.
        long seed = (long) (Math.random() * 999999999);
        Main.setRandomSeed(seed);
        sendMessage("randomSeed:" + seed);
        sendMessage("mpStartIn:" + START_COUNTDOWN_MS);
        // The client's countdown starts when the message lands, one-way latency later than ours, so
        // hold ourselves back by the same amount rather than starting early.
        long oneWayMs = currentPingMs > 0 ? currentPingMs / 2 : 0;
        scheduleSynchronizedStart(START_COUNTDOWN_MS + oneWayMs);
    }

    /**
     * Arms the countdown and the thread that releases the game when it elapses. Runs off the tick
     * thread because the game is paused while waiting - nothing would drive a tick-based gate.
     */
    private static void scheduleSynchronizedStart (long delayMs) {
        mpStartDeadline = System.currentTimeMillis() + delayMs;
        System.out.println("[SYNC] Synchronized start in " + delayMs + "ms");
        asyncService.submit(() -> {
            while(isWaitingForMpStart()) {
                Main.wait(5);
            }
            mpStartDeadline = -1;
            Game g = RTSGame.game;
            // Anchor rate control to the rate the simulation is written against. Reading
            // Main.ticksPerSecond instead would risk latching a value rate control had itself moved.
            baseTicksPerSecond = RTSGame.desiredTPS;
            Main.ticksPerSecond = baseTicksPerSecond;
            partnerTickAtLastHeartbeat = -1; // stale against the tick numbering we are about to use
            if(!isMpStarted) {
                // First start only: both sides agree this instant is tick 0.
                g.handler.globalTickNumber = 0;
                isMpStarted = true;
                System.out.println("[SYNC] Match starting at tick 0");
            } else {
                System.out.println("[SYNC] Resuming after resync at tick " + g.handler.globalTickNumber);
            }
            // The tick counter just changed meaning; any state strings keyed to the old numbering
            // would compare unrelated ticks against each other.
            resetDeterminismCheckState();
            lastResyncCompletedTick = g.handler.globalTickNumber;
            isResyncing = false;
            resyncRequested.set(false);
            g.setPaused(false);
            return null;
        });
    }

    public static boolean isWaitingForMpStart() {
        return mpStartDeadline > 0 && mpStartDeadline > System.currentTimeMillis();
    };

    public static boolean isMPReadyForCommands() {
        if(!isMultiplayer) return true;
        if(!isMpStarted || isResyncing) return false;
        if(!isReadyForMultiplayerOtherMachine || !isReadyForMultiplayerThisMachine) return false;
        return Math.abs(tickTimingOffset) < RTSInput.getInputDelay() - 5;
    }

    /**
     * Reports a peer command that arrived after its scheduled execute tick. Lateness means our clock
     * has run past the peer's, which the rate controller can correct; it is not on its own evidence
     * that the simulations diverged.
     */
    public static void reportLateCommand(long ticksLate) {
        lastLateCommandTick = RTSGame.game != null ? RTSGame.game.handler.globalTickNumber : 0;
        worstRecentCommandLateness = Math.max(worstRecentCommandLateness, ticksLate);
        raiseInputDelayFor(ticksLate);
    }

    /**
     * Worst command lateness seen since the last resync, in ticks, or 0 if none. Late commands are
     * the leading indicator of drift - they show up well before the state check notices anything.
     */
    public static long getWorstRecentCommandLateness() {
        // Decay the reading once commands have been arriving on time again for a while.
        if(RTSGame.game != null && RTSGame.game.handler.globalTickNumber - lastLateCommandTick > DETERMINISM_CHECK_INTERVAL) {
            worstRecentCommandLateness = 0;
        }
        return worstRecentCommandLateness;
    }

    private static String getResyncPath() {return "saves/mp_resync_" + (isServer ? "server" : "client") + ".dat";}
    
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
            startPingLoop();
            startHeartbeatLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Measures round-trip latency from the moment the peers connect, independently of the game loop.
     * The synchronized-start countdown needs a latency estimate before the first tick has run, and
     * the estimate has to stay fresh while a resync has the game paused.
     */
    private static void startPingLoop() {
        Thread pinger = new Thread(() -> {
            while (socket != null && !socket.isClosed()) {
                if (pendingPingSentAt == -1) {
                    pendingPingSentAt = System.currentTimeMillis();
                    sendMessage("ping:" + pendingPingSentAt);
                }
                Main.wait(PING_INTERVAL_MS);
                // A pong that never arrived should not block the next probe forever.
                pendingPingSentAt = -1;
            }
        }, "mp-ping");
        pinger.setDaemon(true);
        pinger.start();
    }

    /**
     * Reports our tick number on a fixed wall-clock cadence, independent of the game loop.
     *
     * Sending this from the tick thread made the heartbeat report the tick thread's health rather
     * than our tick number: a machine that stalled - a lag spike, a long frame, or the tick barrier
     * itself - also stopped reporting, so its peer kept measuring against a frozen value and stalled
     * in turn. Two machines could then hold each other still, each waiting on a number the other had
     * stopped sending. Off the tick thread, a stalled machine still says exactly where it is, so the
     * peer's barrier releases the moment it should and the offset the rate controller acts on stays
     * real.
     */
    private static void startHeartbeatLoop() {
        Thread heartbeat = new Thread(() -> {
            while (socket != null && !socket.isClosed()) {
                Game g = RTSGame.game;
                if (isMpStarted && !isResyncing && g != null) {
                    sendMessage("tickHeartbeat:" + g.handler.globalTickNumber);
                }
                Main.wait(TICK_HEARTBEAT_INTERVAL_MS);
            }
        }, "mp-heartbeat");
        heartbeat.setDaemon(true);
        heartbeat.start();
    }

    public static Consumer<Game> handleSyncTick = game -> {
        long currentTick = game.handler.globalTickNumber;

        // Synchronization only has meaning once both sides agree on the tick numbering. Before the
        // synchronized start each machine is free-running through its own load, so its tick counter
        // and object population are unrelated to the peer's - comparing them there guarantees a
        // spurious desync report.
        if(isMultiplayer && isMpStarted && !isResyncing && currentTick > 0) {
            // A gap this large will not close at any sane tick rate, and usually means a tick counter
            // moved rather than that the machines drifted apart. Rebuild from a snapshot instead.
            if(Math.abs(tickTimingOffset) > LARGE_OFFSET_RESYNC_THRESHOLD
                    && currentTick - lastResyncCompletedTick >= DETERMINISM_GRACE_PERIOD) {
                System.out.println("[SYNC] Offset of " + String.format("%.0f", tickTimingOffset)
                        + " ticks is too large to correct by rate control - resyncing");
                Main.ticksPerSecond = baseTicksPerSecond;
                requestResync(true);
                return;
            }

            applyRateControl();
            enforceTickBarrier(game, currentTick);

            // Periodic determinism check every 5 seconds
            if(currentTick > 0 && currentTick % DETERMINISM_CHECK_INTERVAL == 0 && currentTick != lastDeterminismCheckTick) {
                lastDeterminismCheckTick = currentTick;

                // Skip checks during grace period after resync
                if(currentTick - lastResyncCompletedTick < DETERMINISM_GRACE_PERIOD) {
                    System.out.println("[DETERMINISM] Skipping check at tick " + currentTick + " (grace period: " + (DETERMINISM_GRACE_PERIOD - (currentTick - lastResyncCompletedTick)) + " ticks remaining)");
                } else {
                    runDeterminismCheck(currentTick);
                }
            }

            // Check if we're synchronized enough to decrease input delay
            if(currentInputDelay > getTargetInputDelay()) {
                // If tick offset is small (within 6 ticks) and stable
                if(Math.abs(tickTimingOffset) <= 6.0) {
                    if(!readyToDecreaseDelay) {
                        readyToDecreaseDelay = true;
                        sendMessage("readyToDecrease");
                        System.out.println("[SYNC] Ready to decrease input delay (current: " + currentInputDelay + ", offset: " + String.format("%.1f", tickTimingOffset) + ")");
                    }

                    // If both sides ready, decrease together
                    if(partnerReadyToDecreaseDelay) {
                        currentInputDelay = Math.max(currentInputDelay - 1, getTargetInputDelay());
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

    /**
     * Nudges this machine's tick rate toward the peer's. Speeds up when behind and slows down when
     * ahead, so two machines of different capability meet in the middle instead of the weaker one
     * being asked to make up the whole gap on its own.
     */
    private static void applyRateControl() {
        double offset = tickTimingOffset;
        int targetTPS = baseTicksPerSecond;

        if(offset < -RATE_CONTROL_DEADBAND) {
            // Behind the partner - run hotter, up to the speedup ceiling.
            double maxBoost = baseTicksPerSecond * (MAX_SPEEDUP_RATIO - 1.0);
            double boost = Math.min((-offset - RATE_CONTROL_DEADBAND) * TPS_PER_TICK_OF_OFFSET, maxBoost);
            targetTPS = (int) Math.round(baseTicksPerSecond + boost);
        } else if(offset > RATE_CONTROL_DEADBAND) {
            // Ahead of the partner - ease off so they can close the gap.
            double maxCut = baseTicksPerSecond * (1.0 - MAX_SLOWDOWN_RATIO);
            double cut = Math.min((offset - RATE_CONTROL_DEADBAND) * TPS_PER_TICK_OF_OFFSET, maxCut);
            targetTPS = (int) Math.round(baseTicksPerSecond - cut);
        }

        if(Main.ticksPerSecond != targetTPS) {
            Main.ticksPerSecond = targetTPS;
        }
    }

    /**
     * How far this machine may run ahead of the peer's last reported tick.
     *
     * A command the peer stamps at their tick T executes at T + inputDelay. It reaches us one-way
     * latency later, by which point we may have advanced by our lead. For it to still be in our
     * future we need lead + latency < inputDelay, so the lead budget is whatever the input delay has
     * left over once latency is paid for.
     */
    private static int getMaxTickLead() {
        int budget = (int) (currentInputDelay - getLatencyInTicks() - LEAD_SAFETY_MARGIN);
        return Math.max(MIN_TICK_LEAD, budget);
    }

    /** One-way latency expressed in ticks, from the smoothed round-trip measurement. */
    private static double getLatencyInTicks() {
        return currentPingMs > 0 ? (currentPingMs / 2.0) * RTSGame.desiredTPS / 1000.0 : 0;
    }

    /**
     * Input delay this connection should settle at. Latency has to be paid for out of the delay
     * before any lead budget is left over, so a delay that is fixed regardless of ping starves the
     * barrier on slower connections - it ends up stalling every tick and still letting commands land
     * late. Sizing the delay from the measured ping keeps the lead budget constant instead.
     */
    private static int getTargetInputDelay() {
        int target = (int) Math.ceil(getLatencyInTicks()) + DESIRED_LEAD_BUDGET + LEAD_SAFETY_MARGIN;
        return Math.min(Math.max(MIN_INPUT_DELAY, target), MAX_INPUT_DELAY);
    }

    /**
     * Blocks the tick thread while this machine is further ahead of the peer than the input delay can
     * absorb. Compares against the peer's last reported tick without extrapolating, so the bound
     * holds even if they stall outright - their true tick is never below what they last reported.
     */
    private static void enforceTickBarrier(Game game, long currentTick) {
        if(partnerTickAtLastHeartbeat < 0) return; // no heartbeat yet, nothing to measure against
        int maxLead = getMaxTickLead();
        if(currentTick - partnerTickAtLastHeartbeat <= maxLead) return;

        long stallStart = System.currentTimeMillis();
        while(currentTick - partnerTickAtLastHeartbeat > maxLead) {
            long now = System.currentTimeMillis();
            if(now - stallStart > MAX_STALL_MS) {
                System.out.println("[SYNC] Tick barrier gave up after " + MAX_STALL_MS + "ms (we're at "
                        + currentTick + ", partner last reported " + partnerTickAtLastHeartbeat + ")");
                return;
            }
            // A peer who is behind but alive keeps reporting, and waiting for them is the point of
            // the barrier. A peer who has gone silent is stalled, and holding here would only stall
            // us alongside them for no gain.
            if(now - lastHeartbeatReceivedTime > PARTNER_SILENCE_TIMEOUT_MS) {
                System.out.println("[SYNC] Partner silent for " + (now - lastHeartbeatReceivedTime)
                        + "ms - running on rather than stalling with them");
                return;
            }
            if(isResyncing) return; // resync coordination needs this thread back
            parkReleasingGameLock(game);
        }
        long stalled = System.currentTimeMillis() - stallStart;
        if(stalled > 20) {
            System.out.println("[SYNC] Held " + stalled + "ms at tick " + currentTick + " waiting for partner");
        }
    }

    /**
     * Waits ~1ms without holding the game's monitor.
     *
     * The barrier runs inside Game.tick(), which is synchronized, so sleeping here keeps that
     * monitor held. The socket listener thread needs it - beginResync reaches addTickDelayedEffect,
     * which is synchronized on the game - and that listener thread is the only thing that advances
     * partnerTickAtLastHeartbeat. Sleeping with the monitor held therefore blocks the very update
     * the loop is waiting for, and the two threads hold each other until MAX_STALL_MS expires, every
     * tick. Object.wait releases the monitor for the duration (including reentrant holds) and
     * reacquires it on wake, so messages keep flowing while we are parked.
     *
     * The tick's work is already finished by the time the barrier runs, so the window this opens is
     * the same one that exists between ticks.
     */
    private static void parkReleasingGameLock(Game game) {
        synchronized (game) {
            try {
                game.wait(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Raises the input delay far enough that a command this late would have arrived in time, and asks
     * the peer to do the same. Latency that outgrows the delay is the one condition the tick barrier
     * cannot fix on its own, since the delay is what funds the lead budget.
     */
    private static void raiseInputDelayFor(long ticksLate) {
        int proposed = (int) Math.min(currentInputDelay + ticksLate + LEAD_SAFETY_MARGIN, MAX_INPUT_DELAY);
        if(proposed <= currentInputDelay) return;
        currentInputDelay = proposed;
        sustainedInputDelayFloor = Math.max(sustainedInputDelayFloor, proposed);
        readyToDecreaseDelay = false;
        partnerReadyToDecreaseDelay = false;
        sendMessage("raiseDelay:" + proposed);
        System.out.println("[SYNC] Raised input delay to " + proposed + " after a command arrived " + ticksLate + " ticks late");
    }

    /**
     * Builds this machine's state string for the given tick, ships it to the peer, and compares
     * against the peer's string for the same tick if it has already arrived.
     */
    private static void runDeterminismCheck(long currentTick) {
        // Generate state string from all units
        String ourStateString = generateGameStateString();

        // Store our state string for this tick (for later comparison when partner's state arrives)
        ourStateStrings.put(currentTick, ourStateString);

        // Send to partner with tick number
        sendMessage("stateCheck:" + currentTick + ":" + Base64.getEncoder().encodeToString(ourStateString.getBytes()));

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
                    System.out.println("beginning resync from our own state check");
                    requestResync(true);
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

    /**
     * Drops every stored state string and the last-checked marker. Required whenever globalTickNumber
     * is reassigned, since the stored strings are keyed by tick number and would otherwise be
     * compared against an unrelated tick that happens to reuse the same number.
     */
    private static void resetDeterminismCheckState() {
        lastDeterminismCheckTick = 0;
        partnerStateStrings.clear();
        ourStateStrings.clear();
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
            requestResync(false);
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
        if (s.startsWith("pt:")) {
            if(isResyncing) {
                System.out.println("Dropping preferred target command during resync");
                return;
            }
            SetPreferredTargetCommand cmd = SetPreferredTargetCommand.generateFromMpString(s);
            RTSGame.commandHandler.addCommand(cmd, false);
            updateTickTimingOffset(cmd.getExecuteTick());
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
        
        if(s.startsWith("ta:")) {
            if(isResyncing) {
                System.out.println("Dropping trigger ability command during resync");
                return;
            }
            System.out.println("message " + s);
            TriggerAbilityCommand cmd = TriggerAbilityCommand.generateFromMpString(s);
            RTSGame.commandHandler.addCommand(cmd, false);
            updateTickTimingOffset(cmd.getExecuteTick());
        }

        if(s.startsWith("bt:")) {
            if(isResyncing) {
                System.out.println("Dropping board transport command during resync");
                return;
            }
            System.out.println("message " + s);
            GameDemo.RTSDemo.Commands.BoardTransportCommand cmd = GameDemo.RTSDemo.Commands.BoardTransportCommand.generateFromMpString(s);
            RTSGame.commandHandler.addCommand(cmd, false);
            updateTickTimingOffset(cmd.getExecuteTick());
        }

        if(s.startsWith("rf:")) {
            if(isResyncing) {
                System.out.println("Dropping reinforcement command during resync");
                return;
            }
            System.out.println("message " + s);
            GameDemo.RTSDemo.Commands.CallReinforcementCommand cmd = GameDemo.RTSDemo.Commands.CallReinforcementCommand.generateFromMpString(s);
            RTSGame.commandHandler.addCommand(cmd, false);
            updateTickTimingOffset(cmd.getExecuteTick());
        }

        if(s.startsWith("readyPhase1")) {
            isReadyForMultiplayerOtherMachine = true;
            // Only the server picks the start moment, so readyPhase1 messages crossing in flight
            // cannot produce two competing start times.
            if(isServer && isReadyForMultiplayerThisMachine) beginSynchronizedStart();
        }

        // Relative countdown rather than an absolute timestamp: the two machines' wall clocks may
        // disagree by seconds, which would make one side skip the wait entirely.
        if(s.startsWith("mpStartIn:")) {
            long delayMs = Long.parseLong(s.substring("mpStartIn:".length()));
            scheduleSynchronizedStart(delayMs);
        }

        // Save file transfer messages
        if(s.startsWith("saveFileStart:")) {
            String[] parts = s.split(":");
            expectedSaveFileSize = Integer.parseInt(parts[1]);
            expectedChunks = Integer.parseInt(parts[2]);
            expectedChecksum = parts[3]; // SHA-256 checksum
            receivedChunks = 0;
            saveFileChunks = new String[expectedChunks];
            System.out.println("Receiving save file: " + expectedSaveFileSize + " bytes in " + expectedChunks + " chunks");
            System.out.println("Expected checksum: " + expectedChecksum);
        }

        if(s.startsWith("saveFileChunk:")) {
            String[] parts = s.split(":", 3);
            int chunkIndex = Integer.parseInt(parts[1]);
            String chunkData = parts[2];
            // Store by index rather than appending in arrival order, so reassembly does not depend
            // on the chunks reaching us in the order they were sent.
            if(saveFileChunks != null && chunkIndex >= 0 && chunkIndex < saveFileChunks.length) {
                if(saveFileChunks[chunkIndex] == null) receivedChunks++;
                saveFileChunks[chunkIndex] = chunkData;
            } else {
                System.err.println("Received save chunk " + chunkIndex + " outside the expected range");
            }
            if(receivedChunks % 10 == 0) {
                System.out.println("Received chunk " + receivedChunks + "/" + expectedChunks);
            }
        }

        if(s.equals("saveFileEnd")) {
            try {
                // Verify every chunk arrived before reassembling
                StringBuilder assembled = new StringBuilder();
                for(int i = 0; i < expectedChunks; i++) {
                    if(saveFileChunks == null || saveFileChunks[i] == null) {
                        String errorMsg = "Save file transfer incomplete - missing chunk " + i + " of " + expectedChunks;
                        System.err.println(errorMsg);
                        sendMessage("loadFailed:" + errorMsg);
                        abortResync();
                        saveFileChunks = null;
                        return;
                    }
                    assembled.append(saveFileChunks[i]);
                }

                // Decode Base64 and write to file
                String encodedData = assembled.toString();
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
                    abortResync();
                    saveFileChunks = null;
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
                saveFileChunks = null;
            } catch (Exception e) {
                System.err.println("Error processing received save file: " + e.getMessage());
                e.printStackTrace();
                // Notify server of failure
                sendMessage("loadFailed:" + e.getMessage());
                abortResync();
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
            abortResync();
            clientLoadComplete = true; // Set to true to break server's wait loop
        }

        // Tick heartbeat for continuous synchronization
        if(s.startsWith("tickHeartbeat:") && isMpStarted) {
            long partnerTickAtSend = Long.parseLong(s.substring(14));
            long ourTick = RTSGame.game.handler.globalTickNumber;
            lastHeartbeatReceivedTime = System.currentTimeMillis();

            // Feed the tick barrier. Monotonic: a reordered or delayed heartbeat must not walk the
            // bound backwards and stall us against a tick the peer has already passed.
            if(partnerTickAtSend > partnerTickAtLastHeartbeat) {
                partnerTickAtLastHeartbeat = partnerTickAtSend;
            }

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
                        requestResync(true);
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

        if(s.startsWith("raiseDelay:")) {
            int newDelay = Integer.parseInt(s.substring("raiseDelay:".length()));
            if(newDelay > currentInputDelay) {
                currentInputDelay = newDelay;
                sustainedInputDelayFloor = Math.max(sustainedInputDelayFloor, newDelay);
                readyToDecreaseDelay = false;
                partnerReadyToDecreaseDelay = false;
                System.out.println("Partner raised input delay to " + currentInputDelay);
            }
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
            // Single sender thread: messages leave in the order they were queued.
            senderService.submit(() -> {
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
    
    
    /**
     * The only supported way to start a resync.
     *
     * beginResync is synchronized and reaches Game.addTickDelayedEffect, which is synchronized on
     * the game. Every caller reaches it while already holding a lock the tick thread needs:
     * CommandHandler.addCommand holds the command handler's monitor, and handleSyncTick runs inside
     * the synchronized Game.tick(). That is a lock-order inversion in both directions - the tick
     * thread holds the game monitor and blocks entering CommandHandler.tick(), while the listener
     * thread holds the command handler and blocks on the game monitor - and it deadlocks the tick
     * thread outright, with the socket and AWT threads still alive so the process looks merely
     * frozen rather than hung.
     *
     * Running the resync on its own thread means it holds none of the caller's locks, so no ordering
     * between them exists to invert.
     */
    public static void requestResync(boolean initiator) {
        // One in-flight request at a time; without this the tick thread can queue a fresh request
        // every tick during the hand-off, since isResyncing is not set until beginResync runs.
        if(!resyncRequested.compareAndSet(false, true)) return;
        asyncService.submit(() -> {
            try {
                beginResync(initiator);
            } finally {
                // beginResync declines when a resync is already running or the match has not
                // started. In the decline case nothing will later clear the gate, so clear it here.
                if(!isResyncing) resyncRequested.set(false);
            }
            return null;
        });
    }

    private static synchronized void beginResync(boolean initiator) {
        if(isResyncing) return;
        if(!isMpStarted) {
            // Before the synchronized start the two games are still loading and are paused or
            // free-running independently; there is no shared state worth reconciling, and the save
            // handshake relies on ticks that a paused game will not produce.
            System.out.println("Ignoring resync request - match has not started yet");
            return;
        }
        resetDeterminismCheckState();
        RTSGame.commandHandler.printCommandHistory();
        isResyncing = true;
        System.out.println("beginResync " + initiator);
        if(initiator) sendMessage("beginResync");

        // Reset adaptive synchronization to initial state
        resetAdaptiveSync();

        // Clear any pending operations
        mpStartDeadline = -1;
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
                        abortResync();
                        return null;
                    }

                    System.out.println("Client confirmed save received, both machines loading now...");
                    // Clear before loading: the client's post-load confirmation can land while
                    // loadResyncSaveFile is still returning, and clearing afterwards would swallow it.
                    clientLoadComplete = false;
                    loadResyncSaveFile();

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
                    long waitStart = System.currentTimeMillis();
                    while(!saveFileReceived) {
                        // Without a ceiling, a server that stalls mid-transfer leaves us here
                        // forever with isResyncing set - which drops every incoming command, so the
                        // match is over even though both processes are still running.
                        if(System.currentTimeMillis() - waitStart > RESYNC_HANDSHAKE_TIMEOUT_MS) {
                            System.err.println("Client timed out waiting for the resync save file - aborting resync");
                            sendMessage("loadFailed:timed out waiting for the resync save file");
                            abortResync();
                            return null;
                        }
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
                    abortResync();
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
            // The follow-up work runs through loadGameState's completion callback rather than a
            // tick-delayed effect of our own. Loading reassigns globalTickNumber from the snapshot,
            // so a target tick computed here can land in the future once the counter moves back -
            // leaving this machine unpaused through the restart handshake.
            SerializationManager.loadGameState(RTSGame.game, getResyncPath(), g -> {
                System.out.println("Resync load complete at tick " + g.handler.globalTickNumber + ", pausing for coordination");

                // NOW pause after loading is complete
                g.setPaused(true);
                resetDeterminismCheckState();

                if(!isServer) {
                    // Client signals completion; the server answers with the restart countdown,
                    // which scheduleSynchronizedStart picks up and uses to unpause.
                    sendMessage("loadComplete");
                } else {
                    // Server waits for client to finish, then coordinates restart
                    asyncService.submit(() -> {
                        System.out.println("Server waiting for client load completion...");
                        long waitStart = System.currentTimeMillis();
                        while(!clientLoadComplete) {
                            // The game is paused for this wait, so an unbounded one is a hang.
                            if(System.currentTimeMillis() - waitStart > RESYNC_HANDSHAKE_TIMEOUT_MS) {
                                System.err.println("Server timed out waiting for client load completion - aborting resync");
                                abortResync();
                                return null;
                            }
                            Main.wait(100);
                        }
                        System.out.println("Both sides loaded and paused, coordinating restart...");
                        clientLoadComplete = false; // Reset for next resync
                        beginSynchronizedStart();
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

        // Commands arrive sporadically and carry the sender's queueing delay, so this is a much
        // noisier estimator than the 200ms heartbeat. Give it a small weight so it corroborates the
        // heartbeat rather than yanking the value the rate controller is acting on.
        tickTimingOffset = tickTimingOffset * 0.85 + offset * 0.15;

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
        currentInputDelay = Math.max(INITIAL_INPUT_DELAY, sustainedInputDelayFloor);
        // Tick numbers change across a resync, so a retained partner tick would be measured against
        // a different numbering and could stall the barrier against a tick already passed.
        partnerTickAtLastHeartbeat = -1;
        tickTimingOffset = 0.0;
        readyToDecreaseDelay = false;
        partnerReadyToDecreaseDelay = false;
        // Treat the peer as live as of now; a stale value would read as silence and disarm the
        // barrier for the first stretch after the resync, exactly when the lead bound matters most.
        lastHeartbeatReceivedTime = System.currentTimeMillis();
        pendingPingSentAt = -1;
        worstRecentCommandLateness = 0;

        // Reset determinism check state
        resetDeterminismCheckState();

        // Restore base TPS (in case rate control had it above or below normal)
        if(Main.ticksPerSecond != baseTicksPerSecond) {
            System.out.println("[SYNC] Restoring TPS to " + baseTicksPerSecond + " (was " + Main.ticksPerSecond + ")");
            Main.ticksPerSecond = baseTicksPerSecond;
        }

        System.out.println("[SYNC] Reset adaptive sync - input delay: " + currentInputDelay);
    }

    /**
     * Unwinds a resync that cannot finish. Clearing the start deadline matters as much as clearing
     * the resyncing flag: a stale future deadline leaves the synchronized-start gate armed, and the
     * next machine to reach it would jump its tick counter while its peer stays put.
     */
    private static void abortResync() {
        isResyncing = false;
        resyncRequested.set(false);
        mpStartDeadline = -1;
        waitingForSaveFile = false;
        saveFileReceived = false;
        if(RTSGame.game != null && RTSGame.game.isPaused()) {
            RTSGame.game.setPaused(false);
        }
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

package GameDemo.RTSDemo.Replay;

import Framework.Main;
import GameDemo.RTSDemo.Commands.BoardTransportCommand;
import GameDemo.RTSDemo.Commands.CallReinforcementCommand;
import GameDemo.RTSDemo.Commands.Command;
import GameDemo.RTSDemo.Commands.MoveCommand;
import GameDemo.RTSDemo.Commands.SetPreferredTargetCommand;
import GameDemo.RTSDemo.Commands.StopCommand;
import GameDemo.RTSDemo.Commands.TriggerAbilityCommand;
import GameDemo.RTSDemo.MapEditor.MapData;
import GameDemo.RTSDemo.MapEditor.MapSerializer;
import GameDemo.RTSDemo.RTSGame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Records the deterministic lockstep command stream plus map/metadata into a portable
 * {@code .replay} file, and parses those files back for playback. Because the RTS simulation is
 * deterministic (same map + same random seed + same commands at the same ticks reproduce the same
 * game), the command log is a complete replay.
 *
 * @author guydu
 */
public class ReplayManager {

    /** True while a replay is being played back. Gates player-issued commands (see CommandHandler). */
    public static volatile boolean isReplayMode = false;

    /** The map the currently live game was built from, needed to embed the world when saving a replay. */
    public static MapData currentMapData = null;
    public static String currentMapName = null;

    private static final String HEADER = "JENGINE_RTS_REPLAY v1";
    private static final String REPLAY_DIR = "replays";

    /** Records which map the live game was built from so a replay saved later can embed it. */
    public static void setCurrentMap(MapData data, String name) {
        currentMapData = data;
        currentMapName = name;
    }

    /**
     * Writes the current command log plus map and metadata to {@code replays/<timestamp>.replay}.
     * @return the written file, or {@code null} if there was no map-loaded game to save.
     */
    public static File saveReplay() {
        if (currentMapData == null) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No map-loaded game to record. Replays are only available for games started from a map.",
                    "Cannot Save Replay", javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (RTSGame.commandHandler == null) {
            System.err.println("Cannot save replay: no command handler");
            return null;
        }
        try {
            File dir = new File(REPLAY_DIR);
            if (!dir.exists()) dir.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            File out = new File(dir, timestamp + ".replay");

            StringBuilder sb = new StringBuilder();
            sb.append(HEADER).append("\n");
            sb.append("seed=").append(Main.seed).append("\n");
            sb.append("tps=").append(RTSGame.desiredTPS).append("\n");
            sb.append("mapName=").append(currentMapName == null ? "" : currentMapName).append("\n");
            sb.append("BEGIN_MAP\n");
            sb.append(MapSerializer.toJson(currentMapData)).append("\n");
            sb.append("END_MAP\n");
            sb.append("BEGIN_COMMANDS\n");
            for (Command c : RTSGame.commandHandler.getAllCommands()) {
                sb.append(c.toMpString()).append("\n");
            }
            sb.append("END_COMMANDS\n");

            try (FileWriter fw = new FileWriter(out)) {
                fw.write(sb.toString());
            }
            System.out.println("Replay saved to " + out.getAbsolutePath());
            return out;
        } catch (Exception e) {
            System.err.println("Error saving replay: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** Parses a {@code .replay} file into its metadata, embedded map, and command strings. */
    public static ParsedReplay parseReplayFile(File file) throws IOException {
        ParsedReplay result = new ParsedReplay();
        StringBuilder mapJson = new StringBuilder();
        List<String> commandStrings = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inMap = false;
            boolean inCommands = false;
            while ((line = br.readLine()) != null) {
                if (line.equals("BEGIN_MAP")) { inMap = true; continue; }
                if (line.equals("END_MAP")) { inMap = false; continue; }
                if (line.equals("BEGIN_COMMANDS")) { inCommands = true; continue; }
                if (line.equals("END_COMMANDS")) { inCommands = false; continue; }

                if (inMap) {
                    mapJson.append(line).append("\n");
                } else if (inCommands) {
                    if (!line.isBlank()) commandStrings.add(line);
                } else if (line.startsWith("seed=")) {
                    result.seed = Long.parseLong(line.substring("seed=".length()).trim());
                } else if (line.startsWith("tps=")) {
                    result.tps = Integer.parseInt(line.substring("tps=".length()).trim());
                } else if (line.startsWith("mapName=")) {
                    result.mapName = line.substring("mapName=".length()).trim();
                }
            }
        }

        result.mapData = MapSerializer.fromJson(mapJson.toString());
        result.commandStrings = commandStrings;
        return result;
    }

    /**
     * Reconstructs a {@link Command} from its {@code toMpString()} form, dispatching on the tag
     * before the first colon. Mirrors the inline dispatch in ExternalCommunicator.interperateMessage.
     */
    public static Command parseCommand(String mp) {
        int colon = mp.indexOf(':');
        if (colon < 0) return null;
        String tag = mp.substring(0, colon);
        switch (tag) {
            case "m":  return MoveCommand.generateFromMpString(mp);
            case "s":  return StopCommand.generateFromMpString(mp);
            case "pt": return SetPreferredTargetCommand.generateFromMpString(mp);
            case "ta": return TriggerAbilityCommand.generateFromMpString(mp);
            case "bt": return BoardTransportCommand.generateFromMpString(mp);
            case "rf": return CallReinforcementCommand.generateFromMpString(mp);
            default:
                System.err.println("ReplayManager: unknown command tag '" + tag + "' in '" + mp + "'");
                return null;
        }
    }

    /** Parsed contents of a replay file. */
    public static class ParsedReplay {
        public long seed;
        public int tps = 90;
        public String mapName = "";
        public MapData mapData;
        public List<String> commandStrings = new ArrayList<>();
    }
}

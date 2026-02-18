package GameDemo.RTSDemo.DeterminismTests;

/**
 * Utility to format raw commands into Java code for determinism tests.
 *
 * Usage:
 * 1. Run this class
 * 2. Paste your raw commands (one per line) into the rawCommands string
 * 3. Run again to get formatted output
 * 4. Copy the output and paste into your test's populateCommands() method
 *
 * @author guydu
 */
public class CommandFormatter {

    public static void main(String[] args) {
        // PASTE YOUR RAW COMMANDS HERE (one per line)
        String rawCommands = """
                        message m:Hellicopter_T0_9,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_21,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_24,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_1,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_12,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_8,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_23,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_14,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_16,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_5,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_2,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_13,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_6,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_17,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_10,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_19,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_11,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_4,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_18,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_15,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_22,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_3,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_7,1042,2941,2792,65FR80JV4
                        message m:Hellicopter_T0_20,1042,2941,2792,65FR80JV4
""";

        formatCommands(rawCommands);
    }

    /**
     * Formats raw commands into Java code
     */
    public static void formatCommands(String rawCommands) {
        if (rawCommands == null || rawCommands.trim().isEmpty()) {
            System.out.println("No commands provided. Paste your commands in the rawCommands string.");
            return;
        }

        String[] lines = rawCommands.split("\n");
        int validCount = 0;

        System.out.println("// Formatted commands - copy and paste into populateCommands():");
        System.out.println();

        for (String line : lines) {
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            // Output formatted line
            System.out.println("        ExternalCommunicator.interperateMessage(\"" + line + "\");");
            validCount++;
        }

        System.out.println();
        System.out.println("// Total: " + validCount + " commands formatted");
    }

    /**
     * Alternative method if you want to format from an array of strings
     */
    public static void formatCommandsFromArray(String[] commands) {
        System.out.println("// Formatted commands - copy and paste into populateCommands():");
        System.out.println();

        for (String command : commands) {
            if (command != null && !command.trim().isEmpty()) {
                System.out.println("        ExternalCommunicator.interperateMessage(\"" + command.trim() + "\");");
            }
        }

        System.out.println();
        System.out.println("// Total: " + commands.length + " commands formatted");
    }
}

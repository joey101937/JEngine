package Framework;

import Framework.CoreLoop.Handler;
import java.io.*;
import java.util.ArrayList;

/**
 * Utility class for serializing and deserializing game state
 * Provides methods to save and load game state to/from disk
 *
 * @author JEngine
 */
public class SerializationManager {

    /**
     * Represents a saved game state snapshot
     */
    public static class GameStateSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        public ArrayList<GameObject2> gameObjects;
        public long globalTickNumber;
        public ArrayList<TickDelayedEffect> tickDelayedEffects;
        public ArrayList<TimeTriggeredEffect> timeTriggeredEffects;

        public GameStateSnapshot(Handler handler) {
            this.gameObjects = new ArrayList<>(handler.getAllObjects());
            this.globalTickNumber = handler.globalTickNumber;
            // Effects would need to be accessible - for now we'll skip them
            // as they contain lambda functions which are not easily serializable
            this.tickDelayedEffects = new ArrayList<>();
            this.timeTriggeredEffects = new ArrayList<>();
        }
    }

    /**
     * Saves the current game state to a file
     * Uses tick-delayed effect to ensure save happens between ticks
     *
     * @param game The game instance to save
     * @param filePath The path where the save file should be written
     */
    public static void saveGameState(Game game, String filePath) {
        // Schedule save to happen after next tick completes
        game.addTickDelayedEffect(1, g -> {
            try {
                GameStateSnapshot snapshot = new GameStateSnapshot(game.handler);

                try (FileOutputStream fileOut = new FileOutputStream(filePath);
                     ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                    out.writeObject(snapshot);
                    System.out.println("Game state saved to: " + filePath);
                    System.out.println("Saved " + snapshot.gameObjects.size() + " objects at tick " + snapshot.globalTickNumber);
                }
            } catch (IOException e) {
                System.err.println("Error saving game state: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Loads a game state from a file and applies it to the current game
     * Uses tick-delayed effects to ensure operations happen between ticks
     *
     * @param game The game instance to load state into
     * @param filePath The path to the save file
     */
    public static void loadGameState(Game game, String filePath) {
        try {
            // Load snapshot from disk
            GameStateSnapshot snapshot;
            try (FileInputStream fileIn = new FileInputStream(filePath);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                snapshot = (GameStateSnapshot) in.readObject();
            }

            // Schedule removal of existing objects after next tick
            game.addTickDelayedEffect(1, g -> {
                ArrayList<GameObject2> existingObjects = new ArrayList<>(game.getAllObjects());
                for (GameObject2 obj : existingObjects) {
                    game.removeObject(obj);
                }

                // After removals complete (next tick), add loaded objects
                game.addTickDelayedEffect(1, g2 -> {
                    // Add loaded objects
                    for (GameObject2 obj : snapshot.gameObjects) {
                        game.addObject(obj);
                    }

                    // After additions complete (next tick), finalize
                    game.addTickDelayedEffect(1, g3 -> {
                        // Restore tick number
                        game.handler.globalTickNumber = snapshot.globalTickNumber;

                        // Reinitialize transient fields in handler
                        game.handler.reinitializeTransientFields();

                        System.out.println("Game state loaded from: " + filePath);
                        System.out.println("Loaded " + snapshot.gameObjects.size() + " objects at tick " + snapshot.globalTickNumber);
                    });
                });
            });

        } catch (IOException e) {
            System.err.println("Error loading game state: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Class not found during deserialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves game state to a default location (saves/quicksave.dat)
     *
     * @param game The game instance to save
     */
    public static void quickSave(Game game) {
        // Create saves directory if it doesn't exist
        File savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdir();
        }

        saveGameState(game, "saves/quicksave.dat");
    }

    /**
     * Loads game state from default location (saves/quicksave.dat)
     *
     * @param game The game instance to load state into
     */
    public static void quickLoad(Game game) {
        File saveFile = new File("saves/quicksave.dat");
        if (!saveFile.exists()) {
            System.err.println("No quicksave file found at: saves/quicksave.dat");
            return;
        }

        loadGameState(game, "saves/quicksave.dat");
    }
}

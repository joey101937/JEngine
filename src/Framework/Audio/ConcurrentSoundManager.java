package Framework.Audio;

import Framework.IndependentEffect;
import Framework.Main;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Manages concurrent sound effects in the game, handling multiple simultaneous sounds
 * while preventing too many instances of the same sound from playing at once.
 * <p>
 * This manager tracks sound effects, their concurrent play limits, and their durations.
 * It automatically cleans up completed sound effects and enforces maximum concurrent
 * play limits per sound effect.
 * 
 * @author guydu
 */
public class ConcurrentSoundManager extends IndependentEffect {

    private long tickNumber = 0;

    /**
     * Internal class that maintains the state and configuration for a registered sound effect.
     */
    private static class SoundEffectProfile {

        public SoundEffect soundEffect;
        public int numPlaying = 0;
        public int maxConcurrent = 100;
        public int duration = Main.ticksPerSecond;
        public ArrayList<Long> decrementTicks = new ArrayList<>();

        public SoundEffectProfile(SoundEffect se, int maxConcurrent, int duration) {
            this.soundEffect = se;
            this.maxConcurrent = maxConcurrent;
            this.duration = duration;
        }
    }

    public HashMap<String, SoundEffectProfile> effectMap = new HashMap<>();

    @Override
    public void render(Graphics2D g) {
        // No rendering needed for audio management
    }

    /**
     * Updates the sound manager state each game tick.
     * Handles cleanup of completed sound effects and updates play counts.
     */
    @Override
    public void tick() {
        tickNumber++;
        effectMap.values().forEach(x -> {
            if (x.decrementTicks.contains(tickNumber)) {
                x.decrementTicks.remove(tickNumber);
                x.numPlaying--;
            }
        });
    }

    /**
     * Registers a new sound effect with the manager.
     * 
     * @param name The unique identifier for this sound effect
     * @param se The SoundEffect instance to register
     * @param maxConcurrent Maximum number of concurrent plays allowed for this sound
     * @param tickDuration How many game ticks the sound effect should last
     */
    public void registerSoundEffect(String name, SoundEffect se, int maxConcurrent, int tickDuration) {
        effectMap.put(name, new SoundEffectProfile(
                se,
                maxConcurrent,
                tickDuration
        ));
    }

    /**
     * Plays a registered sound effect with the specified volume and start offset.
     * Will not exceed the maximum concurrent plays limit for the sound effect.
     * 
     * @param effectKey The identifier of the sound effect to play
     * @param volume The volume level to play at (0.0 to 1.0)
     * @param startOffset The number of milliseconds into the sound effect to start from
     */
    public void play(String effectKey, double volume, int startOffset) {
        SoundEffectProfile profile = effectMap.get(effectKey);
        if (profile == null) {
            System.out.println("error! Sound effect key not registered: " + effectKey);
            return;
        }

        if (profile.numPlaying >= profile.maxConcurrent) {
            return;
        }

        profile.soundEffect.playCopy(volume, startOffset);
        profile.numPlaying++;
        profile.decrementTicks.add(tickNumber + startOffset);
    }

}

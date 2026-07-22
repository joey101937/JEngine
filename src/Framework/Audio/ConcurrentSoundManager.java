package Framework.Audio;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.IndependentEffect;
import Framework.Main;
import Framework.Window;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages concurrent sound effects in the game, handling multiple simultaneous
 * sounds while preventing too many instances of the same sound from playing at
 * once.
 * <p>
 * This manager tracks sound effects, their concurrent play limits, and their
 * durations. It automatically cleans up completed sound effects and enforces
 * maximum concurrent play limits per sound effect.
 *
 * @author guydu
 */
public class ConcurrentSoundManager extends IndependentEffect {
    private static final long serialVersionUID = 1L;

    /**
     * Distance from the center of the view, measured in fractions of the viewport's
     * half-size, within which a positional sound plays at its full given volume.
     */
    public static double fullVolumeRadius = 0.5;
    /**
     * The quietest a positional sound may get, as a fraction of the volume it was
     * played at. Distance never takes a sound below this, so far off action stays
     * audible no matter how far the camera has wandered from it.
     */
    public static double minimumVolumeFraction = 0.35;
    /**
     * How far into the stereo field a sound at the edge of the view is pushed.
     * 1 = hard left/right, 0 = no stereo positioning.
     */
    public static double maxPan = 0.7;

    private long tickNumber = 0;
    public transient HashMap<String, SoundEffectProfile> effectMap = new HashMap<>();
    /**
     * Internal class that maintains the state and configuration for a
     * registered sound effect.
     */
    private static class SoundEffectProfile {

        public transient List<SoundEffect> soundEffects;
        private int numPlaying = 0;
        public int maxConcurrent = 100;
        public int duration = Main.ticksPerSecond;
        public double pitchVariation = 0;
        private final transient ArrayList<Long> decrementTicks = new ArrayList<>();

        public SoundEffectProfile(List<SoundEffect> sounds, int maxConcurrent, int duration, double pitchVariation) {
            this.soundEffects = sounds;
            this.maxConcurrent = maxConcurrent;
            this.duration = duration;
            this.pitchVariation = pitchVariation;
        }

        public synchronized void addRemoveDecrementTick(boolean isAddition, Long l) {
            if (isAddition) {
                decrementTicks.add(l);
            } else {
                decrementTicks.remove(l);
            }
        }

        public synchronized List<Long> drainExpiredTicks(long currentTick) {
            List<Long> expired = decrementTicks.stream().filter(y -> y <= currentTick).toList();
            expired.forEach(decrementTicks::remove);
            return expired;
        }

        public synchronized void updateNumPlaying(int change) {
            numPlaying += change;
        }

        public synchronized int getNumPlaying() {
            return numPlaying;
        }
    }

    @Override
    public boolean shouldSerialize() {
        // Don't serialize - this is typically a singleton managed by project code
        return false;
    }

    @Override
    public void onPostDeserialization(Framework.Game game) {
        // Not called since we don't serialize
    }

    @Override
    public void render(Graphics2D g) {
        // No rendering needed for audio management
    }

    /**
     * Updates the sound manager state each game tick. Handles cleanup of
     * completed sound effects and updates play counts.
     */
    @Override
    public void tick() {
        tickNumber++;
        effectMap.values().forEach(x -> {
            int expired = x.drainExpiredTicks(tickNumber).size();
            x.updateNumPlaying(-expired);
        });
    }

    /**
     * Registers a new sound effect with the manager.
     *
     * @param name The unique identifier for this sound effect
     * @param se The SoundEffect instance to register
     * @param maxConcurrent Maximum number of concurrent plays allowed for this
     * sound
     * @param tickDuration How many game ticks the sound effect should last
     */
    public void registerSoundEffect(String name, SoundEffect se, int maxConcurrent, int tickDuration) {
        registerSoundEffect(name, List.of(se), maxConcurrent, tickDuration);
    }

    public void registerSoundEffect(String name, SoundEffect se, int maxConcurrent, int tickDuration, double pitchVariation) {
        registerSoundEffect(name, List.of(se), maxConcurrent, tickDuration, pitchVariation);
    }

    public void registerSoundEffect(String name, List<SoundEffect> variants, int maxConcurrent, int tickDuration) {
        registerSoundEffect(name, variants, maxConcurrent, tickDuration, 0);
    }

    /**
     * Registers a sound effect whose every play is pitch shifted by a random amount,
     * so repeats of the same sound do not stack up identically.
     *
     * @param pitchVariation widest pitch shift applied per play, as a fraction of the
     * original pitch. 0.04 shifts each play by up to ±4%.
     */
    public void registerSoundEffect(String name, List<SoundEffect> variants, int maxConcurrent, int tickDuration, double pitchVariation) {
        effectMap.put(name, new SoundEffectProfile(variants, maxConcurrent, tickDuration, pitchVariation));
    }

    /**
     * Plays a registered sound effect with the specified volume and start
     * delay. Will not exceed the maximum concurrent plays limit for the sound
     * effect.
     *
     * @param effectKey The identifier of the sound effect to play
     * @param volume percentage of the sound file's natural volume, 100 = unaltered
     * @param msDelay The number of milliseconds to wait before playing
     * start from
     */
    public void play(String effectKey, double volume, int msDelay) {
        play(effectKey, volume, msDelay, 0);
    }

    /**
     * Plays a registered sound effect as though it came from a place in the world.
     * Volume falls off with distance from the center of the view and the sound is
     * positioned in the stereo field, so what you hear tracks what the camera sees.
     * Distance never silences a sound, only quietens it down to
     * {@link #minimumVolumeFraction} of the volume given here.
     *
     * @param effectKey The identifier of the sound effect to play
     * @param worldLocation where in the game world the sound comes from
     * @param volume percentage of the sound file's natural volume at point blank, 100 = unaltered
     */
    public void play(String effectKey, DCoordinate worldLocation, double volume) {
        play(effectKey, worldLocation, volume, 0);
    }

    public void play(String effectKey, Coordinate worldLocation, double volume) {
        play(effectKey, worldLocation.toDCoordinate(), volume, 0);
    }

    public void play(String effectKey, Coordinate worldLocation, double volume, int msDelay) {
        play(effectKey, worldLocation.toDCoordinate(), volume, msDelay);
    }

    public void play(String effectKey, DCoordinate worldLocation, double volume, int msDelay) {
        Rectangle view = getViewport();
        if (view == null || view.width <= 0 || view.height <= 0) {
            play(effectKey, volume, msDelay);
            return;
        }
        double halfWidth = view.width / 2.0;
        double halfHeight = view.height / 2.0;
        //offsets from the center of the view, where 1 is the edge of the screen
        double xOffset = (worldLocation.x - (view.x + halfWidth)) / halfWidth;
        double yOffset = (worldLocation.y - (view.y + halfHeight)) / halfHeight;
        double distance = Math.sqrt(xOffset * xOffset + yOffset * yOffset);
        play(effectKey, volume * attenuationAt(distance), msDelay,
                Math.max(-1, Math.min(1, xOffset)) * maxPan);
    }

    private void play(String effectKey, double volume, int msDelay, double pan) {
        SoundEffectProfile profile = effectMap.get(effectKey);
        if (profile == null) {
            System.out.println("error! Sound effect key not registered: " + effectKey);
            return;
        }

        if (profile.getNumPlaying() >= profile.maxConcurrent) {
            return;
        }

        SoundEffect chosen = profile.soundEffects.get(Main.generateRandomIntLocally(0, profile.soundEffects.size() - 1));
        double pitch = chosen.getPitch();
        if (profile.pitchVariation > 0) {
            pitch += Main.generateRandomDoubleLocally(-profile.pitchVariation, profile.pitchVariation);
        }
        SoundEffect copy = chosen.createCopy(pitch);
        copy.setVolume(volume);
        copy.setPan(pan);
        copy.startWithDelay(msDelay);
        profile.updateNumPlaying(1);
        profile.addRemoveDecrementTick(true, tickNumber + profile.duration);
    }

    /**
     * Volume multiplier for a sound the given distance from the center of the view,
     * where distance is measured in fractions of the viewport's half-size. Falls off
     * with distance the way sound does until it reaches
     * {@link #minimumVolumeFraction}, which it then holds however far away the sound is.
     */
    private static double attenuationAt(double distance) {
        if (distance <= fullVolumeRadius) {
            return 1.0;
        }
        return Math.max(minimumVolumeFraction, fullVolumeRadius / distance);
    }

    /**
     * Region of the world the camera can currently see, or null if there is no
     * active game to hear the sound from.
     */
    private static Rectangle getViewport() {
        if (Window.currentGame == null || Window.currentGame.getCamera() == null) {
            return null;
        }
        return Window.currentGame.getCamera().getFieldOfView();
    }

}

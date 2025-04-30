package Framework.Audio;

import Framework.IndependentEffect;
import Framework.Main;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

public class ConcurrentSoundManager extends IndependentEffect {

    private long tickNumber = 0;

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
        // no action   
    }

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

    public void registerSoundEffect(String name, SoundEffect se, int maxConcurrent, int tickDuration) {
        effectMap.put(name, new SoundEffectProfile(
                se,
                maxConcurrent,
                tickDuration
        ));
    }

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

package GameDemo.SpaceInvadersDemo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A tiny self-contained retro sound engine. Rather than loading audio files, this
 * synthesizes classic arcade blips and booms (square waves, noise bursts, pitch
 * sweeps) into in-memory PCM buffers once at startup, then plays them through
 * short-lived {@link Clip}s. Nothing here touches the disk, so the demo carries
 * its whole "chiptune" soundscape in code.
 *
 * All buffers are 16-bit signed little-endian mono at 44.1kHz.
 *
 * @author Joseph
 */
public final class RetroSfx {

    private static final float SAMPLE_RATE = 44100f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    // waveform types for tone()
    private static final int SQUARE = 0, SINE = 1, TRIANGLE = 2, SAW = 3;

    // limit simultaneous clips so a machine-gun fire rate cannot exhaust mixer lines
    private static final int MAX_CONCURRENT = 28;
    private static final AtomicInteger activeClips = new AtomicInteger(0);

    private static boolean enabled = true;

    // pre-rendered sound buffers
    private static byte[] SHOOT, SPREAD_SHOOT, ENEMY_SHOOT, EXPLOSION, BIG_EXPLOSION,
            PLAYER_HIT, SHIELD_HIT, POWERUP, NOVA, NOVA_READY, WAVE_CLEAR, BOSS_WARN,
            BOSS_HIT, GAME_OVER, VICTORY, THRUST;

    private RetroSfx() {}

    /** Builds every sound buffer. Safe to call more than once. */
    public static void initialize() {
        try {
            SHOOT        = toBytes(env(tone(920, 540, 90, 0.32, SQUARE), 2, 40));
            SPREAD_SHOOT = toBytes(env(mix(tone(760, 480, 100, 0.22, SQUARE), tone(1180, 720, 100, 0.16, SQUARE)), 2, 45));
            ENEMY_SHOOT  = toBytes(env(tone(300, 170, 140, 0.26, SAW), 2, 60));

            EXPLOSION    = toBytes(mix(noise(340, 0.55, 2.2), tone(220, 70, 340, 0.22, SQUARE)));
            BIG_EXPLOSION = toBytes(mix(noise(650, 0.7, 1.7), tone(150, 40, 650, 0.28, SQUARE)));

            PLAYER_HIT   = toBytes(env(vibrato(tone(240, 150, 320, 0.4, SQUARE), 22, 0.5), 2, 80));
            SHIELD_HIT   = toBytes(env(tone(1400, 900, 130, 0.3, SINE), 2, 50));

            POWERUP      = toBytes(env(concat(tone(520, 520, 70, 0.3, SQUARE),
                                              tone(780, 780, 70, 0.3, SQUARE),
                                              tone(1180, 1180, 110, 0.3, SQUARE)), 2, 40));
            NOVA_READY   = toBytes(env(concat(tone(700, 700, 60, 0.28, TRIANGLE),
                                              tone(1050, 1050, 60, 0.28, TRIANGLE),
                                              tone(1400, 1600, 120, 0.3, TRIANGLE)), 2, 40));
            NOVA         = toBytes(mix(noise(600, 0.6, 1.4), tone(1200, 120, 600, 0.35, SAW)));

            WAVE_CLEAR   = toBytes(env(concat(tone(600, 600, 90, 0.28, SQUARE),
                                              tone(760, 760, 90, 0.28, SQUARE),
                                              tone(1000, 1000, 90, 0.28, SQUARE),
                                              tone(1320, 1320, 160, 0.3, SQUARE)), 2, 40));
            BOSS_WARN    = toBytes(env(concat(tone(420, 420, 180, 0.34, SQUARE),
                                              silence(70),
                                              tone(420, 420, 180, 0.34, SQUARE),
                                              silence(70),
                                              tone(560, 560, 260, 0.36, SQUARE)), 2, 30));
            BOSS_HIT     = toBytes(env(tone(520, 380, 90, 0.28, SQUARE), 2, 40));

            GAME_OVER    = toBytes(env(concat(tone(500, 500, 170, 0.3, SQUARE),
                                              tone(400, 400, 170, 0.3, SQUARE),
                                              tone(300, 300, 170, 0.3, SQUARE),
                                              tone(200, 140, 420, 0.32, SQUARE)), 2, 60));
            VICTORY      = toBytes(env(concat(tone(660, 660, 110, 0.3, SQUARE),
                                              tone(880, 880, 110, 0.3, SQUARE),
                                              tone(990, 990, 110, 0.3, SQUARE),
                                              tone(1320, 1320, 320, 0.32, SQUARE)), 2, 60));
            THRUST       = toBytes(env(noise(120, 0.14, 0.6), 4, 30));
        } catch (Throwable t) {
            // never let audio problems break the game
            System.out.println("RetroSfx disabled: " + t);
            enabled = false;
        }
    }

    /* ===================== playback ===================== */

    public static void setEnabled(boolean b) { enabled = b; }

    public static void shoot()        { play(SHOOT, 0.7f); }
    public static void spreadShoot()  { play(SPREAD_SHOOT, 0.7f); }
    public static void enemyShoot()   { play(ENEMY_SHOOT, 0.4f); }
    public static void explosion()    { play(EXPLOSION, 0.7f); }
    public static void bigExplosion() { play(BIG_EXPLOSION, 0.9f); }
    public static void playerHit()    { play(PLAYER_HIT, 0.85f); }
    public static void shieldHit()    { play(SHIELD_HIT, 0.6f); }
    public static void powerUp()      { play(POWERUP, 0.8f); }
    public static void novaReady()    { play(NOVA_READY, 0.8f); }
    public static void nova()         { play(NOVA, 0.95f); }
    public static void waveClear()    { play(WAVE_CLEAR, 0.8f); }
    public static void bossWarn()     { play(BOSS_WARN, 0.9f); }
    public static void bossHit()      { play(BOSS_HIT, 0.45f); }
    public static void gameOver()     { play(GAME_OVER, 0.85f); }
    public static void victory()      { play(VICTORY, 0.9f); }
    public static void thrust()       { play(THRUST, 0.35f); }

    /**
     * Plays a pre-rendered buffer on a throwaway clip. Volume is 0..1. Overlapping
     * calls layer naturally because each gets its own clip; the clip closes itself
     * (freeing its mixer line) as soon as it finishes.
     */
    private static void play(byte[] data, float volume) {
        if (!enabled || data == null) return;
        if (activeClips.get() >= MAX_CONCURRENT) return;
        // open + start on a virtual thread so acquiring a mixer line never stalls the game loop
        Thread.ofVirtual().start(() -> openAndStart(data, volume));
    }

    private static void openAndStart(byte[] data, float volume) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, data, 0, data.length);
            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum();
                float max = Math.min(gain.getMaximum(), 6f);
                // map 0..1 volume onto a perceptual-ish gain curve
                float target = (volume <= 0f) ? min : (float) (20.0 * Math.log10(Math.max(0.0001f, volume)));
                gain.setValue(Math.max(min, Math.min(max, target)));
            } catch (IllegalArgumentException ignored) { }
            activeClips.incrementAndGet();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    activeClips.decrementAndGet();
                }
            });
            clip.start();
        } catch (Throwable t) {
            // out of lines or unsupported: just skip this sound
        }
    }

    /* ===================== synthesis ===================== */

    private static double[] tone(double f0, double f1, int ms, double vol, int type) {
        int n = (int) (SAMPLE_RATE * ms / 1000.0);
        double[] out = new double[n];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double f = f0 + (f1 - f0) * (i / (double) n);
            phase += 2 * Math.PI * f / SAMPLE_RATE;
            double s;
            switch (type) {
                case SINE:     s = Math.sin(phase); break;
                case TRIANGLE: s = 2.0 / Math.PI * Math.asin(Math.sin(phase)); break;
                case SAW:      s = 2.0 * (((phase / (2 * Math.PI)) % 1.0)) - 1.0; break;
                case SQUARE:
                default:       s = Math.sin(phase) >= 0 ? 1.0 : -1.0; break;
            }
            out[i] = s * vol;
        }
        return out;
    }

    private static double[] noise(int ms, double vol, double decayPow) {
        int n = (int) (SAMPLE_RATE * ms / 1000.0);
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            double env = Math.pow(1.0 - (i / (double) n), decayPow);
            out[i] = (Math.random() * 2 - 1) * vol * env;
        }
        return out;
    }

    private static double[] silence(int ms) {
        return new double[(int) (SAMPLE_RATE * ms / 1000.0)];
    }

    /** Adds b on top of a into a new array as long as the longer of the two. */
    private static double[] mix(double[] a, double[] b) {
        double[] out = new double[Math.max(a.length, b.length)];
        for (int i = 0; i < out.length; i++) {
            double v = 0;
            if (i < a.length) v += a[i];
            if (i < b.length) v += b[i];
            out[i] = v;
        }
        return out;
    }

    private static double[] concat(double[]... parts) {
        int total = 0;
        for (double[] p : parts) total += p.length;
        double[] out = new double[total];
        int off = 0;
        for (double[] p : parts) {
            System.arraycopy(p, 0, out, off, p.length);
            off += p.length;
        }
        return out;
    }

    /** Applies a short attack ramp and release ramp to avoid clicks/pops. */
    private static double[] env(double[] data, int attackMs, int releaseMs) {
        int attack = (int) (SAMPLE_RATE * attackMs / 1000.0);
        int release = (int) (SAMPLE_RATE * releaseMs / 1000.0);
        for (int i = 0; i < data.length; i++) {
            double g = 1.0;
            if (i < attack) g = i / (double) attack;
            int fromEnd = data.length - i;
            if (fromEnd < release) g = Math.min(g, fromEnd / (double) release);
            data[i] *= g;
        }
        return data;
    }

    /** Amplitude vibrato for a wobbly, distressed sound. */
    private static double[] vibrato(double[] data, double rateHz, double depth) {
        for (int i = 0; i < data.length; i++) {
            double lfo = 1.0 - depth * (0.5 + 0.5 * Math.sin(2 * Math.PI * rateHz * i / SAMPLE_RATE));
            data[i] *= lfo;
        }
        return data;
    }

    private static byte[] toBytes(double[] samples) {
        // soft-clip to keep layered sounds from harshly overflowing
        byte[] out = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            double v = Math.tanh(samples[i]);
            short s = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v * 32767));
            out[i * 2]     = (byte) (s & 0xff);
            out[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
        }
        return out;
    }
}

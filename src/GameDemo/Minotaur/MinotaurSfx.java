package GameDemo.Minotaur;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The game's whole soundscape, synthesised into memory at startup — no audio files.
 *
 * Combat sounds are built from three ingredients: filtered noise for the physical part of a
 * hit, a fast downward pitch sweep for weight, and a short tonal body for material. A light
 * hit is a bright, quick version of the same recipe the heavy hit uses slowly and low, so the
 * two read as the same weapon swung with different intent.
 *
 * All buffers are 16-bit signed little-endian mono at 44.1kHz.
 *
 * @author Joseph
 */
public final class MinotaurSfx {

    private static final float SAMPLE_RATE = 44100f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private static final int SQUARE = 0, SINE = 1, TRIANGLE = 2, SAW = 3;

    /** Cap on simultaneous clips so a fast combo cannot exhaust the mixer's lines. */
    private static final int MAX_CONCURRENT = 26;
    private static final AtomicInteger activeClips = new AtomicInteger(0);

    private static boolean enabled = true;

    private static byte[] SWING_LIGHT, SWING_HEAVY, HIT_LIGHT, HIT_HEAVY, HIT_ARMOR,
            PLAYER_HURT, ENEMY_DIE, BARREL_BREAK, JUMP, DOUBLE_JUMP, LAND, DASH,
            ROAR, RAGE_READY, PICKUP, FIREBALL, FIRE_IMPACT, SLAM,
            PORTAL_OPEN, LEVEL_CLEAR, BOSS_ROAR, GAME_OVER, VICTORY;

    private MinotaurSfx() {}

    /** Builds every buffer. Safe to call more than once; audio failures disable sound silently. */
    public static void initialize() {
        try {
            // ---- weapon ----
            SWING_LIGHT = toBytes(env(bandNoise(130, 0.30, 1.7, 2600, 0.55), 3, 70));
            SWING_HEAVY = toBytes(env(mix(bandNoise(210, 0.34, 1.4, 1500, 0.35),
                                          tone(190, 70, 210, 0.12, SINE)), 4, 110));

            // ---- impacts: noise burst + low thump + a little tonal ring ----
            HIT_LIGHT = toBytes(env(mix(noise(90, 0.5, 2.6),
                                        tone(360, 150, 90, 0.34, TRIANGLE)), 1, 55));
            HIT_HEAVY = toBytes(env(mix(mix(noise(210, 0.6, 2.0),
                                        tone(180, 55, 220, 0.44, SQUARE)),
                                        tone(96, 40, 260, 0.34, SINE)), 2, 130));
            HIT_ARMOR = toBytes(env(mix(tone(1650, 1180, 130, 0.24, SQUARE),
                                        noise(90, 0.34, 2.8)), 1, 70));

            PLAYER_HURT = toBytes(env(vibrato(mix(tone(280, 132, 300, 0.40, SAW),
                                        noise(150, 0.32, 2.2)), 26, 0.45), 2, 110));
            ENEMY_DIE = toBytes(env(mix(tone(420, 90, 330, 0.32, SAW),
                                        noise(260, 0.42, 1.8)), 2, 150));
            BARREL_BREAK = toBytes(env(mix(noise(230, 0.55, 1.5),
                                        concat(tone(700, 520, 60, 0.2, SQUARE),
                                               tone(520, 300, 80, 0.18, SQUARE))), 1, 120));

            // ---- movement ----
            JUMP = toBytes(env(tone(300, 620, 110, 0.24, TRIANGLE), 2, 55));
            DOUBLE_JUMP = toBytes(env(mix(tone(480, 980, 120, 0.22, TRIANGLE),
                                        bandNoise(90, 0.18, 1.6, 3200, 0.5)), 2, 60));
            LAND = toBytes(env(mix(noise(90, 0.32, 2.6), tone(140, 70, 110, 0.3, SINE)), 1, 70));
            DASH = toBytes(env(bandNoise(180, 0.34, 1.3, 1900, 0.6), 3, 110));

            // ---- rage ----
            ROAR = toBytes(env(mix(mix(vibrato(tone(150, 96, 700, 0.42, SAW), 17, 0.35),
                                        tone(74, 52, 700, 0.34, SQUARE)),
                                        bandNoise(620, 0.30, 0.9, 900, 0.3)), 20, 250));
            RAGE_READY = toBytes(env(concat(tone(520, 520, 80, 0.24, TRIANGLE),
                                            tone(700, 700, 80, 0.24, TRIANGLE),
                                            tone(1040, 1160, 190, 0.26, TRIANGLE)), 3, 90));
            PICKUP = toBytes(env(concat(tone(760, 760, 60, 0.24, SINE),
                                        tone(1140, 1250, 130, 0.24, SINE)), 2, 60));

            // ---- projectiles and slams ----
            FIREBALL = toBytes(env(mix(tone(680, 380, 190, 0.2, SAW),
                                       bandNoise(190, 0.22, 1.5, 2300, 0.5)), 3, 90));
            FIRE_IMPACT = toBytes(env(mix(noise(220, 0.5, 1.9), tone(240, 80, 240, 0.3, SAW)), 1, 130));
            SLAM = toBytes(env(mix(mix(noise(320, 0.62, 1.6), tone(120, 38, 340, 0.5, SQUARE)),
                                        tone(62, 34, 420, 0.4, SINE)), 2, 200));

            // ---- flow ----
            PORTAL_OPEN = toBytes(env(mix(sweepChord(new double[]{330, 494, 660}, 900, 0.16),
                                        bandNoise(900, 0.12, 0.7, 1400, 0.4)), 60, 380));
            LEVEL_CLEAR = toBytes(env(concat(tone(523, 523, 110, 0.24, TRIANGLE),
                                             tone(659, 659, 110, 0.24, TRIANGLE),
                                             tone(784, 784, 110, 0.24, TRIANGLE),
                                             tone(1047, 1047, 340, 0.26, TRIANGLE)), 4, 130));
            BOSS_ROAR = toBytes(env(mix(mix(vibrato(tone(112, 66, 1150, 0.44, SAW), 9, 0.4),
                                        tone(56, 38, 1150, 0.36, SQUARE)),
                                        bandNoise(1000, 0.26, 0.8, 700, 0.25)), 30, 420));
            GAME_OVER = toBytes(env(concat(tone(392, 392, 220, 0.26, TRIANGLE),
                                           tone(330, 330, 220, 0.26, TRIANGLE),
                                           tone(262, 262, 260, 0.26, TRIANGLE),
                                           tone(196, 130, 620, 0.28, SAW)), 5, 200));
            VICTORY = toBytes(env(concat(tone(523, 523, 130, 0.25, TRIANGLE),
                                         tone(659, 659, 130, 0.25, TRIANGLE),
                                         tone(784, 784, 130, 0.25, TRIANGLE),
                                         tone(1047, 1047, 180, 0.27, TRIANGLE),
                                         tone(1319, 1319, 460, 0.27, TRIANGLE)), 5, 220));
        } catch (Throwable t) {
            System.out.println("MinotaurSfx disabled: " + t);
            enabled = false;
        }
    }

    /* ===================== playback ===================== */

    public static void setEnabled(boolean b) { enabled = b; }
    public static boolean isEnabled() { return enabled; }

    public static void swingLight()  { play(SWING_LIGHT, 0.45f); }
    public static void swingHeavy()  { play(SWING_HEAVY, 0.6f); }
    public static void hitLight()    { play(HIT_LIGHT, 0.7f); }
    public static void hitHeavy()    { play(HIT_HEAVY, 0.85f); }
    public static void hitArmor()    { play(HIT_ARMOR, 0.55f); }
    public static void playerHurt()  { play(PLAYER_HURT, 0.85f); }
    public static void enemyDie()    { play(ENEMY_DIE, 0.6f); }
    public static void barrelBreak() { play(BARREL_BREAK, 0.6f); }
    public static void jump()        { play(JUMP, 0.4f); }
    public static void doubleJump()  { play(DOUBLE_JUMP, 0.45f); }
    public static void land()        { play(LAND, 0.35f); }
    public static void dash()        { play(DASH, 0.5f); }
    public static void roar()        { play(ROAR, 0.95f); }
    public static void rageReady()   { play(RAGE_READY, 0.7f); }
    public static void pickup()      { play(PICKUP, 0.6f); }
    public static void fireball()    { play(FIREBALL, 0.45f); }
    public static void fireImpact()  { play(FIRE_IMPACT, 0.6f); }
    public static void slam()        { play(SLAM, 0.9f); }
    public static void portalOpen()  { play(PORTAL_OPEN, 0.7f); }
    public static void levelClear()  { play(LEVEL_CLEAR, 0.8f); }
    public static void bossRoar()    { play(BOSS_ROAR, 1.0f); }
    public static void gameOver()    { play(GAME_OVER, 0.85f); }
    public static void victory()     { play(VICTORY, 0.9f); }

    private static void play(byte[] data, float volume) {
        if (!enabled || data == null) return;
        if (activeClips.get() >= MAX_CONCURRENT) return;
        // acquiring a mixer line can block, so never do it on the game loop's thread
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
            // no line available, or unsupported format: drop this one sound
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
            double e = Math.pow(1.0 - (i / (double) n), decayPow);
            out[i] = (Math.random() * 2 - 1) * vol * e;
        }
        return out;
    }

    /**
     * Noise pushed through a one-pole low-pass whose cutoff slides down over the burst.
     * A whoosh is exactly this: broadband air that loses its top end as the swing passes.
     */
    private static double[] bandNoise(int ms, double vol, double decayPow, double startCutoff, double endRatio) {
        int n = (int) (SAMPLE_RATE * ms / 1000.0);
        double[] out = new double[n];
        double last = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            double cutoff = startCutoff * (1 - t) + startCutoff * endRatio * t;
            double a = Math.min(0.999, 2 * Math.PI * cutoff / SAMPLE_RATE);
            double raw = Math.random() * 2 - 1;
            last += a * (raw - last);
            out[i] = last * vol * Math.pow(1.0 - t, decayPow) * 3.0;
        }
        return out;
    }

    /** Several partials rising together — used for the portal's "something opened" swell. */
    private static double[] sweepChord(double[] freqs, int ms, double vol) {
        double[] out = null;
        for (double f : freqs) {
            double[] partial = tone(f * 0.6, f, ms, vol, SINE);
            out = (out == null) ? partial : mix(out, partial);
        }
        return out;
    }

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

    /** Attack and release ramps so nothing starts or stops with a click. */
    private static double[] env(double[] data, int attackMs, int releaseMs) {
        int attack = Math.max(1, (int) (SAMPLE_RATE * attackMs / 1000.0));
        int release = Math.max(1, (int) (SAMPLE_RATE * releaseMs / 1000.0));
        for (int i = 0; i < data.length; i++) {
            double g = 1.0;
            if (i < attack) g = i / (double) attack;
            int fromEnd = data.length - i;
            if (fromEnd < release) g = Math.min(g, fromEnd / (double) release);
            data[i] *= g;
        }
        return data;
    }

    private static double[] vibrato(double[] data, double rateHz, double depth) {
        for (int i = 0; i < data.length; i++) {
            double lfo = 1.0 - depth * (0.5 + 0.5 * Math.sin(2 * Math.PI * rateHz * i / SAMPLE_RATE));
            data[i] *= lfo;
        }
        return data;
    }

    private static byte[] toBytes(double[] samples) {
        byte[] out = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            double v = Math.tanh(samples[i]);   // soft clip keeps layered hits from tearing
            short s = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v * 32767));
            out[i * 2] = (byte) (s & 0xff);
            out[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
        }
        return out;
    }
}

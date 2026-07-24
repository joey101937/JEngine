
package GameDemo.RTSDemo;

import Framework.Audio.ConcurrentSoundManager;
import Framework.Audio.SoundEffect;
import Framework.Main;
import java.io.File;

/**
 *
 * @author guydu
 */
public class RTSSoundManager {
    private static ConcurrentSoundManager createdSoundManager;

    public static String RIFLEMAN_ATTACK = "riflemanAttack";
    public static String BAZOOKA_ATTACK = "bazookaAttack";
    public static String TANK_ATTACK = "tankAttack";
    public static String T2_TURRET_ATTACK = "t2TurretAttack";
    public static String HELICOPTER_ATTACK = "helicopterAttack";
    public static String LIGHT_TANK_ATTACK = "lightTankAttack";
    public static String LANDMINE_EXPLOSION = "landmineExplosion";

    public static String TANK_DEATH = "tankDeath";
    public static String INFANTRY_DEATH = "infantryDeath";

    /** how far each individual play is pitch shifted so repeats don't sound identical */
    private static final double PITCH_VARIATION = .16;

    private static void registerSounds(ConcurrentSoundManager csm) {
        csm.registerSoundEffect(RIFLEMAN_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/machinegun.au")),
                2, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(BAZOOKA_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/bazooka.au")),
                3, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(TANK_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/blast4.6.wav")),
                4, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(T2_TURRET_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/blast4.6.wav")),
                4, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(HELICOPTER_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au")),
                4, RTSGame.desiredTPS, PITCH_VARIATION);
        SoundEffect lightTankAttack = new SoundEffect(new File(Main.assets + "Sounds/armoredCarShooting5.wav"));
        lightTankAttack.alterPitch(-.08);
        csm.registerSoundEffect(LIGHT_TANK_ATTACK, lightTankAttack,
                4, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(LANDMINE_EXPLOSION,
                new SoundEffect(new File(Main.assets + "Sounds/explosion.au")),
                5, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(TANK_DEATH,
                new SoundEffect(new File(Main.assets + "Sounds/landmine explosion.wav")),
                2, RTSGame.desiredTPS, PITCH_VARIATION);
        csm.registerSoundEffect(INFANTRY_DEATH,
                new SoundEffect(new File(Main.assets + "Sounds/scream4.wav")),
                2, RTSGame.desiredTPS, PITCH_VARIATION);
    }


    private static ConcurrentSoundManager create () {
        ConcurrentSoundManager csm = new ConcurrentSoundManager();
        registerSounds(csm);
        return csm;
    }

    public static ConcurrentSoundManager get () {
        if (createdSoundManager == null) {
            createdSoundManager = create();
        }

        return createdSoundManager;
    }
}

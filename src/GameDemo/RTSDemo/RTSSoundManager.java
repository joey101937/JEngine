
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
    public static String HELICOPTER_ATTACK = "helicopterAttack";
    public static String LIGHT_TANK_ATTACK = "lightTankAttack";
    public static String LANDMINE_EXPLOSION = "landmineExplosion";
    
    public static String TANK_DEATH = "tankDeath";
    public static String INFANTRY_DEATH = "infantryDeath";


    
    private static void registerSounds(ConcurrentSoundManager csm) {
        csm.registerSoundEffect(
                RIFLEMAN_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/machinegun.au")),
                2, Main.ticksPerSecond);
        csm.registerSoundEffect(
                BAZOOKA_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/bazooka.au")),
                3, Main.ticksPerSecond);
        csm.registerSoundEffect(
                TANK_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/blast4.6.wav")),
                4, Main.ticksPerSecond);
        csm.registerSoundEffect(
                HELICOPTER_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au")),
                4, Main.ticksPerSecond);
        csm.registerSoundEffect(
                LIGHT_TANK_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/armoredCarShooting5.au")),
                4, Main.ticksPerSecond);
        csm.registerSoundEffect(
                LANDMINE_EXPLOSION,
                new SoundEffect(new File(Main.assets + "Sounds/explosion.au")),
                5, Main.ticksPerSecond);
        csm.registerSoundEffect(
                TANK_DEATH,
                new SoundEffect(new File(Main.assets + "Sounds/landmine explosion.wav")),
                2, Main.ticksPerSecond);
        csm.registerSoundEffect(
                INFANTRY_DEATH,
                new SoundEffect(new File(Main.assets + "Sounds/scream4.wav")),
                2, Main.ticksPerSecond);
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

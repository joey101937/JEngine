
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
    public static String TANK_ATTACK_2 = "tankAttack2";
    public static String HELICOPTER_ATTACK = "helicopterAttack";
    public static String LIGHT_TANK_ATTACK = "lightTankAttack";
    
    private static void registerSounds(ConcurrentSoundManager csm) {
        csm.registerSoundEffect(
                RIFLEMAN_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/machinegun.au")),
                7, Main.ticksPerSecond);
        csm.registerSoundEffect(
                BAZOOKA_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/bazooka.au")),
                10, Main.ticksPerSecond);
        csm.registerSoundEffect(
                TANK_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/blast4.62.wav")),
                6, Main.ticksPerSecond);
        csm.registerSoundEffect(
                TANK_ATTACK_2,
                new SoundEffect(new File(Main.assets + "Sounds/blast4.6.wav")),
                6, Main.ticksPerSecond);
        csm.registerSoundEffect(
                HELICOPTER_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au")),
                5, Main.ticksPerSecond);
        csm.registerSoundEffect(
                LIGHT_TANK_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/gunshot.wav")),
                5, Main.ticksPerSecond);
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

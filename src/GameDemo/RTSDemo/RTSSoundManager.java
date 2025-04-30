
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
    
    private static void registerSounds(ConcurrentSoundManager csm) {
        csm.registerSoundEffect(
                RIFLEMAN_ATTACK,
                new SoundEffect(new File(Main.assets + "Sounds/machinegun.au")),
                7, Main.ticksPerSecond);
        // todo add other sounds here
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

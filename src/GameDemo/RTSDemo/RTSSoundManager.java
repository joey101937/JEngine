
package GameDemo.RTSDemo;

import Framework.Audio.ConcurrentSoundManager;
import Framework.Audio.SoundEffect;
import Framework.Main;
import java.io.File;
import java.util.List;

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
        SoundEffect riflemanAttackBase = new SoundEffect(new File(Main.assets + "Sounds/machinegun.au"));
        csm.registerSoundEffect(RIFLEMAN_ATTACK, List.of(
                riflemanAttackBase,
                riflemanAttackBase.createAlteredCopy(3.0),
                riflemanAttackBase.createAlteredCopy(3.0),
                riflemanAttackBase.createAlteredCopy(3.0)
        ), 2, Main.ticksPerSecond);
        SoundEffect bazookaAttackBase = new SoundEffect(new File(Main.assets + "Sounds/bazooka.au"));
        csm.registerSoundEffect(BAZOOKA_ATTACK, List.of(
                bazookaAttackBase,
                bazookaAttackBase.createAlteredCopy(3.0),
                bazookaAttackBase.createAlteredCopy(3.0),
                bazookaAttackBase.createAlteredCopy(3.0)
        ), 3, Main.ticksPerSecond);
        SoundEffect tankAttackBase = new SoundEffect(new File(Main.assets + "Sounds/blast4.6.wav"));
        csm.registerSoundEffect(TANK_ATTACK, List.of(
                tankAttackBase,
                tankAttackBase.createAlteredCopy(3.0),
                tankAttackBase.createAlteredCopy(3.0),
                tankAttackBase.createAlteredCopy(3.0)
        ), 4, Main.ticksPerSecond);
        SoundEffect helicopterAttackBase = new SoundEffect(new File(Main.assets + "Sounds/missileLaunch.au"));
        csm.registerSoundEffect(HELICOPTER_ATTACK, List.of(
                helicopterAttackBase,
                helicopterAttackBase.createAlteredCopy(3.0),
                helicopterAttackBase.createAlteredCopy(3.0),
                helicopterAttackBase.createAlteredCopy(3.0)
        ), 4, Main.ticksPerSecond);
        SoundEffect lightTankAttackBase = new SoundEffect(new File(Main.assets + "Sounds/armoredCarShooting5.wav"));
        csm.registerSoundEffect(LIGHT_TANK_ATTACK, List.of(
                lightTankAttackBase,
                lightTankAttackBase.createAlteredCopy(3.0),
                lightTankAttackBase.createAlteredCopy(3.0),
                lightTankAttackBase.createAlteredCopy(3.0)
        ), 4, Main.ticksPerSecond);
        SoundEffect landmineExplosionBase = new SoundEffect(new File(Main.assets + "Sounds/explosion.au"));
        csm.registerSoundEffect(LANDMINE_EXPLOSION, List.of(
                landmineExplosionBase,
                landmineExplosionBase.createAlteredCopy(3.0),
                landmineExplosionBase.createAlteredCopy(3.0),
                landmineExplosionBase.createAlteredCopy(3.0)
        ), 5, Main.ticksPerSecond);
        SoundEffect tankDeathBase = new SoundEffect(new File(Main.assets + "Sounds/landmine explosion.wav"));
        csm.registerSoundEffect(TANK_DEATH, List.of(
                tankDeathBase,
                tankDeathBase.createAlteredCopy(3.0),
                tankDeathBase.createAlteredCopy(3.0),
                tankDeathBase.createAlteredCopy(3.0)
        ), 2, Main.ticksPerSecond);
        SoundEffect infantryDeathBase = new SoundEffect(new File(Main.assets + "Sounds/scream4.wav"));
        csm.registerSoundEffect(INFANTRY_DEATH, List.of(
                infantryDeathBase,
                infantryDeathBase.createAlteredCopy(3.0),
                infantryDeathBase.createAlteredCopy(3.0),
                infantryDeathBase.createAlteredCopy(3.0)
        ), 2, Main.ticksPerSecond);
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

/**
 * A class with this interface may be applied to a SoundEffect object using the 
 * setListener method in the SoundEffect class. This interface allows you to
 * detect happenings in the sound effect and respond to them if need be.
 * @author Joseph
 */
public interface SoundEffectListener {
    public void onStart();
    public void onPause();
    public void onResume();
    public void onSetLooping(boolean isLooping);
    public void onDisable();
    public void onReset();
    public void onSetVolume(float percentage);
}

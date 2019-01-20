/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

/**
 *
 * @author Joseph
 */
public interface SoundEffectListener {
    public void onPause();
    public void onResume();
    public void onLoop(int times);
    public void onLoopContinuously();
    public void onDisable();
    public void onReset();
    public void onSetVolume(float percentage);
}

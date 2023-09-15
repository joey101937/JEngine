/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

/**
 *
 * @author guydu
 */
public class AsyncPlayer implements Runnable {
        public SoundEffect myParentSound;
        public SoundEffect mySound;
        public float myVolume;
        
        public AsyncPlayer (SoundEffect se, float volume) {
            myParentSound = se;
            myVolume = volume;
        }

        @Override
        public void run() {
            myParentSound.asyncPlayers.add(this);
            SoundEffect copy = myParentSound.createCopy();
            copy.setListener(new AsyncPlayerListener(this));
            copy.setVolume(myVolume);
            copy.start();
            mySound = copy;
        }
}

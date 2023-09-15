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
public class AsyncPlayerListener implements SoundEffectListener{
     public AsyncPlayer myPlayer;
        
        public AsyncPlayerListener(AsyncPlayer se) {
            myPlayer = se;
        }
        
        @Override
        public void onDisable() {
            myPlayer.myParentSound.asyncPlayers.remove(myPlayer);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onSetLooping(boolean isLooping) {
        }

        @Override
        public void onRestart() {
        }

        @Override
        public void onSetVolume(float percentage) {
        }
}

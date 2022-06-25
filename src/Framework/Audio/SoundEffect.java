/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

import Framework.Game;
import Framework.Main;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class represents a sound effect.
 * Remember only use sound files that Java.sound supports.
 * @author Joseph
 */
public class SoundEffect implements Runnable{
    private static int IDGenerator = 0;
      
    public final int ID; //unique identifier
    
    private Thread thread;                  //thread used to keep audio going
    private SoundEffectListener listener;   //listens to events of this sound
    private volatile File source;           //source file
    private volatile AudioInputStream stream;//stream connected to source
    private volatile Clip clip;              //clip used to control most things
    private volatile FloatControl gainControl;//used to control volueme
    private volatile boolean disabled = false;//disabling makes this sound terminate
    private volatile boolean hasStarted = false;
    private volatile boolean paused = false;    //paused directly
    private volatile Game hostGame = null;
    private volatile Long currentFrame = 0L;
    private volatile int startDelay = 0;
    private boolean looping = false;
    
    /**
     * creates a new sound effect with the given file.
     * @param f File to create sound with
     */
    public SoundEffect(File f) {
        ID = ++IDGenerator;
        initialize(f);   
    }


    /**
     * sets up default settings and loads file. Will throw error if the file
     * given is not supported
     * @param f file to create a soundEffect with
     */
    private void initialize(File f) {
        if(f == null){
            throw new RuntimeException("Error: trying to create SoundEffect with null file");
        }
        try {
            source = f;
            stream = AudioSystem.getAudioInputStream(f);
            clip = AudioSystem.getClip();
            clip.open(stream);
            gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR File " + f.getName() + " is not supported. Remeber to use only supported filetypes \n .au .wav .aiff are good choices");
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.setName("SoundEffect " + stream.toString());
    }

    /**
     * begins playing the sound. If you call this manually, it will likely cause
     * the sound to play globally.
     */
    public void start() {
        if (hasStarted) {
            System.out.println("Sound already started " + source.getName() + " ID:" + ID);
        } else {
            thread.start();
            hasStarted = true;
            if (listener != null) {
                listener.onStart();
            }
        }
    }
    
    public void startWithRandomDelay(int min, int max){
        startDelay = Main.generateRandom(min, max);
        start();
    }
    
    public void startWithDelay(int delay){
        startDelay = delay;
        start();
    }

    /**
     * Gets the thread that is supporting this soundeffect
     * @return Gets the thread that is supporting this soundeffect
     */
    public Thread getThread(){
        return thread;
    }
    
    /**
     * makes the sound replay continuously until told to stop
     */
    public void setLooping(boolean input){
        if(input){
            looping=true;
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }else{
            looping = false;
            clip.loop(0);
        }
         if(listener!=null)listener.onSetLooping(input);
    }
    
    public boolean isLooping(){
        return looping;
    }
    
    
    /**
     * runs this as a thread. 
     * Don't call this directly unless you know what youre doing
     * Keeps a thread active to keep audio going, checks every second to see if
     * the audio has ended and it needs to terminate. 
     * Stop this thread using disable
     */
    @Override
    public void run() {
        if(startDelay > 0) {
            Main.wait(startDelay);
        }
        clip.start();
        while (!isDisabled()) {
            Main.wait(1000);        //check every 1 second
            if (!(clip.isRunning() || isPaused())) {
                Main.wait(1000);   //clip must be not running and not paused for 1 second to terminate thread
                if(!(clip.isRunning() || isPaused())){
                    break;
                }
            }
        }
        disable();
        //thread ending
    }

    /**
     * stops the sound, terminates its thread, and removes from hostGame audiomanager.
     */
    public void disable(){
        clip.stop();
        clip.close();
        disabled = true;
        linkToGame(null);
        if(listener!=null)listener.onDisable();
    }
    
    /**
    Gets the Java.sound audioclip object for this sound effect
    */
    public Clip getClip(){
        return clip;
    }
    
    /**
     * current frame of audio in the sound 
     * @return current frame of audio in the sound 
     */
    public Long getCurrentFrame(){
        return currentFrame;
    }
    
    /**
    * sets the volume of this sound effect to a given percentage of the clip's 
    * base sound level. Note this is not uniform across all sound files as some
    * are just naturally louder than others, however SoundEffect objects using
    * the same audio source will have the same natural volume.
    * 1.0  = loud
    * 0.5  = quiet
    * 0.0  = silent
    */
    public void setVolume(float percentVolume){
        if(percentVolume < 0 || percentVolume > 1){
            throw new RuntimeException("ERROR: Percent Volume must be between 0 and 1");
        }
        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        float range = max-min;
        float toSet = range*percentVolume;
        gainControl.setValue(min+toSet);
        if(listener!=null)listener.onSetVolume(percentVolume);
    }
    
    /**
     * gets current volume percentage as float.
     * 1 = 100%
     * .5 = 50%
     * 0 = 0%
     */
    public float getVolume() {
        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        float range = max - min;
        float current = gainControl.getValue();
        return (current - min) / range;
    }
    
    /**
     *Pauses the current audio in place.
     * Note that this disables looping so if you want to continue looping after
     * resuming, you must call loop method again after you resume.
    */
    public void pause(){
        if(!hasStarted){
            System.out.println("Cant pause, hasnt begun.");
            return;
        }
        if (paused)  
        { 
            System.out.println("audio is already paused"); 
            return; 
        } 
        currentFrame = clip.getMicrosecondPosition(); 
        clip.stop(); 
        paused = true;
        if(listener!=null)listener.onPause();
    }
   
    /**
     * Sets the game pause lock. Pauses the audio via game. Must be undone by calling
     * onGamePause(false) to unpause it via game. Should not call this directly
     * but rather let the game take care of this. This is how
     * soundeffects from paused games are paused independently of direct pausing
     * @param input true pause or false unpause
     */
    public void onGamePaused(boolean input) {
        if (!hasStarted && input) {
            System.out.println("Cant pause, hasnt begun.");
            return;
        }
        if (input) { //pausing
            if (!paused) {
                currentFrame = clip.getMicrosecondPosition();
                clip.stop();
                if (listener != null) {
                    listener.onPause();
                }
            }
        } else {  //resuming
            if (!paused) {
                clip.setMicrosecondPosition(currentFrame);
                currentFrame = 0L;
                if (isLooping()) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
                clip.start();
                if (listener != null) {
                    listener.onResume();
                }
            }
        }

    }

    /**
     * the sound is paused if either pause-lock is enabled. Internally you can call
     * soundeffect.pause() if the sound is part of a game, then you also must deal 
     * with gamePause which pauses the sound when the game is paued. manually undo 
     * that pause with onGamePause(false); .
     * @return 
     */
    public boolean isPaused() {
        return paused || isGamePaused();
    }
    
    /**
     * resumes a directly paused effect.
     * Note this only releases the internal pause. if this sound is in a game,
     * then you will also have to release the gamePause lock if that game is paused
     */
    public void resume() {
        if(!hasStarted){
            System.out.println("cant resume, clip hasnt begun");
            return;
        }
        if (!isPaused()) {
            System.out.println("cant resume, not paused");
            return;
        }
        paused = false;
        if (!isGamePaused()) {
            clip.setMicrosecondPosition(currentFrame);
            currentFrame = 0L;
            if (isLooping()) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
            if (listener != null) {
                listener.onResume();
            }
        }
    }

    /**
     * weather or not this soundeffect is paused due to its host game being paused
     * @return  weather or not this soundeffect is paused due to its host game being paused
     */
    public boolean isGamePaused(){
        if(hostGame == null) return false;
        else return hostGame.isPaused();
    }

    private synchronized void resetAudioStream() {
        try {
            System.out.println("resetting audio stream");
            clip.close();
            stream = AudioSystem.getAudioInputStream(source);
            clip.open(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * resets sound back to its beginning, however this will not revive a disabled
     * sound. If you want to replay a song, start a new SoundEffect with the same
     * source. This can be obtained with .createCopy() method
     */
    public void restart() {
        if(disabled){
            throw new RuntimeException("Cannot restart disabled sound");
        }
        resetAudioStream();
        if (isLooping()) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        if (isPaused()) {
            clip.start();
        }
        if (listener != null) {
            listener.onRestart();
        }
    }

    /**
     * sound effect listener active for this object
     * @return sound effect listener active for this object
     */
    public SoundEffectListener getListener(){
        return listener;
    }
    
    /**
     * sets the listener for this sound effect to the given listener
     * NOTE: this removes any existing listener
     * @param sel new listener
     */
    public void setListener(SoundEffectListener sel){
        listener = sel;
    }
    
    
    
    /**
     * Length of this sound in microseconds
     * NOTE throws nullpointerexception if audio clip is null
     * @return Length of this sound in microseconds
     * NOTE throws nullpointerexception if audio clip is null
     */
    public Long getSoundLength(){
        return clip.getMicrosecondLength();
    }
    /**
     * How far into the sound we are, measured in microseconds
     * NOTE throws nullpointerexception if audio clip is null
     * @return How far into the sound we are, measured in microseconds
     * NOTE throws nullpointerexception if audio clip is null
     */
    public Long getMicroPosition(){
        return clip.getMicrosecondPosition();
    }
    
    /**
     @return Percentage of how far into the sound is playing
     * 1.0 = 100%, sound is over
     * 0.5 = 50%, sound is half over
     * 0.0 = 0%, the sound is at its begining
     */
    public double getPercentDone(){
        return getMicroPosition()/getSoundLength();
    }
    
    /**
     * @return A fresh SoundEffect of the same source
     */
    public SoundEffect createCopy(){
        return new SoundEffect(source);
    }

    /**
     * Assigns a host game to this sound effect. The sound effect will not play
     * while its linked game is paused.
     * @param g game to link to, set to null to remove links
     */
    public void linkToGame(Game g) {
        if (g == null) {
            if (isGamePaused()) {
                this.onGamePaused(false);
            }
            if(hostGame!=null)hostGame.audioManager.removeSound(this);
            hostGame = null;
            return;
        }
        if (hostGame == null) {
            if (g.isPaused()) {
                //pause if assigned to paused game
                onGamePaused(true);
            }
        } else {
            if (!g.isPaused() && hostGame.isPaused()) {
                //if new game is not paused, remove gamepause restriction
                onGamePaused(false);
            }
        }
        hostGame = g;
        g.audioManager.addSound(this);
    }

    public Game getHostGame() {
        return hostGame;
    }
    
    @Override
    public String toString(){
        return "SoundEffect " + source.getName() + " ID:" + ID;
    }
    
    public boolean isDisabled(){
        return disabled;
    }
}

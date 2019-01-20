/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

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
    
    private Thread thread;
    private SoundEffectListener listener;
    private volatile File source;
    private volatile AudioInputStream stream;   
    private volatile Clip clip;
    private volatile FloatControl gainControl;
    private volatile boolean disabled = false;
    private volatile boolean hasStarted = false;
    private volatile boolean paused = false;
    private volatile Long currentFrame = 0l;
    private boolean looping = false;
    
    /**
     * creates a new sound effect with the given file.
     * @param f File to create sound with
     */
    public SoundEffect(File f) {
        ID = ++IDGenerator;
        initialize(f);   
    }
    
    SoundEffect(File f, SoundEffectListener list) {
        ID = ++IDGenerator;
        initialize(f);
        setListener(list);
    }

    /**
     * sets up default settings and loads file. Will throw error if the file
     * given is not supported
     * @param f file to create a soundEffect with
     */
    private void initialize(File f) {
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
     * begins playing the sound
     */
    public void start() {
        if (hasStarted) {
            System.out.println("Sound already started " + source.getName() + " ID:" + ID);
        } else {
           if(listener!=null) listener.onStart();
            thread.start();
            hasStarted = true;
             if(listener!=null)listener.onStart();
        }
    }

    /**
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
     * runs this as a thread. Dont call this directly unless you know what youre
     * doing
     */
    @Override
    public void run() {
        clip.start();
        while(!disabled){
            Main.wait(50);
        }
        disable();
    }
    
    /**
     * stops the sound and terminates its thread.
     */
    public void disable(){
        clip.stop();
        clip.close();
        disabled = true;
        if(listener!=null)listener.onDisable();
    }
    
    /**
    Gets the Java.sound audioclip object for this sound effect
    */
    public Clip getClip(){
        return clip;
    }
    
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

    public boolean isPaused() {
        return paused;
    }
    
    /**
     * resumes a paused effect. Note if you want the sound to loop, you must 
     * call loop method again. 
     */
    public void resume() {
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        if(listener!=null)listener.onResume();
    }
    
    
    private void resetAudioStream() {
        try {
            clip.close();
            stream = AudioSystem.getAudioInputStream(source);
            clip.open(stream);
            if(listener!=null)listener.onReset();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
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
}

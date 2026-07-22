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
import javax.sound.sampled.AudioFormat;
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
    private SoundEffectListener listener;   //listens to events of this sound
    private volatile File source;           //source file
    private volatile byte[] audioData;      //decoded PCM audio of the source
    private volatile AudioFormat baseFormat;//format of audioData at its original pitch
    private volatile Clip clip;              //clip used to control most things
    private volatile FloatControl gainControl;//used to control volueme
    private volatile FloatControl panControl;//used to position the sound in stereo, may be null
    private volatile double volumePercent = 100.0;//current volume, 100 = source file's own volume
    private volatile double panPosition = 0.0;//current stereo position, -1 left to 1 right
    private volatile double pitchOffset = 0.0;//current pitch shift, as a percentage of original
    private volatile boolean disabled = false;//disabling makes this sound terminate
    private volatile boolean hasStarted = false;
    private volatile boolean paused = false;    //paused directly
    private volatile Game hostGame = null;
    private volatile Long currentFrame = 0L;
    private volatile int startDelay = 0;
    private boolean looping = false;
    public volatile boolean running = false;
    private SoundEffect parent = null;
   
    
    /**
     * creates a new sound effect with the given file.
     * @param f File to create sound with
     */
    public SoundEffect(File f) {
        ID = ++IDGenerator;
        initialize(f);   
    }
    
    /**
     * creates a new sound effect sharing the decoded audio of an existing one.
     * @param f File to create sound with
     * @param parent sound effect to copy audio data and settings from
     * @param pitch pitch offset to open the copy at
     */
    private SoundEffect(File f, SoundEffect parent, double pitch) {
        this.parent = parent;
        ID = ++IDGenerator;
        source = f;
        //decoded audio is never modified, so copies can share the parent's array
        audioData = parent.audioData;
        baseFormat = parent.baseFormat;
        volumePercent = parent.volumePercent;
        panPosition = parent.panPosition;
        pitchOffset = clampPitch(pitch);
        try {
            clip = AudioSystem.getClip();
            openClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
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
            AudioInputStream stream = AudioSystem.getAudioInputStream(f);
            AudioFormat sourceFormat = stream.getFormat();
            //decode to plain stereo PCM: playback rate can then be redeclared to shift
            //pitch, and audio lines only offer stereo positioning to stereo sources
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(), 16, 2, 4, sourceFormat.getSampleRate(), false);
            if (!sourceFormat.matches(pcm)) {
                stream = AudioSystem.getAudioInputStream(pcm, stream);
            }
            baseFormat = stream.getFormat();
            audioData = stream.readAllBytes();
            stream.close();
            clip = AudioSystem.getClip();
            openClip();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR File " + f.getName() + " is not supported. Remeber to use only supported filetypes \n .au .wav .aiff are good choices");
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * opens the clip on the decoded audio, declaring the sample rate that
     * corresponds to the current pitch offset. Any existing volume is carried over.
     */
    private void openClip() throws LineUnavailableException {
        if (clip.isOpen()) {
            clip.close();
        }
        clip.open(pitchedFormat(), audioData, 0, audioData.length);
        gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        panControl = findPanControl();
        applyVolume();
        applyPan();
    }

    /**
     * stereo positioning control for the freshly opened line, or null if it has none.
     */
    private FloatControl findPanControl() {
        for (FloatControl.Type type : new FloatControl.Type[]{FloatControl.Type.PAN, FloatControl.Type.BALANCE}) {
            if (clip.isControlSupported(type)) {
                return (FloatControl) clip.getControl(type);
            }
        }
        return null;
    }

    /**
     * base format with its rates scaled by the current pitch offset. Playing samples
     * out faster raises pitch, slower lowers it; the mixer resamples to the device rate.
     */
    private AudioFormat pitchedFormat() {
        float scale = (float) (1.0 + pitchOffset);
        return new AudioFormat(baseFormat.getEncoding(),
                baseFormat.getSampleRate() * scale,
                baseFormat.getSampleSizeInBits(),
                baseFormat.getChannels(),
                baseFormat.getFrameSize(),
                baseFormat.getFrameRate() * scale,
                baseFormat.isBigEndian());
    }

    /**
     * begins playing the sound. If you call this manually, it will likely cause
     * the sound to play globally.
     */
    public void start() {
        if (hasStarted) {
            System.out.println("Sound already started " + source.getName() + " ID:" + ID);
        } else {
            Thread.ofVirtual().start(this);
            hasStarted = true;
            if (listener != null) {
                listener.onStart();
            }
        }
    }
    
    public void startWithRandomDelay(int min, int max){
        startDelay = Main.generateRandomInt(min, max);
        start();
    }
    
    public void startWithDelay(int delay){
        startDelay = delay;
        start();
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
        running = true;
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
        running = false;
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
    * sets the volume of this sound effect to a given percentage of the source
    * file's natural volume. Note this is not uniform across all sound files as
    * some are just naturally louder than others, however SoundEffect objects
    * using the same audio source will have the same natural volume.
    * 200 = twice as loud as the source file
    * 100 = the source file's own volume
    * 50  = half as loud
    * 0   = silent
    * Values above 200 are accepted but most audio lines cannot amplify beyond that.
    */
    public void setVolume(double percentVolume){
        if(percentVolume < 0){
            throw new RuntimeException("ERROR: Percent Volume cannot be negative");
        }
        volumePercent = percentVolume;
        applyVolume();
        if(listener!=null)listener.onSetVolume(percentVolume);
    }

    /**
     * pushes the current volume percentage onto the gain control, converting it to
     * the decibel scale the control actually uses and clamping to what it supports.
     */
    private void applyVolume() {
        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        float decibels = volumePercent <= 0 ? min : (float) (20.0 * Math.log10(volumePercent / 100.0));
        gainControl.setValue(Math.max(min, Math.min(max, decibels)));
    }

    /**
     * gets current volume as a percentage of the source file's natural volume.
     * 100 = original volume
     * 50 = half as loud
     * 0 = silent
     */
    public double getVolume() {
        return volumePercent;
    }

    /**
     * positions the sound in the stereo field.
     * -1 = fully left, 0 = centered, 1 = fully right.
     * Has no effect on audio lines that do not offer stereo positioning.
     */
    public void setPan(double pan) {
        panPosition = Math.max(-1.0, Math.min(1.0, pan));
        applyPan();
    }

    /**
     * @return stereo position of this sound, -1 (left) to 1 (right)
     */
    public double getPan() {
        return panPosition;
    }

    /**
     * pushes the current pan onto whichever stereo control the line offers. Audio
     * lines only expose these controls for stereo sources, which is why sources are
     * decoded to stereo on load.
     */
    private void applyPan() {
        if (panControl == null) {
            return;
        }
        panControl.setValue(Math.max(panControl.getMinimum(), Math.min(panControl.getMaximum(), (float) panPosition)));
    }
    
    /**
     * Adjusts the pitch of this sound by a percentage relative to its original pitch.
     * Uses sample rate manipulation — higher rate raises pitch, lower rate lowers it.
     * Note this also changes playback speed, and is clamped to within one octave.
     * @param percentChange amount to shift pitch: 0.1 = +10%, -0.1 = -10%, 0.0 = original
     */
    public void alterPitch(double percentChange) {
        double newOffset = clampPitch(percentChange);
        if (newOffset == pitchOffset) {
            return;
        }
        boolean wasRunning = clip.isRunning();
        //the same audio frame sits at a different timestamp once the rate changes
        long adjustedPosition = (long) (clip.getMicrosecondPosition() * (1.0 + pitchOffset) / (1.0 + newOffset));
        pitchOffset = newOffset;
        try {
            openClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }
        if (wasRunning) {
            clip.setMicrosecondPosition(adjustedPosition);
            if (isLooping()) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        }
        if (listener != null) listener.onAlterPitch(newOffset);
    }

    /**
     * Returns the current pitch offset as a percentage from the original.
     * 0.0 = original, 0.1 = +10%, -0.1 = -10%
     */
    public double getPitch() {
        return pitchOffset;
    }

    /**
     * holds a pitch offset to within one octave either way, past which resampling
     * artifacts take over and the sound stops resembling its source.
     */
    private static double clampPitch(double pitch) {
        return Math.max(-0.5, Math.min(1.0, pitch));
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
            openClip();
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
        clip.setMicrosecondPosition(0);
        if(!clip.isRunning()) clip.start();
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
     * 100 = 100%, sound is over
     * 50 = 50%, sound is half over
     * 0 = 0%, the sound is at its begining
     */
    public double getPercentDone(){
        return (getMicroPosition() * 100) / getSoundLength();
    }
    
    /**
     * @return A fresh SoundEffect of the same source, at this one's pitch
     */
    public SoundEffect createCopy(){
        return createCopy(pitchOffset);
    }

    /**
     * Creates a fresh SoundEffect of the same source at the given pitch. The copy
     * opens directly at that pitch, so shifting this way costs nothing extra.
     * @param pitch pitch offset relative to the original recording: 0.0 = original, 0.1 = +10%
     * @return A fresh SoundEffect of the same source
     */
    public SoundEffect createCopy(double pitch){
        return new SoundEffect(source, this, pitch);
    }

    public void playCopy() {
        playCopy(100.0);
    }

    public void playCopy(double volume) {
        playCopy(volume, 0);
    }

    public void playCopy(double volume, int msDelay) {
        playCopy(volume, msDelay, pitchOffset);
    }

    /**
     * plays a fresh copy of this sound at the given volume, delay and pitch.
     * @param volume percentage of the source file's natural volume, 100 = original
     * @param msDelay milliseconds to wait before the copy starts
     * @param pitch pitch offset relative to the original recording: 0.0 = original, 0.1 = +10%
     */
    public void playCopy(double volume, int msDelay, double pitch) {
        SoundEffect copy = this.createCopy(pitch);
        copy.setVolume(volume);
        copy.startWithDelay(msDelay);
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
    
    /**
     * use this function to create a copy of the sound but is randomly slightly altered. This is to be used if you have- for example an explosion sound effect
     * that you want to reuse without every explosion sounding literally identical.
     * this function should randomly alter pitch, speed, or distortion.
     * @param intensity 0.0 = no alteration, 1.0 = full alteration (±8% pitch, ±5 volume points)
     * @return
     */
    public SoundEffect createAlteredCopy(double intensity) {
        int pitchRange = (int) Math.round(8 * intensity);
        double pitchVariation = pitchRange > 0 ? Main.generateRandomInt(-pitchRange, pitchRange) / 100.0 : 0;
        SoundEffect copy = createCopy(pitchOffset + pitchVariation);
        int volRange = (int) Math.round(5 * intensity);
        if (volRange > 0) {
            copy.setVolume(Math.max(0, copy.getVolume() + Main.generateRandomInt(-volRange, volRange)));
        }
        return copy;
    }

}

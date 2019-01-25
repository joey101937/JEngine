/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.Audio;

import Framework.Game;
import Framework.Main;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores all sounds linked to host game
 * @author Joseph
 */
public class AudioManager implements Runnable{
    public final Game hostGame;
    private final CopyOnWriteArrayList<SoundEffect> storage = new CopyOnWriteArrayList<>();
    
    public AudioManager(Game g){
        hostGame = g;
    }
    
    /**
     * @return all sound effects associated with this manager
     */
    public ArrayList<SoundEffect> getAllSounds(){
        ArrayList<SoundEffect> output = new ArrayList<>();
        for(SoundEffect se : storage){
            output.add(se);
        }
        return output;
    }
    
    /**
     * adds sound to this manager
     * @param se sound to add
     */
    protected void addSound(SoundEffect se){
        storage.add(se);
    }
    
    protected void  removeSound(SoundEffect se){
        storage.remove(se);
    }
    
    public void updateGamePause(){
        Thread t = new Thread(this);
        t.start();
    }
    

    
    @Override
    public void run(){
        for(SoundEffect se: storage){
            Main.wait(5);
            se.onGamePaused(hostGame.isPaused());
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package Framework.Audio;
import Framework.Main;
import java.io.File; 
import java.io.IOException; 
import java.util.Scanner; 
  
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException; 
import javax.sound.sampled.UnsupportedAudioFileException; 
/**
 *
 * @author Joseph
 */
public class AudioManager {
    public static void main(String[] args) throws Exception {
      File machinegun = new File(Main.getDir()+"/Assets/Sounds/machinegun.au");
      File blast = new File(Main.getDir()+"/Assets/Sounds/blast1.au");
      
        Clip machinegunClip = AudioSystem.getClip();
        AudioInputStream audioInput1 = AudioSystem.getAudioInputStream(machinegun);
        machinegunClip.open(audioInput1);
        
        FloatControl control = (FloatControl) machinegunClip.getControl(FloatControl.Type.MASTER_GAIN);
        System.out.println(control.getMaximum());
        control.setValue(-45);
         machinegunClip.loop(Clip.LOOP_CONTINUOUSLY);

        Clip blastClip = AudioSystem.getClip();
        AudioInputStream blastInput = AudioSystem.getAudioInputStream(blast);
        blastClip.open(blastInput);
        FloatControl control2 = (FloatControl) blastClip.getControl(FloatControl.Type.MASTER_GAIN);
        System.out.println(control2.getMaximum());
        control2.setValue(-37);
        blastClip.loop(Clip.LOOP_CONTINUOUSLY);
        Main.display("looping...");
        blastClip.stop();

      Main.display("close to end");
    }
}

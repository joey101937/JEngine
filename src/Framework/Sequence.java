/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.awt.image.BufferedImage;

/**
 * Animation sequence
 * @author Joseph
 */
public class Sequence {
    BufferedImage[] frames;
    /**Duration to wait before switching frames in ms*/
    public int frameDelay = 60;
    /**Index of frame currently set to render*/
    public int currentFrameIndex = 0;
    boolean disabled = false;
    public Animator animator = new Animator(this);
    
    
    public BufferedImage getCurrentFrame(){
        BufferedImage output = frames[currentFrameIndex];
        if(output==null && currentFrameIndex!=0){
            currentFrameIndex=0;
            return getCurrentFrame();
        }
        return output;
    }
    
    public void startAnimating(){ 
        animator.animating=true;
        animator.thread.start();
    }
    
    public Sequence(BufferedImage[] input){
        frames=input;
    }
    
    public void disable(){
        this.disabled=true;
    }
    
    private class Animator implements Runnable{
       Sequence mySequence;
       Thread thread;
       boolean animating = false;
        public Animator(Sequence s){
            mySequence = s;
            thread = new Thread(this);
        }

        @Override
        public void run() {
            while(animating && !mySequence.disabled){
                Main.wait(mySequence.frameDelay);
                mySequence.currentFrameIndex++;
                if(mySequence.currentFrameIndex>=mySequence.frames.length){
                  mySequence.currentFrameIndex = 0;  
                }
            }
        }
        
    }
    
}

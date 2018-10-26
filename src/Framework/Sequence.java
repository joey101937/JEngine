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
    
    /**
     * @return BufferedImage to be rendered based on frame index, null if 
     * the frames array is null or if the actual image is null
     */
    public BufferedImage getCurrentFrame() {
        if(frames==null){
            System.out.println("Attempting to get current frame with null array");
            return null;
        }
        BufferedImage output = null;
        try{
        output = frames[currentFrameIndex];
        }catch(ArrayIndexOutOfBoundsException e){
            //check to see if threads got out of sync and updated index too fast
            //if so, reset index rather than returning null
            if(currentFrameIndex!=0){
                currentFrameIndex = 0;
                return getCurrentFrame();
            }
        }
        return output;
    }
    /**
     * we have this method so that unused sequences dont hurt performance by
     * running animation threads when they have yet to be rendered
     */
    public void startAnimating() {
        if (animator.animating == false) {
            animator.animating = true;
            animator.thread.start(); 
        }       
    }
    
    public Sequence(BufferedImage[] input){
        frames=input;
    }
    /**
     * stops animator
     */
    public void disable(){
        this.disabled=true;
    }
    
    /**
     * Helper class that updates frames in real time on a separate thread
     */
    private class Animator implements Runnable{
       Sequence mySequence;
       Thread thread;
       boolean animating = false;
       
        public Animator(Sequence s){
            mySequence = s;
            thread = new Thread(this);
            thread.setName("Sequence Animator");
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

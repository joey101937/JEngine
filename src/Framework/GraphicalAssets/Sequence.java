/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
/**
 * Animation sequence
 * @author Joseph
 */
public class Sequence implements Graphic{
    public BufferedImage[] frames;
    private double scale = 1;
    /**Duration to wait before switching frames in ms*/
    public int frameDelay = 60;
    /**Index of frame currently set to render*/
    public volatile int currentFrameIndex = 0;
    private volatile boolean disabled = false;
    private volatile boolean paused = false;
    private Long startTime;
    
    /**
     * @return BufferedImage to be rendered based on frame index, null if 
     * the frames array is null or if the actual image is null
     */
    public BufferedImage getCurrentFrame() {
        if(startTime == null) {
            startTime = System.currentTimeMillis();
        }
        if(frames==null){
            System.out.println("Attempting to get current frame with null array");
            return null;
        }
        if(frames.length==0){
            System.out.println("Attempting to get frame of empty sequence");
            return null;
        }
        BufferedImage output = null;
        try {
            if (!isPaused()) {
                currentFrameIndex = (int) (((System.currentTimeMillis() - startTime) / frameDelay) % frames.length);
            }
            output = frames[currentFrameIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            //check to see if threads got out of sync and updated index too fast
            //if so, reset index rather than returning null
            if (currentFrameIndex != 0) {
                currentFrameIndex = 0;
                return getCurrentFrame();
            }
        }
        return output;
    }
    /**
     * Start the animation
     */
    public void startAnimating() {
        if(startTime==null){
            startTime = System.currentTimeMillis();
        }      
    }
    
    public Sequence(BufferedImage[] input){
        frames = new BufferedImage[input.length];
        for(int i = 0; i < input.length; i++){
            frames[i]=input[i];
        }
    }
    /**
     * stops animator and removes graphics from memory
     */
    public void disable(){
        setPaused(false);
        disabled = true;
        for(BufferedImage bi : frames){
            bi=null;
        }
        try {
            finalize();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        for (BufferedImage bi : frames) {
            bi = scaleImage(bi, d);
        }
        scale*=d;
    }
    
    /**
     * sets the scale of all frames of this sequence to a given scale
     * @param d new value to be scale, relative to the default scale of the images
     */
    @Override
    public void scaleTo(double d) {
        for(int i = 0; i < frames.length; i++){
            frames[i] = scaleImage(frames[i],d/scale);
        }
        scale = d;
    }

    /**
     * returns current scaling of this sequence
     * @return current scale
     */
    @Override
    public double getScale() {
        return scale;
    }

    private BufferedImage scaleImage(BufferedImage before, double scaleAmount) {
      
        int w = before.getWidth();
        int h = before.getHeight();
        BufferedImage after = new BufferedImage((int)(w*scaleAmount), (int)(h*scaleAmount), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(scaleAmount, scaleAmount);
        AffineTransformOp scaleOp
                = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(before, after);
        return after;
    }

    /**
     * returns a copy of this sequence in contained in a new object
     * @return new sequence object with the same information as this object
     */
    @Override
    public Sequence copy(){
        Sequence output = new Sequence(frames);
        output.scale=scale;
        return output;
    }

    @Override
    public BufferedImage getCurrentImage() {
        startAnimating();
        return getCurrentFrame();
    }
    @Override
    public boolean isAnimated(){
        return true;
    }
    @Override
    public void destroy(){
        disable();
    }
    
    /**
     * is the animation paused for this sprite
     * @return is the animation paused for this sprite
     */
    public boolean isPaused(){
        return paused;
    }
    
    /**
     * sets the animation to either pause or resume
     * @param in true = pause, false = resume
     */
    public synchronized void setPaused(boolean in){
        paused = in;
    }
    
    public void restart(){
        startTime = System.currentTimeMillis();
    }
    
    
    /**
     * reverses the animation sequence. 
     * first frame becomes last frame and vice versa
     */
    public void reverse(){
        BufferedImage[] newFrames = new BufferedImage[frames.length];
        for(int i =0 ; i<frames.length;i++){
            newFrames[frames.length-(i+1)]=frames[i];
        }
        frames = newFrames;
    }
    
    
}

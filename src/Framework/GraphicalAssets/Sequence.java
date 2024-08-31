/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
/**
 * Animation sequence
 * @author Joseph
 */
public class Sequence implements Graphic{
    public Sprite[] frames;
    private double scale = 1;
    /**Duration to wait before switching frames in ms*/
    private int frameDelay = 100;
    /**Index of frame currently set to render*/
    public volatile int currentFrameIndex = 0;
    private volatile boolean disabled = false;
    private volatile boolean paused = false;
    private Long startTime;
    private int startTimeOffset = 0;
    private int pausedOnFrame = 0;
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
                currentFrameIndex = getCurrentFrameIndex();
            }
            output = frames[currentFrameIndex].getImage();
        } catch (ArrayIndexOutOfBoundsException e) {
            //check to see if threads got out of sync and updated index too fast
            //if so, reset index rather than returning null
            //UPDATE: this is likely unneeded after I fixed some issues but i will leave just in case
            if (currentFrameIndex != 0) {
                currentFrameIndex = 0;
                return getCurrentFrame();
            }
        }
        return output;
    }
    
    /**
     * @return BufferedImage to be rendered based on frame index, null if 
     * the frames array is null or if the actual image is null
     */
    public VolatileImage getCurrentVolatileFrame() {
        if(startTime == null) {
            startTime = System.currentTimeMillis() + startTimeOffset;
        }
        if(frames==null){
            System.out.println("Attempting to get current frame with null array");
            return null;
        }
        if(frames.length==0){
            System.out.println("Attempting to get frame of empty sequence");
            return null;
        }
        VolatileImage output = null;
        try {
            if (!isPaused()) {
                currentFrameIndex = getCurrentFrameIndex();
            }
            output = frames[currentFrameIndex].getCurrentVolatileImage();
        } catch (ArrayIndexOutOfBoundsException e) {
            //check to see if threads got out of sync and updated index too fast
            //if so, reset index rather than returning null
            //UPDATE: this is likely unneeded after I fixed some issues but i will leave just in case
            if (currentFrameIndex != 0) {
                currentFrameIndex = 0;
                return getCurrentVolatileFrame();
            }
        }
        return output;
    }
    
    public int getCurrentFrameIndex(){
        try{
           return (int) (((System.currentTimeMillis() - startTime) / frameDelay) % frames.length);   
        }catch (NullPointerException e){
            System.out.println("null pointer trying to get Sequence Frame Index");
            return 0;
        }
    }
    
    /**
     * Start the animation
     */
    public void startAnimating() {
        if(startTime==null){
            startTime = System.currentTimeMillis() + startTimeOffset;
        }      
    }
    
    public Sequence(BufferedImage[] input, double inputScale){
        frames = new Sprite[input.length];
        for(int i = 0; i < input.length; i++){
            frames[i]=new Sprite(input[i]);
        }
        this.scale = inputScale;
    }

    public Sequence(Sprite[] input, double inputScale) {
        frames = new Sprite[input.length];
        for (int i = 0; i < input.length; i++) {
            frames[i] = input[i].copy();
        }
        this.scale = inputScale;
    }
    
    public Sequence(BufferedImage[] input){
        frames = new Sprite[input.length];
        for(int i = 0; i < input.length; i++){
            frames[i]=new Sprite(input[i]);
        }
    }

    public Sequence(Sprite[] input) {
        frames = new Sprite[input.length];
        for (int i = 0; i < input.length; i++) {
            frames[i] = input[i].copy();
        }
    }
    /**
     * stops animator and removes graphics from memory
     */
    public void disable(){
        setPaused(false);
        disabled = true;
        for(Sprite bi : frames){
            bi=null;
        }
    }
    
    /**
     * Jumps to number frame in animation, note 1 would be first frame in animation
     * while the number of frames would represet the last frame
     * @param i number frame in sequence, starting at 1.
     */
    public void jumpToFrame(int i){
        i--;//arrays start at zero
        if(i<0 || i > frames.length-1){
            throw new ArrayIndexOutOfBoundsException("jumpToFrame argument must be between 1 and number of frames");
        }else{
            startTime = Long.valueOf(i*frameDelay);
        }
    }

    /**
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        for (Sprite bi : frames) {
            bi.scale(d);
        }
        scale*=d;
    }
    
    /**
     * sets the scale of all frames of this sequence to a given scale
     * @param d new value to be scale, relative to the default scale of the images
     */
    @Override
    public void scaleTo(double d) {
        for (Sprite bi : frames) {
            bi.scaleTo(d);
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
    
    @Override
    public void mirrorHorizontal() {
        for(Sprite s : frames) {
            s.mirrorHorizontal();
        }
    }    
    
    @Override
    public void mirrorVertical() {
        for(Sprite s : frames) {
            s.mirrorVertical();
        }
    }

    /**
     * returns a copy of this sequence in contained in a new object
     * @return new sequence object with the same information as this object
     */
    @Override
    public Sequence copy(){
        Sequence output = new Sequence(frames);
        output.scale=scale;
        output.frameDelay = this.frameDelay;
        return output;
    }

    @Override
    public BufferedImage getCurrentImage() {
        startAnimating();
        return getCurrentFrame();
    }

    @Override
    public VolatileImage getCurrentVolatileImage() {
        startAnimating();
        return getCurrentVolatileFrame();
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
    public void setPaused(boolean in){
        if(in && isPaused()){
            //pause but already paused
            return;
        }
        if(in && !isPaused()){
             paused = true;
             this.pausedOnFrame = this.getCurrentFrameIndex();
             return;
        }
        if(!in && !isPaused()){
            //resume but not paused
            return;
        }
        if(!in && isPaused()){
            jumpToFrame(pausedOnFrame+1);
            paused = false;
        }
    }
    
    
    
    public void restart(){
        startTime = System.currentTimeMillis();
    }
    
    
    /**
     * reverses the animation sequence. 
     * first frame becomes last frame and vice versa
     */
    public void reverse(){
        Sprite[] newFrames = new Sprite[frames.length];
        for(int i =0 ; i<frames.length;i++){
            newFrames[frames.length-(i+1)]=frames[i];
        }
        frames = newFrames;
    }
    
    
    public int getFrameCount() {
        return frames.length;
    }
    
    public void setFrameDelay(int x) {
        this.frameDelay = x;
    }
    
    public int getFrameDelay() {
        return this.frameDelay;
    }
    
    /**
     * Advances the animation by this number of milliseconds
     * @param ms number of ms to advance
     */
    public void advanceMs(int ms) {
        if(startTime != null) this.startTime += ms;
        this.startTimeOffset += ms;
    }
    
    
}

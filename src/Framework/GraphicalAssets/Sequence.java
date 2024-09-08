/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import javafx.scene.image.Image;
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
    private String signuature = "";
    
    
    // indicates that another sequence object has been created with references to this same sprite
    private boolean sharingReferences = true;
    /**
     * @return Image to be rendered based on frame index, null if 
     * the frames array is null or if the actual image is null
     */
    public Image getCurrentFrame() {
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
        Image output = null;
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
    
    public int getCurrentFrameIndex(){
        try{
           return (int) (((System.currentTimeMillis() - startTime + startTimeOffset) / frameDelay) % frames.length);   
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
            startTime = System.currentTimeMillis();
        }      
    }
    
    public Sequence(Image[] input, double inputScale){
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
    
    public Sequence(Image[] input){
        frames = new Sprite[input.length];
        for(int i = 0; i < input.length; i++){
            frames[i]=new Sprite(input[i]);
        }
    }
    
    public Sequence(Sprite[] input, double inputScale, boolean keepSourceReference){
       frames = new Sprite[input.length];
        for (int i = 0; i < input.length; i++) {
            frames[i] = keepSourceReference ? input[i] : input[i].copy();
        }
        this.scale = inputScale;
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
    
    private void resetReferences() {
        System.out.println("resetting sequence references " + (signuature.length() > 0 ? signuature : ""));
         this.sharingReferences = false;
        for(int i = 0; i < frames.length; i++) {
            frames[i] = frames[i].copy();
        }
    }

    /**
     * scales all frames in this sequence by a given amount
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        // if there is another sequence possibly sharing references, make new ones first
        if(sharingReferences) {
            resetReferences();
        }
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
        if(d == scale) return;
        if(sharingReferences) {
            resetReferences();
        }
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
        output.signuature = signuature;
        return output;
    }
    
    
    /**
     * creates a new sequence that uses the same sprites reference as the parent
     * This should be used to create multiple of the same sequence without using that much more memory
     * 
     * scaling either the original or the output sequence after this operator will cause it to create a new source
     * @return new sequence that uses same internal references as the calling sequence.
     */
    public Sequence copyMaintainSource() {
        Sequence output = new Sequence(this.frames, scale, true);
        output.frameDelay = this.frameDelay;
        this.sharingReferences = true;
        output.sharingReferences = true;
        output.signuature = this.signuature;
        return output;
    }

    @Override
    public Image getCurrentImage() {
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
        System.out.println("setting " + ms);
        this.startTimeOffset += ms;
    }

    @Override
    public String getSignature() {
        return signuature;
    }

    @Override
    public void setSignature(String s) {
        this.signuature = s;
    }
    
    
}

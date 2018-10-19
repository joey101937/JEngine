/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects;

import Framework.Coordinate;
import Framework.DCoordinate;
import Framework.Sequence;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Joseph
 */
public class GameObject2 {
    public String name= "Unnamed GameObject2";
    public DCoordinate location = new DCoordinate(0,0); //location relative to the world
    public DCoordinate velocity = new DCoordinate(0,0); //added to location each tick
    private boolean isAnimated = false;
    protected Sequence sequence = null; //animation sequence to run if animated
    public BufferedImage sprite = null; //static sprite if not animated
    public Map<String,Sequence> animations = new HashMap<String,Sequence>(); //stores known animation sequences for ease of access
    public double rotation = 0;
    
    
    /**
     * used to get integer location of object, used when rendering to screen
     * @return integer location
     */
    public Coordinate getPixelLocation(){
        return new Coordinate(location);
    }
    
    /**
     * returns the width of the visual gameobject. If no visual, return 0.
     * @return width of visual
     */
    public int getWidth() {
        try{
        if (isAnimated) {
            return sequence.getCurrentFrame().getWidth();
        } else {
            return sprite.getWidth();
        }
        }catch(NullPointerException npe){
            return 0;
        }
    }
    /**
     * returns the height of the visual gameobject. If no visual, return 0
     * @return height of visual
     */
    public int getHeight() {
        try {
            if (isAnimated) {
                return sequence.getCurrentFrame().getHeight();
            } else {
                return sprite.getHeight();
            }
        } catch (NullPointerException npe) {
            return 0;
        }
    }
    
    public Sequence getCurrentSequence(){
        return sequence;
    }
    public void setSequence(Sequence s){
        if(sequence == s) return;
        else sequence = s;
    }

    public void setRotation(double degrees) {
        rotation = degrees;
    }

    public void rotate(double degrees) {
        rotation += degrees;
    }

    public void lookAt(GameObject2 other) {
        setRotation(DCoordinate.angleFrom(location, other.location));
    }

    public void render(Graphics2D g){
        Coordinate pixelLocation = getPixelLocation();
        AffineTransform old = g.getTransform();
        while(rotation > 360){rotation-=360;}  //constrain rotation size
        while(rotation < -360){rotation+=360;}
        g.rotate(Math.toRadians(rotation),getPixelLocation().x,getPixelLocation().y);
        if(isAnimated){
            if(sequence == null){
                System.out.println("Warning trying to render null sequence object " +name);
                return;
            }
            if(sequence.getCurrentFrame()!=null){
                sequence.startAnimating();
                BufferedImage toRender = sequence.getCurrentFrame();
                g.drawImage(toRender, pixelLocation.x-toRender.getWidth()/2 , pixelLocation.y-toRender.getHeight()/2,null); //draws frmae centered on pixelLocation
            }else{
                System.out.println("Warning: null frame in sequence of " + name);
            }
        }else{
            if(sprite!=null){
                g.drawImage(sprite, pixelLocation.x-sprite.getWidth()/2, pixelLocation.y-sprite.getHeight()/2, null); //draws sprite centered on pixelLocation
            }else{
                System.out.println("Warning: unanimated game object sprite is null " + name);
            }
        }
        g.setTransform(old); //reset rotation for next item to render
    }
    
    /**
     * sets an object to not animate and only render one image as the animation
     * @param image static image to be rendered instead 
     */
    public void setAnimationFalse(BufferedImage image){
        isAnimated = false;
        sprite = image;
    }
    /**
     * sets the object to animate through a sequence
     * @param s sequence to begin rendering. may be changed later
     */
    public void setAnimationTrue(Sequence s){
        sequence = s;
        isAnimated = true;
    }
    
    public void tick(){
        updateLocation();
    }
    
    public void updateLocation(){
        location.add(velocity);
    }
    
    public GameObject2(Coordinate c){
      init(new DCoordinate(c));
    }
    public GameObject2(DCoordinate dc){
        init(dc);
    }
    private void init(DCoordinate dc){
        location = dc;
    }
    
    public void destroy(){
        //todo
    }
}

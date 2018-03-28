/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameObjects;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import Template.Game;
import Template.Main;

/**
 * A game object is everything that will go in the game. it ticks (does somthing every frame)
 * it renders (displays itself) and has coordinates for its X and Y position.
 * also has an x velocity and a Y velocity variable as well has two arrays for sprites.
 * those arnt yet implemented however.
 * @author Joseph
 */
public abstract class GameObject {
    /*  FIELDS  */
    public BufferedImage[] spritesR;     //where we store our sprites for right direction
    public BufferedImage[] spritesL;     //where we store the sprites for left direction
    public BufferedImage[] spritesIdle;  //idle sprites
    public int numFrames;               //number of frames in the animation            
    public int toRender;
    public int x , y, velX, velY, speed;       //X/Y coordinates and X/Y velocity
    public int width, height;                  //length and width
    public String name;                     //used to identify what kind of gameobject this is
    /**
     * constructor that takes two ints for x and y coordinates
     * @param x
     * @param y 
     */
    public GameObject(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    //every game tick, we update the coordinates based on velocity. at the end, clamp the coordinates to within the bounds of the world
    public void tick(){
        GOtick();
        for(int i=0; i < Math.abs(velX);i++){
            if(velX<0)x--;
            else if (velX>0)x++;
        }
        for(int i=0; i < Math.abs(velY);i++){
            if(velY<0)y--;
            else if (velY>0)y++;
        }
        if(this.y >= Game.height-this.height/2){
           collide(null);  //collide with the floor
        }
        x = Main.clamp(x, Game.width-width/2, 0);
        y = Main.clamp(y, Game.height-height/2, 0);
    }
    
    /**
     * this is the equivilant of the update method in unity. run once every frame.
     */
    public void GOtick(){};
    
    /**
     * this is run once a frame and contains code that puts the object on screen.
     * @param g 
     */
    public void render(Graphics g){
        try{
       if(this.velX>=1){ //traveling right
           if(toRender>=spritesR.length) toRender = 0;
           BufferedImage frame = spritesR[toRender];
           g.drawImage(frame, x-frame.getWidth()/2, y-frame.getHeight()/2, null); //draw sprite centered on our x/y coords
       }else if(velY <= -1){ //traveling left
           if(toRender>=spritesL.length) toRender = 0;
            BufferedImage frame = spritesL[toRender];
           g.drawImage(frame, x-frame.getWidth()/2, y-frame.getHeight()/2, null); //draw sprite centered on our x/y coords
       }else{
           if(toRender>=spritesIdle.length) toRender = 0;
           BufferedImage frame = spritesIdle[toRender];
           g.drawImage(frame, x-frame.getWidth()/2, y-frame.getHeight()/2, null); //draw sprite centered on our x/y coords
       }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * what happens when we collide with another game object, or with the floor(null)
     * @param go what we collided with
     */
    public abstract void collide(GameObject go);
}
